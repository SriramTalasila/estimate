package com.talasila.estimate.repository;

import com.talasila.estimate.model.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    List<Estimate> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    @Query("SELECT COALESCE(SUM(e.finalTotal), 0) FROM Estimate e WHERE e.business.id = :businessId AND e.createdAt >= :start AND e.createdAt < :end")
    BigDecimal sumFinalTotalByBusinessIdAndCreatedAtBetween(@Param("businessId") Long businessId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}