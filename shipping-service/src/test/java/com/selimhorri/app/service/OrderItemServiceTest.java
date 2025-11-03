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

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.impl.OrderItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem orderItem;
    private OrderItemDto orderItemDto;
    private OrderItemId orderItemId;
    private ProductDto productDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        this.orderItemId = new OrderItemId(1, 101); // productId=1, orderId=101

        this.productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .priceUnit(29.99)
                .build();

        this.orderDto = OrderDto.builder()
                .orderId(101)
                .orderDesc("Test Order")
                .orderFee(99.99)
                .build();

        this.orderItem = OrderItem.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(5)
                .build();

        this.orderItemDto = OrderItemDto.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(5)
                .productDto(this.productDto)
                .orderDto(this.orderDto)
                .build();
    }

    @Test
    void findAll_shouldReturnOrderItemList() {
        // given
        when(this.orderItemRepository.findAll()).thenReturn(Collections.singletonList(this.orderItem));
        when(this.restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(this.productDto);
        when(this.restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(this.orderDto);

        // when
        List<OrderItemDto> orderItems = this.orderItemService.findAll();

        // then
        assertThat(orderItems).isNotNull();
        assertThat(orderItems.size()).isEqualTo(1);
        assertThat(orderItems.get(0).getOrderedQuantity()).isEqualTo(5);
    }

    @Test
    void findById_shouldReturnOrderItem() {
        // given
        when(this.orderItemRepository.findById(this.orderItemId)).thenReturn(Optional.of(this.orderItem));
        when(this.restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(this.productDto);
        when(this.restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(this.orderDto);

        // when
        OrderItemDto foundOrderItem = this.orderItemService.findById(this.orderItemId);

        // then
        assertThat(foundOrderItem).isNotNull();
        assertThat(foundOrderItem.getProductId()).isEqualTo(1);
        assertThat(foundOrderItem.getOrderId()).isEqualTo(101);
    }

    @Test
    void save_shouldCreateOrderItem() {
        // given
        when(this.orderItemRepository.save(any(OrderItem.class))).thenReturn(this.orderItem);

        // when
        OrderItemDto savedOrderItem = this.orderItemService.save(this.orderItemDto);

        // then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getOrderedQuantity()).isEqualTo(5);
    }

    @Test
    void update_shouldUpdateOrderItem() {
        // given
        OrderItem updatedOrderItem = OrderItem.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(10)
                .build();

        when(this.orderItemRepository.save(any(OrderItem.class))).thenReturn(updatedOrderItem);

        OrderItemDto updatedInfo = OrderItemDto.builder()
                .productId(1)
                .orderId(101)
                .orderedQuantity(10)
                .build();

        // when
        OrderItemDto updatedOrderItemDto = this.orderItemService.update(updatedInfo);

        // then
        assertThat(updatedOrderItemDto).isNotNull();
        assertThat(updatedOrderItemDto.getOrderedQuantity()).isEqualTo(10);
    }

    @Test
    void deleteById_shouldDeleteOrderItem() {
        // when
        this.orderItemService.deleteById(this.orderItemId);

        // then
        // No exception thrown means success
        // The test validates that the method executes without throwing an exception
    }
}