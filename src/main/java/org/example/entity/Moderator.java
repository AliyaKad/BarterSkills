package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "moderators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moderator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", unique = true)
    private Integer adminId;

    @ElementCollection
    @CollectionTable(name = "moderator_permissions", joinColumns = @JoinColumn(name = "moderator_id"))
    @Column(name = "permission")
    @Builder.Default
    private List<String> permissions = new ArrayList<>();

    // Связь с User (один модератор = один пользователь)
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void approveOffer(Long offerId) {
        // Логика одобрения предложения
    }

    public void rejectOffer(Long offerId, String reason) {
        // Логика отклонения предложения
    }

    public void banUser(Long userId) {
        // Логика бана пользователя
    }
}
