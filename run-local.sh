#!/bin/bash

echo "Starting Smart Shop Backend in local mode..."
echo

# Set the active profile to local
export SPRING_PROFILES_ACTIVE=local

# Run the application
mvn spring-boot:run 