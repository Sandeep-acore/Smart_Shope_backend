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

# Create prod properties file with hardcoded values for Render deployment
RUN echo "# Automatically generated production properties\n\
# Database Configuration\n\
spring.datasource.url=jdbc:postgresql://smartshop-sandeep-e8db.h.aivencloud.com:24114/defaultdb?sslmode=require\n\
spring.datasource.username=avnadmin\n\
spring.datasource.password=AVNS_ZhDZPpZQMeC6uyk9TnV\n\
spring.datasource.driver-class-name=org.postgresql.Driver\n\
\n\
# Connection Pool Settings\n\
spring.datasource.hikari.connection-timeout=30000\n\
spring.datasource.hikari.maximum-pool-size=5\n\
spring.datasource.hikari.minimum-idle=2\n\
spring.datasource.hikari.idle-timeout=300000\n\
spring.datasource.hikari.max-lifetime=1800000\n\
spring.datasource.hikari.validation-timeout=20000\n\
spring.datasource.hikari.connection-test-query=SELECT 1\n\
spring.datasource.hikari.auto-commit=true\n\
spring.datasource.testWhileIdle=true\n\
spring.datasource.testOnBorrow=true\n\
\n\
# JPA/Hibernate Configuration\n\
spring.jpa.hibernate.ddl-auto=update\n\
spring.jpa.show-sql=false\n\
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect\n\
spring.jpa.properties.hibernate.format_sql=false\n\
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=false\n\
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true\n\
spring.jpa.open-in-view=false\n\
spring.jpa.properties.hibernate.transaction.jta.platform=org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform\n\
\n\
# Transaction Management\n\
spring.transaction.default-timeout=180\n\
\n\
# Server Configuration\n\
server.port=\${PORT:8080}\n\
server.servlet.context-path=/api\n\
\n\
# JWT Configuration\n\
jwt.secret=9CK3eMF4MZ2nhiGtJ9VJgMTJ2QuK2ZJY\n\
jwt.expiration=86400000\n\
jwt.expiration.ms=86400000\n\
\n\
# Debug and Logging Configuration\n\
debug=false\n\
logging.level.root=INFO\n\
logging.level.org.springframework=INFO\n\
logging.level.com.smartshop.api=INFO\n\
logging.level.org.hibernate=INFO\n\
logging.level.com.zaxxer.hikari=INFO\n\
\n\
# Database Initializer Control\n\
spring.datasource.initialization-mode=never\n\
spring.jpa.defer-datasource-initialization=false\n\
spring.sql.init.mode=never\n\
\n\
# File Upload Configuration\n\
spring.servlet.multipart.max-file-size=10MB\n\
spring.servlet.multipart.max-request-size=10MB\n\
file.upload-dir=./uploads\n\
\n\
# Error Handling\n\
server.error.include-message=always\n\
server.error.include-binding-errors=always\n\
server.error.include-stacktrace=always\n\
server.error.include-exception=true\n\
" > application-prod.properties

# Create uploads directory
RUN mkdir -p ./uploads

# Expose the port the app runs on
EXPOSE 8080

# Run the application with production profile and appropriate memory settings
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "-Dspring.profiles.active=prod", "app.jar"] 