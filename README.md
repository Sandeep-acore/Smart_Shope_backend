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
   - Update database credentials in `src/main/resources/application.properties`
   - Configure email settings for OTP functionality

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn spring-boot:run
   ```

5. Access the API:
   - Base URL: `http://localhost:10000/api`
   - Swagger UI: `http://localhost:10000/api/swagger-ui.html`
   - Health check: `http://localhost:10000/api/health`

## API Documentation

The API is documented using OpenAPI/Swagger. After starting the application, you can access the documentation at:
`http://localhost:10000/api/swagger-ui.html`

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