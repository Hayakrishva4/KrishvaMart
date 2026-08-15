# Multi-stage build: compile the WAR with Maven, then run it on a slim Tomcat
# image. Works unmodified on any container-based cloud platform (Render,
# Railway, Fly.io, AWS App Runner/ECS, GCP Cloud Run, Azure Container Apps).
#
# Build:  docker build -t krishvamart .
# Run:    docker run -p 8080:8080 --env-file .env krishvamart
# (See docs/cloud-deployment.md for a full walkthrough per platform.)

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Cache dependencies in their own layer so code-only changes don't re-download the internet.
RUN mvn -B dependency:go-offline
COPY src ./src
COPY db ./db
RUN mvn -B clean package -DskipTests

# ---- Stage 2: run ----
FROM tomcat:9.0-jdk17-temurin
# Remove Tomcat's default sample apps - smaller image, smaller attack surface.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/krishvamart.war /usr/local/tomcat/webapps/ROOT.war

# Honor a cloud-injected PORT env var (falls back to 8080 if unset) - see
# deploy/docker-server.xml for details.
COPY deploy/docker-server.xml /usr/local/tomcat/conf/server.xml
ENV CATALINA_OPTS="-Dorg.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource"
ENV PORT=8080

EXPOSE 8080

CMD ["catalina.sh", "run"]
