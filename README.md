# Smart Shop Backend API

A comprehensive e-commerce backend API built with Spring Boot, featuring authentication, product management, order processing, and more.

## Features

- **Authentication & Security**: JWT-based authentication, role-based access control, secure password hashing, OTP-based password reset
- **Product Management**: CRUD operations, image upload, search functionality, discounts & offers
- **Category Management**: CRUD operations, product categorization
- **Order & Payment Management**: Order placement, status updates, payment tracking
- **Cart & Wishlist**: Shopping cart and wishlist functionality
- **Coupons & Offers**: Discount coupons and special offers
- **Auto Invoice Generation**: PDF invoice generation for orders
- **Referral System**: User referrals with discount benefits
- **Search & Filters**: Advanced product search and filtering
- **Multi-Admin Support**: Multiple admin accounts with management capabilities
- **Error Handling & Logging**: Centralized exception handling and comprehensive logging

## Technology Stack

- **Framework**: Spring Boot 2.7.x
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit, Mockito
- **Deployment**: Docker, Render

## Accessing the API

### Deployed API

The API is deployed on Render and can be accessed at:
- Base URL: `https://smart-shope-backend.onrender.com/api`
- Swagger UI: `https://smart-shope-backend.onrender.com/api/swagger-ui.html`
- Health Check: `https://smart-shope-backend.onrender.com/api/health`

### Running Locally

You can run the application locally with the same database configuration as the production environment:

#### Windows:
1. Double-click the `run-local.bat` file
2. The application will start on port 8080 with context path `/api`
3. Access the application at: `http://localhost:8080/api`
4. Swagger UI: `http://localhost:8080/api/swagger-ui.html`

#### Linux/Mac:
1. Make the script executable: `chmod +x run-local.sh`
2. Run the script: `./run-local.sh`
3. Access the application at: `http://localhost:8080/api`
4. Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Key Endpoints

- **Authentication**:
  - Register: `POST /api/auth/register`
  - Login: `POST /api/auth/login`
  - Reset Password: `POST /api/auth/reset-password`

- **Products**:
  - List Products: `GET /api/products`
  - Get Product: `GET /api/products/{id}`
  - Add Product (Admin): `POST /api/products`
  - Update Product (Admin): `PUT /api/products/{id}`

- **Categories**:
  - List Categories: `GET /api/categories`
  - Get Category: `GET /api/categories/{id}`
  - Add Category (Admin): `POST /api/categories`

- **Cart**:
  - View Cart: `GET /api/cart`
  - Add to Cart: `POST /api/cart/add`
  - Update Cart Item: `PUT /api/cart/update`
  - Remove from Cart: `DELETE /api/cart/remove/{id}`

- **Orders**:
  - Place Order: `POST /api/orders`
  - View Orders: `GET /api/orders`
  - Get Order Details: `GET /api/orders/{id}`
  - Update Order Status (Admin): `PUT /api/orders/{id}/status`

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6.x or higher
- PostgreSQL (for production)

### Setup & Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/smart-shop-api.git
   cd smart-shop-api
   ```

2. Configure application properties:
   - Update database credentials in `src/main/resources/application-local.properties`
   - Configure email settings for OTP functionality

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   # Windows
   run-local.bat
   
   # Linux/Mac
   ./run-local.sh
   ```

5. Access the API:
   - Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - Health check: `http://localhost:8080/api/health`

## API Documentation

The API is documented using OpenAPI/Swagger. After starting the application, you can access the documentation at:
`http://localhost:8080/api/swagger-ui.html`

When deployed:
`https://smart-shope-backend.onrender.com/api/swagger-ui.html`

## Testing with Postman

A Postman collection is available in the `postman` directory. Import this collection to test all API endpoints.

**Important**: When using the Postman collection, ensure all endpoint URLs include the `/api` prefix.

### Testing Blocks

The API is developed in modular blocks, each independently testable:

1. **Authentication & Security**
2. **Product Management**
3. **Category Management**
4. **Order & Payment Management**
5. **Cart & Wishlist**
6. **Coupons & Offers**
7. **Auto Invoice Generation**
8. **Referral System**
9. **Search & Filters**
10. **Multi-Admin Support**
11. **Error Handling & Logging**

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Deploying to Render

This application is configured for easy deployment to [Render](https://render.com/).

### Prerequisites

1. A Render account
2. Git repository with your Smart Shop code

### Deployment Steps

1. Fork or clone this repository to your GitHub account
2. Update the repository URL in `render.yaml` to point to your repository
3. Log in to your Render account
4. Click on "New" and select "Blueprint"
5. Connect your GitHub account and select the Smart Shop repository
6. Click "Apply Blueprint"
7. Render will automatically set up the web service and database

### Environment Variables

The following environment variables are configured in the `render.yaml` file:

- `PORT`: The port on which the application runs
- `SPRING_PROFILES_ACTIVE`: Set to `prod` for production
- `JDBC_DATABASE_URL`: PostgreSQL database URL (automatically set by Render)
- `JDBC_DATABASE_USERNAME`: Database username (automatically set by Render)
- `JDBC_DATABASE_PASSWORD`: Database password (automatically set by Render)
- `JWT_SECRET`: Automatically generated secret for JWT tokens
- `TZ`: Timezone set to Asia/Kolkata

### Accessing the API

Once deployed, your API will be available at:

```
https://your-service-name.onrender.com/api
```

The Swagger UI documentation will be available at:

```
https://your-service-name.onrender.com/api/swagger-ui.html
```

### Database Migration

The application is configured to automatically update the database schema using Hibernate's `ddl-auto=update`. This means that the database schema will be created or updated when the application starts.

For more information, visit [Render's documentation](https://render.com/docs). 