package com.zomato.plugin.repository;

import com.zomato.plugin.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
    List<Order> findAllByOrderByOrderTimeDesc();
}
