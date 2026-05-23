package org.example.controller;

import org.example.dto.CreateReviewRequest;
import org.example.dto.RatingResponse;
import org.example.dto.ReviewResponse;
import org.example.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Создать отзыв
    // TODO: пока передаем authorId=1 (первый пользователь в БД). Потом заменим на текущего из SecurityContext
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        // ВРЕМЕННО: используем пользователя с ID = 1
        Long tempAuthorId = 1L;
        ReviewResponse response = reviewService.createReview(request, tempAuthorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Получить все отзывы о пользователе
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    // Получить рейтинг пользователя
    @GetMapping("/user/{userId}/rating")
    public ResponseEntity<RatingResponse> getUserRating(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserRating(userId));
    }

    // Получить отзывы, написанные пользователем
    @GetMapping("/author/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    // Удалить отзыв
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        // ВРЕМЕННО: используем пользователя с ID = 1, НЕ админ
        Long tempAuthorId = 1L;
        boolean isAdmin = false;
        reviewService.deleteReview(reviewId, tempAuthorId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}