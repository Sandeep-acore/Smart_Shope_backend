#!/bin/bash

# Health check script for Render

# Set variables
API_URL="http://localhost:${PORT:-10000}/api/health"
MAX_ATTEMPTS=10
SLEEP_INTERVAL=3

# Check API health
echo "Checking API health at $API_URL"

attempt=1
while [ $attempt -le $MAX_ATTEMPTS ]; do
    echo "Attempt $attempt of $MAX_ATTEMPTS"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" $API_URL)
    
    if [ "$response" == "200" ]; then
        echo "API is healthy! (HTTP $response)"
        exit 0
    else
        echo "API not yet healthy. Response: HTTP $response"
        
        if [ $attempt -lt $MAX_ATTEMPTS ]; then
            echo "Retrying in $SLEEP_INTERVAL seconds..."
            sleep $SLEEP_INTERVAL
        fi
    fi
    
    attempt=$((attempt+1))
done

echo "Health check failed after $MAX_ATTEMPTS attempts"
exit 1 