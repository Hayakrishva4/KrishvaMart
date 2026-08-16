# Sprint Retros

## Sprint 0: Architecture & Skeleton
- What worked: Generating the full layered skeleton (model/dao/service/controller) in one pass.
- Change for next sprint: run `mvn -B clean verify` against a real Tomcat/H2 setup as soon as possible to catch integration issues the skeleton can't surface on its own.

## Sprint 1: Database & Connection Pooling
- What worked: Establishing HikariCP with H2 in-memory mode enabled fast unit and DAO testing without external DB provisioning.
- What didn't: Dual configuration between `@WebServlet` annotations and `web.xml` caused Tomcat deployment conflicts during cargo runtime startup.
- Change for next sprint: Centralize servlet mapping declarations exclusively in `web.xml` and rely on schema migration scripts rather than manual DDL execution.

## Sprint 2: User Management & Authentication
- What worked: Using `jBCrypt` with standard HTTP session handling provided clean, stateless password hashing and role enforcement.
- What didn't: Session attribute casting in base controllers occasionally produced `NullPointerException` on unauthenticated requests.
- Change for next sprint: Implement a dedicated `AuthFilter` to intercept protected routes before servlet execution occurs.

## Sprint 3: Product Catalog & Search
- What worked: Building a structured `ProductSearchCriteria` pattern made multi-parameter filtering (category, price range, keyword) clean and extensible.
- What didn't: Manual SQL query string concatenation for dynamic filters was error-prone and hard to maintain.
- Change for next sprint: Standardize parameterized SQL query builders across all DAO implementations to prevent SQL injection risks.

## Sprint 4: Shopping Cart Implementation
- What worked: Isolating cart mutation logic into `CartService` kept cart state calculations and running totals deterministic.
- What didn't: In-memory session cart data conflicted with DB-backed cart records when testing cross-device sessions.
- Change for next sprint: Persist cart line items directly to the database keyed by authenticated user ID.

## Sprint 5: Checkout & Order Processing
- What worked: Wrapping checkout operations (stock decrement, order creation, order item insertion, cart clearance) inside single atomic DB transactions.
- What didn't: Race conditions during simultaneous checkouts for limited inventory items.
- Change for next sprint: Apply row-level locking (`SELECT ... FOR UPDATE`) during inventory validation and stock deductions.

## Sprint 6: JSP Storefront & UI Integration
- What worked: JSTL standard tags and reusable JSP header/footer fragments reduced front-end code duplication across buyer and seller pages.
- What didn't: Mixing scriptlets `<% ... %>` in legacy pages led to silent parsing errors in Tomcat 9.
- Change for next sprint: Enforce pure JSTL and Expression Language (`EL`) across all `.jsp` views.

## Sprint 7: Seller Dashboard & Inventory Management
- What worked: Restricting product updates and deletions at the DAO query level using compound checks (`WHERE id = ? AND seller_id = ?`).
- What didn't: Lack of image upload validation allowed malformed image URLs into the database.
- Change for next sprint: Add URL syntax regex validation in `ProductRequestDTO` before hitting the service layer.

## Sprint 8: Cloud VM Deployment & Nginx Reverse Proxy
- What worked: Packaging to a standalone `.war` and deploying behind Nginx on port 80 enabled seamless public access and SSL termination.
- What didn't: H2 file-mode database file permissions were locked when running Tomcat as a non-root system user.
- Change for next sprint: Explicitly define the H2 base directory in a dedicated `/opt/krishvamart/data` directory with correct ownership.

## Sprint 9: Security, Validation & SpotBugs Auditing
- What worked: Automating SpotBugs and Checkstyle in Maven verify phase caught unclosed `ResultSet` and resource leak scenarios.
- What didn't: Overly strict Google Checkstyle rules flagged standard DTO boilerplate formatting.
- Change for next sprint: Configure `failOnViolation=false` with warning thresholds for Checkstyle while keeping SpotBugs fatal.

## Sprint 10: Final End-to-End Testing & Project Wrap-up
- What worked: JUnit 5 and Mockito coverage reached over 85% across all DAO and service layers with full mock coverage for HTTP requests.
- What didn't: Test teardown didn't always wipe the temporary in-memory database between concurrent test suites.
- Change for next sprint: Use `@BeforeEach` and `@AfterEach` hooks to run schema truncation scripts cleanly between test suites.