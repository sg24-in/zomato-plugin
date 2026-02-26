package com.zomato.plugin.controller;

import com.zomato.plugin.entity.Order;
import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderApiController.class)
class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ConnectionService connectionService;

    @Test
    void shouldReturnOrdersList() throws Exception {
        Order order1 = new Order();
        order1.setOrderId("ORD-001");
        order1.setCustomerName("Alice");
        order1.setTotalAmount(new BigDecimal("150.00"));
        order1.setOrderTime(LocalDateTime.of(2026, 1, 15, 10, 30));
        order1.setStatus("NEW");

        Order order2 = new Order();
        order2.setOrderId("ORD-002");
        order2.setCustomerName("Bob");
        order2.setTotalAmount(new BigDecimal("250.00"));
        order2.setOrderTime(LocalDateTime.of(2026, 1, 15, 11, 0));
        order2.setStatus("DELIVERED");

        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderId").value("ORD-001"))
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].orderId").value("ORD-002"))
                .andExpect(jsonPath("$[1].customerName").value("Bob"))
                .andExpect(jsonPath("$[1].status").value("DELIVERED"));
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnStatusWithConnectedAndOrderCount() throws Exception {
        when(connectionService.isConnected()).thenReturn(true);
        when(orderService.getOrderCount()).thenReturn(5L);

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.orderCount").value(5));
    }

    @Test
    void shouldReturnStatusWithConnectedFalseAndOrderCountZeroInitially() throws Exception {
        when(connectionService.isConnected()).thenReturn(false);
        when(orderService.getOrderCount()).thenReturn(0L);

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.orderCount").value(0));
    }
}
