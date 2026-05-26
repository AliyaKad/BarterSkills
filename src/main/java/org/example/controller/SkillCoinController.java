package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.service.SkillCoinService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SkillCoinController {

    @Autowired
    private SkillCoinService skillCoinService;

    @Autowired
    private UserService userService;

    @GetMapping("/transactions")
    public String transactionHistory(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("transactions", skillCoinService.getTransactionsForUser(userId));
        model.addAttribute("currentUserId", userId);
        return "transactions";
    }
}
