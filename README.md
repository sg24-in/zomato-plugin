# Zomato Live Order Sync – POC (Spring Boot + Playwright)

## Objective

Build a **simple proof-of-concept application** using:

* Java 17
* Spring Boot
* H2 in-memory database
* Playwright Java
* Thymeleaf (or simple HTML) UI

This POC must:

1. Allow entering Zomato Partner login credentials.
2. Attempt login via Playwright.
3. Save session (storage state or token) in H2.
4. Start listening for new orders.
5. Persist new orders into H2.
6. Display live incoming orders on UI without refresh (polling or SSE).

This is a POC only:

* No multi-tenant support.
* No encryption required.
* No production hardening.
* No Docker.
* No Azure-specific logic.
* Everything local for now.

---

# High-Level Architecture

```
Browser (User UI)
      ↓
Spring Boot App
      ↓
Playwright Automation
      ↓
Zomato Partner Portal
      ↓
Intercept Order API / WebSocket
      ↓
Persist Orders (H2)
      ↓
UI Shows Live Orders
```

---

# Functional Requirements

## 1️⃣ Login Page

Create a UI page:

`/login`

Fields:

* Username (email)
* Password

Buttons:

* Connect
* Disconnect

On Connect:

* Call backend API
* Launch Playwright
* Attempt login
* Detect OTP (if required)
* If login success:

  * Save storage state JSON
  * Save login status in DB
  * Start background listener
* Redirect to `/orders`

---

## 2️⃣ Session Persistence

Use Playwright:

* On successful login, save storageState to file:
  `zomato-session.json`

* Save in H2 table:

  * id
  * username
  * sessionPath
  * isConnected
  * connectedAt

If app restarts:

* If session file exists → reuse it

---

## 3️⃣ Order Listening Strategy

We must NOT scrape DOM.

Instead:

* Open Zomato orders page
* Intercept:

  * XHR responses
  * Or WebSocket frames
* Detect order API endpoint
* Extract JSON payload
* Map to Order entity

Order fields:

* orderId
* customerName
* totalAmount
* orderTime
* status
* rawJson (for debugging)

Persist to H2.

Prevent duplicate inserts (orderId unique).

---

## 4️⃣ Live Order Sync Mechanism

After login:

Start a background thread:

* Keep browser open
* Listen continuously
* When new order arrives → save to DB

Do NOT close browser after login.

This is a long-running automation session.

---

## 5️⃣ Orders UI Page

Endpoint:

`/orders`

Display:

* Connection status (Connected / Not Connected)
* Total orders count
* Table:

  * Order ID
  * Customer
  * Amount
  * Time
  * Status

Live update method:

Option A (Simpler POC):

* Frontend polls `/api/orders` every 3 seconds.

Option B (Better POC):

* Use Server-Sent Events (SSE).

Prefer SSE if comfortable.

---

# Technical Requirements

## Dependencies

Maven:

* spring-boot-starter-web
* spring-boot-starter-thymeleaf
* spring-boot-starter-data-jpa
* com.microsoft.playwright
* h2
* lombok

---

## Database

H2 in-memory:

`jdbc:h2:mem:testdb`

Console enabled.

Tables:

### connection_config

* id
* username
* sessionPath
* connected
* connectedAt

### orders

* id
* orderId (unique)
* customerName
* totalAmount
* orderTime
* status
* rawJson

---

# Playwright Requirements

* Run in headless mode false for debugging.
* Use Chromium.
* Intercept network using:

page.onResponse()

If WebSocket exists:

* Add page.onWebSocket()
* Parse incoming frames.

---

# Behavior Expectations

When running:

1. Start app.
2. Open `/login`.
3. Enter credentials.
4. If login success:

   * Redirect to `/orders`.
5. If a new order is received in Zomato:

   * It must appear in UI within few seconds.
   * It must be stored in DB.
6. App restart:

   * Should reuse session file if exists.

---

# Simplifications for POC

* Ignore encryption.
* Ignore multi-tenancy.
* Ignore scheduler.
* Ignore failure recovery.
* Single store only.
* No production logging.
* No scaling concerns.
* No advanced locking.

---

# Deliverables Required

Provide:

1. Complete project structure.
2. Entities.
3. Repositories.
4. Controller classes.
5. Playwright service class.
6. Background listener implementation.
7. Simple UI (Thymeleaf).
8. Application.yml configuration.
9. How to run instructions.
10. How to install Playwright browsers.

Do not provide pseudo-code.

Provide runnable minimal implementation.

---

# Additional Notes

* If OTP appears, return message: “OTP required – not supported in POC”.
* If login fails, return error to UI.
* Avoid crashing app if Playwright fails.
* Ensure browser properly closes on shutdown.

---

# End Goal

This POC must demonstrate:

* Live order capture
* No manual report download
* Feasibility of real-time integration
* Persistence of session
* Clean automation approach

---

If needed, assume Zomato partner portal URL:
https://www.zomato.com/partners
https://www.zomato.com/partners

[https://partner.zomato.com](ht
