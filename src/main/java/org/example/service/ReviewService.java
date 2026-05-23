package org.example.service;

import org.example.dto.CreateReviewRequest;
import org.example.dto.RatingResponse;
import org.example.dto.ReviewResponse;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // Создать отзыв
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long authorId) {
        // Находим автора (кто оставляет отзыв)
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        // Находим получателя (кому оставляют отзыв)
        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("Получатель отзыва не найден"));

        // Нельзя оставить отзыв самому себе
        if (author.getId().equals(reviewee.getId())) {
            throw new RuntimeException("Нельзя оставить отзыв самому себе");
        }

        // Проверяем, что отзыв по этой сделке еще не оставлен
        if (reviewRepository.existsByDealId(request.getDealId())) {
            throw new RuntimeException("Отзыв по этой сделке уже оставлен");
        }

        // Создаем отзыв
        Review review = Review.builder()
                .author(author)
                .user(reviewee)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        // Обновляем поле rating у пользователя (если решишь оставить поле)
        // updateUserRating(reviewee);

        return toResponse(saved);
    }

    // Получить все отзывы о пользователе
    public List<ReviewResponse> getReviewsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return reviewRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    // Получить все отзывы, написанные пользователем
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return reviewRepository.findByAuthor(user).stream()
                .map(this::toResponse)
                .toList();
    }

    // Получить рейтинг пользователя
    public RatingResponse getUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Double avgRating = reviewRepository.getAverageRatingForUser(user);
        Long totalReviews = (long) reviewRepository.findByUser(user).size();

        return RatingResponse.builder()
                .userId(userId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    // Удалить отзыв (только автор или админ)
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // Проверяем права: только автор или админ
        if (!review.getAuthor().getId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Нет прав на удаление этого отзыва");
        }

        reviewRepository.delete(review);
    }

    // Конвертация Review -> ReviewResponse
    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .authorId(review.getAuthor().getId())
                .authorName(review.getAuthor().getFirstName() + " " + review.getAuthor().getLastName())
                .revieweeId(review.getUser().getId())
                .revieweeName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // Обновить поле rating у пользователя (вызывать после каждого отзыва)
    private void updateUserRating(User user) {
        Double avg = reviewRepository.getAverageRatingForUser(user);
        user.setRating(avg != null ? avg.floatValue() : 0.0f);
        userRepository.save(user);
    }
}