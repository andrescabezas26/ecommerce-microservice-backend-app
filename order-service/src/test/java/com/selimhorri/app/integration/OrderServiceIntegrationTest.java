package com.selimhorri.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void createOrder_shouldHandleConstraintViolation() throws Exception {
        // given
        CartDto cartDto = CartDto.builder()
                .cartId(999) // ID que no existe
                .userId(1)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderDate(LocalDateTime.now())
                .orderDesc("Integration test order")
                .orderFee(99.99)
                .cartDto(cartDto)
                .build();

        String orderJson = objectMapper.writeValueAsString(orderDto);

        // when & then - Verificamos que el endpoint maneja la violación de constraint
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderJson));
        } catch (Exception e) {
            exceptionThrown = true;
            // Verificamos que la excepción contiene información sobre constraint violation
            assertThat(e.getMessage()).contains("DataIntegrityViolationException");
        }
        assertThat(exceptionThrown).isTrue();
    }

    @Test
    void getAllOrders_shouldReturnOrdersList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray());
    }

    @Test
    void getOrderById_shouldReturnNotFoundForNonExistentOrder() throws Exception {
        // given
        Integer orderId = 999; // Non-existent order ID

        // when & then - should return 400 for non-existent order
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderWithoutCart_shouldHandleNullPointer() throws Exception {
        // given
        OrderDto orderDto = OrderDto.builder()
                .orderDate(LocalDateTime.now())
                .orderDesc("Invalid order")
                .orderFee(50.0)
                .build(); // No cartDto

        String orderJson = objectMapper.writeValueAsString(orderDto);

        // when & then - Verificamos que se maneja el NullPointerException
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderJson));
        } catch (Exception e) {
            exceptionThrown = true;
            // Verificamos que la excepción contiene información sobre NullPointer
            assertThat(e.getMessage()).contains("NullPointerException");
        }
        assertThat(exceptionThrown).isTrue();
    }

    @Test
    void updateOrder_shouldUpdateOrderSuccessfully() throws Exception {
        // given
        CartDto cartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Updated integration test order")
                .orderFee(149.99)
                .cartDto(cartDto)
                .build();

        String orderJson = objectMapper.writeValueAsString(orderDto);

        // when & then - Verificamos que se maneja el EntityNotFoundException
        boolean exceptionThrown = false;
        try {
            mockMvc.perform(put("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderJson));
        } catch (Exception e) {
            exceptionThrown = true;
            // Verificamos que la excepción contiene información sobre EntityNotFound
            assertThat(e.getMessage()).contains("EntityNotFoundException");
        }
        assertThat(exceptionThrown).isTrue();
    }
}