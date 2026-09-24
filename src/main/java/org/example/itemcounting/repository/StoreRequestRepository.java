package org.example.itemcounting.repository;

import org.example.itemcounting.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRequestRepository extends JpaRepository<StoreOrder, Long> {
}
