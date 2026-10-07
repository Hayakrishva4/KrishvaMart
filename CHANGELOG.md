# Changelog
> All notable changes to this project will be documented in this file.

## [Unreleased]
### Planned - Weeks 10 to 11
- **Transaction Integration Testing**: Concurrency and race condition testing for high-volume checkout scenarios.
- **Project Deliverables**: Final technical report, demonstration script, comprehensive regression checklist, and Final Review slide deck.

## [0.1.0] - Static Analysis Hardening, Personalization & Chatbot Decorator (Current)
### Added
- **Catalog-Aware Chatbot Decorator**: Integrated `CatalogAwareChatProvider` with real-time regex parsing for live inventory and price queries directly against the database catalog.
- **Recently Viewed Products Strip**:
  - Implemented client-side `localStorage` tracking module (`js/recently-viewed.js`) maintaining an active history of up to 8 viewed items.
  - Linked automated tracking directly into `js/product-detail.js` via `window.RecentlyViewed.push()`.
  - Added dynamic rendering strips on both `index.jsp` and `product-detail.jsp` with responsive mini-card styling and automatic self-exclusion for the active product.
- **SpotBugs Annotations Suite**: Added compile-time `spotbugs-annotations` (`provided` scope) to `pom.xml` for declarative static analysis control[span_0](start_span)[span_0](end_span).

### Changed
- **Compiler Configuration Modernization**: Migrated `pom.xml` from legacy `<maven.compiler.source>`/`<maven.compiler.target>` flags to `<maven.compiler.release>17</maven.compiler.release>` to resolve strict Java platform compatibility warnings[span_1](start_span)[span_1](end_span).
- **Chatbot Fallback Behavior**: Updated `CatalogAwareChatProvider` to safely sanitize null messages and forward clean string references to the underlying delegate provider.

### Fixed
- **Static Analysis & SpotBugs Rule Violations**:
  - Resolved `EI_EXPOSE_REP2` in `CatalogAwareChatProvider` using constructor null validations and dependency injection guards[span_2](start_span)[span_2](end_span).
  - Eliminated `NP_LOAD_OF_KNOWN_NULL_VALUE` in `CatalogAwareChatProvider` by removing null-variable reloading down execution branches[span_3](start_span)[span_3](end_span).
- **Eclipse/Red Hat Null-Type Safety Compiler Warnings**:
  - Refactored Java Stream reductions in `CartService.runningTotal()` and `OrderService.checkout()` from `.map(...).reduce(...)` to null-safe iterative loops, eliminating `Function` and `BiFunction` null-safety diagnostics[span_4](start_span)[span_4](end_span).
  - Replaced method references (`CartItem::getQuantity`) in `CartService.addItem()` with defensive `Optional` verification to guarantee null safety[span_5](start_span)[span_5](end_span).
- **UI Script Binding**: Resolved interface mismatch where `product-detail.js` invoked `window.RecentlyViewed.push` before global namespace registration.

## Polish, Test Harness & UX Enhancement
### Added
- **Dynamic Product Recommendations**: Integrated responsive product recommendation sidebars across Cart (`cart.jsp`, `cart.js`) and Wishlist (`wishlist.jsp`, `wishlist.js`) capped cleanly at 3 items.
- **Persistent Wishlist Navigation**: Added sticky sidebar layout featuring a "Browse now!" quick-action banner linking directly to the main catalog.
- **Visual Scaffolding**: Added cross-theme doodle canvas background styling and unified UI token definitions in `style.css`.
- **Dormant Chat Widget Styling**: Base floating widget and message box stylesheets pre-configured for future AI chat integration without layout shifts.

### Fixed
- **IDE Diagnostic & Linter Cleanliness**: 
  - Elevated JUnit 5 lifecycle methods (`setUp`, `tearDown`) to `public` visibility across DAO and Service unit tests, eliminating false-positive "method never used" compiler warnings.
  - Asserted `assertThrows` outcomes across `CartServiceTest`, `OrderServiceTest`, `ProductServiceTest`, and `WishlistServiceTest` to eliminate "Throwable method result is ignored" static analysis diagnostics.
  - Hardened resource lifecycle handling in `JdbcProductDAOCriteriaSearchTest`, `JdbcProductDAOTest`, and `JdbcUserDAOTest` with null-safe teardown routines.
- **Pagination & Query Alignment**: Aligned client query parameters to `pageSize` and added JavaScript defensive slicing (`.slice(0, 3)`) to prevent recommendation overflows.

## Marketplace Expansion & Deployment Readiness
### Added
- **Wishlist & Dashboard Modules**: Functional multi-item wishlist persistence and dedicated seller sales metrics dashboard.
- **Advanced Catalog Search**: Criteria-based product search supporting price ranges (`minPrice`/`maxPrice`), dynamic sorting (`PRICE_ASC`, `PRICE_DESC`, `NEWEST`), and backend pagination via `ProductSearchCriteria` and `PagedResult`.
- **Checkout Address Capture**: Required delivery address capture in checkout workflow passed through order confirmation.
- **Cloud & Containerization Suite**: Production-ready `Dockerfile`, `docker-compose.yml`, centralized `ConfigResolver` for environment variables, and automated first-boot `SchemaInitializer`.
- **Documentation**: Step-by-step deployment guide in `docs/cloud-deployment.md`.

## MVP Review
### Added
- **Core Architecture Scaffold**: Layered MVC pattern utilizing JSP, Java Servlets, JDBC DAOs, and service layers.
- **Foundational Feature Set**: User authentication, seller product management, catalog browsing, and basic shopping cart.
- **Automated Verification**: Baseline JUnit test harness with H2/HikariCP test database provisioning and continuous integration workflow.