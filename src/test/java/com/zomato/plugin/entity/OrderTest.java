package com.zomato.plugin.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void shouldCreateOrderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setId(1L);
        order.setOrderId("ZMT-12345");
        order.setCustomerName("John Doe");
        order.setTotalAmount(new BigDecimal("450.50"));
        order.setOrderTime(now);
        order.setStatus("NEW");
        order.setRawJson("{\"orderId\":\"ZMT-12345\"}");

        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getOrderId()).isEqualTo("ZMT-12345");
        assertThat(order.getCustomerName()).isEqualTo("John Doe");
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(order.getOrderTime()).isEqualTo(now);
        assertThat(order.getStatus()).isEqualTo("NEW");
        assertThat(order.getRawJson()).isEqualTo("{\"orderId\":\"ZMT-12345\"}");
    }

    @Test
    void shouldCreateOrderWithDefaults() {
        Order order = new Order();

        assertThat(order.getId()).isNull();
        assertThat(order.getOrderId()).isNull();
        assertThat(order.getCustomerName()).isNull();
        assertThat(order.getTotalAmount()).isNull();
        assertThat(order.getOrderTime()).isNull();
        assertThat(order.getStatus()).isNull();
        assertThat(order.getRawJson()).isNull();
    }

    @Test
    void shouldUpdateOrderStatus() {
        Order order = new Order();
        order.setStatus("NEW");
        assertThat(order.getStatus()).isEqualTo("NEW");

        order.setStatus("DELIVERED");
        assertThat(order.getStatus()).isEqualTo("DELIVERED");
    }
}
