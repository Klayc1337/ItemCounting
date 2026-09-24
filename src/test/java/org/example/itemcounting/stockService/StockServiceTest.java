//package org.example.itemcounting.stockService;
//import org.example.itemcounting.business.service.StockService;
//import org.example.itemcounting.entity.Product;
//import org.example.itemcounting.entity.Stock;
//import org.example.itemcounting.exception.EntityNotFoundException;
//import org.example.itemcounting.exception.InsufficientStockException;
//import org.example.itemcounting.exception.InvalidQuantityException;
//import org.example.itemcounting.repository.ProductRepository;
//import org.example.itemcounting.repository.StockRepository;
//import org.example.itemcounting.rest.dto.StockDTO;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//import static org.junit.jupiter.api.Assertions.*;
//
//
//@ExtendWith(MockitoExtension.class)
//class StockServiceTest {
//
//    @Mock
//    private StockRepository stockRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @InjectMocks
//    private StockService stockService;
//
//    @Test
//    void getAllStocks() {
//
//        Product product1 = Product.builder()
//                .id(1L)
//                .build();
//
//        Product product2 = Product.builder()
//                .id(2L)
//                .build();
//
//        Stock stock1 = Stock.builder()
//                .id(1L)
//                .quantity(BigDecimal.valueOf(10))
//                .product(product1)
//                .build();
//
//        Stock stock2 = Stock.builder()
//                .id(2L)
//                .quantity(BigDecimal.valueOf(5))
//                .product(product2)
//                .build();
//
//        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));
//
//        List<StockDTO> result = stockService.getAllStocks();
//
//        assertEquals(2, result.size());
//        assertEquals(1L, result.get(0).getProductId());
//        assertEquals(BigDecimal.valueOf(10), result.get(0).getQuantity());
//        assertEquals(2L, result.get(1).getProductId());
//        assertEquals(BigDecimal.valueOf(5), result.get(1).getQuantity());
//
//    }
//
//    @Test
//    void increaseStock_existingStock() {
//
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(3);
//        Product product = Product.builder().id(productId).build();
//        Stock stock = Stock.builder()
//                .id(1L)
//                .quantity(BigDecimal.valueOf(10))
//                .product(product)
//                .build();
//
//        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
//
//
//        stockService.increaseStock(productId, quantity);
//
//
//        assertEquals(BigDecimal.valueOf(13), stock.getQuantity());
//
//    }
//
//    @Test
//    void increaseStock_newStock() {
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(3);
//        Product product = Product.builder().id(productId).build();
//
//        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());
//        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> {
//            Stock s = invocation.getArgument(0);
//            return Stock.builder()
//                    .id(1L)
//                    .quantity(s.getQuantity())
//                    .product(s.getProduct())
//                    .build();
//        });
//
//        stockService.increaseStock(productId, quantity);
//    }
//
//    @Test
//    void increaseStock_invalidQuantity() {
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.ZERO;
//
//        assertThrows(InvalidQuantityException.class,
//                () -> stockService.increaseStock(productId, quantity));
//
//    }
//
//    @Test
//    void increaseStock_negativeQuantity() {
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(-5);
//
//        assertThrows(InvalidQuantityException.class,
//                () -> stockService.increaseStock(productId, quantity));
//
//    }
//
//    @Test
//    void decreaseStock_success() {
//
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(3);
//        Product product = Product.builder().id(productId).build();
//        Stock stock = Stock.builder()
//                .id(1L)
//                .quantity(BigDecimal.valueOf(10))
//                .product(product)
//                .build();
//
//        when(stockRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(stock));
//
//
//        stockService.decreaseStock(productId, quantity);
//
//
//        assertEquals(BigDecimal.valueOf(7), stock.getQuantity());
//    }
//
//    @Test
//    void decreaseStock_insufficientStock() {
//
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(15);
//        Stock stock = Stock.builder()
//                .quantity(BigDecimal.valueOf(10))
//                .build();
//
//        when(stockRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(stock));
//
//
//        assertThrows(InsufficientStockException.class,
//                () -> stockService.decreaseStock(productId, quantity));
//        assertEquals(BigDecimal.valueOf(10), stock.getQuantity());
//
//    }
//
//    @Test
//    void decreaseStock_stockNotFound() {
//
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.valueOf(3);
//        when(stockRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());
//
//
//        assertThrows(EntityNotFoundException.class,
//                () -> stockService.decreaseStock(productId, quantity));
//
//    }
//
//    @Test
//    void decreaseStock_invalidQuantity() {
//        Long productId = 1L;
//        BigDecimal quantity = BigDecimal.ZERO;
//
//        assertThrows(InvalidQuantityException.class,
//                () -> stockService.decreaseStock(productId, quantity));
//
//    }
//
//    @Test
//    void getStockByProductId_success() {
//
//        Long productId = 1L;
//        Stock stock = Stock.builder().id(1L).build();
//        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
//
//
//        Stock result = stockService.getStockByProductId(productId);
//
//
//        assertSame(stock, result);
//
//    }
//
//    @Test
//    void getStockByProductId_notFound() {
//
//        Long productId = 1L;
//        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());
//
//
//        assertThrows(EntityNotFoundException.class,
//                () -> stockService.getStockByProductId(productId));
//    }
//}