package org.example.itemcounting.repository;

import org.example.itemcounting.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    // все строки по ID накладной
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    // все строки по ID товара
    List<InvoiceItem> findByProductId(Long productId);
}
