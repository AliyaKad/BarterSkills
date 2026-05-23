package org.example.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private Long revieweeId;
    private String revieweeName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}