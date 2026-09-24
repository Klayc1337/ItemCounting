package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.exception.DuplicateSkuException;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.rest.dto.ProductCreateRequestDTO;
import org.example.itemcounting.rest.dto.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // создание товара
    @Transactional
    public Product createProduct(ProductCreateRequestDTO request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(" Товар с артикулом " + request.getSku() + " уже существует");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setDepth(request.getDepth());
        product.setWeight(request.getWeight());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        //Product saved = productRepository.save(product);
        return productRepository.save(product);
    }






    // получение по id
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("продукта с id не найдено: " + id));
        return ProductDTO.fromEntity(product);
    }

    // получение всех товаров
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    //обновление(частичное)
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("продукта с id не найдено: " + id));

        if(productDTO.getName() != null){
            product.setName(productDTO.getName());
        }
        if(productDTO.getSku() != null){
            product.setSku(productDTO.getSku());
        }
        if(productDTO.getUnit() != null){
            product.setUnit(productDTO.getUnit());
        }
        Product updated = productRepository.save(product);
        return ProductDTO.fromEntity(updated);
    }

    // удвление
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("продукта с id не найдено: " + id);
        }
        productRepository.deleteById(id);
    }
}
