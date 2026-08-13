package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.entity.Stock;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.InsufficientStockException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.repository.StockRepository;
import org.example.itemcounting.rest.dto.StockDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    
    // получить все остатки
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

//      увеличить остаток (приход)
//      если записи нет то создать новую
//      используется ри создании приходной накладной
    @Transactional
    public void increaseStock(Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("колличество должно быть больше 0");
        }

        Stock stock = stockRepository.findByProductId(productId).orElseGet(() -> createNewStock(productId));

        stock.addQuantity(quantity);
    }

    // уменьшить остаток (расход)
    @Transactional
    public void decreaseStock(Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("колличество должно быть больше 0");
        }

        Stock stock = stockRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new EntityNotFoundException("нет продукта с id: " + productId));

        if (stock.getQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException("недостаточно запасов");
        }

        stock.addQuantity(quantity.negate());
        stockRepository.save(stock);
    }

    // возвращает сущность Stock для указанного товара
    @Transactional(readOnly = true)
    public Stock getStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("нет продукта с id: " + productId));
    }
    
    // создаёт запись остатка для нового товара
    private Stock createNewStock(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("нет продукта с id: " + productId));

        Stock newStock = new Stock();
        newStock.setProduct(product);
        newStock.setQuantity(BigDecimal.ZERO);
        return stockRepository.save(newStock);
    }
}
