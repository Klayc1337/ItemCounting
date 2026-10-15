package org.example.itemcounting.repository;

import org.example.itemcounting.entity.ReplenishmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, Long> {
}
