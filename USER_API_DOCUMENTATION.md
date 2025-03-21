# Smart Shop User API Documentation

Base URL: `https://smart-shope-backend.onrender.com/api`

This documentation covers all API endpoints available for regular users.

## Authentication

Most user endpoints require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## User Authentication

### Register
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/register`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | User's full name |
  | email | string | Yes | User's email address |
  | password | string | Yes | User's password (min 6 characters) |
  | phone | string | Yes | User's phone number |
  | profileImage | file | No | Profile image (JPEG, JPG, PNG only) |
- **Response**: JSON containing JWT token and user information
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "profileImage": "profiles/default.jpg",
    "roles": ["ROLE_USER"]
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If email is already in use
  - 400 Bad Request: If required fields are missing
  - 400 Bad Request: If email format is invalid
  - 400 Bad Request: If password is too weak (less than 6 characters)
  - 400 Bad Request: If image is not JPEG, JPG, or PNG

### Login
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/login`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "email": "john@example.com",
    "password": "Password123"
  }
  ```
- **Response**: JSON containing JWT token and user information
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "profileImage": "profiles/default.jpg",
    "roles": ["ROLE_USER"]
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If credentials are invalid

### Get User Profile
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/profile`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: User profile details
  ```json
  {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "profileImage": "profiles/default.jpg",
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "roles": ["ROLE_USER"],
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid
  - 404 Not Found: If user profile not found

### Update Profile
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/profile`
- **Method**: PUT
- **Authorization**: Bearer Token
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | No | User's full name |
  | phone | string | No | User's phone number |
  | addressLine1 | string | No | Address line 1 |
  | addressLine2 | string | No | Address line 2 |
  | city | string | No | City |
  | state | string | No | State/Province |
  | postalCode | string | No | Postal/ZIP code |
  | country | string | No | Country |
  | profileImage | file | No | Profile image (JPEG, JPG, PNG only) |
- **Response**: Updated user profile
  ```json
  {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "profileImage": "profiles/john.jpg",
    "addressLine1": "456 Park Ave",
    "addressLine2": "Suite 789",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "roles": ["ROLE_USER"],
    "message": "Profile updated successfully"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 401 Unauthorized: If token is missing or invalid
  - 500 Internal Server Error: For server-side errors

### Change Password
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/password`
- **Method**: PUT
- **Authorization**: Bearer Token
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "currentPassword": "Password123",
    "newPassword": "NewPassword123"
  }
  ```
- **Response**: Success message
  ```json
  {
    "message": "Password changed successfully!"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If current password is incorrect
  - 400 Bad Request: If new password is too weak (less than 6 characters)
  - 401 Unauthorized: If token is missing or invalid

### Forgot Password
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/forgot-password`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "email": "john@example.com"
  }
  ```
- **Response**: Success message
  ```json
  {
    "message": "If your email is registered, we have sent a password reset link."
  }
  ```

### Reset Password
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/reset-password`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "token": "password-reset-token-from-email",
    "password": "NewPassword123"
  }
  ```
- **Response**: Success message
  ```json
  {
    "message": "Password has been reset successfully!"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If token is invalid or expired
  - 400 Bad Request: If password is too weak (less than 6 characters)

## Browse Products

### Get All Categories
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Response**: List of categories
  ```json
  [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and gadgets",
      "imagePath": "categories/electronics.jpg",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00"
    }
  ]
  ```

### Get Category by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories/{id}`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Response**: Category details
  ```json
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "imagePath": "categories/electronics.jpg",
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If category ID doesn't exist

### Get All Products
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Query Parameters** (all optional):
  | Parameter | Type | Description |
  |-----------|------|-------------|
  | page | integer | Page number (default: 0) |
  | size | integer | Page size (default: 10) |
  | sortBy | string | Field to sort by (default: "id") |
  | sortDir | string | Sort direction ("asc" or "desc", default: "asc") |
- **Response**: List of products with pagination
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Smartphone",
        "description": "High-end smartphone with great features",
        "price": 699.99,
        "discountPercentage": 10,
        "discountedPrice": 629.99,
        "stockQuantity": 50,
        "imageUrl": "products/smartphone.jpg",
        "category": {
          "id": 1,
          "name": "Electronics"
        },
        "createdAt": "2023-01-01T12:00:00",
        "updatedAt": "2023-01-01T12:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 35,
    "totalPages": 4,
    "lastPage": false
  }
  ```

### Get Product by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/{id}`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Response**: Product details
  ```json
  {
    "id": 1,
    "name": "Smartphone",
    "description": "High-end smartphone with great features",
    "price": 699.99,
    "discountPercentage": 10,
    "discountedPrice": 629.99,
    "stockQuantity": 50,
    "imageUrl": "products/smartphone.jpg",
    "category": {
      "id": 1,
      "name": "Electronics"
    },
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If product ID doesn't exist

### Search Products
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/search`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Query Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | query | string | Yes | Search term |
  | page | integer | No | Page number (default: 0) |
  | size | integer | No | Page size (default: 10) |
  | sortBy | string | No | Field to sort by (default: "id") |
  | sortDir | string | No | Sort direction ("asc" or "desc", default: "asc") |
- **Response**: List of products matching the search query with pagination
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Smartphone",
        "description": "High-end smartphone with great features",
        "price": 699.99,
        "discountPercentage": 10,
        "discountedPrice": 629.99,
        "stockQuantity": 50,
        "imageUrl": "products/smartphone.jpg",
        "category": {
          "id": 1,
          "name": "Electronics"
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1,
    "lastPage": true
  }
  ```

### Filter Products
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/filter`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Query Parameters** (all optional):
  | Parameter | Type | Description |
  |-----------|------|-------------|
  | categoryId | integer | Filter by category ID |
  | minPrice | number | Minimum price |
  | maxPrice | number | Maximum price |
  | inStock | boolean | Filter for in-stock products only |
  | onSale | boolean | Filter for products with discounts only |
  | page | integer | Page number (default: 0) |
  | size | integer | Page size (default: 10) |
  | sortBy | string | Field to sort by (default: "id") |
  | sortDir | string | Sort direction ("asc" or "desc", default: "asc") |
- **Response**: List of filtered products with pagination
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Smartphone",
        "description": "High-end smartphone with great features",
        "price": 699.99,
        "discountPercentage": 10,
        "discountedPrice": 629.99,
        "stockQuantity": 50,
        "imageUrl": "products/smartphone.jpg",
        "category": {
          "id": 1,
          "name": "Electronics"
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 8,
    "totalPages": 1,
    "lastPage": true
  }
  ```

## Cart Management

### Get User Cart
- **Full URL**: `https://smart-shope-backend.onrender.com/api/cart`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: User's cart details
  ```json
  {
    "id": 1,
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Smartphone",
          "price": 699.99,
          "discountPercentage": 10,
          "discountedPrice": 629.99,
          "imageUrl": "products/smartphone.jpg",
          "stockQuantity": 50
        },
        "quantity": 2,
        "totalPrice": 1259.98
      }
    ],
    "totalItems": 2,
    "totalPrice": 1259.98
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid

### Add Item to Cart
- **Full URL**: `https://smart-shope-backend.onrender.com/api/cart/items`
- **Method**: POST
- **Authorization**: Bearer Token
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | productId | integer | Yes | Product ID to add to cart |
  | quantity | integer | Yes | Quantity to add (minimum 1) |
- **Response**: Updated cart details
  ```json
  {
    "id": 1,
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Smartphone",
          "price": 699.99,
          "discountPercentage": 10,
          "discountedPrice": 629.99,
          "imageUrl": "products/smartphone.jpg",
          "stockQuantity": 50
        },
        "quantity": 2,
        "totalPrice": 1259.98
      }
    ],
    "totalItems": 2,
    "totalPrice": 1259.98
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If product ID doesn't exist
  - 400 Bad Request: If quantity is less than 1
  - 400 Bad Request: If requested quantity exceeds available stock
  - 401 Unauthorized: If token is missing or invalid

### Update Cart Item
- **Full URL**: `https://smart-shope-backend.onrender.com/api/cart/items/{itemId}`
- **Method**: PUT
- **Authorization**: Bearer Token
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | productId | integer | Yes | Product ID |
  | quantity | integer | Yes | New quantity for the cart item |
- **Response**: Updated cart details
  ```json
  {
    "id": 1,
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Smartphone",
          "price": 699.99,
          "discountPercentage": 10,
          "discountedPrice": 629.99,
          "imageUrl": "products/smartphone.jpg",
          "stockQuantity": 50
        },
        "quantity": 3,
        "totalPrice": 1889.97
      }
    ],
    "totalItems": 3,
    "totalPrice": 1889.97
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If quantity is less than 1
  - 400 Bad Request: If requested quantity exceeds available stock
  - 400 Bad Request: If product ID doesn't exist
  - 401 Unauthorized: If token is missing or invalid
  - 404 Not Found: If cart item ID doesn't exist

### Remove Item from Cart
- **Full URL**: `https://smart-shope-backend.onrender.com/api/cart/items/{itemId}`
- **Method**: DELETE
- **Authorization**: Bearer Token
- **Response**: Updated cart details
  ```json
  {
    "id": 1,
    "items": [],
    "totalItems": 0,
    "totalPrice": 0.00
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid
  - 404 Not Found: If cart item ID doesn't exist

### Clear Cart
- **Full URL**: `https://smart-shope-backend.onrender.com/api/cart/clear`
- **Method**: DELETE
- **Authorization**: Bearer Token
- **Response**: Empty cart details
  ```json
  {
    "id": 1,
    "items": [],
    "totalItems": 0,
    "totalPrice": 0.00
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid

## Wishlist Management

### Get User Wishlist
- **Full URL**: `https://smart-shope-backend.onrender.com/api/wishlist`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: User's wishlist details
  ```json
  {
    "id": 1,
    "products": [
      {
        "id": 1,
        "name": "Smartphone",
        "price": 699.99,
        "discountPercentage": 10,
        "discountedPrice": 629.99,
        "imageUrl": "products/smartphone.jpg",
        "stockQuantity": 50
      }
    ],
    "totalItems": 1
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid

### Add Product to Wishlist
- **Full URL**: `https://smart-shope-backend.onrender.com/api/wishlist/products/{productId}`
- **Method**: POST
- **Authorization**: Bearer Token
- **Response**: Updated wishlist details
  ```json
  {
    "id": 1,
    "products": [
      {
        "id": 1,
        "name": "Smartphone",
        "price": 699.99,
        "discountPercentage": 10,
        "discountedPrice": 629.99,
        "imageUrl": "products/smartphone.jpg",
        "stockQuantity": 50
      }
    ],
    "totalItems": 1,
    "message": "Product added to wishlist"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If product already exists in wishlist
  - 401 Unauthorized: If token is missing or invalid
  - 404 Not Found: If product ID doesn't exist

### Remove Product from Wishlist
- **Full URL**: `https://smart-shope-backend.onrender.com/api/wishlist/products/{productId}`
- **Method**: DELETE
- **Authorization**: Bearer Token
- **Response**: Updated wishlist details
  ```json
  {
    "id": 1,
    "products": [],
    "totalItems": 0,
    "message": "Product removed from wishlist"
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid
  - 404 Not Found: If product ID doesn't exist in wishlist

### Clear Wishlist
- **Full URL**: `https://smart-shope-backend.onrender.com/api/wishlist/clear`
- **Method**: DELETE
- **Authorization**: Bearer Token
- **Response**: Empty wishlist details
  ```json
  {
    "id": 1,
    "products": [],
    "totalItems": 0,
    "message": "Wishlist cleared"
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid

## Order Management

### Create Order
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders`
- **Method**: POST
- **Authorization**: Bearer Token
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | productIds | array | Yes | Array of product IDs |
  | quantities | array | Yes | Array of quantities (must match productIds in order) |
  | useProfileAddress | boolean | No | Whether to use the user's profile address for delivery (default: false) |
  | paymentMethod | string | Yes | Payment method (CREDIT_CARD, PAYPAL, CASH_ON_DELIVERY) |
  | notes | string | No | Order notes or special instructions |
  | deliveryAddressLine1 | string | Yes* | Delivery address line 1 (*required if useProfileAddress is false) |
  | deliveryAddressLine2 | string | No | Delivery address line 2 |
  | deliveryCity | string | Yes* | Delivery city (*required if useProfileAddress is false) |
  | deliveryState | string | Yes* | Delivery state/province (*required if useProfileAddress is false) |
  | deliveryPostalCode | string | Yes* | Delivery postal/ZIP code (*required if useProfileAddress is false) |
  | deliveryCountry | string | Yes* | Delivery country (*required if useProfileAddress is false) |
- **Response**: Created order details
  ```json
  {
    "id": 1,
    "orderNumber": "ORD-12345",
    "status": "PENDING",
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "subtotal": 1259.98,
    "shippingFee": 10.00,
    "discount": 0,
    "tax": 126.00,
    "total": 1395.98,
    "notes": "Please deliver after 6 PM",
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Smartphone",
          "imageUrl": "products/smartphone.jpg"
        },
        "quantity": 2,
        "price": 629.99,
        "subtotal": 1259.98
      }
    ],
    "shippingAddress": {
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "deliveryAddress": {
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "createdAt": "2023-01-01T12:00:00"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If required products or quantities are missing
  - 400 Bad Request: If product IDs don't exist
  - 400 Bad Request: If quantities are less than 1
  - 400 Bad Request: If delivery address is incomplete (when useProfileAddress is false)
  - 400 Bad Request: If payment method is invalid
  - 401 Unauthorized: If token is missing or invalid
  - 500 Internal Server Error: For server-side errors

### Get User Orders
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: List of user's orders
  ```json
  [
    {
      "id": 1,
      "orderNumber": "ORD-12345",
      "status": "PROCESSING",
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PAID",
      "total": 1395.98,
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T14:30:00",
      "items": [
        {
          "id": 1,
          "product": {
            "id": 1,
            "name": "Smartphone",
            "imageUrl": "products/smartphone.jpg"
          },
          "quantity": 2,
          "price": 629.99,
          "subtotal": 1259.98
        }
      ]
    }
  ]
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid

### Get Order by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders/{id}`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: Order details
  ```json
  {
    "id": 1,
    "orderNumber": "ORD-12345",
    "status": "PROCESSING",
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PAID",
    "subtotal": 1259.98,
    "shippingFee": 10.00,
    "discount": 0,
    "tax": 126.00,
    "total": 1395.98,
    "notes": "Please deliver after 6 PM",
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Smartphone",
          "imageUrl": "products/smartphone.jpg"
        },
        "quantity": 2,
        "price": 629.99,
        "subtotal": 1259.98
      }
    ],
    "shippingAddress": {
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "deliveryAddress": {
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T14:30:00"
  }
  ```
- **Error Responses**:
  - 401 Unauthorized: If token is missing or invalid
  - 403 Forbidden: If order does not belong to the user
  - 404 Not Found: If order ID doesn't exist

## Special Offers

### Get Active Offers
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Response**: List of active offers
  ```json
  [
    {
      "id": 1,
      "title": "Summer Sale",
      "description": "Get 20% off on all summer products",
      "discountPercentage": 20,
      "validFrom": "2023-06-01T00:00:00",
      "validUntil": "2023-08-31T23:59:59",
      "bannerImage": "offers/summer-sale.jpg",
      "active": true,
      "createdAt": "2023-05-15T12:00:00",
      "updatedAt": "2023-05-15T12:00:00"
    }
  ]
  ```

### Get Offer by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers/{id}`
- **Method**: GET
- **Authorization**: Optional Bearer Token
- **Response**: Offer details
  ```json
  {
    "id": 1,
    "title": "Summer Sale",
    "description": "Get 20% off on all summer products",
    "discountPercentage": 20,
    "validFrom": "2023-06-01T00:00:00",
    "validUntil": "2023-08-31T23:59:59",
    "bannerImage": "offers/summer-sale.jpg",
    "active": true,
    "createdAt": "2023-05-15T12:00:00",
    "updatedAt": "2023-05-15T12:00:00"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If offer ID doesn't exist

## File Access

### Get File
- **Full URL**: `https://smart-shope-backend.onrender.com/api/files/{filename}`
- **Method**: GET
- **Authorization**: None
- **Response**: File content (image, PDF, etc.)
- **Error Responses**:
  - 404 Not Found: If file doesn't exist

## Error Responses

All API endpoints return appropriate HTTP status codes:

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

Error responses follow this format:
```json
{
  "message": "Error description"
}
```

For validation errors:
```json
{
  "field1": "Error for field1",
  "field2": "Error for field2"
}
``` 