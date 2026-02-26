package com.zomato.plugin.controller;

import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import com.zomato.plugin.service.PlaywrightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.zomato.plugin.entity.Order;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConnectionService connectionService;

    @MockBean
    private PlaywrightService playwrightService;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldReturnLoginViewWithConnectedFalse() throws Exception {
        when(connectionService.isConnected()).thenReturn(false);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("connected", false));
    }

    @Test
    void shouldReturnLoginViewWithConnectedTrueWhenConnected() throws Exception {
        when(connectionService.isConnected()).thenReturn(true);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("connected", true));
    }

    @Test
    void shouldRedirectToOrdersOnSuccessfulConnect() throws Exception {
        when(playwrightService.login("testuser", "testpass")).thenReturn("/tmp/session.json");

        mockMvc.perform(post("/connect")
                        .param("username", "testuser")
                        .param("password", "testpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attribute("message", "Connected successfully"));
    }

    @Test
    void shouldCallServicesInOrderOnConnect() throws Exception {
        when(playwrightService.login("testuser", "testpass")).thenReturn("/tmp/session.json");

        mockMvc.perform(post("/connect")
                .param("username", "testuser")
                .param("password", "testpass"));

        verify(playwrightService).login("testuser", "testpass");
        verify(connectionService).connect("testuser", "/tmp/session.json");
        verify(playwrightService).setOrderCallback(any());
        verify(playwrightService).startListening();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSetOrderCallbackThatSavesOrders() throws Exception {
        when(playwrightService.login("testuser", "testpass")).thenReturn("/tmp/session.json");

        mockMvc.perform(post("/connect")
                .param("username", "testuser")
                .param("password", "testpass"));

        ArgumentCaptor<Consumer<Order>> callbackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(playwrightService).setOrderCallback(callbackCaptor.capture());

        // Exercise the captured lambda
        Order order = new Order();
        order.setOrderId("TEST-001");
        callbackCaptor.getValue().accept(order);

        verify(orderService).saveOrder(order);
    }

    @Test
    void shouldRedirectToLoginWithErrorOnConnectFailure() throws Exception {
        when(playwrightService.login("testuser", "badpass"))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/connect")
                        .param("username", "testuser")
                        .param("password", "badpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "Login failed: Invalid credentials"));
    }

    @Test
    void shouldRedirectToLoginWithSuccessMessageOnDisconnect() throws Exception {
        mockMvc.perform(post("/disconnect")
                        .param("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", "Disconnected successfully"));

        verify(playwrightService).stopListening();
        verify(connectionService).disconnect("testuser");
    }

    @Test
    void shouldRedirectToLoginWithErrorWhenDisconnectFails() throws Exception {
        doThrow(new RuntimeException("Connection not found"))
                .when(connectionService).disconnect("unknownuser");

        mockMvc.perform(post("/disconnect")
                        .param("username", "unknownuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "Disconnect failed: Connection not found"));
    }
}
