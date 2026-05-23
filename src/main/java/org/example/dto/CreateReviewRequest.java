package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "ID получателя отзыва обязателен")
    private Long revieweeId;  // кому оставляем отзыв

    @NotNull(message = "ID сделки обязателен")
    private Long dealId;  // по какой сделке отзыв

    @Min(value = 1, message = "Оценка должна быть от 1 до 5")
    @Max(value = 5, message = "Оценка должна быть от 1 до 5")
    private Integer rating;

    @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
    private String comment;
}