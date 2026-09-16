# Sprint Retros

## Week 1: Architecture & Skeleton
- What worked: Generating the full layered skeleton (model/dao/service/controller) in one pass.
- What didn't: Integration friction between components that were scaffolded simultaneously without early smoke tests.
- Change for next week: Run `mvn -B clean verify` against a real Tomcat/H2 setup as early as possible to catch integration issues the initial skeleton cannot surface alone.

## Week 2: Database & Connection Pooling
- What worked: Establishing HikariCP with H2 in-memory mode enabled fast unit and DAO testing without external DB provisioning.
- What didn't: Dual configuration between `@WebServlet` annotations and `web.xml` caused Tomcat deployment conflicts during cargo runtime startup.
- Change for next week: Centralize servlet mapping declarations exclusively in `web.xml` and rely on schema migration scripts rather than manual DDL execution.

## Week 3: User Management & Authentication
- What worked: Using `jBCrypt` with standard HTTP session handling provided clean, stateless password hashing and role enforcement.
- What didn't: Session attribute casting in base controllers occasionally produced `NullPointerException` on unauthenticated requests.
- Change for next week: Implement a dedicated `AuthFilter` to intercept protected routes before servlet execution occurs.

## Week 4: Product Catalog & Search
- What worked: Building a structured `ProductSearchCriteria` pattern made multi-parameter filtering clean and extensible.
- What didn't: Manual SQL query string concatenation for dynamic filters was error-prone and hard to maintain.
- Change for next week: Standardize parameterized SQL query builders across all DAO implementations to prevent SQL injection risks.

## Week 5: Shopping Cart Implementation
- What worked: Isolating cart mutation logic into `CartService` kept cart state calculations and running totals deterministic.
- What didn't: In-memory session cart data conflicted with DB-backed cart records when testing cross-device sessions.
- Change for next week: Persist cart line items directly to the database keyed by authenticated user ID.

## Week 6: Checkout & Order Processing
- What worked: Wrapping checkout operations (stock decrement, order creation, order item insertion, cart clearance) inside single atomic DB transactions.
- What didn't: Race conditions during simultaneous checkouts for limited inventory items.
- Change for next week: Apply row-level locking (`SELECT ... FOR UPDATE`) during inventory validation and stock deductions.

## Week 7: JSP Storefront & UI Integration
- What worked: JSTL standard tags and reusable JSP header/footer fragments reduced front-end code duplication across buyer and seller pages.
- What didn't: Mixing scriptlets `<% ... %>` in legacy pages led to silent parsing errors in Tomcat 9.
- Change for next week: Enforce pure JSTL and Expression Language (`EL`) across all `.jsp` views.

## Week 8: Seller Dashboard & Inventory Management
- What worked: Restricting product updates and deletions at the DAO query level using compound checks (`WHERE id = ? AND seller_id = ?`).
- What didn't: Lack of image upload validation allowed malformed image URLs into the database.
- Change for next week: Add URL syntax regex validation in `ProductRequestDTO` before hitting the service layer.

## Week 9: Cloud VM Deployment & Nginx Reverse Proxy
- What worked: Packaging to a standalone `.war` and deploying behind Nginx on port 80 enabled seamless public access and SSL termination.
- What didn't: H2 file-mode database file permissions were locked when running Tomcat as a non-root system user.
- Change for next week: Explicitly define the H2 base directory in a dedicated `/opt/krishvamart/data` directory with correct ownership.

## Week 10: Security, Validation & SpotBugs Auditing
- What worked: Automating SpotBugs and Checkstyle in the Maven verify phase caught unclosed `ResultSet` and resource leak scenarios.
- What didn't: Overly strict Google Checkstyle rules flagged standard DTO boilerplate formatting.
- Change for next week: Configure `failOnViolation=false` with warning thresholds for Checkstyle while keeping SpotBugs fatal.

## Week 11: Static Analysis Clean-Up, UI Polish & AI Readiness
- What worked: Resolved compiler and linter diagnostics across all test suites by standardizing lifecycle visibility to `public` and actively asserting `assertThrows` outcomes; integrated dynamic product recommendation sidebars across cart and wishlist views with strict slice limits.
- What didn't: Floating chat widget markup remained dormant without backend conversational context, requiring stub styling adjustments to prevent layout shifts.
- Retrospective Summary: Core architecture, unit test harness, deployment containers, and database seed assets are verified and stabilized at zero warnings, leaving the project production-ready for intelligent catalog chat integration.