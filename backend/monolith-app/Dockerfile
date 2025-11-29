# Multi-stage build for ServiceDesk Monolith Application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent POM and common-lib
COPY pom.xml .
COPY common-lib ./common-lib

# Copy monolith-app
COPY monolith-app/pom.xml ./monolith-app/
COPY monolith-app/src ./monolith-app/src

# Build the application (skip tests for faster build)
RUN mvn clean package -pl monolith-app -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install curl for healthchecks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 servicedesk && \
    adduser -u 1001 -G servicedesk -s /bin/sh -D servicedesk

# Copy JAR from build stage
COPY --from=build /app/monolith-app/target/servicedesk-monolith.jar /app/servicedesk-monolith.jar

# Change ownership
RUN chown -R servicedesk:servicedesk /app

# Switch to non-root user
USER servicedesk

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=docker", \
    "-jar", \
    "/app/servicedesk-monolith.jar"]
