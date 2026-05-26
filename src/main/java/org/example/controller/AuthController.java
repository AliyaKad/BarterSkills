package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.User;
import org.example.service.SkillCoinService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

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
                                  Model model,
                                  RedirectAttributes redirectAttrs) {
        try {
            User newUser = userService.registerUser(email, password, firstName, lastName);
            session.setAttribute("userId", newUser.getId());
            redirectAttrs.addFlashAttribute("success",
                    "Регистрация успешна! Начислено " + SkillCoinService.REGISTRATION_BONUS + " SC.");
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

        var userOpt = userService.getUserWithDetails(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            if (user.getReviews() != null && !user.getReviews().isEmpty()) {
                double avgRating = user.getReviews().stream()
                        .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                        .average()
                        .orElse(0.0);
                model.addAttribute("avgRating", String.format("%.1f", avgRating));
            } else {
                model.addAttribute("avgRating", "0.0");
            }

            model.addAttribute("availableBalance", user.getSkillCoinBalance());
            model.addAttribute("heldBalance", user.getSkillCoinHeld());

            return "profile";
        }
        return "redirect:/login";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String lastName,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String city,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            userService.updateProfile(userId, firstName, lastName, bio, city);
            redirectAttrs.addFlashAttribute("success", "Профиль обновлён!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/skills/add-can")
    public String addSkillCan(@RequestParam String skill,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        if (skill != null && !skill.trim().isEmpty()) {
            int bonus = userService.addSkillCanOffer(userId, skill.trim());
            if (bonus > 0) {
                redirectAttrs.addFlashAttribute("success",
                        "Навык добавлен! Начислено " + bonus + " SC за первые три навыка «Могу предложить».");
            } else {
                redirectAttrs.addFlashAttribute("success", "Навык добавлен!");
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/skills/add-need")
    public String addSkillNeed(@RequestParam String skill,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        if (skill != null && !skill.trim().isEmpty()) {
            int bonus = userService.addSkillNeeded(userId, skill.trim());
            if (bonus > 0) {
                redirectAttrs.addFlashAttribute("success",
                        "Потребность добавлена! Начислено " + bonus + " SC за первые три пункта «Мне требуется».");
            } else {
                redirectAttrs.addFlashAttribute("success", "Потребность добавлена!");
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Просмотр профиля другого пользователя (только чтение)
    @GetMapping("/profile/{userId}")
    public String viewOtherProfile(@PathVariable Long userId,
                                   HttpSession session,
                                   Model model) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        // Не даём смотреть свой профиль через этот URL (редирект на /profile)
        if (currentUserId.equals(userId)) {
            return "redirect:/profile";
        }

        var userOpt = userService.getUserWithDetails(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/profile";
        }

        User viewedUser = userOpt.get();
        model.addAttribute("user", viewedUser);
        model.addAttribute("isOwnProfile", false);
        model.addAttribute("currentUserId", currentUserId);

        // Рейтинг
        if (viewedUser.getReviews() != null && !viewedUser.getReviews().isEmpty()) {
            double avgRating = viewedUser.getReviews().stream()
                    .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                    .average()
                    .orElse(0.0);
            model.addAttribute("avgRating", String.format("%.1f", avgRating));
        } else {
            model.addAttribute("avgRating", "0.0");
        }

        return "profile-view";
    }
}