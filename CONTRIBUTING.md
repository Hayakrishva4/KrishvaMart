# Contributing to KrishvaMart

Thank you for contributing to **KrishvaMart**, a multi-seller e-commerce marketplace built on Java 17, Java Servlets 4.0, JDBC, HikariCP, and H2 according to the Anna University R2025 Semester 3 Capstone Specification.

To maintain production engineering hygiene and ensure a smooth review process, all contributors must follow the setup guide, coding conventions, and pull request workflow outlined below.

---

## 1. Local Development Setup

### Prerequisites
* **Java Development Kit (JDK)**: OpenJDK 17 LTS
* **Build Automation**: Apache Maven 3.9+
* **Application Server**: Apache Tomcat 9.0.x (Servlet API `javax.servlet.*`, configured with `$CATALINA_HOME`)
* **Database**: H2 Database (Server mode for local execution; embedded in-memory for unit testing)
* **Git**: 2.30+

### Step-by-Step Installation

**1. Clone the Repository**
```bash
git clone https://github.com/Hayakrishva4/KrishvaMart.git
cd KrishvaMart
```

**2. Configure Environment Properties**
Copy the template configuration to create your active environment file:
```bash
cp .env.example .env
# or if using property files:
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Verify your local configuration parameters:
```ini
DB_JDBC_URL=jdbc:h2:tcp://localhost:9092/./data/krishvamart
DB_USERNAME=sa
DB_PASSWORD=
PORT=8080
AI_CHATBOT_PROVIDER=mock
AI_CHATBOT_API_KEY=
```

**3. Start the H2 Database Server**
Run the H2 server listener in TCP mode so data persists across Tomcat restarts:
```bash
# Execute using Maven dependency or standalone JAR
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -baseDir ./data
```
* Initialize schema and seed data by executing `src/main/resources/db/schema.sql` and `src/main/resources/db/seed.sql` on the first run.

**4. Build and Package the Application**
Compile classes, run the automated test suite, and package the WAR artifact:
```bash
mvn clean package
```

**5. Deploy to Apache Tomcat**
Deploy the generated `krishvamart.war` to your Tomcat webapps directory:
```bash
cp target/krishvamart.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

**6. Verify Health and Smoke Test**
Verify database and application connectivity:
```bash
curl -X GET http://localhost:8080/krishvamart/api/v1/health
```
Expected response:
```json
{
  "status": "UP",
  "db": "UP"
}
```

---

## 2. Testing and Static Analysis

Every change must pass automated unit testing, static bug analysis, and style checks before opening a pull request.

```bash
# Run unit tests and DAO integration tests against embedded H2 (jdbc:h2:mem:test)
mvn -B clean verify

# Run SpotBugs static code analysis
mvn spotbugs:check

# Run Checkstyle formatting and style audit
mvn checkstyle:check
```

---

## 3. Mandatory Engineering Rules

Contributions must adhere to the core architectural constraints of the project:

1. **Layered MVC Isolation**:
   * **Controller Layer (`controller/`)**: Servlets handle HTTP orchestration, request parsing, session checks, and JSON/JSP dispatching only. No SQL or business logic.
   * **Service Layer (`service/`)**: Contains business validations and transaction management. Services depend on DAO interfaces, not concrete implementations.
   * **DAO Layer (`dao/`)**: All SQL execution is isolated inside DAO classes.
2. **Strict SQL Parameterization**: Every database query must use `java.sql.PreparedStatement`. String-concatenated SQL queries are strictly prohibited under any circumstances.
3. **Database Connection Lifecycle**: Database connections are obtained exclusively through the HikariCP connection pool initialized in `DBConnectionListener` (`ServletContextListener`). Never invoke `DriverManager.getConnection()`.
4. **Resource Management**: Every `Connection`, `PreparedStatement`, and `ResultSet` must be managed using Java try-with-resources blocks.
5. **Security & Authentication**:
   * Passwords must be hashed using `jBCrypt` (`BCrypt.hashpw`) with automatic salting. Never store or log plaintext passwords.
   * Protected endpoints must validate session state via `AuthFilter`.
   * Sessions must regenerate session IDs upon successful login and specify an explicit 30-minute timeout.
6. **Output Escaping**: All user-supplied data rendered in JSPs must be escaped using JSTL `<c:out>` or `fn:escapeXml` to prevent Cross-Site Scripting (XSS).
7. **API Contract Standards**:
   * Version all REST endpoints under `/api/v1/...`.
   * Return the standard JSON response envelope:
     ```json
     // Success
     { "success": true, "data": { ... }, "error": null }

     // Failure
     { "success": false, "data": null, "error": { "code": "VALIDATION_ERROR", "message": "..." } }
     ```
   * Match appropriate HTTP status codes (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`, `500 Internal Server Error`).

---

## 4. Branching & Commit Conventions

### Branch Strategy
* `main`: Always deployable and protected. All production deployments on Render deploy from `main`.
* `feature/<feature-name>`: Dedicated branch for new features (e.g., `feature/seller-analytics`, `feature/wishlist`).
* `fix/<bug-name>`: Dedicated branch for bug fixes (e.g., `fix/cart-quantity-decrement`).

### Commit Message Format
We enforce Conventional Commits:
```
<type>: <concise description in imperative mood>

[optional body explaining motivation and technical decisions]
```

**Allowed types:**
* `feat`: A new user-facing feature or API endpoint
* `fix`: A bug fix
* `test`: Adding or refactoring test cases
* `docs`: Documentation updates (README, CONTRIBUTING, diagrams)
* `refactor`: Code restructuring without changing external behavior
* `chore`: Build configuration or dependency maintenance

---

## 5. Pull Request Checklist (Definition of Done)

Before submitting a Pull Request, ensure your branch fulfills the following criteria:

- [ ] Code compiles cleanly with zero Checkstyle or SpotBugs major warnings.
- [ ] Unit and DAO tests written, passing locally, and executing against embedded H2.
- [ ] All database schema adjustments are added as versioned SQL migration scripts (`db/migrations/V...`).
- [ ] Database credentials and secrets are excluded from git (`.env` and `config.properties` in `.gitignore`).
- [ ] Verified endpoints against the live cloud instance (`krishvamart.onrender.com`) or local Tomcat instance.
- [ ] Relevant documentation updated (`README.md`, `CHANGELOG.md`, or API contracts).
- [ ] GitHub Actions CI pipeline runs green on push.
