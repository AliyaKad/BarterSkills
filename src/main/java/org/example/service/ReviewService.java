package org.example.service;

import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    // Создать новый отзыв
    @Transactional
    public Review createReview(Long authorId, Long userId, Integer rating, String comment, Long dealId) {
        // Проверяем автора
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        // Проверяем получателя отзыва
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Нельзя отзыв самому себе
        if (authorId.equals(userId)) {
            throw new RuntimeException("Нельзя оставить отзыв самому себе");
        }

        // Проверяем валидность оценки
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5");
        }

        // Создаём отзыв
        Review review = Review.builder()
                .author(author)
                .user(user)
                .rating(rating)
                .comment(comment != null ? comment : "")
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Обновляем средний рейтинг пользователя
        updateUserAverageRating(userId);

        return savedReview;
    }

    // Получить все отзывы о пользователе
    public List<Review> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return reviewRepository.findByUser(user);
    }

    //Получить все отзывы, написанные пользователем
    public List<Review> getReviewsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));
        return reviewRepository.findByAuthor(author);
    }

    // Получить отзыв по ID
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Обновить отзыв
    @Transactional
    public Review updateReview(Long reviewId, Long currentUserId, Integer newRating, String newComment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // Проверяем права
        if (!review.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("Только автор может редактировать отзыв");
        }

        if (newRating < 1 || newRating > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5");
        }

        review.setRating(newRating);
        if (newComment != null) {
            review.setComment(newComment);
        }

        Review updatedReview = reviewRepository.save(review);

        // Обновляем рейтинг пользователя
        updateUserAverageRating(review.getUser().getId());

        return updatedReview;
    }

    // Удалить отзыв
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // Проверяем права
        if (!review.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("Только автор может удалить отзыв");
        }

        Long userId = review.getUser().getId();
        reviewRepository.delete(review);

        // Обновляем рейтинг пользователя
        updateUserAverageRating(userId);
    }

    //Получить информацию о рейтинге пользователя
    public RatingInfo getUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Review> reviews = reviewRepository.findByUser(user);

        if (reviews.isEmpty()) {
            return new RatingInfo(0.0, 0L);
        }

        double avg = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        return new RatingInfo(avg, (long) reviews.size());
    }

    // Обновить поле rating в таблице User
    private void updateUserAverageRating(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Review> reviews = reviewRepository.findByUser(user);
            if (reviews.isEmpty()) {
                user.setRating(0.0f);
            } else {
                double avg = reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);
                user.setRating((float) avg);
            }
            userRepository.save(user);
        }
    }


    // Вспомогательный класс для рейтинга
    public static class RatingInfo {
        private final double averageRating;
        private final long totalReviews;

        public RatingInfo(double averageRating, long totalReviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
        }

        public double getAverageRating() { return averageRating; }
        public long getTotalReviews() { return totalReviews; }
    }
}