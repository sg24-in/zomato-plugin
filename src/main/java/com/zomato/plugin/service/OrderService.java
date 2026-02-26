package com.zomato.plugin.service;

import com.zomato.plugin.entity.Order;
import com.zomato.plugin.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order saveOrder(Order order) {
        if (repository.existsByOrderId(order.getOrderId())) {
            return repository.findByOrderId(order.getOrderId()).get();
        }
        return repository.save(order);
    }

    public List<Order> getAllOrders() {
        return repository.findAllByOrderByOrderTimeDesc();
    }

    public Optional<Order> getOrderByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    public long getOrderCount() {
        return repository.count();
    }
}
