# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Zomato Live Order Sync – a proof-of-concept Spring Boot application that uses Playwright (Java) to automate login to the Zomato Partner Portal, intercept order data via network requests (XHR/WebSocket), persist orders to an H2 database, and display them in a live-updating UI.

## Technology Stack

- Java 17, Spring Boot
- Maven build system
- Playwright Java (Chromium, non-headless for debugging)
- H2 in-memory database (`jdbc:h2:mem:testdb`)
- Thymeleaf templates for UI
- Lombok for boilerplate reduction

## Build & Run Commands

```bash
# Install Playwright browsers (required once)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Build
mvn clean package

# Run
mvn spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

## Architecture

```
Browser (User UI) → Spring Boot App → Playwright Automation → Zomato Partner Portal
                                                                     ↓
                                              Intercept Order API / WebSocket
                                                                     ↓
                                                        Persist Orders (H2)
                                                                     ↓
                                                       UI Shows Live Orders (SSE or polling)
```

**Key design decisions:**
- Orders are captured by intercepting network responses (`page.onResponse()` / `page.onWebSocket()`), NOT by scraping the DOM
- The Playwright browser session stays open as a long-running background thread after login
- Session state is saved to `zomato-session.json` and reused across app restarts
- `orderId` is unique — duplicates are rejected on insert

## Database Tables

- **connection_config**: Stores login session info (username, sessionPath, connected status, connectedAt)
- **orders**: Stores captured orders (orderId unique, customerName, totalAmount, orderTime, status, rawJson)

## Key Endpoints

- `/login` — UI page for entering Zomato Partner credentials and connecting
- `/orders` — Live orders dashboard with connection status and order table
- `/api/orders` — REST endpoint for order data (used by frontend polling or SSE)

## Maven Dependencies

`spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-data-jpa`, `com.microsoft.playwright`, `h2`, `lombok`

## POC Constraints

- Single store only, no multi-tenancy
- No encryption, no Docker, no cloud-specific logic
- OTP during login is not supported — return message to UI
- Browser must close cleanly on app shutdown
