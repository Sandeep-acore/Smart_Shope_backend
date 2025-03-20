FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Create directory for uploads
RUN mkdir -p /app/uploads
RUN mkdir -p /app/logs

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"] 