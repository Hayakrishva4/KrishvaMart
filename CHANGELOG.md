# Changelog

> All notable changes to this project will be documented in this file.

## [Unreleased]

### Planned - Weeks 9 to 11
- **Catalog-Aware Chatbot Decorator**: Live stock, price querying, and recommendation integration.
- **Recently Viewed & Personalization**: Session-backed tracking of viewed items with recommendations.
- **Transaction Integration Testing**: Concurrency and race condition testing for high-volume checkout scenarios.
- **Project Deliverables**: Final technical report, demonstration script, comprehensive regression checklist, and Final Review slide deck.

## Polish, Test Harness & UX Enhancement (Current)

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
