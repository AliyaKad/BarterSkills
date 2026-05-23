package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "max_price")
    private Integer maxPrice;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Связи
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Deal> deals = new ArrayList<>();

    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ServiceRequest(String description, Integer maxPrice, User requestedBy) {
        this.description = description;
        this.maxPrice = maxPrice;
        this.requestedBy = requestedBy;
        this.category = Category.OTHER;
    }

    public Deal acceptOffer(Long offerId) {
        // Логика принятия предложения
        this.status = RequestStatus.IN_PROGRESS;
        // Возвращает Deal (реализуется в сервисе)
        return null;
    }
}

enum Category {
    EDUCATION,    // Образование
    IT,           // IT и программирование
    HOUSEHOLD,    // Бытовые услуги
    CREATIVE,     // Творчество
    TRANSLATION,  // Переводы
    LEGAL,        // Юридические услуги
    OTHER         // Другое
}

enum RequestStatus {
    OPEN,           // Открыт
    IN_PROGRESS,    // В работе
    DONE,           // Завершён
    CANCELLED       // Отменён
}
