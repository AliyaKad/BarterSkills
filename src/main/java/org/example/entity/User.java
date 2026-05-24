package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private Float rating = 0.0f;

    @Column(name = "skill_coin_balance")
    private Integer skillCoinBalance = 0;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate = LocalDateTime.now();

    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String city;

    @ElementCollection
    @CollectionTable(name = "user_skills_can_offer", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    @Builder.Default
    private List<String> skillsCanOffer = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_skills_needed", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "need")
    @Builder.Default
    private List<String> skillsNeeded = new ArrayList<>();

    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceRequest> serviceRequests = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceOffer> serviceOffers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "executor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Deal> executedDeals = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Deal> customerDeals = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Message> sentMessages = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Message> receivedMessages = new ArrayList<>();

    public void addSkillCoins(Integer amount) {
        this.skillCoinBalance += amount;
    }

    public void spendSkillCoins(Integer amount) {
        if (this.skillCoinBalance < amount) {
            throw new IllegalStateException("Недостаточно SkillCoin");
        }
        this.skillCoinBalance -= amount;
    }

    public Double getRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // обновить рейтинг
    public void updateAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            this.rating = 0.0f;
        } else {
            double avg = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            this.rating = (float) avg;
        }
    }
}