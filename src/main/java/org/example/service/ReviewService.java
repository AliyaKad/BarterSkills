package org.example.service;

import org.example.dto.CreateReviewRequest;
import org.example.dto.RatingResponse;
import org.example.dto.ReviewResponse;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    // private final DealRepository dealRepository;
    // private final ServiceRequestRepository serviceRequestRepository;

    // Создать новый отзыв
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long authorId) {
        // 1. Проверяем, существует ли автор отзыва
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id: " + authorId));

        // 2. Проверяем, существует ли получатель отзыва
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + request.getUserId()));

        // 3. Нельзя оставить отзыв самому себе
        if (author.getId().equals(user.getId())) {
            throw new RuntimeException("Нельзя оставить отзыв самому себе");
        }

        // 4. Проверяем, не оставлял ли уже отзыв для этой сделки
        if (request.getDealId() != null) {
            boolean alreadyReviewed = reviewRepository.existsByAuthorAndDealId(author, request.getDealId());
            if (alreadyReviewed) {
                throw new RuntimeException("Вы уже оставили отзыв для этой сделки");
            }
        }

        // 5. Создаем отзыв
        Review review = Review.builder()
                .author(author)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        // TODO 6. Привязываем к сделке, если указана (нужен DealRepository)
        //  if (request.getDealId() != null) {
        //    Deal deal = dealRepository.findById(request.getDealId())
        //            .orElseThrow(() -> new RuntimeException("Сделка не найдена с id: " + request.getDealId()));
        //    review.setDeal(deal);
        //}

        // TODO 7. Привязываем к запросу услуги, если указан (нужен ServiceRequestRepository)
//          if (request.getServiceRequestId() != null) {
//            ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
//                    .orElseThrow(() -> new RuntimeException("Запрос услуги не найден с id: " + request.getServiceRequestId()));
//            review.setServiceRequest(serviceRequest);
//        }

        // 8. Сохраняем отзыв
        Review savedReview = reviewRepository.save(review);

        // TODO 9. Обновляем рейтинг пользователя (через метод из User)
        //  User.updateRating()

        return mapToResponse(savedReview);
    }

    // Получить все отзывы о пользователе
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        return reviewRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Получить все отзывы, написанные пользователем
    public List<ReviewResponse> getReviewsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id: " + authorId));

        return reviewRepository.findByAuthor(author).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Получить рейтинг пользователя (средняя оценка + количество отзывов)
    public RatingResponse getUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        Double avgRating = reviewRepository.getAverageRatingForUser(user);
        long totalReviews = reviewRepository.countByUser(user);

        return RatingResponse.builder()
                .userId(userId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    // Получить отзыв по ID
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден с id: " + id));
        return mapToResponse(review);
    }

    // Обновить отзыв (ТОЛЬКО АВТОР)
    @Transactional
    public ReviewResponse updateReview(Long id, CreateReviewRequest request, Long currentUserId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден с id: " + id));

        // Проверяем, что текущий пользователь — автор отзыва
        if (!review.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("Только автор может редактировать отзыв");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        return mapToResponse(updatedReview);
    }

    // Удалить отзыв (ТОЛЬКО АВТОР ИЛИ АДМИН)
    @Transactional
    public void deleteReview(Long id, Long currentUserId, boolean isAdmin) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден с id: " + id));

        // Проверяем права: либо автор, либо админ
        if (!review.getAuthor().getId().equals(currentUserId) && !isAdmin) {
            throw new RuntimeException("Нет прав для удаления этого отзыва");
        }

        reviewRepository.delete(review);
    }

    // Маппинг Review -> ReviewResponse
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .authorId(review.getAuthor().getId())
                .authorFirstName(review.getAuthor().getFirstName())
                .authorLastName(review.getAuthor().getLastName())
                .userId(review.getUser().getId())
                .userFirstName(review.getUser().getFirstName())
                .userLastName(review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .dealId(review.getDeal() != null ? review.getDeal().getId() : null)
                .serviceRequestId(review.getServiceRequest() != null ? review.getServiceRequest().getId() : null)
                .build();
    }
}