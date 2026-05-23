package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private ServiceOffer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ServiceRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = false)
    private User executor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private DealStatus status = DealStatus.PROPOSED;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Связи
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Message> messages = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Review> reviews = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Transaction> transactions = new java.util.ArrayList<>();

    // Методы
    public void propose() {
        this.status = DealStatus.PROPOSED;
    }

    public void accept() {
        this.status = DealStatus.ACCEPTED;
    }

    public void complete() {
        this.status = DealStatus.COMPLETED;
        this.completionDate = LocalDateTime.now();
        // Запускает транзакцию токенов
    }

    public void cancel() {
        this.status = DealStatus.CANCELLED;
    }

    public void dispute() {
        this.status = DealStatus.DISPUTED;
    }
}

enum DealStatus {
    PROPOSED,     // Предложена
    ACCEPTED,     // Принята
    IN_PROGRESS,  // В процессе
    COMPLETED,    // Завершена
    DISPUTED,     // Спор
    CANCELLED     // Отменена
}