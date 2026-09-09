# Design Patterns Used (Section 12)

| Pattern | Where | Why |
|---|---|---|
| **DAO** | `dao/*` interfaces + `dao/impl/Jdbc*DAO` | Isolates all SQL/PreparedStatement code from the service layer; each entity has an interface (`UserDAO`, `ProductDAO`, `CartDAO`, `OrderDAO`, `ReviewDAO`) and a JDBC implementation. |
| **Front Controller** | Servlet container's URL-mapping table (per-resource servlets: `AuthServlet`, `ProductServlet`, `CartServlet`, `OrderServlet`, `ReviewServlet`, `AdminServlet`, `ChatServlet`, `HealthServlet`), all funneled through the shared `AuthFilter`/`EncodingFilter`/`RequestIdFilter` chain | Section 2 offers a choice between a single `DispatcherServlet` and per-resource servlets. This project uses per-resource servlets (the spec's stated alternative), with the filter chain acting as the shared front-controller-style entry point that every `/api/v1/*` request passes through before reaching a resource servlet. `BaseApiServlet` centralizes the response-envelope and error-mapping logic all resource servlets share. |
| **Singleton** | `AppContextListener` creates exactly one `HikariDataSource` for the application's lifetime and stores it on the `ServletContext`; all DAOs read from that single instance. | Section 2, Rule 5: one connection pool, owned by one listener. |
| **Factory** | `ServiceRegistry` (constructed once in `AppContextListener.contextInitialized`) instantiates every `JdbcXxxDAO` and wires it into the matching service. | Centralizes object construction/wiring so servlets never `new` a DAO or service themselves - they pull the shared instances via `services()`. |
| **Strategy** | `com.krishva.krishvamart.chat.ChatProvider` interface, with `MockChatProvider` and `GeminiChatProvider` as interchangeable implementations selected at startup by the `ai.chatbot.provider` config flag (Section 17, Rule 2). | Lets the AI backend be swapped without touching `ChatService` or `ChatServlet`. |
| **Builder** | `dto.OrderConfirmationDTO.Builder`, used by `OrderServlet` to assemble the checkout response (`OrderConfirmationDTO.fromOrder(order)`). | The checkout response combines several optional/derived fields (order id, status, total, items, a generated confirmation message) - a builder keeps that construction readable instead of a multi-arg constructor or a hand-built `Map`. |

## Notes on scope

Most other DTOs (`ProductRequestDTO`, `CartItemRequestDTO`, etc.) are simple
JavaBeans deserialized directly from JSON by Gson - a Builder would add
ceremony without value there, so it's used only where construction is
genuinely non-trivial (checkout confirmation, product search criteria,
seller sales summary), per the spirit of the requirement rather than
mechanically applying it everywhere.

## Cloud deployment infrastructure (not a Section 12 pattern, but worth noting)

`ConfigResolver` (env var > `config.properties` > default) and
`SchemaInitializer` (idempotent schema apply + one-time demo seed on an
empty database) aren't classic GoF patterns, but they're what makes the
same build artifact (WAR or Docker image) deployable unmodified across
local, VM, and PaaS environments - see `docs/cloud-deployment.md`.
