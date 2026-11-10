package org.example.itemcounting.repository;

import jakarta.persistence.LockModeType;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("select i.id from Invoice i where i.status = :status order by i.createdAt, i.id")
    List<Long> findIdsByStatusOrderByCreatedAtAscIdAsc(@Param("status") InvoiceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findLockedById(@Param("id") Long id);

    // типу и статусу
    List<Invoice> findByTypeAndStatus(InvoiceType type, InvoiceStatus status);

    // накладные по статусу
    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByStatusAndFromWarehouseId(InvoiceStatus status, Long warehouseId);

    //по типу
    List<Invoice> findByType(InvoiceType type);
}
