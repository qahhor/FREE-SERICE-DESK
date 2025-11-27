# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ../pom.xml ../pom.xml
COPY ../common-lib ../common-lib
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S servicedesk && adduser -S servicedesk -G servicedesk

# Create modules directory
RUN mkdir -p /app/modules && chown -R servicedesk:servicedesk /app/modules

# Copy jar
COPY --from=build /app/target/*.jar app.jar
RUN chown servicedesk:servicedesk app.jar

USER servicedesk

EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
