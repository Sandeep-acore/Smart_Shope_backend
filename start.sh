#!/bin/bash
echo "Starting Smart Shop API on Render"

# Ensure folders exist
mkdir -p uploads
mkdir -p logs

# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Run the application
java -jar target/*.jar 