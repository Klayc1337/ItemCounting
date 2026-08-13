package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.exception.DuplicateSkuException;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.rest.dto.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // создание товара
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateSkuException(" продукт с SKU " + productDTO.getSku() + " уже существует");
        }

        Product product = productDTO.toEntity();
        Product saved = productRepository.save(product);
        return ProductDTO.fromEntity(saved);
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
