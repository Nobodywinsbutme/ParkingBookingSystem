package com.app.web;

import com.app.service.ParkingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ParkingMvcController {
    private final ParkingService parkingService;

    public ParkingMvcController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/parking-areas")
    public String listAreas(Model model) {
        model.addAttribute("areas", parkingService.listActiveAreas());
        return "parking-areas";
    }
}
