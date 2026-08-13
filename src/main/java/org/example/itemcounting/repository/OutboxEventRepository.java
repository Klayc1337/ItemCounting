package org.example.itemcounting.repository;

import org.example.itemcounting.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findBySentAtIsNullOrderByCreatedAtAsc();
}