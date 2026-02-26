package com.zomato.plugin.service;

import com.zomato.plugin.entity.Order;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
public class PlaywrightService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightService.class);

    @Value("${app.playwright.headless:true}")
    private boolean headless;

    @Value("${app.playwright.session-file:zomato-session.json}")
    private String sessionFile;

    @Value("${app.zomato.partner-url:https://www.zomato.com/partners}")
    private String partnerUrl;

    private final AtomicBoolean listening = new AtomicBoolean(false);

    private Consumer<Order> orderCallback;

    public boolean hasExistingSession() {
        return Files.exists(Paths.get(sessionFile));
    }

    public String getSessionFilePath() {
        return sessionFile;
    }

    public boolean isListening() {
        return listening.get();
    }

    public void setOrderCallback(Consumer<Order> callback) {
        this.orderCallback = callback;
    }

    public String login(String username, String password) {
        log.info("Attempting login for user: {}", username);
        // In a real implementation, this would use Playwright to automate the login
        // For POC, this is a placeholder that would be replaced with actual Playwright code
        return sessionFile;
    }

    public void startListening() {
        if (listening.compareAndSet(false, true)) {
            log.info("Started order listening");
        }
    }

    public void stopListening() {
        if (listening.compareAndSet(true, false)) {
            log.info("Stopped order listening");
        }
    }

    public void processOrderJson(String json) {
        if (orderCallback != null) {
            Order order = parseOrderFromJson(json);
            if (order != null) {
                orderCallback.accept(order);
            }
        }
    }

    Order parseOrderFromJson(String json) {
        try {
            // Simple JSON parsing for POC - in production would use Jackson
            Order order = new Order();
            order.setOrderId(extractJsonField(json, "orderId"));
            order.setCustomerName(extractJsonField(json, "customerName"));
            String amount = extractJsonField(json, "totalAmount");
            if (amount != null) {
                order.setTotalAmount(new BigDecimal(amount));
            }
            order.setStatus(extractJsonField(json, "status"));
            order.setOrderTime(LocalDateTime.now());
            order.setRawJson(json);
            return order;
        } catch (Exception e) {
            log.error("Failed to parse order JSON: {}", json, e);
            return null;
        }
    }

    String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return null;
        // Skip whitespace after colon
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;
        if (valueStart >= json.length()) return null;
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // String value
            int end = json.indexOf('"', valueStart + 1);
            if (end < 0) return null;
            return json.substring(valueStart + 1, end);
        } else if (Character.isDigit(firstChar) || firstChar == '-') {
            // Numeric value
            int numEnd = valueStart;
            while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) || json.charAt(numEnd) == '.' || json.charAt(numEnd) == '-')) numEnd++;
            return json.substring(valueStart, numEnd);
        }
        return null;
    }

    @PreDestroy
    public void cleanup() {
        stopListening();
        log.info("PlaywrightService cleaned up");
    }
}
