package com.zomato.plugin.service;

import com.zomato.plugin.entity.Order;
import com.zomato.plugin.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveOrderShouldSaveNewOrderWhenOrderIdDoesNotExist() {
        Order order = new Order();
        order.setOrderId("ZMT-001");
        order.setCustomerName("Alice");
        order.setTotalAmount(new BigDecimal("250.00"));

        when(repository.existsByOrderId("ZMT-001")).thenReturn(false);
        when(repository.save(order)).thenReturn(order);

        Order result = orderService.saveOrder(order);

        assertThat(result.getOrderId()).isEqualTo("ZMT-001");
        assertThat(result.getCustomerName()).isEqualTo("Alice");
        verify(repository).save(order);
    }

    @Test
    void saveOrderShouldReturnExistingOrderWhenOrderIdAlreadyExists() {
        Order newOrder = new Order();
        newOrder.setOrderId("ZMT-002");
        newOrder.setCustomerName("Bob");

        Order existingOrder = new Order();
        existingOrder.setId(5L);
        existingOrder.setOrderId("ZMT-002");
        existingOrder.setCustomerName("Bob");

        when(repository.existsByOrderId("ZMT-002")).thenReturn(true);
        when(repository.findByOrderId("ZMT-002")).thenReturn(Optional.of(existingOrder));

        Order result = orderService.saveOrder(newOrder);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getOrderId()).isEqualTo("ZMT-002");
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrdersShouldDelegateToRepository() {
        Order order1 = new Order();
        order1.setOrderId("ZMT-001");
        order1.setOrderTime(LocalDateTime.now());

        Order order2 = new Order();
        order2.setOrderId("ZMT-002");
        order2.setOrderTime(LocalDateTime.now().minusHours(1));

        when(repository.findAllByOrderByOrderTimeDesc()).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderId()).isEqualTo("ZMT-001");
        assertThat(result.get(1).getOrderId()).isEqualTo("ZMT-002");
        verify(repository).findAllByOrderByOrderTimeDesc();
    }

    @Test
    void getOrderByOrderIdShouldDelegateToRepository() {
        Order order = new Order();
        order.setOrderId("ZMT-003");
        when(repository.findByOrderId("ZMT-003")).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderByOrderId("ZMT-003");

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("ZMT-003");
        verify(repository).findByOrderId("ZMT-003");
    }

    @Test
    void getOrderCountShouldDelegateToRepository() {
        when(repository.count()).thenReturn(42L);

        long result = orderService.getOrderCount();

        assertThat(result).isEqualTo(42L);
        verify(repository).count();
    }
}
