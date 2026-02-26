package com.zomato.plugin.controller;

import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final ConnectionService connectionService;

    public OrderController(OrderService orderService, ConnectionService connectionService) {
        this.orderService = orderService;
        this.connectionService = connectionService;
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("orderCount", orderService.getOrderCount());
        model.addAttribute("connected", connectionService.isConnected());
        return "orders";
    }
}
