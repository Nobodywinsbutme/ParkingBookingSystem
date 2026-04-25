package com.app.web;

import com.app.service.UserService;
import com.app.support.ApiException;
import com.app.web.form.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthMvcController {
    private final UserService userService;

    public AuthMvcController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(registerForm);
        } catch (ApiException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
