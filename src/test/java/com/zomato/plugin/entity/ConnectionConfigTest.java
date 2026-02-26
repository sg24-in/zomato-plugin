package com.zomato.plugin.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionConfigTest {

    @Test
    void shouldCreateConnectionConfigWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        ConnectionConfig config = new ConnectionConfig();
        config.setId(1L);
        config.setUsername("test@example.com");
        config.setSessionPath("/path/to/session.json");
        config.setConnected(true);
        config.setConnectedAt(now);

        assertThat(config.getId()).isEqualTo(1L);
        assertThat(config.getUsername()).isEqualTo("test@example.com");
        assertThat(config.getSessionPath()).isEqualTo("/path/to/session.json");
        assertThat(config.isConnected()).isTrue();
        assertThat(config.getConnectedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateConnectionConfigWithDefaults() {
        ConnectionConfig config = new ConnectionConfig();

        assertThat(config.getId()).isNull();
        assertThat(config.getUsername()).isNull();
        assertThat(config.getSessionPath()).isNull();
        assertThat(config.isConnected()).isFalse();
        assertThat(config.getConnectedAt()).isNull();
    }

    @Test
    void shouldUpdateConnectionStatus() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnected(false);
        assertThat(config.isConnected()).isFalse();

        config.setConnected(true);
        config.setConnectedAt(LocalDateTime.now());
        assertThat(config.isConnected()).isTrue();
        assertThat(config.getConnectedAt()).isNotNull();
    }
}
