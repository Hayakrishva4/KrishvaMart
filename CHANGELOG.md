# Changelog

All notable changes to KrishvaMart are recorded here, one line per release,
per the semver scheme in Section 15 of the project spec
(v0.1.0 MVP -> v0.2.0 ... -> v1.0.0 full build -> v1.1.0 AI chatbot).

## [Unreleased]
- O1 wishlist and O3 seller sales dashboard implemented (previously listed
  as not started). Real-marketplace browse: price range, sort, pagination.
  Shipping address captured at checkout.
- Cloud deployment readiness: Dockerfile + docker-compose.yml, environment-
  variable-driven config (ConfigResolver), self-initializing database on
  first boot (SchemaInitializer), docs/cloud-deployment.md.
- Week 9-11: catalog-aware chatbot decorator (live stock/price answers),
  dark mode, recently-viewed products, checkout transaction integration
  test, final report, demo script, regression checklist, Final Review
  slide deck.
- Initial project scaffold generated: layered MVC architecture, schema,
  DAO/service/controller layers for F1-F8, mock-provider AI chatbot (O4),
  JSP+JS frontend, DAO/service unit tests, CI workflow.

## [0.1.0] - MVP Review (target: 2026-08-10)
- F1 authentication, F2 seller listings, F3 browse/search, F4 cart scaffolded.
