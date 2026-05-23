package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false)
    private Long dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String description;

    // Связь с Deal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", insertable = false, updatable = false)
    private Deal deal;

    public Boolean execute() {
        try {
            fromUser.spendSkillCoins(amount);
            toUser.addSkillCoins(amount);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void rollback() {
        toUser.spendSkillCoins(amount);
        fromUser.addSkillCoins(amount);
    }
}

enum TransactionType {
    CREDIT,   // Начисление
    DEBIT     // Списание
}
