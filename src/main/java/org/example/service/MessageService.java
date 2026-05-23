package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.repository.MessageRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Message sendMessage(Long senderId, Long recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = Message.create(sender, recipient, content.trim());
        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long userId1, Long userId2, int limit) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        return messageRepository.findRecentBetween(user1, user2, PageRequest.of(0, limit));
    }

    public List<Message> getUnreadMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long recipientId, Long senderId) {
        User recipient = userRepository.findById(recipientId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();
        messageRepository.markAsRead(recipient, sender);
    }

    public List<User> getChatContacts(Long userId) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId))
                .limit(20)
                .toList();
    }
}