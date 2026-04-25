package com.app.web;

import com.app.domain.enums.SlotStatus;
import com.app.repository.BookingRepository;
import com.app.repository.ParkingSlotRepository;
import com.app.security.DbUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardMvcController {

    private final BookingRepository bookingRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    public DashboardMvcController(
            BookingRepository bookingRepository,
            ParkingSlotRepository parkingSlotRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal DbUserDetails user, Model model) {
        long totalBookings = bookingRepository.countByUserIdAndDeletedAtIsNull(user.getUserId());
        long availableSlots = parkingSlotRepository.countByDeletedAtIsNullAndActiveTrueAndStatus(SlotStatus.AVAILABLE);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("availableSlots", availableSlots);
        return "dashboard";
    }
}
