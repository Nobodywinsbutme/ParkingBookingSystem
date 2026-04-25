package com.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminMvcController {

    @GetMapping
    public String root() {
        return "redirect:/admin/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("adminNavHome", true);
        return "admin/home";
    }
}
