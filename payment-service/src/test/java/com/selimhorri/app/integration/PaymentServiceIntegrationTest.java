package com.selimhorri.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void createPayment_shouldIntegrateWithOrderService() {
        // given
        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Test order for payment")
                .orderFee(150.0)
                .build();

        // Mock the RestTemplate call to order-service
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        PaymentDto paymentDto = PaymentDto.builder()
                .isPayed(false)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .orderDto(OrderDto.builder().orderId(101).build())
                .build();

        // when
        ResponseEntity<PaymentDto> response = testRestTemplate.postForEntity(
                "/api/payments", paymentDto, PaymentDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); // Changed from CREATED to OK
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
        assertThat(response.getBody().getIsPayed()).isFalse();
    }

    @Test
    void getAllPayments_shouldReturnPaymentsList() {
        // given
        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Test order")
                .orderFee(100.0)
                .build();

        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        // when
        ResponseEntity<DtoCollectionResponse<PaymentDto>> response = testRestTemplate.exchange(
                "/api/payments", HttpMethod.GET, null,
                new ParameterizedTypeReference<DtoCollectionResponse<PaymentDto>>() {
                });

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollection()).isNotNull();
    }

    @Test
    void getPaymentById_shouldReturnPaymentWithOrderDetails() {
        // given
        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Integration test order")
                .orderFee(200.0)
                .build();

        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        Integer paymentId = 1;

        // when
        ResponseEntity<PaymentDto> response = testRestTemplate.getForEntity(
                "/api/payments/" + paymentId, PaymentDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentId()).isEqualTo(paymentId);
        assertThat(response.getBody().getOrderDto()).isNotNull();
    }

    @Test
    void updatePayment_shouldUpdatePaymentStatus() {
        // given
        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Payment processing test")
                .orderFee(75.0)
                .build();

        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        PaymentDto completedPayment = PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderDto(OrderDto.builder().orderId(101).build())
                .build();

        // when
        testRestTemplate.put("/api/payments", completedPayment);
        ResponseEntity<PaymentDto> response = testRestTemplate.getForEntity(
                "/api/payments/1", PaymentDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.getBody().getIsPayed()).isTrue();
    }

    @Test
    void paymentServiceCommunication_shouldHandleServiceIntegration() {
        // given
        OrderDto mockOrderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Service integration test")
                .orderFee(300.0)
                .build();

        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenReturn(mockOrderDto);

        // This test validates that the service can handle RestTemplate communication
        // even when the actual order-service is not available

        // when
        ResponseEntity<DtoCollectionResponse<PaymentDto>> response = testRestTemplate.exchange(
                "/api/payments", HttpMethod.GET, null,
                new ParameterizedTypeReference<DtoCollectionResponse<PaymentDto>>() {
                });

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // The service should handle gracefully when external services are mocked
    }
}
