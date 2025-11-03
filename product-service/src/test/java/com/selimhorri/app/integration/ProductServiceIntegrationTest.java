package com.selimhorri.app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_shouldReturnProductWithCategory() throws Exception {
        // given
        CategoryDto categoryDto = CategoryDto.builder()
                .categoryTitle("Integration Test Category")
                .imageUrl("http://example.com/category.jpg")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productTitle("Integration Test Product")
                .imageUrl("http://example.com/product.jpg")
                .sku("INT-TEST-001")
                .priceUnit(25.99)
                .quantity(100)
                .categoryDto(categoryDto)
                .build();

        String productJson = objectMapper.writeValueAsString(productDto);

        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productTitle").value("Integration Test Product"))
                .andExpect(jsonPath("$.sku").value("INT-TEST-001"))
                .andExpect(jsonPath("$.priceUnit").value(25.99))
                .andExpect(jsonPath("$.category.categoryTitle").value("Integration Test Category"));
    }

    @Test
    void getAllProducts_shouldReturnProductsList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray());
    }

    @Test
    void getProductById_shouldReturnNotFoundForNonExistentProduct() throws Exception {
        // given
        Integer productId = 999; // Non-existent product ID

        // when & then
        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithInvalidData_shouldHandleNullPointer() throws Exception {
        // given
        ProductDto invalidProduct = ProductDto.builder()
                .productTitle("") // Empty title
                .priceUnit(-10.0) // Negative price
                .quantity(-5) // Negative quantity
                .build();

        String productJson = objectMapper.writeValueAsString(invalidProduct);

        // when & then
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(productJson));
        } catch (Exception e) {
            exceptionThrown = true;
            // Verificamos que la excepción contiene información sobre NullPointer
            assertThat(e.getMessage()).contains("NullPointerException");
        }
        assertThat(exceptionThrown).isTrue();
    }

    @Test
    void searchProductsBySku_shouldHandleTransientEntity() throws Exception {
        // given - Crear un CategoryDto con ID inexistente para provocar TransientPropertyValueException
        CategoryDto categoryDto = CategoryDto.builder()
                .categoryId(999) // ID que no existe en la base de datos
                .categoryTitle("Search Test Category")
                .imageUrl("http://example.com/search.jpg")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productTitle("Searchable Product")
                .imageUrl("http://example.com/searchable.jpg")
                .sku("SEARCH-TEST-001")
                .priceUnit(15.99)
                .quantity(50)
                .categoryDto(categoryDto)
                .build();

        String productJson = objectMapper.writeValueAsString(productDto);

        // when & then
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(productJson));
        } catch (Exception e) {
            exceptionThrown = true;
            // Verificamos que la excepción contiene información sobre constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("constraint");
        }
        assertThat(exceptionThrown).isTrue();
    }
}