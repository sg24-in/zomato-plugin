package com.zomato.plugin.controller;

import com.zomato.plugin.entity.Order;
import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ConnectionService connectionService;

    @Test
    void shouldReturnOrdersView() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());
        when(orderService.getOrderCount()).thenReturn(0L);
        when(connectionService.isConnected()).thenReturn(false);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void shouldContainOrdersListOrderCountAndConnectedStatus() throws Exception {
        Order order1 = new Order();
        order1.setOrderId("ORD-001");
        order1.setCustomerName("Alice");
        order1.setTotalAmount(new BigDecimal("150.00"));
        order1.setOrderTime(LocalDateTime.now());
        order1.setStatus("NEW");

        Order order2 = new Order();
        order2.setOrderId("ORD-002");
        order2.setCustomerName("Bob");
        order2.setTotalAmount(new BigDecimal("250.00"));
        order2.setOrderTime(LocalDateTime.now());
        order2.setStatus("DELIVERED");

        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));
        when(orderService.getOrderCount()).thenReturn(2L);
        when(connectionService.isConnected()).thenReturn(true);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orderCount", 2L))
                .andExpect(model().attribute("connected", true));
    }

    @Test
    void shouldDisplayEmptyListWhenNoOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());
        when(orderService.getOrderCount()).thenReturn(0L);
        when(connectionService.isConnected()).thenReturn(false);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", hasSize(0)))
                .andExpect(model().attribute("orderCount", 0L))
                .andExpect(model().attribute("connected", false));
    }
}
