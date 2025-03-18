FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/application.properties application.properties
COPY src/main/resources/application-prod.properties application-prod.properties

# Create uploads directory
RUN mkdir -p ./uploads

# Expose the port the app runs on
EXPOSE 8080

# Run the application with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"] 