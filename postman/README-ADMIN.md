# Smart Shop API - Admin Guide

This guide covers all administrative operations available in the Smart Shop API.

## Authentication

### Register an Admin

- **URL**: `POST http://localhost:8080/api/auth/register-json`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - name: Admin User
  - email: admin@example.com
  - password: admin123
  - phone: 9876543210
  - roles: admin
- **Response**: JWT token and user details with admin role.

### Login as Admin

- **URL**: `POST http://localhost:8080/api/auth/login`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - email: admin@example.com
  - password: admin123
- **Response**: JWT token and user details with admin role.

## Category Management (Admin Only)

### Create Category with Image

- **URL**: `POST http://localhost:8080/api/categories`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Electronics (required)
  - description: Electronic devices and gadgets (required)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Created category details.

### Update Category with Image

- **URL**: `PUT http://localhost:8080/api/categories/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Electronics & Gadgets (required)
  - description: Updated description for electronics category (required)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Updated category details.

### Delete Category

- **URL**: `DELETE http://localhost:8080/api/categories/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

## Product Management (Admin Only)

### Create Product

- **URL**: `POST http://localhost:8080/api/products`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Smartphone X (required)
  - description: Latest smartphone with advanced features (required)
  - price: 999.99 (required)
  - stockQuantity: 100 (required)
  - categoryId: 1 (required)
  - image: (file upload - JPEG, JPG, or PNG only) (optional)
- **Response**: Created product details.

### Update Product

- **URL**: `PUT http://localhost:8080/api/products/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Smartphone X Pro (required)
  - description: Updated description with new features (required)
  - price: 1099.99 (required)
  - stockQuantity: 50 (required)
  - categoryId: 1 (optional)
  - discountPercentage: 10 (optional, defaults to 0)
  - image: (file upload - JPEG, JPG, or PNG only) (optional)
- **Response**: Updated product details.
```json
{
  "id": 1,
  "name": "Smartphone X Pro",
  "description": "Updated description with new features",
  "price": 1099.99,
  "stockQuantity": 50,
  "imageUrl": "products/59eb642b-248b-49f4-97a2-34ae7f26c9c1.png",
  "discountPercentage": 10,
  "category": {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "imagePath": "categories/a1b2c3d4-5678-90ab-cdef-ghijklmnopqr.jpg",
    "createdAt": "2023-07-15T10:30:45",
    "updatedAt": "2023-07-15T10:30:45"
  },
  "createdAt": "2023-07-15T10:30:45",
  "updatedAt": "2023-07-15T11:15:30",
  "discountedPrice": 989.99
}
```

### Delete Product

- **URL**: `DELETE http://localhost:8080/api/products/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

## Order Management (Admin Only)

### Get All Orders

- **URL**: `GET http://localhost:8080/api/orders`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: List of all orders in the system.

### Update Order Status

- **URL**: `PUT http://localhost:8080/api/orders/{id}/status`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
  - Content-Type: application/x-www-form-urlencoded
- **Body** (x-www-form-urlencoded):
  - status: PROCESSING (or SHIPPED, DELIVERED, CANCELLED)
- **Response**: Updated order details.
- **Note**: If status is set to DELIVERED, the deliveredAt timestamp will be automatically set.

### Update Payment Status

- **URL**: `PUT http://localhost:8080/api/orders/{id}/payment-status`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
  - Content-Type: application/x-www-form-urlencoded
- **Body** (x-www-form-urlencoded):
  - status: PAID (or PENDING, FAILED, REFUNDED)
  - transactionId: TXN123456 (optional)
- **Response**: Updated order details.

## Offer Management (Admin Only)

### Create Offer

- **URL**: `POST http://localhost:8080/api/offers`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - title: Summer Sale (required)
  - description: Get 20% off on all summer products (optional)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Created offer details.

### Update Offer

- **URL**: `PUT http://localhost:8080/api/offers/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - title: Summer Sale Extended (required)
  - description: Updated description for the offer (optional)
  - image: (file upload - JPEG, JPG, or PNG only) (optional)
  - active: true/false (optional)
- **Response**: Updated offer details.

### Delete Offer

- **URL**: `DELETE http://localhost:8080/api/offers/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

## Testing Tips for Admins

1. **Authentication**: Always include the JWT token in the Authorization header for all admin operations.
2. **File Uploads**: When uploading images, ensure they are in JPEG, JPG, or PNG format.
3. **Order Management**: You can view all orders in the system and update their status.
4. **Validation**: The API includes validation for all inputs. Check error messages for guidance.

## Data Validation Rules

- **Name**: Must be between 3 and 50 characters
- **Email**: Must be a valid email format
- **Phone**: Must be exactly 10 digits
- **Password**: Must be at least 6 characters
- **Postal Code**: Must be 5-6 digits 