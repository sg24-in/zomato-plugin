package com.zomato.plugin.repository;

import com.zomato.plugin.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void shouldSaveAndFindById() {
        Order order = new Order();
        order.setOrderId("ORD-001");
        order.setCustomerName("John Doe");
        order.setTotalAmount(new BigDecimal("250.50"));
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("DELIVERED");
        order.setRawJson("{\"id\": \"ORD-001\"}");

        Order saved = repository.save(order);

        assertThat(saved.getId()).isNotNull();

        Optional<Order> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo("ORD-001");
        assertThat(found.get().getCustomerName()).isEqualTo("John Doe");
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(found.get().getStatus()).isEqualTo("DELIVERED");
        assertThat(found.get().getRawJson()).isEqualTo("{\"id\": \"ORD-001\"}");
    }

    @Test
    void shouldFindByOrderId() {
        Order order = new Order();
        order.setOrderId("ORD-100");
        order.setCustomerName("Jane Smith");
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PENDING");
        repository.save(order);

        Optional<Order> found = repository.findByOrderId("ORD-100");

        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo("ORD-100");
        assertThat(found.get().getCustomerName()).isEqualTo("Jane Smith");
    }

    @Test
    void shouldReturnTrueWhenOrderIdExists() {
        Order order = new Order();
        order.setOrderId("ORD-200");
        order.setCustomerName("Bob");
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("CONFIRMED");
        repository.save(order);

        assertThat(repository.existsByOrderId("ORD-200")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderIdDoesNotExist() {
        assertThat(repository.existsByOrderId("ORD-NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindAllOrderedByOrderTimeDesc() {
        LocalDateTime now = LocalDateTime.now();

        Order oldest = new Order();
        oldest.setOrderId("ORD-OLD");
        oldest.setCustomerName("Customer A");
        oldest.setTotalAmount(new BigDecimal("10.00"));
        oldest.setOrderTime(now.minusHours(2));
        oldest.setStatus("DELIVERED");
        repository.save(oldest);

        Order middle = new Order();
        middle.setOrderId("ORD-MID");
        middle.setCustomerName("Customer B");
        middle.setTotalAmount(new BigDecimal("20.00"));
        middle.setOrderTime(now.minusHours(1));
        middle.setStatus("DELIVERED");
        repository.save(middle);

        Order newest = new Order();
        newest.setOrderId("ORD-NEW");
        newest.setCustomerName("Customer C");
        newest.setTotalAmount(new BigDecimal("30.00"));
        newest.setOrderTime(now);
        newest.setStatus("PENDING");
        repository.save(newest);

        List<Order> orders = repository.findAllByOrderByOrderTimeDesc();

        assertThat(orders).hasSize(3);
        assertThat(orders.get(0).getOrderId()).isEqualTo("ORD-NEW");
        assertThat(orders.get(1).getOrderId()).isEqualTo("ORD-MID");
        assertThat(orders.get(2).getOrderId()).isEqualTo("ORD-OLD");
    }

    @Test
    void shouldThrowExceptionWhenDuplicateOrderId() {
        Order order1 = new Order();
        order1.setOrderId("ORD-DUP");
        order1.setCustomerName("Customer X");
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setOrderTime(LocalDateTime.now());
        order1.setStatus("PENDING");
        repository.saveAndFlush(order1);

        Order order2 = new Order();
        order2.setOrderId("ORD-DUP");
        order2.setCustomerName("Customer Y");
        order2.setTotalAmount(new BigDecimal("200.00"));
        order2.setOrderTime(LocalDateTime.now());
        order2.setStatus("CONFIRMED");

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(order2));
    }
}
