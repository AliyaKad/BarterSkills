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

    @Column(name = "deal_id")
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
    public static Message create(User sender, User recipient, String content, Long dealId) {
        return Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .dealId(dealId)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    public static Message create(User sender, User recipient, String content) {
        return create(sender, recipient, content, null);
    }

    public boolean isRecipient(User user) {
        return user != null && recipient != null && recipient.getId().equals(user.getId());
    }

    public boolean isSender(User user) {
        return user != null && sender != null && sender.getId().equals(user.getId());
    }

    public boolean involvesUser(User user) {
        return isSender(user) || isRecipient(user);
    }

    public String getOtherUserName(User currentUser) {
        if (currentUser == null) return "Неизвестный";
        if (isSender(currentUser) && recipient != null) {
            return recipient.getFirstName() + " " + (recipient.getLastName() != null ? recipient.getLastName() : "");
        } else if (isRecipient(currentUser) && sender != null) {
            return sender.getFirstName() + " " + (sender.getLastName() != null ? sender.getLastName() : "");
        }
        return "Неизвестный";
    }

    public String getFormattedTime() {
        if (sentAt == null) return "";
        return String.format("%02d:%02d", sentAt.getHour(), sentAt.getMinute());
    }

    public String getContentPreview(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "…";
    }
}