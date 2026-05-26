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
    @Builder.Default
    private Integer skillCoinBalance = 0;

    @Column(name = "skill_coin_held")
    @Builder.Default
    private Integer skillCoinHeld = 0;

<<<<<<< HEAD
=======
    @Column(name = "bonus_skills_can_offer_granted")
    @Builder.Default
    private Boolean bonusSkillsCanOfferGranted = false;

    @Column(name = "bonus_skills_needed_granted")
    @Builder.Default
    private Boolean bonusSkillsNeededGranted = false;

>>>>>>> 4231912c2b2a9be697fc4e8e028a5edca4be66d7
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

    public void addSkillCoins(int amount) {
        this.skillCoinBalance += amount;
    }

    public boolean hasAvailableSkillCoins(int amount) {
        return this.skillCoinBalance >= amount;
    }

    public void holdSkillCoins(int amount) {
        if (!hasAvailableSkillCoins(amount)) {
            throw new IllegalStateException("Недостаточно SkillCoin на балансе");
        }
        this.skillCoinBalance -= amount;
        this.skillCoinHeld += amount;
    }

    public void releaseHeldSkillCoins(int amount) {
        if (this.skillCoinHeld < amount) {
            throw new IllegalStateException("Недостаточно замороженных SkillCoin");
        }
        this.skillCoinHeld -= amount;
    }

    public void refundHeldSkillCoins(int amount) {
        if (this.skillCoinHeld < amount) {
            throw new IllegalStateException("Недостаточно замороженных SkillCoin");
        }
        this.skillCoinHeld -= amount;
        this.skillCoinBalance += amount;
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