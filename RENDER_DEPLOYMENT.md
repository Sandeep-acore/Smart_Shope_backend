# Smart Shop API Deployment to Render

This document explains how the Smart Shop API has been prepared for deployment to Render.

## Changes Made

### 1. Enhanced Error Handling

- Added better validation messages for multipart form data
- Added proper error responses for missing required fields
- Updated the `GlobalExceptionHandler` to handle missing form fields and files

### 2. Production Configuration

- Updated `application-prod.properties` to use environment variables for database credentials
- Set appropriate logging levels for production
- Secured error responses by hiding stack traces

### 3. Deployment Files

- Created `Dockerfile` for containerization
- Created `render.yaml` for Render Blueprint deployment
- Added `build.sh` script for Render deployment process

### 4. Improved Validation

- Added field validation in `ProductController`
- Added image type validation for uploads
- Added proper error messages with field names

## How to Deploy

1. Push your code to GitHub
2. Connect your Render account to GitHub
3. Create a new Web Service in Render
4. Select your repository
5. Use these settings:
   - Build Command: `./build.sh`
   - Start Command: `java -jar app.jar`
   - Environment: Docker
   - Plan: Free (or choose a plan based on your needs)

6. Add the following environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `PORT=8080`
   - `JDBC_DATABASE_URL` (your PostgreSQL connection string)
   - `JDBC_DATABASE_USERNAME` (your database username)
   - `JDBC_DATABASE_PASSWORD` (your database password)
   - `JWT_SECRET` (a secure random string)

7. Click "Create Web Service"

## Testing in Postman

When testing API endpoints in Postman, make sure to:

1. For file uploads:
   - Use form-data
   - Set the type to "File" for image fields
   - Include all required fields

2. For authentication:
   - Include the JWT token in the Authorization header
   - Format: `Bearer your_token_here`

3. For validation errors:
   - Check the response body for detailed error messages
   - Look for the specific field that failed validation

## Database Migration

The application is configured to automatically update the database schema using Hibernate's `ddl-auto=update`. This means that the database schema will be created or updated when the application starts. 