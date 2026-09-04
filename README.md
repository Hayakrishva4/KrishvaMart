# KrishvaMart
Multi-seller e-commerce marketplace web application built with Java Servlets, JDBC, and Apache Tomcat.
> Duration: Jul 27 - Oct 10, 2026 ; Builder: solo
## Problem statement
> Sellers list products. Buyers browse, search, add to cart, and purchase. An admin manages users, orders, and listings. Checkout uses a payment confirmation step. An AI chatbot answers FAQ-style questions about products, orders, shipping, and returns.
## Feature status
| ID | Requirement | Status |
|----|-------------|--------|
| F1 | Login, BUYER/SELLER roles, seed ADMIN | Implemented |
| F2 | Seller create/edit/delete listings | Implemented |
| F3 | Buyer browse/search by category & keyword | Implemented |
| F4 | Cart: add/update/remove, running total | Implemented |
| F5 | Checkout via mock payment confirmation | Implemented |
| F6 | Buyer order history / seller incoming orders | Implemented |
| F7 | Admin: view users/orders, moderate listings | Implemented |
| F8 | Product reviews & ratings on completed orders | Implemented |
| O1 | Wishlist / save-for-later | Implemented |
| O2 | Order status workflow (Pending->Confirmed->Shipped->Delivered) | Implemented |
| O3 | Seller sales dashboard (counts/revenue) | Implemented |
| O4 | AI chatbot | Implemented (mock provider by default; swap in Gemini via config flag) |
Also implemented beyond the base spec :
Shipping address capture at checkout, price-range/sort/pagination on browse.
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
See `docs/D1-er-diagram.svg`, `docs/D2-use-case-diagram.svg`, and
`docs/D3-sequence-diagram.svg` for the three required design diagrams
(rendered images, ready to drop into the final report). PlantUML source for each is also checked in at `docs/*.puml` if you want to regenerate/edit them.
`docs/design-patterns.md` documents where each of the six required design patterns is used.
### Package structure
```
com.krishva.krishvamart
|-- controller   Servlets - thin, no SQL, no business logic
|-- service      business rules, orchestration
|-- dao          interfaces + JDBC implementation
|-- model        POJOs / entities
|-- dto          request/response shapes for JSON endpoints
|-- filter       auth, request-id, encoding
|-- listener     DataSource init/teardown, DI wiring (ServiceRegistry), SchemaInitializer
|-- chat         AI chatbot: ChatProvider strategy, MockChatProvider, GeminiChatProvider, CatalogAwareChatProvider decorator
|-- util         PasswordUtil, ValidationUtil, JsonUtil, ConfigResolver, DbSeeder
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
### Fastest path: Docker (self-initializing, no manual DB steps)
```bash
docker compose up -d --build
```
That's it - `SchemaInitializer` applies the schema and seeds demo accounts
+ sample products automatically on first boot. Visit
`http://localhost:8080/`. See `docs/cloud-deployment.md` for pushing this
same image to a live cloud URL (Render, Railway, a VM - several options).
### Manual (Maven + local Tomcat)
See `CONTRIBUTING.md` for the full walkthrough. Quick version:
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
mvn clean package
cp target/krishvamart.war $CATALINA_HOME/webapps/
```
`SchemaInitializer` runs automatically when Tomcat starts the app, the same way it does in the Docker image - no separate seed step needed here either.
(`DbSeeder` still exists for manually re-seeding without restarting Tomcat:
`mvn exec:java -Dexec.mainClass="com.krishva.krishvamart.util.DbSeeder"`.)
### Demo accounts (seeded automatically on first boot)
| Role | Email | Password |
|---|---|---|
| Admin | admin@krishvamart.com | Admin@12345 |
| Seller | priya.seller@krishvamart.com | Seller@123 |
| Seller | arjun.seller@krishvamart.com | Seller@123 |
| Buyer | divya.buyer@krishvamart.com | Buyer@1234 |
| Buyer | karthik.buyer@krishvamart.com | Buyer@1234 |
Change or remove these before any real/public deployment.
## Running live on the cloud
The app is packaged as a Docker image (`Dockerfile`) with environment-
variable-driven config (`ConfigResolver`: env var > `config.properties` >  
default) and a self-initializing database (`SchemaInitializer`), so it
deploys to any container-hosting platform without code changes. Full
step-by-step instructions for Render, Railway, a plain VM, and Docker
Compose: **`docs/cloud-deployment.md`**.
## Deployed link
> https://krishvamart.onrender.com
`docs/cloud-deployment.md`
## AI chatbot configuration
`ai.chatbot.provider` in `config.properties` selects the implementation
- `mock` (default) - canned FAQ answers, no network call, no API key needed.
- `gemini` - calls the Gemini API server-side using `ai.chatbot.apiKey`
  (never exposed to the browser). See `com.krishva.krishvamart.chat.GeminiChatProvider`.
Guardrails : 10 messages/minute per session, 500-character input
cap, 10s outbound timeout, fixed server-side prompt template restricting the bot to product/order/shipping/returns questions, and in-memory per-session
caching of repeated questions.
## API contract
All JSON endpoints are versioned under `/api/v1/...` and return the fixed envelope:
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
| GET  | /api/v1/products | Public | `?q=&category=&minPrice=&maxPrice=&sort=&page=&pageSize=` - returns a paged result (`items`, `page`, `pageSize`, `totalItems`, `totalPages`) |
| GET  | /api/v1/products/{id} | Public | |
| POST/PUT/DELETE | /api/v1/products(/{id}) | Seller | owner-only |
| GET/POST/PUT/DELETE | /api/v1/cart... | Buyer session | |
| GET/POST/DELETE | /api/v1/wishlist(/{productId}) | Buyer session | O1 |
| POST | /api/v1/orders/checkout | Buyer | mock payment; body `{ "shippingAddress": "..." }` |
| GET  | /api/v1/orders(/{id}) | Session | scoped by role |
| PATCH| /api/v1/orders/{id}/status | Seller/Admin | O2 workflow |
| GET/POST | /api/v1/reviews... | Public GET / Buyer POST | F8 |
| GET  | /api/v1/admin/users, /api/v1/admin/orders | Admin | F7 |
| DELETE | /api/v1/admin/products/{id} | Admin | moderate/deactivate |
| GET  | /api/v1/seller/analytics | Seller session | O3 sales dashboard |
| POST | /api/v1/chat | Public | O4 |
| GET  | /api/v1/health | Public | `{status, db}` |
## Docs & deployment reference
| File | What it's for |
|---|---|
| `docs/D1-er-diagram.svg`, `docs/D2-use-case-diagram.svg`, `docs/D3-sequence-diagram.svg` | Rendered diagrams |
| `docs/design-patterns.md` | Where each required pattern is used |
| `docs/security-checklist.md` | Filled security checklist + how to re-verify |
| `docs/test-cases.md` | Manual end-to-end test case sheet |
| `docs/load-testing.md`, `docs/load-test-plan.jmx` | Load test instructions + ready-to-run JMeter plan |
| `docs/final-report.md` | Final Review report: architecture, ER diagram, technical decisions, known limitations |
| `docs/demo-script.md` | Rehearsed demo script for the Final Review |
| `docs/regression-checklist.md` | Final regression pass sign-off sheet |
| `docs/presentation/KrishvaMart-Final-Review.pptx` | Slide deck for the Final Review |
| `docs/cloud-deployment.md` | Step-by-step cloud deployment: Render, Railway, VM, Docker Compose |
| `Dockerfile`, `Dockerfile.h2`, `docker-compose.yml`, `.dockerignore` | Container images for cloud deployment |
| `deploy/*.service`, `deploy/nginx-krishvamart.conf`, `deploy/docker-server.xml`, `deploy/README.md` | systemd + Nginx + Tomcat reference config setup |
## What's distinct about this build
Beyond the Minimum features :
- **Catalog-aware chatbot** 
- **Dark mode** 
- **Recently viewed products** 
- **Real-marketplace browse**
- **Shipping address capture at checkout** 
- **Self-initializing, cloud-ready deployment** 
- **Transaction-tested checkout** 
- **Order Cancellation & returns**
## Known limitations
- `GeminiChatProvider` is wired but untested against a live API key in this environment; `mock` is the safe default until a key is configured.
- Test coverage covers the highest-risk logic but doesn't exhaustively cover every DAO/service `docs/test-cases.md` for the manual E2E sheet and `docs/load-testing.md` / `docs/load-test-plan.jmx` for the load test - neither has been executed against a live deployment yet.
- Docker/cloud config has been written and manually reviewed but not actually deployed and smoke-tested against a real cloud platform in this environment.
- Only a handful of commits exist so far . The commits/week,`feature/<name>`-branch workflow is a process to follow going forward.
- A GitHub Projects Kanban board has to be created manually in the GitHub UI - it isn't a repo file.
