# Smart Shop API Deployment to Render

This document provides instructions for deploying the Smart Shop API to Render.

## Deployment Steps

1. **Push your code to a Git repository** (GitHub, GitLab, or Bitbucket)

2. **Set up a Render account**
   - Sign up at https://render.com/ if you haven't already

3. **Create a new Web Service**
   - Click "New +" and select "Web Service"
   - Connect to your Git repository
   - Select the repository containing your Smart Shop API code
   - Give your service a name (e.g., "smart-shop-api")
   - Set the Environment to "Docker"
   - Click "Create Web Service"

4. **Configure Environment Variables**
   - Under the "Environment" section, add the following environment variables:
     ```
     SPRING_PROFILES_ACTIVE=prod
     PORT=10000
     AIVEN_DB_HOST=smartshop-sandeep-e8db.h.aivencloud.com
     AIVEN_DB_PORT=24114
     AIVEN_DB_NAME=defaultdb
     AIVEN_DB_USERNAME=avnadmin
     AIVEN_DB_PASSWORD=AVNS_ZhDZPpZQMeC6uyk9TnV
     JWT_SECRET=your_jwt_secret_or_leave_empty_for_auto_generation
     ```

5. **Deploy the Service**
   - Render will automatically deploy your service based on the configuration in your repository
   - The deployment process includes building the application and running it according to the Dockerfile

## Files for Render Deployment

The repository contains the following files required for Render deployment:

1. **Dockerfile**
   - Defines how to build and run the application in a Docker container

2. **render.yaml**
   - Contains configuration for Render services
   - Specifies build and start commands
   - Defines environment variables

3. **system.properties**
   - Specifies Java version requirements (Java 11)

4. **application-prod.properties**
   - Production configuration that uses environment variables
   - Database connection settings specifically for Aiven PostgreSQL
   - Logging and error handling configuration

## Monitoring and Troubleshooting

1. **View Logs**
   - In the Render dashboard, go to your web service
   - Click on the "Logs" tab to view application logs

2. **Check Service Status**
   - The "Events" tab shows deployment events and status

3. **Test Endpoints**
   - Once deployed, your API will be available at:
     `https://your-service-name.onrender.com/api`
   - Swagger UI: `https://your-service-name.onrender.com/api/swagger-ui.html`

## Database Migration

The application uses Hibernate's `ddl-auto=update` setting, which will automatically:
1. Create the necessary tables if they don't exist
2. Update the schema based on entity changes

No manual database migration is needed for initial deployment.

## Troubleshooting Common Issues

1. **Application Fails to Start**
   - Check logs for errors
   - Verify environment variables are set correctly
   - Ensure database connectivity

2. **Database Connection Issues**
   - Verify Aiven database credentials
   - Check if your Aiven PostgreSQL allows connections from Render IP addresses
   - Ensure proper SSL settings are configured (sslmode=require)
   - The URL format should be: `jdbc:postgresql://hostname:port/database?sslmode=require`

3. **Out of Memory Errors**
   - Upgrade to a higher tier Render plan for more memory
   - Optimize application memory usage in application-prod.properties 