package com.zomato.plugin.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.zomato.plugin.entity.Order;
import com.zomato.plugin.repository.OrderRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdersE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private OrderRepository orderRepository;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void tearDownBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
        orderRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void shouldDisplayOrdersPage() {
        page.navigate("http://localhost:" + port + "/orders");

        assertThat(page).hasTitle("Live Orders");
        assertThat(page.locator("h1")).hasText("Live Orders");
    }

    @Test
    void shouldShowEmptyStateWhenNoOrders() {
        page.navigate("http://localhost:" + port + "/orders");

        assertThat(page.locator(".empty-message")).hasText("No orders yet");
        assertThat(page.locator("#order-count")).hasText("0");
    }

    @Test
    void shouldDisplayOrdersInTable() {
        Order order = new Order();
        order.setOrderId("ZMT-001");
        order.setCustomerName("Alice");
        order.setTotalAmount(new BigDecimal("250.00"));
        order.setOrderTime(LocalDateTime.of(2026, 1, 15, 10, 30));
        order.setStatus("NEW");
        order.setRawJson("{\"orderId\":\"ZMT-001\"}");
        orderRepository.save(order);

        page.navigate("http://localhost:" + port + "/orders");

        assertThat(page.locator("#order-count")).hasText("1");
        assertThat(page.locator("#orders-table tbody tr").first().locator("td").first()).hasText("ZMT-001");
    }

    @Test
    void shouldShowMultipleOrders() {
        Order order1 = new Order();
        order1.setOrderId("ZMT-001");
        order1.setCustomerName("Alice");
        order1.setTotalAmount(new BigDecimal("250.00"));
        order1.setOrderTime(LocalDateTime.of(2026, 1, 15, 10, 30));
        order1.setStatus("NEW");
        order1.setRawJson("{}");
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setOrderId("ZMT-002");
        order2.setCustomerName("Bob");
        order2.setTotalAmount(new BigDecimal("150.00"));
        order2.setOrderTime(LocalDateTime.of(2026, 1, 15, 11, 0));
        order2.setStatus("DELIVERED");
        order2.setRawJson("{}");
        orderRepository.save(order2);

        page.navigate("http://localhost:" + port + "/orders");

        assertThat(page.locator("#order-count")).hasText("2");
        assertThat(page.locator("#orders-table tbody tr")).hasCount(2);
    }

    @Test
    void shouldHaveBackToLoginLink() {
        page.navigate("http://localhost:" + port + "/orders");

        assertThat(page.locator("a.btn-link")).hasText("Back to Login");
        page.locator("a.btn-link").click();
        assertThat(page).hasURL("http://localhost:" + port + "/login");
    }

    @Test
    void apiOrdersShouldReturnJson() {
        Order order = new Order();
        order.setOrderId("ZMT-API-001");
        order.setCustomerName("Charlie");
        order.setTotalAmount(new BigDecimal("300.00"));
        order.setOrderTime(LocalDateTime.of(2026, 1, 15, 12, 0));
        order.setStatus("PREPARING");
        order.setRawJson("{}");
        orderRepository.save(order);

        var response = page.request().get("http://localhost:" + port + "/api/orders");
        assertThat(response).isOK();
        String body = response.text();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("ZMT-API-001"));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("Charlie"));
    }

    @Test
    void apiStatusShouldReturnConnectionInfo() {
        var response = page.request().get("http://localhost:" + port + "/api/status");
        assertThat(response).isOK();
        String body = response.text();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"connected\""));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"orderCount\""));
    }
}
