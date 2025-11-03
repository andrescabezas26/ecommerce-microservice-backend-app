package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        this.category = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/category.jpg")
                .build();

        this.categoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/category.jpg")
                .build();

        this.product = Product.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("http://example.com/image.jpg")
                .sku("TST-001")
                .priceUnit(29.99)
                .quantity(100)
                .category(this.category)
                .build();

        this.productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("http://example.com/image.jpg")
                .sku("TST-001")
                .priceUnit(29.99)
                .quantity(100)
                .categoryDto(this.categoryDto)
                .build();
    }

    @Test
    void findAll_shouldReturnProductList() {
        // given
        when(this.productRepository.findAll()).thenReturn(Collections.singletonList(this.product));

        // when
        List<ProductDto> products = this.productService.findAll();

        // then
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.get(0).getProductTitle()).isEqualTo("Test Product");
    }

    @Test
    void findById_shouldReturnProduct() {
        // given
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        // when
        ProductDto foundProduct = this.productService.findById(1);

        // then
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getProductId()).isEqualTo(1);
        assertThat(foundProduct.getSku()).isEqualTo("TST-001");
    }

    @Test
    void save_shouldCreateProduct() {
        // given
        when(this.productRepository.save(any(Product.class))).thenReturn(this.product);

        // when
        ProductDto savedProduct = this.productService.save(this.productDto);

        // then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getProductTitle()).isEqualTo("Test Product");
    }

    @Test
    void update_shouldUpdateProduct() {
        // given
        Product updatedProduct = Product.builder()
                .productId(1)
                .productTitle("Updated Product")
                .imageUrl("http://example.com/updated-image.jpg")
                .sku("TST-001")
                .priceUnit(39.99)
                .quantity(80)
                .category(this.category)
                .build();

        when(this.productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDto updatedInfo = ProductDto.builder()
                .productId(1)
                .productTitle("Updated Product")
                .imageUrl("http://example.com/updated-image.jpg")
                .sku("TST-001")
                .priceUnit(39.99)
                .quantity(80)
                .categoryDto(this.categoryDto)
                .build();

        // when
        ProductDto updatedProductDto = this.productService.update(updatedInfo);

        // then
        assertThat(updatedProductDto).isNotNull();
        assertThat(updatedProductDto.getProductTitle()).isEqualTo("Updated Product");
        assertThat(updatedProductDto.getPriceUnit()).isEqualTo(39.99);
    }

    @Test
    void deleteById_shouldDeleteProduct() {
        // given
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        // when
        this.productService.deleteById(1);

        // then
        // No exception thrown means success
        // The test validates that the method executes without throwing an exception
    }
}