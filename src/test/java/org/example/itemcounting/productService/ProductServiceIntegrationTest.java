package org.example.itemcounting.productService;
import org.example.itemcounting.business.service.ProductService;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.exception.DuplicateSkuException;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.rest.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ProductService.class)
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProduct() {
        ProductDTO dto = ProductDTO.builder()
                .name("Мыло")
                .sku("SOAP-001")
                .unit("шт")
                .build();

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Мыло");
        assertThat(result.getSku()).isEqualTo("SOAP-001");
        assertThat(result.getUnit()).isEqualTo("шт");
    }

    @Test
    void createProduct_whenSkuExists() {
        Product existing = Product.builder()
                .name("Существующий")
                .sku("EXISTING")
                .unit("шт")
                .build();
        productRepository.save(existing);

        ProductDTO dto = ProductDTO.builder()
                .name("Новый")
                .sku("EXISTING")
                .unit("кг")
                .build();

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("продукт с SKU EXISTING уже существует");
    }

    @Test
    void getProductById_whenExists() {
        Product saved = productRepository.save(
                Product.builder().name("Тест").sku("TEST-1").unit("шт").build()
        );

        ProductDTO result = productService.getProductById(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Тест");
        assertThat(result.getSku()).isEqualTo("TEST-1");
        assertThat(result.getUnit()).isEqualTo("шт");
    }

    @Test
    void getProductById_whenNotFound() {
        Long fakeId = 999L;
        assertThatThrownBy(() -> productService.getProductById(fakeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + fakeId);
    }

    @Test
    void getAllProducts() {
        productRepository.save(Product.builder().name("Мыло").sku("SOAP-1").unit("шт").build());
        productRepository.save(Product.builder().name("Шампунь").sku("SHAM-1").unit("шт").build());

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductDTO::getSku).containsExactlyInAnyOrder("SOAP-1", "SHAM-1");
    }

    @Test
    void updateProduct_OnlyName() {
        Product saved = productRepository.save(
                Product.builder().name("Старое имя").sku("OLD-SKU").unit("литр").build()
        );

        ProductDTO updateDto = ProductDTO.builder()
                .name("Новое имя")
                .build();

        ProductDTO updated = productService.updateProduct(saved.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getSku()).isEqualTo("OLD-SKU");
        assertThat(updated.getUnit()).isEqualTo("литр");
    }

    @Test
    void updateProduct_AllFields() {
        Product saved = productRepository.save(
                Product.builder().name("Старое").sku("OLD").unit("шт").build()
        );

        ProductDTO updateDto = ProductDTO.builder()
                .name("Новое")
                .sku("NEW")
                .unit("кг")
                .build();

        ProductDTO updated = productService.updateProduct(saved.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Новое");
        assertThat(updated.getSku()).isEqualTo("NEW");
        assertThat(updated.getUnit()).isEqualTo("кг");
    }

    @Test
    void updateProduct_AllFieldsNull() {
        Product saved = productRepository.save(
                Product.builder().name("Имя").sku("SKU").unit("шт").build()
        );

        ProductDTO updateDto = ProductDTO.builder().build();

        ProductDTO updated = productService.updateProduct(saved.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Имя");
        assertThat(updated.getSku()).isEqualTo("SKU");
        assertThat(updated.getUnit()).isEqualTo("шт");
    }

    @Test
    void updateProduct_whenNotFound() {
        Long fakeId = 999L;
        ProductDTO updateDto = ProductDTO.builder().name("Любое").build();

        assertThatThrownBy(() -> productService.updateProduct(fakeId, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + fakeId);
    }

    @Test
    void deleteProduct_whenExists() {
        Product saved = productRepository.save(
                Product.builder().name("Удалить").sku("DELETE").unit("шт").build()
        );

        productService.deleteProduct(saved.getId());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteProduct_whenNotFound() {
        Long fakeId = 999L;

        assertThatThrownBy(() -> productService.deleteProduct(fakeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + fakeId);
    }

}
