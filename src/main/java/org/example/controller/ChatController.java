package org.example.controller;

import jakarta.servlet.http.HttpSession;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.service.MessageService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ChatController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    @GetMapping("/{recipientId}")
    public String chatPage(@PathVariable Long recipientId,
                           HttpSession session,
                           Model model) {

        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return "redirect:/login";
        }

        var recipientOpt = userService.getUserById(recipientId);
        if (recipientOpt.isEmpty()) {
            model.addAttribute("error", "Пользователь не найден");
            return "error";
        }

        User currentUser = userService.getUserById(currentUserId).orElseThrow();
        User recipient = recipientOpt.get();

        List<Message> messages = messageService.getConversation(currentUserId, recipientId, 100);

        messageService.markAsRead(currentUserId, recipientId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("recipient", recipient);
        model.addAttribute("messages", messages);
        model.addAttribute("timeFormat", TIME_FORMAT);

        return "chat";
    }

    @GetMapping
    public String chatsList(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) return "redirect:/login";

        List<User> contacts = messageService.getChatContacts(userId);
        List<Message> unread = messageService.getUnreadMessages(userId);

        model.addAttribute("currentUser", userOpt.get());
        model.addAttribute("contacts", contacts);
        model.addAttribute("unreadCount", unread.size());

        return "chat-list";
    }

    @PostMapping("/api/{recipientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long recipientId,
            @RequestParam String content,
            HttpSession session) {

        Long senderId = (Long) session.getAttribute("userId");
        if (senderId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            Message message = messageService.sendMessage(senderId, recipientId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("content", message.getContent());
            response.put("sentAt", message.getSentAt().format(TIME_FORMAT));
            response.put("isMine", true);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/{recipientId}/new")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getNewMessages(
            @PathVariable Long recipientId,
            @RequestParam(required = false) Long lastMessageId,
            HttpSession session) {

        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(List.of());
        }

        List<Message> messages = messageService.getConversation(currentUserId, recipientId, 50);

        if (lastMessageId != null) {
            messages = messages.stream()
                    .filter(m -> m.getId() > lastMessageId)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = messages.stream().map(m -> {
            Map<String, Object> msg = new HashMap<>();
            msg.put("id", m.getId());
            msg.put("content", m.getContent());
            msg.put("sentAt", m.getSentAt().format(TIME_FORMAT));
            msg.put("isMine", m.getSender().getId().equals(currentUserId));
            msg.put("senderName", m.getSender().getFirstName());
            return msg;
        }).collect(Collectors.toList());

        messageService.markAsRead(currentUserId, recipientId);

        return ResponseEntity.ok(result);
    }
}
