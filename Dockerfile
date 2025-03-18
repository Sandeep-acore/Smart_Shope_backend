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

# Create prod properties file if it doesn't exist
RUN echo "# Automatically generated production properties\n\
# Database Configuration\n\
spring.datasource.url=jdbc:postgresql://dpg-cvcklot2ng1s738vea30-a.oregon-postgres.render.com:5452/smart_shope?sslmode=require\n\
spring.datasource.username=smart_shope_user\n\
spring.datasource.password=5NRBpEElN781d2U22J1lMUocKVukqBm3\n\
spring.datasource.driver-class-name=org.postgresql.Driver\n\
\n\
# Connection Pool Settings\n\
spring.datasource.hikari.connection-timeout=30000\n\
spring.datasource.hikari.maximum-pool-size=10\n\
spring.datasource.hikari.minimum-idle=5\n\
spring.datasource.hikari.idle-timeout=300000\n\
spring.datasource.hikari.max-lifetime=1200000\n\
\n\
# JPA/Hibernate Configuration\n\
spring.jpa.hibernate.ddl-auto=update\n\
spring.jpa.show-sql=false\n\
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect\n\
spring.jpa.properties.hibernate.format_sql=false\n\
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true\n\
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true\n\
\n\
# Server Configuration\n\
server.port=\${PORT:8080}\n\
server.servlet.context-path=/api\n\
\n\
# JWT Configuration\n\
jwt.expiration.ms=86400000\n\
" > application-prod.properties

# Create uploads directory
RUN mkdir -p ./uploads

# Expose the port the app runs on
EXPOSE 8080

# Run the application with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"] 