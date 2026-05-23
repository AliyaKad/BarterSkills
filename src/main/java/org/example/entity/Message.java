package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false)
    private Long dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    // Связь с Deal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", insertable = false, updatable = false)
    private Deal deal;

    public void send(String content) {
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
