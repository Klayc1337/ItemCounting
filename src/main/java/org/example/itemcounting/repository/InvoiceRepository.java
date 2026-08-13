package org.example.itemcounting.repository;

import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // типу и статусу
    List<Invoice> findByTypeAndStatus(InvoiceType type, InvoiceStatus status);

    // накладные по статусу
    List<Invoice> findByStatus(InvoiceStatus status);

    //по типу
    List<Invoice> findByType(InvoiceType type);
}
