package org.example.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long authorId;
    private String authorFirstName;
    private String authorLastName;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long dealId;
    private Long serviceRequestId;
}