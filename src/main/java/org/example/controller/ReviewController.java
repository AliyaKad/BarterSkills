package org.example.controller;

import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // Страница "Оставить отзыв"
    @GetMapping("/reviews/create/{userId}")
    public String showLeaveReviewForm(@PathVariable Long userId, Model model) {
        Long currentUserId = getCurrentUserId();

        // Нельзя оставить отзыв самому себе
        if (currentUserId.equals(userId)) {
            model.addAttribute("error", "Нельзя оставить отзыв самому себе");
            return "redirect:/profile";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        return "review-create";
    }


    // Обработка отправки отзыва
    @PostMapping("/reviews/create")
    public String createReview(
            @RequestParam Long userId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
//            @RequestParam(required = false) Long dealId,
            Model model) {

        try {
            Long currentUserId = getCurrentUserId();

            // Нельзя оставить отзыв самому себе
            if (currentUserId.equals(userId)) {
                throw new RuntimeException("Нельзя оставить отзыв самому себе");
            }

            User author = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Автор не найден"));
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверка: не оставлял ли уже отзыв для этой сделки
//            if (dealId != null) {
//                boolean alreadyReviewed = reviewRepository.existsByAuthorIdAndDealId(currentUserId, dealId);
//                if (alreadyReviewed) {
//                    throw new RuntimeException("Вы уже оставили отзыв для этой сделки");
//                }
//            }

            // Создаем отзыв
            Review review = Review.builder()
                    .author(author)
                    .user(targetUser)
                    .rating(rating)
                    .comment(comment != null ? comment : "")
                    .createdAt(LocalDateTime.now())
//                    .deal(dealId)
                    .build();

            reviewRepository.save(review);

            // Обновляем средний рейтинг пользователя
            updateUserAverageRating(targetUser);

            model.addAttribute("success", "Отзыв успешно оставлен!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/profile";
        }

        return "redirect:/profile";
    }

    // Редактирование отзыва
    @GetMapping("/reviews/edit/{id}")
    public String showEditReviewForm(@PathVariable Long id, Model model) {
        Long currentUserId = getCurrentUserId();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // Проверяем, что текущий пользователь — автор отзыва
        if (!review.getAuthor().getId().equals(currentUserId)) {
            model.addAttribute("error", "Только автор может редактировать отзыв");
            return "redirect:/profile";
        }

        model.addAttribute("review", review);
        model.addAttribute("targetUser", review.getUser());
        return "edit-review";
    }

    // Обработка обновления отзыва
    @PostMapping("/reviews/update/{id}")
    public String updateReview(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            Model model) {

        try {
            Long currentUserId = getCurrentUserId();

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

            // Проверяем права
            if (!review.getAuthor().getId().equals(currentUserId)) {
                throw new RuntimeException("Только автор может редактировать отзыв");
            }

            review.setRating(rating);
            review.setComment(comment != null ? comment : "");
            reviewRepository.save(review);

            // Обновляем рейтинг пользователя
            updateUserAverageRating(review.getUser());

            model.addAttribute("success", "Отзыв обновлен!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    // Удаление отзыва
    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, Model model) {
        try {
            Long currentUserId = getCurrentUserId();

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

            // Проверяем права (автор или админ)
            if (!review.getAuthor().getId().equals(currentUserId)) {
                // TODO: добавить проверку на админа
                throw new RuntimeException("Нет прав для удаления отзыва");
            }

            User targetUser = review.getUser();
            reviewRepository.delete(review);

            // Обновляем рейтинг пользователя
            updateUserAverageRating(targetUser);

            model.addAttribute("success", "Отзыв удален!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    // Получить ID текущего пользователя
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow().getId();
    }

    // Обновить средний рейтинг пользователя
    private void updateUserAverageRating(User user) {
        Double avgRating = reviewRepository.getAverageRatingForUser(user);
        user.setRating(avgRating != null ? avgRating.floatValue() : 0.0f);
        userRepository.save(user);
    }
}