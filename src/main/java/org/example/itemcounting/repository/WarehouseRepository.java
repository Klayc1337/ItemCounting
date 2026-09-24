package org.example.itemcounting.repository;

import org.example.itemcounting.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
