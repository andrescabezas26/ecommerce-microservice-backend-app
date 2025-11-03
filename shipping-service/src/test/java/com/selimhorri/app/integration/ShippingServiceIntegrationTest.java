package com.selimhorri.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.NestedServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ShippingServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void createOrderItem_shouldIntegrateWithExternalServices() throws Exception {
        // given
        ProductDto mockProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .priceUnit(25.99)
                .quantity(100)
                .build();

        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Test order for shipping")
                .orderFee(150.0)
                .build();

        // Mock external service calls
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(mockProductDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(3)
                .build();

        String orderItemJson = objectMapper.writeValueAsString(orderItemDto);

        // when & then
        mockMvc.perform(post("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedQuantity").value(3))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.orderId").value(101));
    }

    @Test
    void getAllOrderItems_shouldReturnOrderItemsList() throws Exception {
        // given
        ProductDto mockProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .build();

        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Test order")
                .build();

        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(mockProductDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        // when & then
        mockMvc.perform(get("/api/shippings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray());
    }

    @Test
    void getOrderItemById_shouldReturnNotFoundForNonExistentItem() throws Exception {
        // given
        OrderItemId orderItemId = OrderItemId.builder()
                .productId(999)
                .orderId(999)
                .build();

        String orderItemIdJson = objectMapper.writeValueAsString(orderItemId);

        // when & then - should throw OrderItemNotFoundException for non-existent order
        // item
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(get("/api/shippings/find")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderItemIdJson));
        } catch (Exception e) {
            exceptionThrown = true;
            assertThat(e).isInstanceOf(NestedServletException.class);
            assertThat(e.getCause())
                    .hasMessageContaining("OrderItem with id: OrderItemId(productId=999, orderId=999) not found");
        }
        assertThat(exceptionThrown).isTrue();
    }

    @Test
    void microserviceCommunication_shouldValidateRestTemplateIntegration() throws Exception {
        // given
        ProductDto mockProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Communication Test Product")
                .priceUnit(99.99)
                .build();

        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Communication test order")
                .orderFee(299.97)
                .build();

        // Mock the RestTemplate calls that simulate communication with product-service
        // and order-service
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(mockProductDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        // when & then - This validates that the shipping service can handle
        // the integration layer correctly even when external services are mocked
        mockMvc.perform(get("/api/shippings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").exists());
    }

    @Test
    void updateOrderItem_shouldHandleServiceIntegration() throws Exception {
        // given
        ProductDto mockProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Updated Test Product")
                .priceUnit(35.99)
                .build();

        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Updated test order")
                .orderFee(107.97)
                .build();

        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(mockProductDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        OrderItemDto updatedOrderItem = OrderItemDto.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(5)
                .build();

        String orderItemJson = objectMapper.writeValueAsString(updatedOrderItem);

        // when & then
        mockMvc.perform(put("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedQuantity").value(5));
    }
}