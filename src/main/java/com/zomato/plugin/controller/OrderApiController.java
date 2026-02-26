package com.zomato.plugin.controller;

import com.zomato.plugin.entity.Order;
import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderApiController {

    private final OrderService orderService;
    private final ConnectionService connectionService;

    public OrderApiController(OrderService orderService, ConnectionService connectionService) {
        this.orderService = orderService;
        this.connectionService = connectionService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", connectionService.isConnected());
        status.put("orderCount", orderService.getOrderCount());
        return ResponseEntity.ok(status);
    }
}
