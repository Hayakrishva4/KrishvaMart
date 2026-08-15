# Contributing to KrishvaMart

## From `git clone` to a running local instance

### Prerequisites
- JDK 17
- Maven 3.9+
- Tomcat 9.0.x installed locally (or use `mvn tomcat7:run`-style plugin / manual deploy)
- No external services needed - H2 runs locally

### 1. Clone and configure
```bash
git clone <your-repo-url> krishvamart
cd krishvamart
cp src/main/resources/config.properties.example src/main/resources/config.properties
```
Edit `config.properties` if you want to change the DB location or the AI
chatbot provider (defaults to `mock`, which needs no API key).

### 2. Start H2 in server mode
```bash
mkdir -p data
java -cp ~/.m2/repository/com/h2database/h2/*/h2-*.jar org.h2.tools.Server \
  -tcp -tcpAllowOthers -baseDir ./data
```
(Or run `mvn dependency:build-classpath` once and reuse the resolved h2 jar path.)

### 3. Create schema and seed data
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.krishva.krishvamart.util.DbSeeder"
```
This applies `db/schema.sql`, inserts the demo admin/seller/buyer accounts
with real bcrypt hashes, then applies `db/seed.sql` (sample products).
Demo credentials are printed to the console - see also `README.md`.

### 4. Build and deploy the WAR
```bash
mvn clean package
cp target/krishvamart.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```
Visit `http://localhost:8080/krishvamart/`.

### 5. Run tests
```bash
mvn -B clean verify
```
DAO tests spin up their own embedded `jdbc:h2:mem:test` instance per test
class - no external DB needed for `mvn verify`.

## Branching and commits
- `main` is always deployable.
- Work happens on `feature/<name>` branches, merged via self-reviewed PR.
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `test:`, `docs:`.
- See `CHANGELOG.md` for the semver history and `RETRO.md` for sprint retros.

## Code style
- Checkstyle (Google style) and SpotBugs run in CI (`mvn -B clean verify`);
  fix warnings before opening a PR.
- Javadoc is required on every public class/method in `service` and `dao`.
- Servlets stay thin (HTTP orchestration only); business rules live in
  `service`; all SQL lives in `dao.impl`, PreparedStatement only.
