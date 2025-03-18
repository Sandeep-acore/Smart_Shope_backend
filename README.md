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
- **Database**: H2 (Development), MySQL (Production)
- **Security**: Spring Security, JWT
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit, Mockito

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6.x or higher
- MySQL (for production)

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
   - Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - H2 Console (dev only): `http://localhost:8080/api/h2-console`

## API Documentation

The API is documented using OpenAPI/Swagger. After starting the application, you can access the documentation at:
`http://localhost:8080/api/swagger-ui.html`

## Testing with Postman

A Postman collection is available in the `postman` directory. Import this collection to test all API endpoints.

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