package org.example.itemcounting.rest.controller;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.InvoiceService;
import org.example.itemcounting.business.service.InvoiceSummCalculationService;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final InvoiceSummCalculationService calculationService;

    // ---------- Приходные накладные ----------
    // создание приходной накладной
    @PostMapping("/arrival")
    public ResponseEntity<InvoiceDTO> createArrivalInvoice(@RequestBody InvoiceDTO requestDto) {
        requestDto.setType(InvoiceType.ARRIVAL);

        if (requestDto.getStatus() == null) {
            requestDto.setStatus(InvoiceStatus.DRAFT);
        }

        InvoiceDTO created = invoiceService.createInvoice(requestDto);
        return ResponseEntity.status(201).body(created);
    }

    // получение приходных накладных
    @GetMapping("/arrival")
    public ResponseEntity<List<InvoiceDTO>> getArrivalInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoicesByType(InvoiceType.ARRIVAL);
        return ResponseEntity.ok(invoices);
    }

    // ---------- Расходные накладные ----------
    // создание расходной накладной
    @PostMapping("/shipment")
    public ResponseEntity<InvoiceDTO> createShipmentInvoice(@RequestBody InvoiceDTO requestDto) {
        requestDto.setType(InvoiceType.SHIPMENT);

        if (requestDto.getStatus() == null) {
            requestDto.setStatus(InvoiceStatus.DRAFT);
        }

        InvoiceDTO created = invoiceService.createInvoice(requestDto);
        return ResponseEntity.status(201).body(created);
    }

    // история отгрузок
    @GetMapping("/shipment")
    public ResponseEntity<List<InvoiceDTO>> getShipmentInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoicesByType(InvoiceType.SHIPMENT);
        return ResponseEntity.ok(invoices);
    }

    // получение накладной по id
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // отмена накладной
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInvoice(@PathVariable Long id) {
        invoiceService.cancelInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summ")
    public ResponseEntity<BigDecimal> getTotal(
            @PathVariable Long id,
            @RequestParam(defaultValue = "default") String strategy,
            @RequestParam(required = false) String deliveryTime) {

        LocalDateTime time = (deliveryTime != null) ? LocalDateTime.parse(deliveryTime) : LocalDateTime.now();

        BigDecimal total = calculationService.calculateInvoiceTotal(id, strategy, time);
        return ResponseEntity.ok(total);
    }
}
