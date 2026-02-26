package com.zomato.plugin.repository;

import com.zomato.plugin.entity.ConnectionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionConfigRepository extends JpaRepository<ConnectionConfig, Long> {
    Optional<ConnectionConfig> findByUsername(String username);
    Optional<ConnectionConfig> findFirstByConnectedTrue();
}
