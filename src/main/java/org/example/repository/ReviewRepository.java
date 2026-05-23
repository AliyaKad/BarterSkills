package org.example.repository;

import org.example.entity.Review;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Найти все отзывы о пользователе (кому оставили)
    List<Review> findByUser(User user);

    // Найти все отзывы, написанные пользователем
    List<Review> findByAuthor(User author);

    // Найти отзыв по сделке
    List<Review> findByDealId(Long dealId);

    // Проверить, оставлял ли пользователь уже отзыв для этой сделки
    boolean existsByAuthorAndDealId(User author, Long dealId);

    // Подсчитать количество отзывов о пользователе
    long countByUser(User user);

    // Получить средний рейтинг пользователя (если через JPQL)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.user = :user")
    Double getAverageRatingForUser(@Param("user") User user);
}