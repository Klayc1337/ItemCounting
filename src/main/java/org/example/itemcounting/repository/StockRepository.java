package org.example.itemcounting.repository;

import jakarta.persistence.LockModeType;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.entity.Stock;
import org.example.itemcounting.entity.Warehouse;
import org.example.itemcounting.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stock> findByProductAndWarehouseAndLocation(Product product, Warehouse warehouse, WarehouseLocation location);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.warehouse.id = :warehouseId order by s.product.id, s.id")
    List<Stock> findLockedByWarehouseId(@Param("warehouseId") Long warehouseId);

    List<Stock> findByWarehouseIn(Collection<Warehouse> warehouses);

    List<Stock> findByWarehouseId(Long warehouseId);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    BigDecimal getTotalQuantityByProductAndWarehouse(Long productId, Long warehouseId);
}
