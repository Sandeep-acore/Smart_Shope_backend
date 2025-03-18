# Smart Shop API - User Guide

This guide covers all user operations available in the Smart Shop API.

## Authentication

### Register a User

- **URL**: `POST http://localhost:8080/api/auth/register`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - name: Test User
  - email: user@example.com
  - password: password123
  - phone: 1234567890
  - profileImage: (optional file upload - JPEG, JPG, or PNG only)
- **Response**: JWT token and user details. Save the token for subsequent requests.
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "Test User",
  "email": "user@example.com",
  "phone": "1234567890",
  "profileImage": "/uploads/profiles/user1_profile.jpg",
  "roles": ["ROLE_USER"]
}
```

### Login

- **URL**: `POST http://localhost:8080/api/auth/login`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - email: user@example.com
  - password: password123
- **Response**: JWT token and user details. Save the token for subsequent requests.

### Get User Profile

- **URL**: `GET http://localhost:8080/api/auth/profile`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: User profile details including address information.
```json
{
  "id": 1,
  "name": "Test User",
  "email": "user@example.com",
  "phone": "1234567890",
  "profileImage": null,
  "address": {
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "roles": ["ROLE_USER"]
}
```

### Add User Address

- **URL**: `POST http://localhost:8080/api/auth/add-address`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - addressLine1: 123 Main St
  - addressLine2: Apt 4B
  - city: New York
  - state: NY
  - postalCode: 10001
  - country: USA
- **Response**: Added address information.

### Update User Address

- **URL**: `PUT http://localhost:8080/api/auth/update-address`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - addressLine1: 123 Main St
  - addressLine2: Apt 4B
  - city: New York
  - state: NY
  - postalCode: 10001
  - country: USA
- **Response**: Updated address information.

### Update User Profile

- **URL**: `PUT http://localhost:8080/api/auth/update-profile`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - name: Updated Name
  - phone: 5551234567
  - addressLine1: 123 Main St
  - addressLine2: Apt 4B
  - city: New York
  - state: NY
  - postalCode: 10001
  - country: USA
  - profileImage: (file upload)
- **Response**: Success message.

### Upload Profile Image

- **URL**: `POST http://localhost:8080/api/auth/upload-profile-image`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - image: (file upload - JPEG, JPG, or PNG only)
- **Response**: Profile image path and success message.

## Browse Products

### Get All Categories

- **URL**: `GET http://localhost:8080/api/categories`
- **Response**: List of all categories.

### Get Category by ID

- **URL**: `GET http://localhost:8080/api/categories/{id}`
- **Response**: Category details.

### Get All Products

- **URL**: `GET http://localhost:8080/api/products`
- **Response**: List of all products.

### Get Product by ID

- **URL**: `GET http://localhost:8080/api/products/{id}`
- **Response**: Product details.

### Search Products

- **URL**: `GET http://localhost:8080/api/products/search?name=smartphone` or `GET http://localhost:8080/api/products/search?query=smartphone`
- **Response**: List of matching products.

### Filter Products

- **URL**: `GET http://localhost:8080/api/products/filter?category=1&minPrice=100&maxPrice=1000`
- **Response**: List of filtered products.

## Cart Management

### Add to Cart

- **URL**: `POST http://localhost:8080/api/cart`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - productId: 1
  - quantity: 2
- **Response**: Updated cart contents.

### Get Cart

- **URL**: `GET http://localhost:8080/api/cart`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Cart contents with subtotal.

### Update Cart Item

- **URL**: `PUT http://localhost:8080/api/cart/{cartItemId}`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Body** (form-data):
  - productId: 1
  - quantity: 3
- **Response**: Updated cart contents.

### Remove from Cart

- **URL**: `DELETE http://localhost:8080/api/cart/{cartItemId}`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Updated cart contents.

### Clear Cart

- **URL**: `DELETE http://localhost:8080/api/cart`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Success message.

## Wishlist Management

### Add to Wishlist

- **URL**: `POST http://localhost:8080/api/wishlist/{productId}`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Success message.

### Get Wishlist

- **URL**: `GET http://localhost:8080/api/wishlist`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Wishlist contents.

### Remove from Wishlist

- **URL**: `DELETE http://localhost:8080/api/wishlist/{productId}`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Success message.

### Clear Wishlist

- **URL**: `DELETE http://localhost:8080/api/wishlist`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Success message.

## Order Management

### Create Order

- **URL**: `POST http://localhost:8080/api/orders`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
  - Content-Type: application/x-www-form-urlencoded
- **Body** (x-www-form-urlencoded):
  - productIds: 1,2,3 (comma-separated product IDs)
  - quantities: 2,1,3 (comma-separated quantities matching the order of productIds)
  - paymentMethod: CREDIT_CARD
  - notes: Please deliver in the evening
  - useProfileAddress: true/false (optional, defaults to true - whether to use profile address for delivery)
  - deliveryAddressLine1: 456 Delivery St (required if useProfileAddress is false)
  - deliveryAddressLine2: Suite 7C (optional)
  - deliveryCity: Chicago (required if useProfileAddress is false)
  - deliveryState: IL (required if useProfileAddress is false)
  - deliveryPostalCode: 60601 (required if useProfileAddress is false)
  - deliveryCountry: USA (required if useProfileAddress is false)
- **Response**: Created order details with shipping address (from profile) and delivery address (either from profile or custom).
- **Note**: 
  - The shipping address is always taken from your profile. Make sure your profile has a complete address.
  - The delivery address can be the same as your profile address (useProfileAddress=true) or a custom address (useProfileAddress=false).

### Get User Orders

- **URL**: `GET http://localhost:8080/api/orders`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: List of user's orders.

### Get Order by ID

- **URL**: `GET http://localhost:8080/api/orders/{id}`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Order details.

### Get Order Invoice

- **URL**: `GET http://localhost:8080/api/orders/{id}/invoice`
- **Headers**: 
  - Authorization: Bearer {your_jwt_token}
- **Response**: Invoice details or file download.

## Offers

### Get All Active Offers

- **URL**: `GET http://localhost:8080/api/offers`
- **Response**: List of all active offers.
```json
[
  {
    "id": 1,
    "title": "Summer Sale",
    "description": "Get 20% off on all summer products",
    "imageUrl": "offers/summer-sale.jpg",
    "active": true,
    "createdAt": "2023-07-15T10:30:45",
    "updatedAt": "2023-07-15T10:30:45"
  }
]
```

### Get Offer by ID

- **URL**: `GET http://localhost:8080/api/offers/{id}`
- **Response**: Offer details.

### View All Offer Images

- **URL**: `GET http://localhost:8080/api/files/offers/images`
- **Response**: List of all offer image URLs.

### View All Offer Images with Details

- **URL**: `GET http://localhost:8080/api/files/offers/images/with-details`
- **Response**: List of all offer images with details.

## Testing Tips for Users

1. **Authentication**: Always include the JWT token in the Authorization header for authenticated operations.
2. **File Uploads**: When uploading images, ensure they are in JPEG, JPG, or PNG format.
3. **Order Creation**: Make sure your profile has a complete address before creating an order.
4. **Validation**: The API includes validation for all inputs. Check error messages for guidance.

## Data Validation Rules

- **Name**: Must be between 3 and 50 characters
- **Email**: Must be a valid email format
- **Phone**: Must be exactly 10 digits
- **Password**: Must be at least 6 characters
- **Postal Code**: Must be 5-6 digits 