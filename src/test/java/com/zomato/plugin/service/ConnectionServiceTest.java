package com.zomato.plugin.service;

import com.zomato.plugin.entity.ConnectionConfig;
import com.zomato.plugin.repository.ConnectionConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private ConnectionConfigRepository repository;

    @InjectMocks
    private ConnectionService connectionService;

    @Test
    void connectShouldCreateNewConnectionWhenUsernameNotFound() {
        when(repository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(repository.save(any(ConnectionConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionConfig result = connectionService.connect("newuser", "/path/to/session.json");

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getSessionPath()).isEqualTo("/path/to/session.json");
        assertThat(result.isConnected()).isTrue();
        assertThat(result.getConnectedAt()).isNotNull();
        verify(repository).save(any(ConnectionConfig.class));
    }

    @Test
    void connectShouldUpdateExistingConnection() {
        ConnectionConfig existing = new ConnectionConfig();
        existing.setId(1L);
        existing.setUsername("existinguser");
        existing.setConnected(false);

        when(repository.findByUsername("existinguser")).thenReturn(Optional.of(existing));
        when(repository.save(any(ConnectionConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionConfig result = connectionService.connect("existinguser", "/new/path/session.json");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("existinguser");
        assertThat(result.getSessionPath()).isEqualTo("/new/path/session.json");
        assertThat(result.isConnected()).isTrue();
        assertThat(result.getConnectedAt()).isNotNull();
        verify(repository).save(existing);
    }

    @Test
    void disconnectShouldThrowWhenUsernameNotFound() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectionService.disconnect("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No connection found for: unknown");
    }

    @Test
    void disconnectShouldSetConnectedToFalse() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUsername("testuser");
        config.setConnected(true);

        when(repository.findByUsername("testuser")).thenReturn(Optional.of(config));
        when(repository.save(any(ConnectionConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionConfig result = connectionService.disconnect("testuser");

        assertThat(result.isConnected()).isFalse();
        verify(repository).save(config);
    }

    @Test
    void getActiveConnectionShouldDelegateToRepository() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnected(true);
        when(repository.findFirstByConnectedTrue()).thenReturn(Optional.of(config));

        Optional<ConnectionConfig> result = connectionService.getActiveConnection();

        assertThat(result).isPresent();
        assertThat(result.get().isConnected()).isTrue();
        verify(repository).findFirstByConnectedTrue();
    }

    @Test
    void getConnectionByUsernameShouldDelegateToRepository() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUsername("testuser");
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(config));

        Optional<ConnectionConfig> result = connectionService.getConnectionByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(repository).findByUsername("testuser");
    }

    @Test
    void isConnectedShouldReturnTrueWhenActiveConnectionExists() {
        when(repository.findFirstByConnectedTrue()).thenReturn(Optional.of(new ConnectionConfig()));

        assertThat(connectionService.isConnected()).isTrue();
    }

    @Test
    void isConnectedShouldReturnFalseWhenNoActiveConnection() {
        when(repository.findFirstByConnectedTrue()).thenReturn(Optional.empty());

        assertThat(connectionService.isConnected()).isFalse();
    }
}
