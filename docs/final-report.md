# KrishvaMart - Final Report

Anna University R2025, Semester 3 Capstone &middot; Multi-seller e-commerce marketplace

## 1. Problem statement

Sellers list products. Buyers browse, search, add to cart, and purchase. An
admin manages users, orders, and listings. Checkout uses a mock payment
confirmation step - no real payment gateway is integrated, per the project's
scope constraints. An AI chatbot answers FAQ-style and (as a differentiator
beyond the minimum spec, see Section 5) live-catalog questions.

## 2. Architecture

Layered MVC over Servlets (Front Controller pattern realized via the
servlet container's URL-mapping table plus a shared filter chain):

```
Browser (JSP shell + vanilla JS/fetch)
  -> Filter layer: EncodingFilter, RequestIdFilter, AuthFilter
  -> Servlets (controller/*) - thin, no SQL, no business logic
  -> Service layer (service/*) - business rules, validation, no JDBC
  -> DAO layer (dao/impl/*) - all SQL, PreparedStatement only
  -> HikariCP connection pool (listener/AppContextListener)
  -> H2 Database (server mode)
```

Full package breakdown and tech stack table: see `README.md`.

## 3. ER diagram

See `docs/D1-er-diagram.svg` (rendered) / `docs/D1-er-diagram.puml` (source).
Six tables: `users`, `products`, `orders`, `order_items`, `cart_items`,
`reviews`. Every foreign key is indexed; money fields are `DECIMAL(10,2)`;
every table has `created_at`. Full DDL: `db/schema.sql`.

## 4. Technical decisions

| Decision | Rationale |
|---|---|
| Per-resource servlets instead of one `DispatcherServlet` | Section 2 explicitly offers both as options. Per-resource servlets keep each resource's HTTP verbs (`GET/POST/PUT/PATCH/DELETE`) together in one class, which reads more naturally than a single servlet branching on both path and verb. The shared filter chain (`AuthFilter`, etc.) plays the front-controller role across all of them. |
| Transaction boundary lives in `OrderService`, not the DAO layer | Checkout touches four tables (`orders`, `order_items`, `products`, `cart_items`) and must commit or roll back as one unit. `OrderService.checkout()` owns the `Connection`/`commit`/`rollback` calls and hands that same `Connection` to each DAO method's transactional overload - no SQL is written in the service, only transaction demarcation. |
| Hand-rolled DI (`ServiceRegistry`) instead of Spring | The project's scale (5 services, 5 DAOs) doesn't justify a DI framework's learning curve/footprint for a semester capstone; a single class wiring everything once at startup is easier to explain in a viva than Spring's container. |
| `ChatProvider` Strategy + `CatalogAwareChatProvider` Decorator | Section 17 requires the AI provider be swappable via config flag - that's the Strategy. Wrapping the selected provider in a catalog-aware decorator (Section 5, differentiator) means the mock/Gemini implementations stay simple (pure FAQ/LLM) while stock/price questions get grounded in real DB data, without duplicating that logic in both providers. |
| bcrypt over a faster hash | Section 2 mandates it; bcrypt's deliberate slowness is the point for password storage (resists brute-force better than SHA-256 even salted). |
| H2 over a heavier RDBMS | Section 3 specifies it; zero-install embedded/server modes suit a solo capstone with no dedicated DB ops. |

## 5. Differentiators beyond the minimum spec

- **Catalog-aware chatbot** (`chat/CatalogAwareChatProvider.java`): answers
  "is X in stock" / "price of X" from the live database instead of only
  canned FAQ text, while still meeting the 5-10 FAQ-question minimum via the
  underlying `MockChatProvider`.
- **O1 (wishlist) and O3 (seller sales dashboard)** implemented, not just
  the mandatory F1-F8 - wishlist supports add/remove/move-to-cart; the
  dashboard shows delivered-order counts, units sold, and per-product
  revenue.
- **Real-marketplace browse**: price range, sort order, and pagination on
  top of keyword/category search (`ProductSearchCriteria`, Builder-built).
- **Shipping address capture at checkout** - stored per order, surfaced in
  order history.
- **Dark mode** (`js/theme.js`, `css/style.css`), applied synchronously in
  `<head>` to avoid a flash of the wrong theme, persisted per-browser.
- **Recently viewed products** (`js/recently-viewed.js`), a client-side
  convenience feature with no server round-trip.
- **Cloud-ready deployment**: a single Docker image, environment-variable
  config (`ConfigResolver`), and a self-initializing database
  (`SchemaInitializer`) that applies the schema and seeds demo data on
  first boot - no shell access needed for a fresh cloud deployment to be
  immediately usable. See `docs/cloud-deployment.md`.
- **Checkout transaction integration test** (`OrderServiceTest`) that
  exercises the real commit/rollback path against embedded H2, including the
  insufficient-stock-mid-transaction race case - not just mocked unit tests.
- **Rendered diagrams** checked in as actual `.svg` images alongside
  PlantUML source, not just source someone still has to render.
- **Filled security checklist with verification commands**
  (`docs/security-checklist.md`) rather than a checklist of unverified claims.

## 6. Known limitations

- **Test coverage** covers the riskiest logic (checkout transaction, auth,
  cart limits, catalog-aware chat, product ownership checks, wishlist,
  criteria-based product search/pagination) but not every DAO/service
  exhaustively, and there are no servlet-level (Mockito
  `HttpServletRequest`/`Response`) tests yet.
- **Load testing** (`docs/load-testing.md`, `docs/load-test-plan.jmx`) has a
  ready-to-run plan but no results recorded yet - needs a live deployment.
- **Cloud deployment** (`Dockerfile`, `docker-compose.yml`,
  `docs/cloud-deployment.md`) has been written and manually reviewed but not
  actually run against a live cloud platform in this environment - verify
  it end-to-end before trusting it for the Full Build + Deploy checkpoint.
- **`GeminiChatProvider`** is implemented and wired behind the
  `ai.chatbot.provider=gemini` config flag but untested against a live API
  key in this environment; `mock` (with the catalog-aware decorator) is the
  default and is what's actually been exercised.

## 7. Design patterns used

See `docs/design-patterns.md` for the full table (DAO, Front Controller,
Singleton, Factory, Strategy, Builder - all six required by Section 12 -
plus the Decorator pattern used for the catalog-aware chatbot as a bonus).
