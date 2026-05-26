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

    @Column(name = "customer_confirmed")
    @Builder.Default
    private Boolean customerConfirmed = false;

    @Column(name = "executor_confirmed")
    @Builder.Default
    private Boolean executorConfirmed = false;

    @Column(name = "coins_held")
    @Builder.Default
    private Boolean coinsHeld = false;

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
        this.status = DealStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = DealStatus.COMPLETED;
        this.completionDate = LocalDateTime.now();
    }

    public void confirmByCustomer() {
        this.customerConfirmed = true;
    }

    public void confirmByExecutor() {
        this.executorConfirmed = true;
    }

    public boolean isFullyConfirmed() {
        return Boolean.TRUE.equals(customerConfirmed) && Boolean.TRUE.equals(executorConfirmed);
    }

    public void cancel() {
        this.status = DealStatus.CANCELLED;
    }

    public void dispute() {
        this.status = DealStatus.DISPUTED;
    }
}
