package org.example.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingResponse {
    private Long userId;
    private Double averageRating;   // средний рейтинг (1.0 - 5.0)
    private Long totalReviews;      // количество отзывов
}