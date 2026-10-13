package com.studx.repository;

import com.studx.entity.ReservationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationAuditLogRepository extends JpaRepository<ReservationAuditLog, Long> {
    List<ReservationAuditLog> findByProductIdOrderByChangedAtDesc(Long productId);
}
