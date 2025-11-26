# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml /app/parent-pom.xml

# Copy common-lib
COPY common-lib/pom.xml /app/common-lib/pom.xml
COPY common-lib/src /app/common-lib/src

# Copy ticket-service
COPY ticket-service/pom.xml /app/ticket-service/pom.xml
COPY ticket-service/src /app/ticket-service/src

# Build common-lib first
WORKDIR /app
RUN mvn -f parent-pom.xml -pl common-lib install -DskipTests

# Build ticket-service
RUN mvn -f parent-pom.xml -pl ticket-service package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S servicedesk && adduser -S servicedesk -G servicedesk

# Copy the built jar
COPY --from=build /app/ticket-service/target/*.jar app.jar

# Set ownership
RUN chown -R servicedesk:servicedesk /app

USER servicedesk

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
