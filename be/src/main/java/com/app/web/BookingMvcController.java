package com.app.web;

import com.app.domain.entity.ParkingAreaEntity;
import com.app.domain.entity.ParkingSlotEntity;
import com.app.dto.booking.BookingDto;
import com.app.dto.booking.CreateBookingRequest;
import com.app.security.DbUserDetails;
import com.app.service.BookingService;
import com.app.service.ParkingService;
import com.app.service.StripePaymentService;
import com.app.support.ApiException;
import com.app.support.BookingConflictException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/bookings")
@Validated
public class BookingMvcController {
    private static final ZoneId BOOKING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BookingService bookingService;
    private final ParkingService parkingService;
    private final StripePaymentService stripePaymentService;

    public BookingMvcController(
            BookingService bookingService,
            ParkingService parkingService,
            StripePaymentService stripePaymentService
    ) {
        this.bookingService = bookingService;
        this.parkingService = parkingService;
        this.stripePaymentService = stripePaymentService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal DbUserDetails user, Model model) {
        List<BookingDto> bookings = bookingService.listBookings(user.getUserId(), null, 50, 0);
        model.addAttribute("bookings", bookings);
        return "bookings";
    }

    @GetMapping("/new")
    public String newForm(
            @RequestParam @NotBlank String areaId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        ParkingAreaEntity area = parkingService.getActiveArea(areaId);
        if (area == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Parking area not found.");
            return "redirect:/parking-areas";
        }
        List<ParkingSlotEntity> slots = parkingService.listActiveSlotsForArea(areaId);
        model.addAttribute("area", area);
        model.addAttribute("slots", slots);
        model.addAttribute("stripeEnabled", stripePaymentService.isReady());
        model.addAttribute("defaultStart", LocalDateTime.now(BOOKING_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        model.addAttribute("defaultEnd", LocalDateTime.now(BOOKING_ZONE).plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return "booking-form";
    }

    @PostMapping
    public String create(
            @AuthenticationPrincipal DbUserDetails user,
            @RequestParam String parkingAreaId,
            @RequestParam String parkingSlotId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt,
            @RequestParam(defaultValue = "CASH") String paymentMethod,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes
    ) {
        String pm = paymentMethod == null ? "CASH" : paymentMethod.trim().toUpperCase();
        if ("STRIPE".equals(pm) && !stripePaymentService.isReady()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Card payment is not available (missing STRIPE_SECRET_KEY)."
            );
            return "redirect:/bookings/new?areaId=" + parkingAreaId;
        }
        String startIso = startAt.atZone(BOOKING_ZONE).toInstant().toString();
        String endIso = endAt.atZone(BOOKING_ZONE).toInstant().toString();
        CreateBookingRequest request = new CreateBookingRequest(
                parkingAreaId,
                parkingSlotId,
                startIso,
                endIso,
                pm,
                notes == null ? null : notes.trim()
        );
        try {
            BookingDto created = bookingService.createBooking(user.getUserId(), request);
            if ("STRIPE".equals(pm)) {
                return "redirect:/payment/checkout?bookingId=" + created.id();
            }
        } catch (ApiException | BookingConflictException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bookings/new?areaId=" + parkingAreaId;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Booking created and confirmed.");
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @AuthenticationPrincipal DbUserDetails user,
            @PathVariable String id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.cancelBooking(user.getUserId(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled.");
        } catch (ApiException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }
}
