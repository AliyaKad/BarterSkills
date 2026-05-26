package org.example.controller;
import jakarta.servlet.http.HttpSession;
import org.example.dto.UserProfileResponse;
import org.example.entity.User;
import org.example.dto.ReviewResponse;
import java.util.stream.Collectors;
import org.example.service.ReviewService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ApiController {

    private final UserService userService;
    private final ReviewService reviewService;

    public ApiController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    // Вход
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        var userOpt = userService.loginUser(email, password);
        if (userOpt.isPresent()) {
            session.setAttribute("userId", userOpt.get().getId());
            return ResponseEntity.ok(Map.of(
                    "id", userOpt.get().getId(),
                    "name", userOpt.get().getFirstName()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Неверный email или пароль"));
    }

    // Регистрация
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String email, @RequestParam String password,
                                      @RequestParam String firstName, @RequestParam String lastName, HttpSession session) {
        try {
            var user = userService.registerUser(email, password, firstName, lastName);
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "message", "Успешная регистрация"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Текущий пользователь
    @GetMapping("/auth/me")
    public ResponseEntity<UserProfileResponse> getMe(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        return userService.getUserById(userId)
                .map(u -> ResponseEntity.ok(UserProfileResponse.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .skillCoins(u.getSkillCoinBalance())
                        .rating(u.getRating())
                        .isVerified(u.getIsVerified())
                        .city(u.getCity())
                        .bio(u.getBio())
                        .skillsCanOffer(u.getSkillsCanOffer())
                        .skillsNeeded(u.getSkillsNeeded())
                        .build()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    // Выход
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    //Получить отзывы о пользователе (REST API)
    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<?> getUserReviews(@PathVariable Long userId) {
        try {
            var info = reviewService.getUserRating(userId);

            var reviews = reviewService.getReviewsByUser(userId).stream()
                    .map(r -> ReviewResponse.builder()
                            .id(r.getId())
                            .authorId(r.getAuthor().getId())
                            .authorFirstName(r.getAuthor().getFirstName())
                            .authorLastName(r.getAuthor().getLastName())
                            .userId(r.getUser().getId())
                            .rating(r.getRating())
                            .comment(r.getComment())
                            .createdAt(r.getCreatedAt())
                            .build()
                    )
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "averageRating", info.getAverageRating(),
                    "totalReviews", info.getTotalReviews(),
                    "reviews", reviews
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //Создать отзыв (REST API)
    @PostMapping("/reviews/user/{userId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long userId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session) {

        Long authorId = (Long) session.getAttribute("userId");
        if (authorId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            var review = reviewService.createReview(authorId, userId, rating, comment, null);

            Map<String, Object> response = new HashMap<>();
            response.put("id", review.getId());
            response.put("rating", review.getRating());
            response.put("comment", review.getComment());
            response.put("createdAt", review.getCreatedAt());
            response.put("authorName", review.getAuthor().getFirstName() + " " + review.getAuthor().getLastName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //Обновить отзыв (REST API)
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session) {

        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            var review = reviewService.updateReview(reviewId, currentUserId, rating, comment);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "review", Map.of(
                            "id", review.getId(),
                            "rating", review.getRating(),
                            "comment", review.getComment()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //Удалить отзыв (REST API)
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            reviewService.deleteReview(reviewId, currentUserId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Обновление профиля
    @PutMapping("/user/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String city,
            @RequestParam String bio,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        try {
            User updatedUser = userService.updateProfile(userId, firstName, lastName, bio, city);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Добавление навыка
    @PostMapping("/user/skills/add")
    public ResponseEntity<?> addSkill(
            @RequestParam String type,
            @RequestParam String skill,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        try {
            if (type.equalsIgnoreCase("offer")) {
                return ResponseEntity.ok(userService.addSkillCanOffer(userId, skill));
            } else if (type.equalsIgnoreCase("need")) {
                return ResponseEntity.ok(userService.addSkillNeeded(userId, skill));
            }
            return ResponseEntity.badRequest().body("Неверный тип навыка");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "firstName", u.getFirstName(),
                        "lastName", u.getLastName(),
                        "email", u.getEmail(),
                        "rating", u.getRating(),
                        "skillCoins", u.getSkillCoinBalance(),
                        "bio", u.getBio(),
                        "city", u.getCity(),
                        "skillsCanOffer", u.getSkillsCanOffer(),
                        "skillsNeeded", u.getSkillsNeeded()
                        // "createdAt" убрали, так как его нет в User
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}