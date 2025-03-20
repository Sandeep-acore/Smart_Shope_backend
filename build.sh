#!/usr/bin/env bash
# exit on error
set -o errexit

# Build the application
./mvnw clean package -DskipTests

# Make the uploads directory
mkdir -p uploads
mkdir -p logs

# Copy the JAR file
cp target/*.jar app.jar

# Make the script executable
chmod +x app.jar

echo "Build completed successfully" 