package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.Deal;
import org.example.entity.DealStatus;
import org.example.service.DealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DealController {

    @Autowired
    private DealService dealService;

    @GetMapping("/deals")
    public String listDeals(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("deals", dealService.getDealsForUser(userId));
        model.addAttribute("currentUserId", userId);
        return "deals-list";
    }

    @GetMapping("/deals/{id}")
    public String viewDeal(@PathVariable Long id,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        var dealOpt = dealService.getDealById(id);
        if (dealOpt.isEmpty() || !dealService.isParticipant(dealOpt.get(), userId)) {
            redirectAttrs.addFlashAttribute("error", "Сделка не найдена");
            return "redirect:/deals";
        }

        Deal deal = dealOpt.get();
        model.addAttribute("deal", deal);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("canAccept", dealService.canAccept(deal, userId));
        model.addAttribute("canCancel", dealService.canCancel(deal, userId));
        model.addAttribute("canConfirmCompletion", dealService.canConfirmCompletion(deal, userId));
        model.addAttribute("canDispute", dealService.canDispute(deal, userId));

        return "deal-detail";
    }

    @PostMapping("/deals/from-offer/{offerId}")
    public String createFromOffer(@PathVariable Long offerId,
                                  @RequestParam(required = false) Integer amount,
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Deal deal = dealService.createFromOffer(offerId, userId, amount);
            redirectAttrs.addFlashAttribute("success", "Сделка предложена! Ожидается подтверждение исполнителя.");
            return "redirect:/deals/" + deal.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/offers/" + offerId;
        }
    }

    @PostMapping("/deals/from-request/{requestId}")
    public String createFromRequest(@PathVariable Long requestId,
                                    @RequestParam(required = false) Integer amount,
                                    HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Deal deal = dealService.createFromRequest(requestId, userId, amount);
            redirectAttrs.addFlashAttribute("success", "Отклик отправлен! Ожидается подтверждение заказчика.");
            return "redirect:/deals/" + deal.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/requests/" + requestId;
        }
    }

    @PostMapping("/deals/{id}/accept")
    public String acceptDeal(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttrs) {
        return performAction(id, session, redirectAttrs, () -> {
            dealService.acceptDeal(id, (Long) session.getAttribute("userId"));
            redirectAttrs.addFlashAttribute("success",
                    "Сделка принята! SkillCoin заказчика заморожены до завершения.");
        });
    }

    @PostMapping("/deals/{id}/cancel")
    public String cancelDeal(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttrs) {
        return performAction(id, session, redirectAttrs, () -> {
            dealService.cancelDeal(id, (Long) session.getAttribute("userId"));
            redirectAttrs.addFlashAttribute("success", "Сделка отменена. Замороженные монеты возвращены.");
        });
    }

    @PostMapping("/deals/{id}/complete")
    public String confirmCompletion(@PathVariable Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        return performAction(id, session, redirectAttrs, () -> {
            Deal deal = dealService.confirmCompletion(id, (Long) session.getAttribute("userId"));
            if (deal.getStatus() == DealStatus.COMPLETED) {
                redirectAttrs.addFlashAttribute("success",
                        "Сделка завершена! SkillCoin переведены исполнителю.");
            } else {
                redirectAttrs.addFlashAttribute("success",
                        "Ваше подтверждение принято. Ожидается подтверждение второй стороны.");
            }
        });
    }

    @PostMapping("/deals/{id}/dispute")
    public String disputeDeal(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
        return performAction(id, session, redirectAttrs, () -> {
            dealService.disputeDeal(id, (Long) session.getAttribute("userId"));
            redirectAttrs.addFlashAttribute("success",
                    "Спор открыт. Замороженные SkillCoin возвращены заказчику.");
        });
    }

    private String performAction(Long dealId, HttpSession session,
                                 RedirectAttributes redirectAttrs, Runnable action) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        try {
            action.run();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/deals/" + dealId;
    }
}
