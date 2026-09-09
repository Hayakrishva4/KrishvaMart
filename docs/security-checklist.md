# Security Checklist (Section 9)

Status as of the initial scaffold commit. Re-verify every item before the
Sep 21 Full Build + Deploy checkpoint, especially anything added after this
was last checked.

- [x] **Every query parameterized.** Verified: `grep -rn "Statement)" src/main/java/ | grep -v "PreparedStatement)"`
      returns no results - every `Statement` construction in the codebase is a
      `PreparedStatement`. Re-run this exact command after adding any new DAO code.
- [x] **Passwords bcrypt-hashed, never logged.** `PasswordUtil` (jBCrypt, work
      factor 12) is the only place password hashes are created or compared.
      No log statement anywhere references a password or password hash field.
- [x] **All protected servlets enforce session checks via AuthFilter.**
      `AuthFilter` is mapped to `/api/v1/*` (every controller servlet lives
      under that path) and denies with 401 unless the route is on the
      explicit public allowlist (`/auth/register`, `/auth/login`, GET on
      `/products*` and `/reviews*`, `/health`, POST `/chat`). Role-specific
      checks (seller-only, admin-only, buyer-only) are then enforced in the
      service layer.
- [x] **User-supplied input escaped before rendering.** Two mechanisms, matching
      the two rendering paths in Section 2's architecture diagram:
      - Server-rendered JSP content uses JSTL `<c:out>` (see
        `WEB-INF/jspf/header.jspf`, which renders the session user's name).
      - AJAX-rendered content (product names/descriptions, review comments,
        etc., which is the majority of user-supplied data in this app) is
        inserted via `escapeHtml()` in `js/api.js` before being placed in the
        DOM through `innerHTML`, everywhere that helper is used in
        `js/products.js`, `js/product-detail.js`, `js/cart.js`,
        `js/orders.js`, `js/seller.js`, `js/admin.js`.
      **Action item:** audit every `innerHTML` assignment in `src/main/webapp/js/`
      before deployment to confirm no interpolated value bypasses `escapeHtml()`.
- [ ] **File upload validates type and size; never trusts client-supplied filename.**
      Not applicable in the current scope - products take an `imageUrl` string
      (Section 1, F2), not a file upload. Revisit if file upload is added later.
- [x] **Error pages do not expose stack traces.** `web.xml` maps 404, 500, and
      `java.lang.Throwable` to `/jsp/error.jsp`, which shows a generic message
      only. `BaseApiServlet.handleError()` logs the real exception server-side
      via SLF4J but only ever sends the client a generic
      `"A server error occurred. Please try again."` for unexpected/internal
      errors - `DataAccessException` and unhandled `Exception` never leak their
      message or stack trace in the JSON response.
- [x] **Database credentials excluded from version control.** `config.properties`
      is listed in `.gitignore`; only `config.properties.example` (no real
      secrets, points at localhost) is committed.

## How to re-run the automated part of this checklist

```bash
# Parameterized queries
grep -rn "Statement)" src/main/java/ | grep -v "PreparedStatement)"   # expect no output

# Static analysis (now bound to the verify phase in pom.xml)
mvn -B clean verify   # runs Checkstyle + SpotBugs as part of the build

# Credentials not tracked
git check-ignore -v src/main/resources/config.properties   # should print a match
```
