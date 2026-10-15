package org.example.itemcounting.repository;

import org.example.itemcounting.entity.StoreOrder;
import org.example.itemcounting.entity.Warehouse;
import org.example.itemcounting.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface StoreRequestRepository extends JpaRepository<StoreOrder, Long> {
    @Query("select r from StoreOrder r where r.warehouse = :warehouse and r.status in :statuses")
    List<StoreOrder> findByToWarehouseAndStatusIn(Warehouse warehouse, Collection<RequestStatus> statuses);
}
