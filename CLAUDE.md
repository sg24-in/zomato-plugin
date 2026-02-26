# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Zomato Live Order Sync – a proof-of-concept Spring Boot application that uses Playwright (Java) to automate login to the Zomato Partner Portal, intercept order data via network requests (XHR/WebSocket), persist orders to an H2 database, and display them in a live-updating UI.

## Technology Stack

- Java 17+, Spring Boot 3.2.5
- Gradle build system
- Playwright Java (Chromium, headless in tests)
- H2 in-memory database (`jdbc:h2:mem:testdb`)
- Thymeleaf templates for UI
- Lombok for boilerplate reduction
- JaCoCo for code coverage (99% line coverage enforced)

## Build & Run Commands

```bash
# Install Playwright browsers (required once)
npx playwright install chromium

# Build
gradle build

# Run the application
gradle bootRun

# Run unit + integration tests (excludes E2E)
gradle test

# Run E2E tests with Playwright
gradle e2eTest

# Run a single test class
gradle test --tests "com.zomato.plugin.service.OrderServiceTest"

# Run a single test method
gradle test --tests "com.zomato.plugin.service.OrderServiceTest.shouldSaveNewOrder"

# Check code coverage (fails if < 99% line coverage)
gradle jacocoTestCoverageVerification

# Generate coverage report (build/reports/jacoco/test/html/index.html)
gradle jacocoTestReport

# Full check (tests + coverage verification)
gradle check
```

## Architecture

```
Browser (User UI) -> Spring Boot App -> Playwright Automation -> Zomato Partner Portal
                                                                      |
                                              Intercept Order API / WebSocket
                                                                      |
                                                        Persist Orders (H2)
                                                                      |
                                                       UI Shows Live Orders (3s polling)
```

**Key design decisions:**
- Orders are captured by intercepting network responses (`page.onResponse()` / `page.onWebSocket()`), NOT by scraping the DOM
- The Playwright browser session stays open as a long-running background thread after login
- Session state is saved to `zomato-session.json` and reused across app restarts
- `orderId` is unique — duplicates are rejected on insert (OrderService returns existing order)
- `PlaywrightService.extractJsonField()` is a simple POC JSON parser — uses Jackson in production

## Test Strategy

Tests are organized in three tiers:
- **Unit tests** (`src/test/java/.../entity/`, `.../service/`): Plain JUnit 5 + Mockito, no Spring context
- **Integration tests** (`src/test/java/.../repository/`): `@DataJpaTest` with H2, test JPA queries
- **Controller tests** (`src/test/java/.../controller/`): `@WebMvcTest` + `@MockBean`, test HTTP layer with MockMvc
- **E2E tests** (`src/test/java/.../e2e/`): `@SpringBootTest(RANDOM_PORT)` + Playwright Chromium, test full browser flows

E2E tests run separately via `gradle e2eTest` (Failsafe-style separation). Unit/integration/controller tests run via `gradle test`.

JaCoCo enforces 99% line coverage on `gradle check`. The `ZomatoPluginApplication` bootstrap class is excluded from coverage.

## Database Tables

- **connection_config**: Stores login session info (username, sessionPath, connected status, connectedAt)
- **orders**: Stores captured orders (orderId unique, customerName, totalAmount, orderTime, status, rawJson)

## Key Endpoints

- `/login` — UI page for entering Zomato Partner credentials and connecting
- `/orders` — Live orders dashboard with connection status and order table
- `/api/orders` — REST endpoint returning orders as JSON (used by frontend polling)
- `/api/status` — REST endpoint returning connection status and order count

## POC Constraints

- Single store only, no multi-tenancy
- No encryption, no Docker, no cloud-specific logic
- OTP during login is not supported — return message to UI
- Browser must close cleanly on app shutdown (`@PreDestroy` in PlaywrightService)
