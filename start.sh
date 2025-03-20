#!/bin/bash
echo "Starting Smart Shop API on Render"

# Ensure folders exist
mkdir -p uploads
mkdir -p logs

# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Check if we need to convert Render PostgreSQL URL to JDBC format
if [[ -n "$RENDER_POSTGRES_URL" && -z "$DATABASE_URL" ]]; then
  # Parse and convert Render's PostgreSQL URL to JDBC format if needed
  echo "Converting Render PostgreSQL URL to JDBC format"
  # Extract components from postgres:// URL
  PGUSER=$(echo $RENDER_POSTGRES_URL | sed -e 's/^postgres:\/\/\([^:]*\):.*$/\1/')
  PGPASS=$(echo $RENDER_POSTGRES_URL | sed -e 's/^postgres:\/\/[^:]*:\([^@]*\)@.*$/\1/')
  PGHOST=$(echo $RENDER_POSTGRES_URL | sed -e 's/^postgres:\/\/[^@]*@\([^:]*\):.*$/\1/')
  PGPORT=$(echo $RENDER_POSTGRES_URL | sed -e 's/^postgres:\/\/[^:]*:[^@]*@[^:]*:\([0-9]*\)\/.*$/\1/')
  PGDATABASE=$(echo $RENDER_POSTGRES_URL | sed -e 's/^postgres:\/\/[^:]*:[^@]*@[^:]*:[0-9]*\/\(.*\)$/\1/')
  
  # Set environment variables for Spring
  export DATABASE_URL="jdbc:postgresql://$PGHOST:$PGPORT/$PGDATABASE"
  export POSTGRES_USER="$PGUSER"
  export POSTGRES_PASSWORD="$PGPASS"
  
  echo "Database connection set up"
fi

# Run the application
java -jar target/*.jar 