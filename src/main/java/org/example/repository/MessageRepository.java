package org.example.repository;

import jakarta.transaction.Transactional;
import org.example.entity.Message;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndRecipientOrSenderAndRecipientOrderBySentAtAsc(
            User user1, User user2, User user2_, User user1_);

    List<Message> findByRecipientAndIsReadFalse(User recipient);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.recipient = :recipient AND m.sender = :sender AND m.isRead = false")
    void markAsRead(User recipient, User sender);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.sentAt DESC")
    List<Message> findRecentBetween(User user1, User user2, org.springframework.data.domain.Pageable pageable);
}