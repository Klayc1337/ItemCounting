package org.example.itemcounting.invoiceService;
import org.example.itemcounting.business.service.InvoiceService;
import org.example.itemcounting.business.service.OutboxEventService;
import org.example.itemcounting.business.service.StockService;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.IllegalInvoiceStateException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.rest.dto.InvoiceItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private OutboxEventService outboxEventService;
    
    private InvoiceService invoiceService;

    private final Long INVOICE_ID = 1L;
    private final Long PRODUCT_ID = 10L;
    private final int QUANTITY = 5;

    private Invoice invoice;
    private Product product;
    private InvoiceItem invoiceItem;
    private InvoiceDTO requestDto;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceItemRepository,
                productRepository,
                stockService,
                outboxEventService
        );

        invoice = Invoice.builder()
                .id(INVOICE_ID)
                .status(InvoiceStatus.DRAFT)
                .type(InvoiceType.ARRIVAL)
                .build();

        product = Product.builder()
                .id(PRODUCT_ID)
                .build();

        invoiceItem = InvoiceItem.builder()
                .id(100L)
                .product(product)
                .quantity(BigDecimal.valueOf(QUANTITY))
                .invoice(invoice)
                .build();

        InvoiceItemDTO itemDto = InvoiceItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(BigDecimal.valueOf(QUANTITY))
                .build();

        requestDto = InvoiceDTO.builder()
                .type(InvoiceType.ARRIVAL)
                .items(List.of(itemDto))
                .comment("Test comment")
                .build();
    }

    // создание приходной накладной
    @Test
    void createInvoice_arrival() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(List.of(invoiceItem));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceDTO result = invoiceService.createInvoice(requestDto);

        assertEquals(INVOICE_ID, result.getId());
        assertEquals(InvoiceStatus.COMPLETED, result.getStatus());
    }

    // создание отходной накладной
    @Test
    void createInvoice_shipment() {
        requestDto.setType(InvoiceType.SHIPMENT);
        invoice.setType(InvoiceType.SHIPMENT);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(invoiceItemRepository.saveAll(anyList())).thenReturn(List.of(invoiceItem));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceDTO result = invoiceService.createInvoice(requestDto);

        assertEquals(InvoiceStatus.COMPLETED, result.getStatus());
    }

    // создание накладной с отсутствием типа
    @Test
    void createInvoice_nullType() {
        requestDto.setType(null);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(requestDto));
    }

    // создание накладной с пустыми товарами
    @Test
    void createInvoice_emptyItems() {
        requestDto.setItems(Collections.emptyList());
        assertThrows(InvalidQuantityException.class, () -> invoiceService.createInvoice(requestDto));
    }

    // создание накладной с отсутствием id продукта
    @Test
    void createInvoice_productNotFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(requestDto));
    }

    // получение накладной по id
    @Test
    void getInvoiceById() {
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

        InvoiceDTO result = invoiceService.getInvoiceById(INVOICE_ID);

        assertEquals(INVOICE_ID, result.getId());
        assertEquals(1, result.getItems().size());
    }

    // получение накладной по id (отсутствие)
    @Test
    void getInvoiceById_notFound() {
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> invoiceService.getInvoiceById(INVOICE_ID));
    }

    // получение всех накладных
    @Test
    void getAllInvoices() {
        List<Invoice> invoices = List.of(invoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

        List<InvoiceDTO> result = invoiceService.getAllInvoices();

        assertEquals(1, result.size());
        assertEquals(INVOICE_ID, result.getFirst().getId());
    }

    // получение всех накладных(пусто)
    @Test
    void getAllInvoices_emptyList() {
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());
        List<InvoiceDTO> result = invoiceService.getAllInvoices();
        assertTrue(result.isEmpty());
    }

    // получение накладных по типу и статусу
    @Test
    void getAllInvoicesByTypeAndStatus() {
        List<Invoice> invoices = List.of(invoice);
        when(invoiceRepository.findByTypeAndStatus(InvoiceType.ARRIVAL, InvoiceStatus.COMPLETED))
                .thenReturn(invoices);
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

        List<InvoiceDTO> result = invoiceService.getAllInvoicesByTypeAndStatus(InvoiceType.ARRIVAL, InvoiceStatus.COMPLETED);

        assertEquals(1, result.size());
    }

    // получение накладных по типу
    @Test
    void getAllInvoicesByType() {
        List<Invoice> invoices = List.of(invoice);
        when(invoiceRepository.findByType(InvoiceType.ARRIVAL)).thenReturn(invoices);
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

        List<InvoiceDTO> result = invoiceService.getAllInvoicesByType(InvoiceType.ARRIVAL);
        assertEquals(1, result.size());
    }

    // получение накладных по статусу
    @Test
    void getAllInvoicesByStatus() {
        List<Invoice> invoices = List.of(invoice);
        when(invoiceRepository.findByStatus(InvoiceStatus.COMPLETED)).thenReturn(invoices);
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));

        List<InvoiceDTO> result = invoiceService.getAllInvoicesByStatus(InvoiceStatus.COMPLETED);
        assertEquals(1, result.size());
    }

    // отмена накладных (уже отменено)
    @Test
    void cancelInvoice_alreadyCancelled() {
        invoice.setStatus(InvoiceStatus.CANCELLED);
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

        assertThrows(IllegalInvoiceStateException.class, () -> invoiceService.cancelInvoice(INVOICE_ID));
    }

    // отмена накладных(DRAFT)
    @Test
    void cancelInvoice_draft() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.cancelInvoice(INVOICE_ID);

        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
    }

    // отмена накладных (COMPLETED and ARRIVAL)
    @Test
    void cancelInvoice_completed_arrival() {
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setType(InvoiceType.ARRIVAL);
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.cancelInvoice(INVOICE_ID);

        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
    }

    // отмена накладных (COMPLETED and SHIPMENT)
    @Test
    void cancelInvoice_completed_shipment() {
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setType(InvoiceType.SHIPMENT);
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of(invoiceItem));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.cancelInvoice(INVOICE_ID);

        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
    }

    @Test
    void cancelInvoice_completed_noItems() {
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setType(InvoiceType.ARRIVAL);
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(INVOICE_ID)).thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.cancelInvoice(INVOICE_ID);

        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
    }

    // отмена накладной(накладная не найдена)
    @Test
    void cancelInvoice_invoiceNotFound() {
        when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> invoiceService.cancelInvoice(INVOICE_ID));
    }
}
