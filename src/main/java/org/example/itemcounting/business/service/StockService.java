package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.entity.Stock;
import org.example.itemcounting.entity.Warehouse;
import org.example.itemcounting.entity.WarehouseLocation;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.InsufficientStockException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.StockDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addStock(Long productId, Long warehouseId, Long locationId, BigDecimal quantity,
                         String referenceType, Long referenceId, Long userId, String comment) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Склад не найден"));

        WarehouseLocation location = locationId != null
                ? locationRepository.findById(locationId).orElse(null)
                : null;

        Stock stock = stockRepository.findByProductAndWarehouseAndLocation(product, warehouse, location)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setProduct(product);
                    newStock.setWarehouse(warehouse);
                    newStock.setLocation(location);
                    newStock.setQuantity(BigDecimal.ZERO);
                    return newStock;
                });

        BigDecimal oldQuantity = stock.getQuantity();
        stock.setQuantity(oldQuantity.add(quantity));

        if (stock.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Недостаточно товара на складе");
        }

        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        // Логируем движение
        //logMovement();
    }

    @Transactional
    public void removeStock(Long productId, Long warehouseId, BigDecimal quantity,
                            String referenceType, Long referenceId, Long userId, String comment) {
        addStock(productId, warehouseId, null, quantity.negate(), referenceType, referenceId, userId, comment);
    }

    // общий остаток товара на складе
    public BigDecimal getTotalStockInWarehouse(Long productId, Long warehouseId) {
        return stockRepository.getTotalQuantityByProductAndWarehouse(productId, warehouseId);
    }
}
