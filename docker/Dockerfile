# ================================
# Stage 1: Build with Maven
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (layer caching optimization)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Package application
RUN ./mvnw clean package -DskipTests

# ================================
# Stage 2: Run lightweight image
# ================================
FROM eclipse-temurin:17-jre-alpine AS runner

# Install curl for healthcheck and tini for proper signal handling
RUN apk add --no-cache curl tini

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Healthcheck endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use tini as entrypoint for better signal handling
ENTRYPOINT ["/sbin/tini", "--"]

# Pass JVM and Spring profile configs
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
