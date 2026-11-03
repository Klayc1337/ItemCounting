package org.example.itemcounting.repository;

import jakarta.persistence.LockModeType;
import org.example.itemcounting.entity.ReplenishmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReplenishmentRequest r where r.id = :id")
    Optional<ReplenishmentRequest> findLockedById(@Param("id") Long id);
}