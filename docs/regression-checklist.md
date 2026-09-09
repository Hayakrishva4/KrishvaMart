# Final Regression Checklist

Section 7 requirement: "Full regression pass completed - no functionality
present at Sep 21 broken." Run every row in `docs/test-cases.md` again
(all 29 cases, including the security spot-checks) plus the items below
that are specific to Week 9-11 additions, and sign off before the Oct 10
Final Review.

## Re-run the full existing test sheet

- [ ] All 24 functional rows in `docs/test-cases.md` still pass
- [ ] All 5 security spot-check rows in `docs/test-cases.md` still pass
- [ ] `mvn -B clean verify` passes (unit + DAO + integration tests, plus
      Checkstyle/SpotBugs now that they're bound to the `verify` phase)

## New in Week 9-11 - regression-check these specifically

- [ ] Chatbot still answers the original mock FAQ questions (shipping,
      returns, payment, tracking, seller signup, reviews, cancellation,
      stock) unchanged by the catalog-aware decorator
- [ ] Chatbot correctly answers a live stock question for a real product
      name (`CatalogAwareChatProvider`)
- [ ] Chatbot correctly answers a live price question for a real product name
- [ ] Chatbot falls through to the FAQ/LLM layer when the product name in a
      stock/price-shaped question doesn't match anything in the catalog
- [ ] Dark mode toggle persists across a page navigation and a browser
      refresh (localStorage), on every page (index, product-detail, cart,
      orders, seller-dashboard, admin, login, register)
- [ ] Dark mode does not break contrast/readability on any page (spot-check
      forms, buttons, status badges)
- [ ] "Recently viewed" strip appears after viewing 2+ products, excludes
      the product currently being viewed on its own detail page, and links
      correctly back to each product
- [ ] Checkout confirmation message still displays correctly now that it
      comes from `OrderConfirmationDTO` instead of the raw `Order` (verify
      `cart.js` reads `order.message` / `order.orderId`, not `order.id`)
- [ ] `CatalogAwareChatProviderTest` and `OrderServiceTest` pass in CI

## Sign-off

| Reviewer | Date | Result |
|---|---|---|
| | | |
