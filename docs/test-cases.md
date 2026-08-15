# Manual End-to-End Test Case Sheet (Section 9)

Covers the register -> browse -> order -> review flow, run against a live
deployment before each checkpoint review. Fill in Actual Result / Pass-Fail
columns when you execute it.

| # | Step | Action | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|
| 1 | Register buyer | POST `/api/v1/auth/register` with a new email, role `BUYER` | 201, session cookie set, `data.role` = `BUYER` | | |
| 2 | Register seller | POST `/api/v1/auth/register` with a different email, role `SELLER` | 201, session cookie set for that account | | |
| 3 | Reject admin self-signup | POST `/api/v1/auth/register` with role `ADMIN` | 400 `VALIDATION_ERROR` | | |
| 4 | Duplicate email rejected | Register again with the same email as step 1 | 409 `CONFLICT` | | |
| 5 | Seller creates listing | As seller from step 2, POST `/api/v1/products` with valid fields | 201, listing appears in `GET /api/v1/products` | | |
| 6 | Buyer browses | `GET /api/v1/products?q=<keyword>` (no login required) | 200, listing from step 5 appears if keyword matches | | |
| 7 | Buyer filters by category | `GET /api/v1/products?category=<category>` | 200, only matching category returned | | |
| 8 | Buyer adds to cart | As buyer, POST `/api/v1/cart/items` with the product id and quantity | 201, `GET /api/v1/cart` shows the item with correct running total | | |
| 9 | Cart rejects over-stock | POST `/api/v1/cart/items` with quantity greater than `stockQty` | 409 `CONFLICT` | | |
| 10 | Buyer checks out | POST `/api/v1/orders/checkout` | 201, order confirmation returned, cart is now empty | | |
| 11 | Stock decremented | `GET /api/v1/products/{id}` for the purchased product | `stockQty` reduced by the ordered quantity | | |
| 12 | Buyer sees order history | `GET /api/v1/orders` as buyer | 200, the new order appears with status `CONFIRMED` | | |
| 13 | Seller sees incoming order | `GET /api/v1/orders` as the seller who owns the product | 200, the same order appears | | |
| 14 | Seller advances status | `PATCH /api/v1/orders/{id}/status` with `SHIPPED` from `CONFIRMED` | 200, status updated | | |
| 15 | Invalid status transition rejected | `PATCH .../status` with `DELIVERED` while still `PENDING` | 400 `VALIDATION_ERROR` | | |
| 16 | Advance to DELIVERED | `PATCH .../status` with `DELIVERED` from `SHIPPED` | 200, status updated | | |
| 17 | Buyer submits review | POST `/api/v1/reviews` with the order id, product id, rating, comment | 201, review stored | | |
| 18 | Duplicate review rejected | Repeat step 17 with the same order/product/user | 409 `CONFLICT` | | |
| 19 | Review visible publicly | `GET /api/v1/reviews/product/{id}` (no login) | 200, review appears, `averageRating` updated | | |
| 20 | Admin sees all users/orders | `GET /api/v1/admin/users` and `/api/v1/admin/orders` as admin | 200, buyer/seller/order all present | | |
| 21 | Admin moderates listing | `DELETE /api/v1/admin/products/{id}` as admin | 200, listing no longer appears in `GET /api/v1/products` (buyer-facing) | | |
| 22 | Chatbot answers FAQ | POST `/api/v1/chat` with `{"message":"how do I track my order"}` | 200, relevant canned/AI reply | | |
| 23 | Health check | `GET /api/v1/health` | 200, `{"status":"UP","db":"UP"}` | | |
| 24 | Logout invalidates session | POST `/api/v1/auth/logout`, then `GET /api/v1/auth/me` | second call returns 401 | | |

## Security spot-checks (pairs with `docs/security-checklist.md`)

| # | Action | Expected result |
|---|---|---|
| 25 | `GET /api/v1/orders` with no session cookie | 401 `UNAUTHENTICATED` |
| 26 | Buyer calls `POST /api/v1/products` (seller-only) | 403 `FORBIDDEN` |
| 27 | Seller A tries `PUT /api/v1/products/{id}` on Seller B's listing | 403 `FORBIDDEN` |
| 28 | Product name containing `<script>alert(1)</script>` | Stored as-is, rendered as inert text (not executed) on the product page |
| 29 | SQL-injection-shaped search query, e.g. `q=' OR '1'='1` | 200, treated as a literal (no error, no unexpected rows) |
