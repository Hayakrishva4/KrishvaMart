# KrishvaMart
Multi-seller e-commerce marketplace web application built with Java Servlets, JDBC, and Apache Tomcat.
> Checkpoint window: Jul 27 - Oct 10, 2026 &middot; Builder: solo
## Problem statement
Sellers list products. Buyers browse, search, add to cart, and purchase. An admin manages users, orders, and listings. Checkout uses a mock payment confirmation step. An AI chatbot answers FAQ-style questions about products, orders, shipping, and returns.
## Feature status
| ID | Requirement | Status |
|----|-------------|--------|
| F1 | Register/login, BUYER/SELLER roles, seed ADMIN | Implemented |
| F2 | Seller create/edit/delete listings | Implemented |
| F3 | Buyer browse/search by category & keyword | Implemented |
| F4 | Cart: Add/update/remove, running total | Implemented |
| F5 | Checkout via mock payment confirmation | Implemented |
| F6 | Buyer order history / seller incoming orders | Implemented |
| F7 | Admin: View users/orders, moderate listings | Implemented |
| F8 | Product reviews & ratings on completed orders | Implemented |
| O1 | Wishlist / save-for-later | Not started |
| O2 | Orders Workflow (Pending->Confirmed->Shipped->Delivered) | Implemented |
| O3 | Seller sales dashboard (counts/revenue) | Not started |
| O4 | AI chatbot (Mandatory for Final Review) | Implemented (mock provider by default; swap in Gemini via config flag) |
See `CHANGELOG.md` for release history and the timeline in the original spec
## Architecture
Layered MVC over Servlets (Front Controller pattern):
```
Browser (JSP shell + vanilla JS/fetch)
  -> Filter layer: EncodingFilter, RequestIdFilter, AuthFilter
  -> Servlets (controller/*) - thin, no SQL, no business logic
  -> Service layer (service/*) - business rules, validation, no JDBC
  -> DAO layer (dao/impl/*) - all SQL, PreparedStatement only
  -> HikariCP connection pool (listener/AppContextListener)
  -> H2 Database (server mode)
```
See `docs/D1-er-diagram.puml`, `docs/D2-use-case-diagram.puml`, and
`docs/D3-sequence-diagram.puml` for the three required design diagrams
(render with the [PlantUML](https://plantuml.com/) VS Code extension, the online editor, or `plantuml docs/*.puml`).
### Package structure
```
com.krishva.krishvamart
|-- controller   Servlets - thin, no SQL, no business logic
|-- service      business rules, orchestration
|-- dao          interfaces + JDBC implementations
|-- model        POJOs / entities
|-- dto          request/response shapes for JSON endpoints
|-- filter       auth, request-id, encoding
|-- listener     DataSource init/teardown, DI wiring (ServiceRegistry)
|-- chat         AI chatbot: ChatProvider strategy, MockChatProvider, GeminiChatProvider
|-- util         PasswordUtil, ValidationUtil, JsonUtil, DbSeeder
`-- exception    checked exceptions mapped to HTTP status codes
```
## Tech stack
| Component | Choice |
|---|---|
| JDK | 17 (LTS) |
| Servlet container | Tomcat 9.0.x (`javax.servlet.*`) |
| Build tool | Maven |
| Database | H2 (server mode deployed, embedded for local dev/tests) |
| JDBC driver | `com.h2database:h2`, driver class `org.h2.Driver` |
| Connection pooling | HikariCP via `ServletContextListener` |
| View layer | JSP + JSTL (shells) + vanilla JS/`fetch()` (AJAX) |
| JSON | Gson |
| Password hashing | jBCrypt |
| Testing | JUnit 5 + Mockito |
| Logging | SLF4J + Logback |
| CI | GitHub Actions (`mvn -B test` on every push) |
## Setup instructions
See `CONTRIBUTING.md` for the full clone-to-running-instance walkthrough.
Quick version:
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
mvn compile
mvn exec:java -Dexec.mainClass="com.krishva.krishvamart.util.DbSeeder"
mvn clean package
cp target/krishvamart.war $CATALINA_HOME/webapps/
```
### Demo accounts (DbSeeder)
| Role | Email | Password |
|---|---|---|
| Admin | admin@krishvamart.com | Admin@12345 |
| Seller | priya.seller@krishvamart.com | Seller@123 |
| Seller | arjun.seller@krishvamart.com | Seller@123 |
| Buyer | divya.buyer@krishvamart.com | Buyer@1234 |
| Buyer | karthik.buyer@krishvamart.com | Buyer@1234 |
Change or remove these before any real/public deployment.
## Deployed link
_Not yet deployed - add the live Tomcat URL
 is complete (due with the Full Build + Deploy checkpoint, Sep 21)._
## AI chatbot configuration
`ai.chatbot.provider` in `config.properties` selects the implementation
(Strategy pattern, Section 12):
- `mock` (default) - canned FAQ answers, no network call, no API key needed.
- `gemini` - calls the Gemini API server-side using `ai.chatbot.apiKey`
 (never exposed to the browser). See `com.krishva.krishvamart.chat.GeminiChatProvider`.
Guardrails (Section 17): 10 messages/minute per session, 500-character input cap, 10s outbound timeout, fixed server-side prompt template restricting the bot to product/order/shipping/returns questions, and in-memory per-session caching of repeated questions.
## API contract
All JSON endpoints are versioned under `/api/v1/...` and return the fixed
envelope:
```json
{ "success": true, "data": { }, "error": null }
{ "success": false, "data": null, "error": { "code": "VALIDATION_ERROR", "message": "..." } }
```
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /api/v1/auth/register | Public | BUYER/SELLER only (admin is seed-only) |
| POST | /api/v1/auth/login | Public | regenerates session id |
| POST | /api/v1/auth/logout | Session | |
| GET  | /api/v1/auth/me | Session | |
| GET  | /api/v1/products | Public | `?q=&category=` |
| GET  | /api/v1/products/{id} | Public | |
| POST/PUT/DELETE | /api/v1/products(/{id}) | Seller | owner-only |
| GET/POST/PUT/DELETE | /api/v1/cart... | Buyer session | |
| POST | /api/v1/orders/checkout | Buyer | mock payment |
| GET  | /api/v1/orders(/{id}) | Session | scoped by role |
| PATCH| /api/v1/orders/{id}/status | Seller/Admin | O2 workflow |
| GET/POST | /api/v1/reviews... | Public GET / Buyer POST | F8 |
| GET  | /api/v1/admin/users, /api/v1/admin/orders | Admin | F7 |
| DELETE | /api/v1/admin/products/{id} | Admin | moderate/deactivate |
| POST | /api/v1/chat | Public | O4 |
| GET  | /api/v1/health | Public | `{status, db}` |
## Screenshots
_Add screenshots here once the app is running against the deployed URL
(Full Build + Deploy checkpoint requirement).
## Known limitations
- O1 (wishlist) and O3 (seller sales dashboard) are not yet implemented 
- `GeminiChatProvider` is wired but untested against a live API key in this
  environment; `mock` is the safe default until a key is configured.
- Diagrams are checked in as PlantUML source (`docs/*.puml`); render them to images before the design-diagram submission.