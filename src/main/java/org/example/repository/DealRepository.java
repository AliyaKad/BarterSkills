package org.example.repository;

import org.example.entity.Deal;
import org.example.entity.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("SELECT d FROM Deal d WHERE d.customer.id = :userId OR d.executor.id = :userId ORDER BY d.createdAt DESC")
    List<Deal> findAllForUser(@Param("userId") Long userId);

    boolean existsByOfferIdAndCustomerIdAndStatusIn(Long offerId, Long customerId, List<DealStatus> statuses);

    boolean existsByRequestIdAndExecutorIdAndStatusIn(Long requestId, Long executorId, List<DealStatus> statuses);
}
