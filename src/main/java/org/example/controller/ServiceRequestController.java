package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.Category;
import org.example.entity.RequestStatus;
import org.example.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService requestService;

    @GetMapping("/requests")
    public String listRequests(@RequestParam(required = false) String q,
                               @RequestParam(required = false) Category category,
                               @RequestParam(required = false) Integer maxBudget,
                               @RequestParam(required = false) RequestStatus status,
                               HttpSession session,
                               Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("requests", requestService.searchRequests(q, category, maxBudget, status));
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", RequestStatus.values());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("maxBudget", maxBudget);
        model.addAttribute("selectedStatus", status);

        return "requests-list";
    }

    @GetMapping("/requests/new")
    public String showCreateForm(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", Category.values());
        return "request-create";
    }

    @PostMapping("/requests/create")
    public String createRequest(@RequestParam String title,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) Category category,
                                @RequestParam(required = false) Integer maxPrice,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            requestService.createRequest(userId, title, description, category, maxPrice);
            redirectAttrs.addFlashAttribute("success", "Запрос на услугу создан!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/requests/new";
        }
    }

    @GetMapping("/requests/{id}")
    public String viewRequest(@PathVariable Long id,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        var requestOpt = requestService.getRequestById(id);
        if (requestOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Запрос не найден");
            return "redirect:/requests";
        }

        model.addAttribute("request", requestOpt.get());
        model.addAttribute("currentUserId", userId);
        return "request-detail";
    }
}
