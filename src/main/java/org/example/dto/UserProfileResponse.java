package org.example.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer skillCoins;
    private Double rating;
    private Boolean isVerified;
    private String city;
    private String bio;
    private List<String> skillsCanOffer;
    private List<String> skillsNeeded;
}