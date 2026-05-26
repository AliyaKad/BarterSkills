package org.example.repository;

import org.example.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
           "WHERE t.fromUser.id = :userId OR t.toUser.id = :userId " +
           "ORDER BY t.timestamp DESC")
    List<Transaction> findAllForUser(@Param("userId") Long userId);

    List<Transaction> findByDealIdOrderByTimestampDesc(Long dealId);
}
