//package org.example.itemcounting.invoiceService;
//import org.example.itemcounting.ItemCountingApplication;
//import org.example.itemcounting.business.service.InvoiceService;
//import org.example.itemcounting.business.service.OutboxEventService;
//import org.example.itemcounting.business.service.StockService;
//import org.example.itemcounting.entity.Invoice;
//import org.example.itemcounting.entity.InvoiceItem;
//import org.example.itemcounting.entity.Product;
//import org.example.itemcounting.enums.InvoiceStatus;
//import org.example.itemcounting.enums.InvoiceType;
//import org.example.itemcounting.exception.EntityNotFoundException;
//import org.example.itemcounting.exception.IllegalInvoiceStateException;
//import org.example.itemcounting.repository.InvoiceItemRepository;
//import org.example.itemcounting.repository.InvoiceRepository;
//import org.example.itemcounting.repository.ProductRepository;
//import org.example.itemcounting.rest.dto.InvoiceDTO;
//import org.example.itemcounting.rest.dto.InvoiceItemDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import org.springframework.boot.persistence.autoconfigure.EntityScan;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.mockito.Mock;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@DataJpaTest
//@Import(InvoiceService.class)
//@ActiveProfiles("test")
//public class InvoiceServiceIntegrationTest {
//
//    @Autowired
//    private InvoiceService invoiceService;
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    @Autowired
//    private InvoiceItemRepository invoiceItemRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @MockitoBean
//    private OutboxEventService outboxEventService;
//
//    @MockitoBean
//    private StockService stockService;
//
//    private Product product;
//    private InvoiceDTO requestDto;
//
//    @BeforeEach
//    void setUp() {
//        product = Product.builder()
//                .name("Test Product")
//                .sku("SKU-001")
//                .unit("шт")
//                .build();
//
//        product = productRepository.save(product);
//
//        InvoiceItemDTO itemDto = InvoiceItemDTO.builder()
//                .productId(product.getId())
//                .quantity(BigDecimal.valueOf(5))
//                .build();
//
//        requestDto = InvoiceDTO.builder()
//                .type(InvoiceType.ARRIVAL)
//                .items(List.of(itemDto))
//                .comment("Test comment")
//                .build();
//    }
//
//    @Test
//    void createInvoice_success_arrival() {
//
//        InvoiceDTO result = invoiceService.createInvoice(requestDto);
//
//        assertThat(result.getId()).isNotNull();
//        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.COMPLETED);
//        assertThat(result.getItems()).hasSize(1);
//        assertThat(result.getItems().getFirst().getProductId()).isEqualTo(product.getId());
//
//
//
//        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(result.getId());
//        assertThat(items).hasSize(1);
//        assertThat(items.getFirst().getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5));
//        assertThat(items.getFirst().getProduct().getId()).isEqualTo(product.getId());
//    }
//}
