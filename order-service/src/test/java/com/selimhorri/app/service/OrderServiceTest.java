package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDto orderDto;
    private Cart cart;
    private CartDto cartDto;

    @BeforeEach
    void setUp() {
        LocalDateTime orderDate = LocalDateTime.now();

        this.cart = Cart.builder()
                .cartId(1)
                .userId(1)
                .build();

        this.cartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .build();

        this.order = Order.builder()
                .orderId(1)
                .orderDate(orderDate)
                .orderDesc("Test order description")
                .orderFee(99.99)
                .cart(this.cart)
                .build();

        this.orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(orderDate)
                .orderDesc("Test order description")
                .orderFee(99.99)
                .cartDto(this.cartDto)
                .build();
    }

    @Test
    void findAll_shouldReturnOrderList() {
        // given
        when(this.orderRepository.findAll()).thenReturn(Collections.singletonList(this.order));

        // when
        List<OrderDto> orders = this.orderService.findAll();

        // then
        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(1);
        assertThat(orders.get(0).getOrderDesc()).isEqualTo("Test order description");
    }

    @Test
    void findById_shouldReturnOrder() {
        // given
        when(this.orderRepository.findById(1)).thenReturn(Optional.of(this.order));

        // when
        OrderDto foundOrder = this.orderService.findById(1);

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderId()).isEqualTo(1);
        assertThat(foundOrder.getOrderFee()).isEqualTo(99.99);
    }

    @Test
    void save_shouldCreateOrder() {
        // given
        when(this.orderRepository.save(any(Order.class))).thenReturn(this.order);

        // when
        OrderDto savedOrder = this.orderService.save(this.orderDto);

        // then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderDesc()).isEqualTo("Test order description");
    }

    @Test
    void update_shouldUpdateOrder() {
        // given
        Order updatedOrder = Order.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Updated order description")
                .orderFee(149.99)
                .cart(this.cart)
                .build();

        when(this.orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        OrderDto updatedInfo = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Updated order description")
                .orderFee(149.99)
                .cartDto(this.cartDto)
                .build();

        // when
        OrderDto updatedOrderDto = this.orderService.update(updatedInfo);

        // then
        assertThat(updatedOrderDto).isNotNull();
        assertThat(updatedOrderDto.getOrderDesc()).isEqualTo("Updated order description");
        assertThat(updatedOrderDto.getOrderFee()).isEqualTo(149.99);
    }

    @Test
    void deleteById_shouldDeleteOrder() {
        // given
        when(this.orderRepository.findById(1)).thenReturn(Optional.of(this.order));

        // when
        this.orderService.deleteById(1);

        // then
        // No exception thrown means success
        // The test validates that the method executes without throwing an exception
    }
}