package org.example.repository;

import org.example.entity.Deal;
import org.example.entity.Review;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Найти все отзывы о пользователе (кому оставили)
    List<Review> findByUser(User user);

    // Найти все отзывы, написанные пользователем
    List<Review> findByAuthor(User author);

    // Проверить, оставлял ли пользователь уже отзыв для этой сделки
    boolean existsByAuthorAndDeal(User author, Deal deal);
}