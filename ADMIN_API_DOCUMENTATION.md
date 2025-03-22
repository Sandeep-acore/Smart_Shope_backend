# Smart Shop Admin API Documentation

Base URL: `https://smart-shope-backend.onrender.com/api`

This documentation covers all API endpoints available for admin users.

## Authentication

All admin endpoints require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## Admin Authentication

### Register Admin User
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/register`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | Admin's full name |
  | email | string | Yes | Admin's email address |
  | password | string | Yes | Admin's password (min 6 characters) |
  | phone | string | Yes | Admin's phone number |
  | roles | string | Yes | Must include "admin" |
  | profileImage | file | No | Profile image (JPEG, JPG, PNG only) |
- **Response**: JSON containing JWT token and user information
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "Admin User",
    "email": "admin@example.com",
    "phone": "1234567890",
    "profileImage": "profiles/abc123.jpg",
    "roles": ["ROLE_ADMIN"]
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If email is already in use
  - 400 Bad Request: If required fields are missing
  - 400 Bad Request: If image is not JPEG, JPG, or PNG

### Login
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/login`
- **Method**: POST
- **Authorization**: None
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "email": "admin@example.com",
    "password": "admin123"
  }
  ```
- **Response**: JSON containing JWT token and user information
  ```json
  {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "Admin User",
    "email": "admin@example.com",
    "phone": "1234567890",
    "profileImage": "profiles/abc123.jpg",
    "roles": ["ROLE_ADMIN"]
  }
  ```

## Category Management

### Get All Categories
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories`
- **Method**: GET
- **Authorization**: Bearer Token
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
- **Authorization**: Bearer Token
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

### Create Category (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories`
- **Method**: POST
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | Category name (must be unique) |
  | description | string | Yes | Category description |
  | image | file | Yes | Category image (JPEG, JPG, PNG only) |
- **Response**: Created category
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
  - 400 Bad Request: If name, description, or image is missing
  - 400 Bad Request: If category name already exists
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 500 Internal Server Error: For server-side errors

### Update Category (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories/{id}`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | Category name |
  | description | string | Yes | Category description |
  | image | file | Yes | Category image (JPEG, JPG, PNG only) |
- **Response**: Updated category
  ```json
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "imagePath": "categories/electronics.jpg",
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-02T14:30:00"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If name, description, or image is missing
  - 400 Bad Request: If category name already exists
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 404 Not Found: If category ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Delete Category (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/categories/{id}`
- **Method**: DELETE
- **Authorization**: Bearer Token (Admin only)
- **Response**: Success message
  ```json
  {
    "message": "Category deleted successfully!"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If category ID doesn't exist
  - 500 Internal Server Error: For server-side errors

## Product Management

### Get All Products
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: List of products
  ```json
  [
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
  ]
  ```

### Get Product by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/{id}`
- **Method**: GET
- **Authorization**: Bearer Token
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

### Create Product (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products`
- **Method**: POST
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | Product name |
  | description | string | Yes | Product description |
  | price | number | Yes | Product price (positive number) |
  | stockQuantity | integer | Yes | Stock quantity (non-negative integer) |
  | categoryId | integer | Yes | Category ID |
  | discountPercentage | integer | No | Discount percentage (0-100) |
  | image | file | No | Product image (JPEG, JPG, PNG only) |
- **Response**: Created product
  ```json
  {
    "id": 1,
    "name": "Smartphone",
    "description": "High-end smartphone with great features",
    "price": 699.99,
    "discountPercentage": 0,
    "discountedPrice": 699.99,
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
  - 400 Bad Request: If required fields are missing or invalid
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 404 Not Found: If category ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Update Product (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/{id}`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | name | string | Yes | Product name |
  | description | string | Yes | Product description |
  | price | number | Yes | Product price (positive number) |
  | stockQuantity | integer | Yes | Stock quantity (non-negative integer) |
  | categoryId | integer | No | Category ID |
  | discountPercentage | integer | No | Discount percentage (0-100) |
  | image | file | No | Product image (JPEG, JPG, PNG only) |
- **Response**: Updated product
  ```json
  {
    "id": 1,
    "name": "Smartphone",
    "description": "High-end smartphone with great features",
    "price": 699.99,
    "discountPercentage": 10,
    "discountedPrice": 629.99,
    "stockQuantity": 45,
    "imageUrl": "products/smartphone.jpg",
    "category": {
      "id": 1,
      "name": "Electronics"
    },
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-02T14:30:00"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If required fields are missing or invalid
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 400 Bad Request: If discount percentage is not between 0 and 100
  - 404 Not Found: If product ID doesn't exist
  - 404 Not Found: If category ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Delete Product (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/products/{id}`
- **Method**: DELETE
- **Authorization**: Bearer Token (Admin only)
- **Response**: Success message
  ```json
  {
    "message": "Product deleted successfully!"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If product ID doesn't exist
  - 500 Internal Server Error: For server-side errors

## Order Management

### Get All Orders (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders`
- **Method**: GET
- **Authorization**: Bearer Token (Admin only)
- **Response**: List of all orders in the system
  ```json
  [
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
      "user": {
        "id": 2,
        "name": "John Doe",
        "email": "john@example.com"
      },
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "deliveredAt": null
    }
  ]
  ```

### Get Order by ID (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders/{id}`
- **Method**: GET
- **Authorization**: Bearer Token (Admin only)
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
    "user": {
      "id": 2,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00",
    "deliveredAt": null
  }
  ```
- **Error Responses**:
  - 404 Not Found: If order ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Update Order Status (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders/{id}/status`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | status | string | Yes | Order status (PROCESSING, SHIPPED, DELIVERED, CANCELLED) |
- **Response**: Updated order
  ```json
  {
    "id": 1,
    "orderNumber": "ORD-12345",
    "status": "SHIPPED",
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PAID",
    "subtotal": 1259.98,
    "shippingFee": 10.00,
    "discount": 0,
    "tax": 126.00,
    "total": 1395.98,
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
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-02T14:30:00",
    "deliveredAt": null
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If status is invalid
  - 404 Not Found: If order ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Update Payment Status (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders/{id}/payment-status`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | status | string | Yes | Payment status (PENDING, PAID, FAILED, REFUNDED) |
- **Response**: Updated order with new payment status
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
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-02T14:30:00"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If status is invalid
  - 404 Not Found: If order ID doesn't exist
  - 500 Internal Server Error: For server-side errors

## User Management (Admin only)

### Get All Users
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/users`
- **Method**: GET
- **Authorization**: Bearer Token (Admin only)
- **Response**: List of all users
  ```json
  [
    {
      "id": 1,
      "name": "Admin User",
      "email": "admin@example.com",
      "phone": "1234567890",
      "profileImage": "profiles/admin.jpg",
      "roles": ["ROLE_ADMIN"],
      "createdAt": "2023-01-01T12:00:00"
    },
    {
      "id": 2,
      "name": "John Doe",
      "email": "john@example.com",
      "phone": "9876543210",
      "profileImage": "profiles/john.jpg",
      "roles": ["ROLE_USER"],
      "createdAt": "2023-01-02T12:00:00"
    }
  ]
  ```

### Get User by ID
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/users/{id}`
- **Method**: GET
- **Authorization**: Bearer Token (Admin only)
- **Response**: User details
  ```json
  {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "profileImage": "profiles/john.jpg",
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "roles": ["ROLE_USER"],
    "createdAt": "2023-01-02T12:00:00",
    "updatedAt": "2023-01-03T14:30:00"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If user ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Update User Role
- **Full URL**: `https://smart-shope-backend.onrender.com/api/auth/users/{id}/roles`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: application/json
- **Request Body**:
  ```json
  {
    "roles": ["admin", "product_manager"]
  }
  ```
- **Response**: Updated user
  ```json
  {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "roles": ["ROLE_ADMIN", "ROLE_PRODUCT_MANAGER"],
    "message": "User roles updated successfully"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If roles array is empty or contains invalid roles
  - 404 Not Found: If user ID doesn't exist
  - 500 Internal Server Error: For server-side errors

## Offer Management (Admin only)

### Get All Offers
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers`
- **Method**: GET
- **Authorization**: Bearer Token
- **Response**: List of offers
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

### Create Offer (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers`
- **Method**: POST
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | title | string | Yes | Offer title |
  | description | string | Yes | Offer description |
  | discountPercentage | integer | Yes | Discount percentage (1-100) |
  | validFrom | string | Yes | Valid from date (ISO format) |
  | validUntil | string | Yes | Valid until date (ISO format) |
  | bannerImage | file | Yes | Offer banner image (JPEG, JPG, PNG only) |
  | active | boolean | No | Whether the offer is active (default: true) |
- **Response**: Created offer
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
  - 400 Bad Request: If required fields are missing or invalid
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 400 Bad Request: If discount percentage is not between 1 and 100
  - 500 Internal Server Error: For server-side errors

### Update Offer (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers/{id}`
- **Method**: PUT
- **Authorization**: Bearer Token (Admin only)
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | title | string | Yes | Offer title |
  | description | string | Yes | Offer description |
  | discountPercentage | integer | Yes | Discount percentage (1-100) |
  | validFrom | string | Yes | Valid from date (ISO format) |
  | validUntil | string | Yes | Valid until date (ISO format) |
  | bannerImage | file | No | Offer banner image (JPEG, JPG, PNG only) |
  | active | boolean | No | Whether the offer is active |
- **Response**: Updated offer
  ```json
  {
    "id": 1,
    "title": "Extended Summer Sale",
    "description": "Get 25% off on all summer products",
    "discountPercentage": 25,
    "validFrom": "2023-06-01T00:00:00",
    "validUntil": "2023-09-30T23:59:59",
    "bannerImage": "offers/summer-sale.jpg",
    "active": true,
    "createdAt": "2023-05-15T12:00:00",
    "updatedAt": "2023-06-01T10:30:00"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: If required fields are missing or invalid
  - 400 Bad Request: If image is not JPEG, JPG, or PNG
  - 400 Bad Request: If discount percentage is not between 1 and 100
  - 404 Not Found: If offer ID doesn't exist
  - 500 Internal Server Error: For server-side errors

### Delete Offer (Admin only)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/offers/{id}`
- **Method**: DELETE
- **Authorization**: Bearer Token (Admin only)
- **Response**: Success message
  ```json
  {
    "message": "Offer deleted successfully!"
  }
  ```
- **Error Responses**:
  - 404 Not Found: If offer ID doesn't exist
  - 500 Internal Server Error: For server-side errors

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

### Create Order (Admin or User)
- **Full URL**: `https://smart-shope-backend.onrender.com/api/orders`
- **Method**: POST
- **Authorization**: Bearer Token (Admin or User)
- **Content-Type**: application/x-www-form-urlencoded
- **Request Parameters**:
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | productIds | string | Yes | Comma-separated list of product IDs |
  | quantities | string | Yes | Comma-separated list of quantities (must match productIds in order) |
  | useProfileAddress | boolean | No | Whether to use the user's profile address for delivery (default: true) |
  | paymentMethod | string | Yes | Payment method (CREDIT_CARD, PAYPAL, CASH_ON_DELIVERY) |
  | notes | string | No | Order notes or special instructions |
  | deliveryAddressLine1 | string | No* | Delivery address line 1 (*required if useProfileAddress is false) |
  | deliveryAddressLine2 | string | No | Delivery address line 2 |
  | deliveryCity | string | No* | Delivery city (*required if useProfileAddress is false) |
  | deliveryState | string | No* | Delivery state/province (*required if useProfileAddress is false) |
  | deliveryPostalCode | string | No* | Delivery postal/ZIP code (*required if useProfileAddress is false) |
  | deliveryCountry | string | No* | Delivery country (*required if useProfileAddress is false) |
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
    "user": {
      "id": 2,
      "name": "John Doe",
      "email": "john@example.com"
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