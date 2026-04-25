package com.app.web;

import com.app.domain.entity.ParkingAreaEntity;
import com.app.domain.entity.ParkingSlotEntity;
import com.app.domain.enums.SlotStatus;
import com.app.service.ParkingSlotService;
import com.app.support.ApiException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/slots")
public class AdminParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    public AdminParkingSlotController(ParkingSlotService parkingSlotService) {
        this.parkingSlotService = parkingSlotService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNavSlots", true);
        List<ParkingAreaEntity> areas = parkingSlotService.listAreasForSlotForms();
        Map<String, String> areaNameById =
                areas.stream().collect(Collectors.toMap(ParkingAreaEntity::getId, ParkingAreaEntity::getName, (a, b) -> a));
        model.addAttribute("areaNameById", areaNameById);
        model.addAttribute("slots", parkingSlotService.listAllForAdmin());
        return "admin/slots";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("adminNavSlots", true);
        model.addAttribute("formTitle", "New parking slot");
        model.addAttribute("slot", null);
        model.addAttribute("areas", parkingSlotService.listAreasForSlotForms());
        return "admin/slot-form";
    }

    @PostMapping
    public String create(
            @RequestParam String parkingAreaId,
            @RequestParam String code,
            @RequestParam(required = false) String floor,
            @RequestParam(defaultValue = "AVAILABLE") SlotStatus status,
            RedirectAttributes ra) {
        try {
            parkingSlotService.create(parkingAreaId, code, floor, status);
            ra.addFlashAttribute("msgSuccess", "Slot created.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
            return "redirect:/admin/slots/create";
        }
        return "redirect:/admin/slots";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        return parkingSlotService
                .findForAdmin(id)
                .map(
                        slot -> {
                            model.addAttribute("adminNavSlots", true);
                            model.addAttribute("formTitle", "Edit parking slot");
                            model.addAttribute("slot", slot);
                            model.addAttribute("areas", parkingSlotService.listAreasForSlotForms());
                            return "admin/slot-form";
                        })
                .orElse("redirect:/admin/slots");
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable String id,
            @RequestParam String parkingAreaId,
            @RequestParam String code,
            @RequestParam(required = false) String floor,
            @RequestParam SlotStatus status,
            RedirectAttributes ra) {
        try {
            parkingSlotService.update(id, parkingAreaId, code, floor, status);
            ra.addFlashAttribute("msgSuccess", "Slot updated.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
            return "redirect:/admin/slots/" + id + "/edit";
        }
        return "redirect:/admin/slots";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        try {
            parkingSlotService.disable(id);
            ra.addFlashAttribute("msgSuccess", "Slot deactivated.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/admin/slots";
    }
}
