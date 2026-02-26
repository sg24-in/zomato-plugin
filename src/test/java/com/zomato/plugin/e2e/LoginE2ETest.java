package com.zomato.plugin.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.zomato.plugin.repository.ConnectionConfigRepository;
import com.zomato.plugin.repository.OrderRepository;
import com.zomato.plugin.service.PlaywrightService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConnectionConfigRepository connectionConfigRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlaywrightService playwrightService;

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
        connectionConfigRepository.deleteAll();
        orderRepository.deleteAll();
        playwrightService.stopListening();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void shouldDisplayLoginPage() {
        page.navigate("http://localhost:" + port + "/login");

        assertThat(page).hasTitle("Zomato Partner Login");
        assertThat(page.locator("h1")).hasText("Zomato Partner Login");
        assertThat(page.locator(".status.disconnected")).isVisible();
        assertThat(page.locator("#login-form")).isVisible();
    }

    @Test
    void shouldShowEmailAndPasswordFields() {
        page.navigate("http://localhost:" + port + "/login");

        assertThat(page.locator("#username")).isVisible();
        assertThat(page.locator("#password")).isVisible();
        assertThat(page.locator("button[type='submit']")).hasText("Connect");
    }

    @Test
    void shouldShowNotConnectedStatus() {
        page.navigate("http://localhost:" + port + "/login");

        assertThat(page.locator(".status.disconnected")).containsText("Not Connected");
    }

    @Test
    void shouldSubmitLoginFormAndRedirectToOrders() {
        page.navigate("http://localhost:" + port + "/login");

        page.locator("#username").fill("test@example.com");
        page.locator("#password").fill("password123");
        page.locator("button[type='submit']").click();

        // Should redirect to /orders after successful connect (URL may contain jsessionid)
        assertThat(page).hasURL(Pattern.compile(".*/orders.*"));
    }

    @Test
    void shouldShowConnectedMessageAfterLogin() {
        page.navigate("http://localhost:" + port + "/login");

        page.locator("#username").fill("test@example.com");
        page.locator("#password").fill("password123");
        page.locator("button[type='submit']").click();

        // After redirect to /orders, should show connected status
        assertThat(page.locator(".status.connected")).isVisible();
    }
}
