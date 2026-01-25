# ================================
# Stage 1: Build with Maven
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven wrapper and config
COPY pom.xml .

COPY .mvn .mvn
COPY mvnw mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests


# ================================
# Stage 2: Runtime image
# ================================
FROM eclipse-temurin:17-jre-alpine

# Install utilities
RUN apk add --no-cache curl tini

WORKDIR /app

# Create logs directory FIRST (critical for logback)
RUN mkdir -p /app/logs

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Fix permissions
RUN chown -R spring:spring /app

USER spring:spring

# Copy built jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Proper signal handling
ENTRYPOINT ["/sbin/tini", "--"]

# Run app
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
