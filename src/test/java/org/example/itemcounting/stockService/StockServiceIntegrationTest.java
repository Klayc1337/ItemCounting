package org.example.itemcounting.stockService;

import org.example.itemcounting.business.service.StockService;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.entity.Stock;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.InsufficientStockException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.repository.StockRepository;
import org.example.itemcounting.rest.dto.StockDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

/**
 * Интеграционные тесты для {@link StockService}.
 */
@DataJpaTest
@Import(StockService.class)
@ActiveProfiles("test")
public class StockServiceIntegrationTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("Test Product")
                .sku("TEST-001")
                .unit("шт")
                .build();
        product = productRepository.save(product);
    }

    /**
     * Проверяет, что метод {@link StockService#getAllStocks()} возвращает
     * список всех существующих остатков в виде {@link StockDTO}.
     * <p>
     * Условие: в БД сохранён один остаток для продукта.
     * Ожидаемый результат: список содержит один элемент с корректными полями.
     */
    @Test
    void getAllStocks() {
        Stock stock = createStock(BigDecimal.valueOf(15.5));

        List<StockDTO> result = stockService.getAllStocks();

        assertThat(result).hasSize(1);
        StockDTO dto = result.getFirst();
        assertThat(dto.getProductId()).isEqualTo(product.getId());
        assertThat(dto.getQuantity()).isEqualByComparingTo("15.5");
    }

    /**
     * Проверяет, что {@link StockService#increaseStock(Long, BigDecimal)}
     * увеличивает существующий остаток на указанное количество.
     * <p>
     * Условие: для продукта уже есть остаток с количеством 10.
     * Действие: увеличиваем на 5.5.
     * Ожидаемый результат: количество становится 15.5.
     */
    @Test
    void increaseStock_whenHaveProduct() {

        Stock stock = createStock(BigDecimal.valueOf(10));

        stockService.increaseStock(product.getId(), BigDecimal.valueOf(5.5));

        Stock updated = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualByComparingTo("15.5");
    }

    /**
     * Проверяет, что {@link StockService#increaseStock(Long, BigDecimal)}
     * создаёт новую запись остатка, если для продукта она ещё не существует,
     * и устанавливает переданное количество.
     * <p>
     * Условие: для продукта нет остатка.
     * Действие: увеличиваем на 7.0.
     * Ожидаемый результат: создаётся новый остаток с количеством 7.0.
     */
    @Test
    void increaseStock_whenStockNotHaveProduct() {

        stockService.increaseStock(product.getId(), BigDecimal.valueOf(7.0));

        Stock created = stockRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(created.getQuantity()).isEqualByComparingTo("7.0");
        assertThat(created.getProduct().getId()).isEqualTo(product.getId());
    }

    /**
     * Проверяет, что {@link StockService#increaseStock(Long, BigDecimal)}
     * выбрасывает {@link InvalidQuantityException} при передаче некорректного количества
     * и при этом запись остатка не создаётся.
     * <p>
     * Условие: для продукта нет остатка.
     * Ожидаемый результат: исключение с соответствующим сообщением, остаток в БД отсутствует.
     */
    @Test
    void increaseStock_withInvalidQuantity() {

        Long productId = product.getId();

        assertThatThrownBy(() -> stockService.increaseStock(productId, null))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");

        assertThatThrownBy(() -> stockService.increaseStock(productId, BigDecimal.ZERO))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");

        assertThatThrownBy(() -> stockService.increaseStock(productId, BigDecimal.valueOf(-1)))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");

        assertThat(stockRepository.findByProductId(productId)).isEmpty();
    }

    /**
     * Проверяет, что {@link StockService#decreaseStock(Long, BigDecimal)}
     * уменьшает существующий остаток на указанное количество.
     * <p>
     * Условие: для продукта есть остаток с количеством 20.
     * Действие: уменьшаем на 8.
     * Ожидаемый результат: количество становится 12.
     */
    @Test
    void decreaseStock() {

        Stock stock = createStock(BigDecimal.valueOf(20));


        stockService.decreaseStock(product.getId(), BigDecimal.valueOf(8));

        Stock updated = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualByComparingTo("12");
    }

    /**
     * Проверяет, что {@link StockService#decreaseStock(Long, BigDecimal)}
     * выбрасывает {@link EntityNotFoundException}, если для продукта нет записи остатка.
     * <p>
     * Условие: для продукта отсутствует остаток.
     * Ожидаемый результат: исключение с сообщением о том, что продукт не найден.
     */
    @Test
    void decreaseStock_whenStockNotFound() {

        assertThatThrownBy(() -> stockService.decreaseStock(product.getId(), BigDecimal.valueOf(5)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("нет продукта с id: " + product.getId());
    }

    /**
     * Проверяет, что {@link StockService#decreaseStock(Long, BigDecimal)}
     * выбрасывает {@link InsufficientStockException}, если текущего количества недостаточно.
     * <p>
     * Условие: для продукта есть остаток с количеством 5.
     * Действие: пытаемся уменьшить на 10.
     * Ожидаемый результат: исключение, количество остаётся прежним.
     */
    @Test
    void decreaseStock_whenQuantityLow() {

        Stock stock = createStock(BigDecimal.valueOf(5));

        assertThatThrownBy(() -> stockService.decreaseStock(product.getId(), BigDecimal.valueOf(10)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("недостаточно запасов");

        Stock unchanged = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualByComparingTo("5");
    }

    /**
     * Проверяет, что {@link StockService#decreaseStock(Long, BigDecimal)}
     * выбрасывает {@link InvalidQuantityException} при передаче некорректного количества
     * и при этом остаток не изменяется.
     * <p>
     * Условие: для продукта есть остаток с количеством 10.
     * Ожидаемый результат: исключение с соответствующим сообщением,
     * количество остаётся прежним.
     */
    @Test
    void decreaseStock_withInvalidQuantity() {

        Long productId = product.getId();

        assertThatThrownBy(() -> stockService.decreaseStock(productId, null))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");

        assertThatThrownBy(() -> stockService.decreaseStock(productId, BigDecimal.ZERO))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");

        assertThatThrownBy(() -> stockService.decreaseStock(productId, BigDecimal.valueOf(-1)))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("колличество должно быть больше 0");
    }

    private Stock createStock(BigDecimal quantity) {
        Stock stock = Stock.builder()
                .product(product)
                .quantity(quantity)
                .build();
        stockRepository.save(stock);
        return stock;
    }
}
