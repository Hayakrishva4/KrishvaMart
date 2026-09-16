# Contributing to KrishvaMart

Thank you for contributing to KrishvaMart. Follow this guide to set up your local development environment, maintain code hygiene, and adhere to project standards.

## Local Development Setup

### Prerequisites

* **JDK**: 17 LTS
* **Build Tool**: Apache Maven 3.9+
* **Application Server**: Apache Tomcat 9.0.x (configured with `$CATALINA_HOME`)
* **Database**: Embedded H2 (runs locally; no external database service required)

### Step-by-Step Setup

**1. Clone and Configure**

```bash
git clone <your-repo-url> krishvamart
cd krishvamart
cp src/main/resources/config.properties.example src/main/resources/config.properties

### Execute Test Suites
 > mvn -B clean verify