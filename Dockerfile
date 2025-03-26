FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app

# Copy only the POM file first to cache dependencies
COPY pom.xml .
# Download dependencies in a separate layer
RUN mvn dependency:go-offline

# Copy the source code
COPY src/ ./src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
WORKDIR /app

# Install PostgreSQL client for potential troubleshooting
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Create necessary directories
RUN mkdir -p /tmp/uploads
RUN mkdir -p /var/log

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=10000
ENV TZ=UTC

# Expose the port
EXPOSE 10000

# Run the application with Render's environment variables
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"] 