package com.zomato.plugin.service;

import com.zomato.plugin.entity.ConnectionConfig;
import com.zomato.plugin.repository.ConnectionConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionConfigRepository repository;

    public ConnectionService(ConnectionConfigRepository repository) {
        this.repository = repository;
    }

    public ConnectionConfig connect(String username, String sessionPath) {
        ConnectionConfig config = repository.findByUsername(username)
                .orElse(new ConnectionConfig());
        config.setUsername(username);
        config.setSessionPath(sessionPath);
        config.setConnected(true);
        config.setConnectedAt(LocalDateTime.now());
        return repository.save(config);
    }

    public ConnectionConfig disconnect(String username) {
        ConnectionConfig config = repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No connection found for: " + username));
        config.setConnected(false);
        return repository.save(config);
    }

    public Optional<ConnectionConfig> getActiveConnection() {
        return repository.findFirstByConnectedTrue();
    }

    public Optional<ConnectionConfig> getConnectionByUsername(String username) {
        return repository.findByUsername(username);
    }

    public boolean isConnected() {
        return repository.findFirstByConnectedTrue().isPresent();
    }
}
