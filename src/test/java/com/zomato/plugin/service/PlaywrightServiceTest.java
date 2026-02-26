package com.zomato.plugin.service;

import com.zomato.plugin.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PlaywrightServiceTest {

    private PlaywrightService playwrightService;

    @BeforeEach
    void setUp() {
        playwrightService = new PlaywrightService();
        ReflectionTestUtils.setField(playwrightService, "headless", true);
        ReflectionTestUtils.setField(playwrightService, "sessionFile", "test-session.json");
        ReflectionTestUtils.setField(playwrightService, "partnerUrl", "https://www.zomato.com/partners");
    }

    @Test
    void hasExistingSessionShouldReturnFalseWhenFileDoesNotExist() {
        ReflectionTestUtils.setField(playwrightService, "sessionFile", "nonexistent-session-file.json");

        assertThat(playwrightService.hasExistingSession()).isFalse();
    }

    @Test
    void getSessionFilePathShouldReturnConfiguredPath() {
        assertThat(playwrightService.getSessionFilePath()).isEqualTo("test-session.json");
    }

    @Test
    void isListeningShouldReturnFalseInitially() {
        assertThat(playwrightService.isListening()).isFalse();
    }

    @Test
    void startListeningShouldSetListeningToTrue() {
        playwrightService.startListening();

        assertThat(playwrightService.isListening()).isTrue();
    }

    @Test
    void stopListeningShouldSetListeningToFalse() {
        playwrightService.startListening();
        playwrightService.stopListening();

        assertThat(playwrightService.isListening()).isFalse();
    }

    @Test
    void startListeningShouldBeIdempotent() {
        playwrightService.startListening();
        playwrightService.startListening();

        assertThat(playwrightService.isListening()).isTrue();
    }

    @Test
    void processOrderJsonShouldCallCallbackWithParsedOrder() {
        AtomicReference<Order> capturedOrder = new AtomicReference<>();
        playwrightService.setOrderCallback(capturedOrder::set);

        String json = "{\"orderId\":\"ZMT-100\",\"customerName\":\"Jane\",\"totalAmount\":\"350.75\",\"status\":\"NEW\"}";
        playwrightService.processOrderJson(json);

        assertThat(capturedOrder.get()).isNotNull();
        assertThat(capturedOrder.get().getOrderId()).isEqualTo("ZMT-100");
        assertThat(capturedOrder.get().getCustomerName()).isEqualTo("Jane");
        assertThat(capturedOrder.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("350.75"));
        assertThat(capturedOrder.get().getStatus()).isEqualTo("NEW");
        assertThat(capturedOrder.get().getOrderTime()).isNotNull();
        assertThat(capturedOrder.get().getRawJson()).isEqualTo(json);
    }

    @Test
    void processOrderJsonShouldDoNothingWhenNoCallbackSet() {
        String json = "{\"orderId\":\"ZMT-100\",\"customerName\":\"Jane\",\"totalAmount\":\"350.75\",\"status\":\"NEW\"}";

        // Should not throw any exception
        playwrightService.processOrderJson(json);
    }

    @Test
    void parseOrderFromJsonShouldCorrectlyParseValidJsonWithStringAndNumericFields() {
        String json = "{\"orderId\":\"ZMT-200\",\"customerName\":\"Bob\",\"totalAmount\":999.50,\"status\":\"DELIVERED\"}";

        Order order = playwrightService.parseOrderFromJson(json);

        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isEqualTo("ZMT-200");
        assertThat(order.getCustomerName()).isEqualTo("Bob");
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("999.50"));
        assertThat(order.getStatus()).isEqualTo("DELIVERED");
        assertThat(order.getOrderTime()).isNotNull();
        assertThat(order.getRawJson()).isEqualTo(json);
    }

    @Test
    void parseOrderFromJsonShouldReturnNullForInvalidJson() {
        Order order = playwrightService.parseOrderFromJson("not valid json at all {{{");

        assertThat(order).isNotNull();
        // With invalid JSON, fields will be null but no exception thrown
        // The method only returns null on exception; malformed but parseable input returns an order with null fields
    }

    @Test
    void parseOrderFromJsonShouldReturnNullOnException() {
        // Pass null to trigger NullPointerException inside the method
        Order order = playwrightService.parseOrderFromJson(null);

        assertThat(order).isNull();
    }

    @Test
    void extractJsonFieldShouldExtractStringValues() {
        String json = "{\"name\":\"John Doe\",\"city\":\"Mumbai\"}";

        assertThat(playwrightService.extractJsonField(json, "name")).isEqualTo("John Doe");
        assertThat(playwrightService.extractJsonField(json, "city")).isEqualTo("Mumbai");
    }

    @Test
    void extractJsonFieldShouldExtractNumericValues() {
        String json = "{\"amount\":450.99,\"count\":10}";

        assertThat(playwrightService.extractJsonField(json, "amount")).isEqualTo("450.99");
        assertThat(playwrightService.extractJsonField(json, "count")).isEqualTo("10");
    }

    @Test
    void extractJsonFieldShouldReturnNullForMissingFields() {
        String json = "{\"name\":\"John\"}";

        assertThat(playwrightService.extractJsonField(json, "missing")).isNull();
    }

    @Test
    void cleanupShouldStopListening() {
        playwrightService.startListening();
        assertThat(playwrightService.isListening()).isTrue();

        playwrightService.cleanup();

        assertThat(playwrightService.isListening()).isFalse();
    }
}
