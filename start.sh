#!/bin/bash
echo "Starting Smart Shop API on Render"

# Ensure folders exist
mkdir -p uploads
mkdir -p logs

# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Run the application with memory settings optimized for Render
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar target/*.jar 