FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Create directory for uploads
RUN mkdir -p uploads
RUN mkdir -p logs

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 