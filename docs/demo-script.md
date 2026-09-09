# Final Review Demo Script

Target length: 6-8 minutes. Rehearse against the live deployed URL, not
localhost, per Section 19's Definition of Done. Have `docs/final-report.md`
and the slide deck open as backup if live demo has issues; the backup
screen-capture video (Section 10) is the last resort.

## 0. Setup (before you start talking)

- Two browser windows/profiles open: one logged out (for buyer flows),
  one you'll log a seller into mid-demo.
- Demo accounts ready (see README): `divya.buyer@krishvamart.com` /
  `Buyer@1234`, `priya.seller@krishvamart.com` / `Seller@123`,
  `admin@krishvamart.com` / `Admin@12345`.
- Have one product with low stock (e.g. 1-2 units) seeded, to demo the
  out-of-stock guardrail live.

## 1. Opening (30s)

"KrishvaMart is a multi-seller marketplace - sellers list products, buyers
browse and check out, admins moderate. It's built on Java Servlets, JDBC,
and Tomcat, all SQL through PreparedStatements, with an AI chatbot that can
actually check live stock, not just canned answers."

## 2. Buyer flow (2 min)

1. Land on the homepage - point out dark mode toggle, click it once.
2. Search a product by keyword, then filter by category.
3. Open a product detail page - point out the star rating average and the
   "Recently viewed" strip appearing after a second product view.
4. Add to cart; try adding more than available stock -> show the 409
   `CONFLICT` guard rejecting it.
5. Go to cart, adjust quantity, then checkout (mock payment).
6. Show the order in Order History with status `CONFIRMED`.

## 3. Seller flow (1.5 min)

1. Log in as seller in the second window.
2. Create a new listing (F2).
3. Go to Orders - show the incoming order from step 2 above containing this
   seller's product.
4. Advance its status `CONFIRMED -> SHIPPED` (O2 workflow) - point out the
   invalid-transition guard if time allows (try skipping straight to
   `DELIVERED` from `PENDING` and show the 400 rejection).

## 4. Review flow (45s)

1. Advance the same order to `DELIVERED`.
2. Back in the buyer window, submit a review + star rating on that order's
   product.
3. Refresh the product page - show the review and updated average rating.

## 5. Admin flow (45s)

1. Log in as admin.
2. Show the all-users and all-orders views (F7).
3. Deactivate a listing - show it disappear from the buyer-facing search.

## 6. AI chatbot - the differentiator (1 min)

1. Open the chat widget.
2. Ask "is the wireless mouse in stock" - show it answers with the actual
   live `stock_qty`, not a canned line.
3. Ask a generic FAQ question ("how do returns work") - show it falls back
   to the scripted answer.
4. Briefly explain: `ChatProvider` is a Strategy (swappable mock/Gemini via
   config flag), wrapped in a `CatalogAwareChatProvider` Decorator that
   intercepts stock/price questions before they'd otherwise go to the
   canned/LLM layer.

## 7. Closing (30s)

"Everything you saw runs through the same layered architecture - servlets
stay thin, business rules live in services, all SQL is parameterized in the
DAO layer behind a single connection pool. Known gaps are the wishlist and
seller analytics features, both explicitly optional and out of scope for
this checkpoint - documented honestly in the final report."

## If something breaks live

- Fall back to the backup screen-capture video (record this before the
  actual review, per Section 10).
- Have `docs/test-cases.md` open - you can narrate the expected behavior
  from the test sheet even if a live click fails.
