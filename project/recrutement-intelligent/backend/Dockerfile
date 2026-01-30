# =====================================================
# Multi-stage Dockerfile for Spring Boot Application
# Plateforme de Recrutement Intelligente
# =====================================================

# -----------------------------------------------------
# Stage 1: Build with Maven
# -----------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# -----------------------------------------------------
# Stage 2: Runtime image
# -----------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Install bash and other utilities
RUN apk add --no-cache bash curl

# Create app user
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories
RUN mkdir -p /app/uploads/cv /app/logs && \
    chown -R spring:spring /app

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Switch to spring user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
    "-jar", \
    "app.jar"]

# Optional JVM parameters for production
# ENTRYPOINT ["java", \
#     "-Xms512m", \
#     "-Xmx1024m", \
#     "-XX:+UseG1GC", \
#     "-XX:MaxGCPauseMillis=200", \
#     "-Djava.security.egd=file:/dev/./urandom", \
#     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
#     "-jar", \
#     "app.jar"]
