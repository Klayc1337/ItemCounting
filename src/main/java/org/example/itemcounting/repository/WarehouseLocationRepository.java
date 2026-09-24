package org.example.itemcounting.repository;

import org.example.itemcounting.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {
    List<WarehouseLocation> findByWarehouseIdAndIsActiveTrue(Long warehouseId);
}
