package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/login")
    public String performLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        var userOpt = userService.loginUser(email, password);

        if (userOpt.isPresent()) {
            session.setAttribute("userId", userOpt.get().getId());
            return "redirect:/profile";
        } else {
            model.addAttribute("error", "Неверный email или пароль");
            return "login";
        }
    }

    @PostMapping("/register")
    public String performRegister(@RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String firstName,
                                  @RequestParam String lastName,
                                  HttpSession session,
                                  Model model) {
        try {
            User newUser = userService.registerUser(email, password, firstName, lastName);
            session.setAttribute("userId", newUser.getId());
            return "redirect:/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.getUserById(userId);

        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "profile";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}