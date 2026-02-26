package com.zomato.plugin.repository;

import com.zomato.plugin.entity.ConnectionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConnectionConfigRepositoryTest {

    @Autowired
    private ConnectionConfigRepository repository;

    @Test
    void shouldSaveAndFindById() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUsername("user@example.com");
        config.setSessionPath("/path/to/session.json");
        config.setConnected(true);
        config.setConnectedAt(LocalDateTime.now());

        ConnectionConfig saved = repository.save(config);

        assertThat(saved.getId()).isNotNull();

        Optional<ConnectionConfig> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user@example.com");
        assertThat(found.get().getSessionPath()).isEqualTo("/path/to/session.json");
        assertThat(found.get().isConnected()).isTrue();
        assertThat(found.get().getConnectedAt()).isNotNull();
    }

    @Test
    void shouldFindByUsername() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUsername("alice@example.com");
        config.setSessionPath("/path/alice.json");
        config.setConnected(false);
        repository.save(config);

        Optional<ConnectionConfig> found = repository.findByUsername("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("alice@example.com");
        assertThat(found.get().getSessionPath()).isEqualTo("/path/alice.json");
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<ConnectionConfig> found = repository.findByUsername("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindFirstByConnectedTrue() {
        ConnectionConfig disconnected = new ConnectionConfig();
        disconnected.setUsername("disconnected@example.com");
        disconnected.setConnected(false);
        repository.save(disconnected);

        ConnectionConfig connected = new ConnectionConfig();
        connected.setUsername("connected@example.com");
        connected.setConnected(true);
        connected.setConnectedAt(LocalDateTime.now());
        repository.save(connected);

        Optional<ConnectionConfig> found = repository.findFirstByConnectedTrue();

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("connected@example.com");
        assertThat(found.get().isConnected()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoneConnected() {
        ConnectionConfig config1 = new ConnectionConfig();
        config1.setUsername("user1@example.com");
        config1.setConnected(false);
        repository.save(config1);

        ConnectionConfig config2 = new ConnectionConfig();
        config2.setUsername("user2@example.com");
        config2.setConnected(false);
        repository.save(config2);

        Optional<ConnectionConfig> found = repository.findFirstByConnectedTrue();

        assertThat(found).isEmpty();
    }
}
