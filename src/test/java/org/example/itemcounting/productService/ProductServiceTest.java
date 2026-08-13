package org.example.itemcounting.productService;

import org.example.itemcounting.business.service.ProductService;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.exception.DuplicateSkuException;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.rest.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct() {
        ProductDTO dto = ProductDTO.builder()
                .name("Мыло")
                .sku("SOAP-001")
                .unit("шт")
                .build();


        Product saved = createProductWithId(1L,"Мыло","SOAP-001","шт");

        when(productRepository.existsBySku(dto.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(saved);


        ProductDTO result = productService.createProduct(dto);

        ProductDTO expected = ProductDTO.builder()
                .id(1L)
                .name("Мыло")
                .sku("SOAP-001")
                .unit("шт")
                .build();

        assertEquals(result, expected);


    }

    @Test
    void createProduct_DuplicateSku() {
        ProductDTO dto = ProductDTO.builder()
                .sku("EXISTING-SKU")
                .build();

        when(productRepository.existsBySku(dto.getSku())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("продукт с SKU EXISTING-SKU уже существует");
    }

    @Test
    void getProductById() {

        Long id = 1L;
        Product found = createProductWithId(id,"Тест", "TEST-1","шт");


        when(productRepository.findById(id)).thenReturn(Optional.of(found));


        ProductDTO result = productService.getProductById(id);

        ProductDTO expected = ProductDTO.builder()
                .id(id)
                .name("Тест")
                .sku("TEST-1")
                .unit("шт")
                .build();

        assertEquals(result, expected);

    }

    @Test
    void getProductById_whenNotFound() {
        Long id = 999L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + id);
    }

    @Test
    void getAllProducts() {
        List<Product> products = List.of(
                createProductWithId(1L, "Мыло", "SOAP-001", "шт"),
                createProductWithId(2L, "Шампунь", "SHAM-002", "шт")
        );

        when(productRepository.findAll()).thenReturn(products);

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductDTO::getSku).containsExactly("SOAP-001", "SHAM-002");

    }

    @Test
    void updateProduct_OnlyName() {
        Long id = 1L;
        Product existing = createProductWithId(id, "Старое имя", "OLD-SKU", "литр");
        Product updatedEntity = createProductWithId(id, "Новое имя", "OLD-SKU", "литр");

        ProductDTO updateDto = ProductDTO.builder()
                .name("Новое имя")
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updatedEntity);

        ProductDTO result = productService.updateProduct(id, updateDto);

        assertThat(result.getName()).isEqualTo("Новое имя");
        assertThat(result.getSku()).isEqualTo("OLD-SKU");
        assertThat(result.getUnit()).isEqualTo("литр");

    }

    @Test
    void updateProduct_OnlySku() {
        Long id = 1L;
        Product existing = createProductWithId(id, "Товар", "OLD-SKU", "шт");
        Product updatedEntity = createProductWithId(id, "Товар", "NEW-SKU", "шт");

        ProductDTO updateDto = ProductDTO.builder()
                .sku("NEW-SKU")
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updatedEntity);

        ProductDTO result = productService.updateProduct(id, updateDto);

        assertThat(result.getName()).isEqualTo("Товар");
        assertThat(result.getSku()).isEqualTo("NEW-SKU");
        assertThat(result.getUnit()).isEqualTo("шт");
    }

    @Test
    void updateProduct_AllFields() {
        Long id = 1L;
        Product existing = createProductWithId(id, "Старое", "OLD", "шт");
        Product updatedEntity = createProductWithId(id, "Новое", "NEW", "кг");

        ProductDTO updateDto = ProductDTO.builder()
                .name("Новое")
                .sku("NEW")
                .unit("кг")
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updatedEntity);

        ProductDTO result = productService.updateProduct(id, updateDto);

        assertThat(result.getName()).isEqualTo("Новое");
        assertThat(result.getSku()).isEqualTo("NEW");
        assertThat(result.getUnit()).isEqualTo("кг");
    }

    @Test
    void updateProduct_AllFieldsNull() {
        Long id = 1L;
        Product existing = createProductWithId(id, "Имя", "SKU", "шт");

        ProductDTO updateDto = ProductDTO.builder().build();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        ProductDTO result = productService.updateProduct(id, updateDto);

        assertThat(result.getName()).isEqualTo("Имя");
        assertThat(result.getSku()).isEqualTo("SKU");
        assertThat(result.getUnit()).isEqualTo("шт");
    }

    @Test
    void updateProduct_whenNotFound() {
        Long id = 999L;
        ProductDTO updateDto = ProductDTO.builder().name("Любое").build();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + id);
    }

    @Test
    void deleteProduct_shouldDeleteSuccessfully_whenExists() {
        Long id = 1L;
        when(productRepository.existsById(id)).thenReturn(true);

        productService.deleteProduct(id);
    }

    @Test
    void deleteProduct_whenNotFound() {
        Long id = 999L;
        when(productRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("продукта с id не найдено: " + id);
    }

    private Product createProductWithId(Long id,String name, String sku, String unit){
        Product product;
        return product = Product.builder()
                .id(id)
                .name(name)
                .sku(sku)
                .unit(unit)
                .build();
    }
}
