package org.example.itemcounting.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.business.service.WaitingInvoiceService;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.repository.InvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingInvoiceScheduler {
    private final InvoiceRepository invoiceRepository;
    private final WaitingInvoiceService waitingInvoiceService;

    @Scheduled(fixedDelayString = "${invoice.wait-processing-delay:10000}")
    public void processWaitingInvoices() {
        for (Long invoiceId : invoiceRepository.findIdsByStatusOrderByCreatedAtAscIdAsc(InvoiceStatus.WAIT)) {
            try {
                waitingInvoiceService.completeIfStockAvailable(invoiceId);
            } catch (Exception e) {
                log.error("ошибка завершения waiting invoice #{}", invoiceId, e);
            }
        }
    }
}
