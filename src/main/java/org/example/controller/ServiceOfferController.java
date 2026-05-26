package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.Category;
import org.example.entity.OfferStatus;
import org.example.service.ServiceOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ServiceOfferController {

    @Autowired
    private ServiceOfferService offerService;

    @GetMapping("/offers")
    public String listOffers(@RequestParam(required = false) String q,
                             @RequestParam(required = false) Category category,
                             @RequestParam(required = false) Integer minPrice,
                             @RequestParam(required = false) Integer maxPrice,
                             @RequestParam(required = false) OfferStatus status,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("offers", offerService.searchOffers(q, category, minPrice, maxPrice, status));
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", OfferStatus.values());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedStatus", status);

        return "offers-list";
    }

    @GetMapping("/offers/new")
    public String showCreateForm(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", Category.values());
        return "offer-create";
    }

    @PostMapping("/offers/create")
    public String createOffer(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) Category category,
                              @RequestParam(required = false) Integer priceInSkillCoins,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            offerService.createOffer(userId, title, description, category, priceInSkillCoins);
            redirectAttrs.addFlashAttribute("success", "Предложение услуги создано!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/new";
        }
    }

    @GetMapping("/offers/{id}")
    public String viewOffer(@PathVariable Long id,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        var offerOpt = offerService.getOfferById(id);
        if (offerOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Предложение не найдено");
            return "redirect:/offers";
        }

        model.addAttribute("offer", offerOpt.get());
        model.addAttribute("currentUserId", userId);
        return "offer-detail";
    }
}
