package com.app.web;

import com.app.service.ParkingAreaService;
import com.app.support.ApiException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/areas")
public class AdminParkingAreaController {

    private final ParkingAreaService parkingAreaService;

    public AdminParkingAreaController(ParkingAreaService parkingAreaService) {
        this.parkingAreaService = parkingAreaService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("adminNavAreas", true);
        model.addAttribute("areas", parkingAreaService.listAllForAdmin());
        return "admin/areas";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("adminNavAreas", true);
        model.addAttribute("formTitle", "New parking area");
        model.addAttribute("area", null);
        return "admin/area-form";
    }

    @PostMapping
    public String create(
            @RequestParam String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String addressLine1,
            RedirectAttributes ra) {
        try {
            parkingAreaService.create(city, name, addressLine1);
            ra.addFlashAttribute("msgSuccess", "Parking area created.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
            return "redirect:/admin/areas/create";
        }
        return "redirect:/admin/areas";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        return parkingAreaService
                .findForAdmin(id)
                .map(
                        area -> {
                            model.addAttribute("adminNavAreas", true);
                            model.addAttribute("formTitle", "Edit parking area");
                            model.addAttribute("area", area);
                            return "admin/area-form";
                        })
                .orElse("redirect:/admin/areas");
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String addressLine1,
            RedirectAttributes ra) {
        try {
            parkingAreaService.update(id, city, name, addressLine1);
            ra.addFlashAttribute("msgSuccess", "Parking area updated.");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
            return "redirect:/admin/areas/" + id + "/edit";
        }
        return "redirect:/admin/areas";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        try {
            parkingAreaService.disable(id);
            ra.addFlashAttribute("msgSuccess", "Parking area deactivated (hidden from public list).");
        } catch (ApiException e) {
            ra.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/admin/areas";
    }
}
