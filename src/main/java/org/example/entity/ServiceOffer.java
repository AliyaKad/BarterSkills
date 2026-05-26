package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "price_in_skill_coins")
    private Integer priceInSkillCoins;

    @Enumerated(EnumType.STRING)
    private OfferStatus status = OfferStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Связи
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Deal> deals = new ArrayList<>();

    public ServiceOffer(String title, String description, Integer price, User createdBy) {
        this.title = title;
        this.description = description;
        this.priceInSkillCoins = price;
        this.createdBy = createdBy;
        this.category = Category.OTHER;
    }

    public void updateStatus(OfferStatus newStatus) {
        this.status = newStatus;
    }

    public void delete() {
        this.status = OfferStatus.CANCELLED;
    }
}
