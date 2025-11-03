package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        this.orderDto = OrderDto.builder()
                .orderId(101)
                .build();

        this.payment = Payment.builder()
                .paymentId(1)
                .orderId(101)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        this.paymentDto = PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderDto(this.orderDto)
                .build();
    }

    @Test
    void findAll_shouldReturnPaymentList() {
        // given
        when(this.paymentRepository.findAll()).thenReturn(Collections.singletonList(this.payment));
        when(this.restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(this.orderDto);

        // when
        List<PaymentDto> payments = this.paymentService.findAll();

        // then
        assertThat(payments).isNotNull();
        assertThat(payments.size()).isEqualTo(1);
        assertThat(payments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void findById_shouldReturnPayment() {
        // given
        when(this.paymentRepository.findById(1)).thenReturn(Optional.of(this.payment));
        when(this.restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(this.orderDto);

        // when
        PaymentDto foundPayment = this.paymentService.findById(1);

        // then
        assertThat(foundPayment).isNotNull();
        assertThat(foundPayment.getPaymentId()).isEqualTo(1);
        assertThat(foundPayment.getIsPayed()).isTrue();
    }

    @Test
    void save_shouldCreatePayment() {
        // given
        when(this.paymentRepository.save(any(Payment.class))).thenReturn(this.payment);

        // when
        PaymentDto savedPayment = this.paymentService.save(this.paymentDto);

        // then
        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void update_shouldUpdatePayment() {
        // given
        Payment updatedPayment = Payment.builder()
                .paymentId(1)
                .orderId(101)
                .isPayed(false)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .build();

        when(this.paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        PaymentDto updatedInfo = PaymentDto.builder()
                .paymentId(1)
                .isPayed(false)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .orderDto(this.orderDto)
                .build();

        // when
        PaymentDto updatedPaymentDto = this.paymentService.update(updatedInfo);

        // then
        assertThat(updatedPaymentDto).isNotNull();
        assertThat(updatedPaymentDto.getIsPayed()).isFalse();
        assertThat(updatedPaymentDto.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
    }

    @Test
    void deleteById_shouldDeletePayment() {
        // when
        this.paymentService.deleteById(1);

        // then
        // No exception thrown means success
        // The test validates that the method executes without throwing an exception
    }
}