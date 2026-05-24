package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.service.ReviewService;
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
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Страница создания отзыва
    @GetMapping("/reviews/create/{userId}")
    public String showCreateReviewForm(@PathVariable Long userId,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        // Нельзя оставить отзыв самому себе
        if (currentUserId.equals(userId)) {
            redirectAttrs.addFlashAttribute("error", "Нельзя оставить отзыв самому себе");
            return "redirect:/profile";
        }

        // Проверяем, существует ли пользователь, о котором пишут отзыв
        var reviewedUserOpt = userService.getUserById(userId);
        if (reviewedUserOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/profile";
        }

        model.addAttribute("reviewedUser", reviewedUserOpt.get());
        return "create-review";
    }

    // Сохранение отзыва
    @PostMapping("/reviews/create")
    public String createReview(@RequestParam Long userId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               @RequestParam(required = false) Long dealId,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {
        Long authorId = (Long) session.getAttribute("userId");
        if (authorId == null) {
            return "redirect:/login";
        }

        try {
            reviewService.createReview(authorId, userId, rating, comment, dealId);
            redirectAttrs.addFlashAttribute("success", "Отзыв успешно добавлен!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Страница редактирования отзыва
     * GET /reviews/edit/{reviewId}
     */
    @GetMapping("/reviews/edit/{reviewId}")
    public String showEditReviewForm(@PathVariable Long reviewId,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        var reviewOpt = reviewService.getReviewById(reviewId);
        if (reviewOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Отзыв не найден");
            return "redirect:/profile";
        }

        Review review = reviewOpt.get();

        // Проверяем, что текущий пользователь — автор отзыва
        if (!review.getAuthor().getId().equals(currentUserId)) {
            redirectAttrs.addFlashAttribute("error", "Только автор может редактировать отзыв");
            return "redirect:/profile";
        }

        model.addAttribute("review", review);
        model.addAttribute("reviewedUser", review.getUser());
        return "edit-review";
    }

    /**
     * Обновление отзыва
     * POST /reviews/update/{reviewId}
     */
    @PostMapping("/reviews/update/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            reviewService.updateReview(reviewId, currentUserId, rating, comment);
            redirectAttrs.addFlashAttribute("success", "Отзыв обновлён!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Удаление отзыва
     * POST /reviews/delete/{reviewId}
     */
    @PostMapping("/reviews/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            reviewService.deleteReview(reviewId, currentUserId);
            redirectAttrs.addFlashAttribute("success", "Отзыв удалён!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Страница "Мои отзывы" (отзывы, которые написал текущий пользователь)
     * GET /reviews/my
     */
    @GetMapping("/reviews/my")
    public String myReviews(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        var reviews = reviewService.getReviewsByAuthor(currentUserId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("title", "Мои отзывы");

        return "my-reviews";
    }

    /**
     * Страница отзывов о пользователе
     * GET /reviews/user/{userId}
     */
    @GetMapping("/reviews/user/{userId}")
    public String userReviews(@PathVariable Long userId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttrs) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/profile";
        }

        var reviews = reviewService.getReviewsByUser(userId);
        var rating = reviewService.getUserRating(userId);

        model.addAttribute("reviewedUser", userOpt.get());
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", rating.getAverageRating());
        model.addAttribute("totalReviews", rating.getTotalReviews());
        model.addAttribute("currentUserId", currentUserId);

        return "user-reviews";
    }
}