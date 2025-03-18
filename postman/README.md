# Smart Shop API - Postman Testing Guide


## Authentication Endpoints

### Register a User (Form Data with Optional Profile Image)

- **URL**: `POST http://localhost:8080/api/auth/register`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - name: Test User
  - email: user@example.com
  - password: password123
  - phone: 1234567890
  - roles: (optional) admin, product_manager, or delivery_partner
  - profileImage: (optional file upload - JPEG, JPG, or PNG only)
- **Response**: JWT token and user details, similar to the login response. Save the token for subsequent requests.
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

**Note**: If you don't specify any roles during registration, the user will automatically be assigned the `ROLE_USER` role.

### Login

- **URL**: `POST http://localhost:8080/api/auth/login`
- **Headers**: None (form-data automatically sets the correct Content-Type)
- **Body** (form-data):
  - email: user@example.com
  - password: password123
- **Response**: JWT token and user details. Save the token for subsequent requests.
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "Test User",
  "email": "user@example.com",
  "phone": "1234567890",
  "profileImage": null,
  "roles": ["ROLE_USER"]
}
```

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
```json
{
  "address": {
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "message": "Address added successfully"
}
```

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
```json
{
  "address": {
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "message": "Address updated successfully"
}
```

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
```json
{
  "profileImage": "/uploads/profiles/user1_profile.jpg",
  "message": "Profile image uploaded successfully"
}
```

### View Profile Image

To view a profile image in Postman or a browser, use one of these methods:

1. **Direct URL**: `GET http://localhost:8080/api/files/profiles/{filename}`
   - Example: `http://localhost:8080/api/files/profiles/ff14b814-78cd-4bdd-b535-6642a8a4abd0.png`
   - No authentication required for public images

2. **In Postman**: 
   - Send a GET request to the URL above
   - The image will be displayed in the response body (if you have "Automatically preview responses" enabled)
   - You can also save the image by clicking the "Save Response" button

3. **In Browser**:
   - Simply paste the URL in your browser address bar
   - The image will be displayed directly in the browser

The filename is the last part of the `profileImage` path returned in the user profile or upload response.

## Category Management

### Create Category with Image (Admin only)

- **URL**: `POST http://localhost:8080/api/categories`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Electronics (required)
  - description: Electronic devices and gadgets (required)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Created category details.
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices and gadgets",
  "imagePath": "categories/a1b2c3d4-5678-90ab-cdef-ghijklmnopqr.jpg",
  "createdAt": "2023-07-15T10:30:45",
  "updatedAt": "2023-07-15T10:30:45"
}
```

### Get All Categories

- **URL**: `GET http://localhost:8080/api/categories`
- **Response**: List of all categories.

### Get Category by ID

- **URL**: `GET http://localhost:8080/api/categories/{id}`
- **Response**: Category details.

### Update Category with Image (Admin only)

- **URL**: `PUT http://localhost:8080/api/categories/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - name: Electronics & Gadgets (required)
  - description: Updated description for electronics category (required)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Updated category details.

### Delete Category (Admin only)

- **URL**: `DELETE http://localhost:8080/api/categories/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

### View Category Image

To view a category image in Postman or a browser, use one of these methods:

1. **Direct URL**: `GET http://localhost:8080/api/files/categories/{filename}`
   - Example: `http://localhost:8080/api/files/categories/a1b2c3d4-5678-90ab-cdef-ghijklmnopqr.jpg`
   - No authentication required for public images

2. **In Postman**: 
   - Send a GET request to the URL above
   - The image will be displayed in the response body (if you have "Automatically preview responses" enabled)

3. **In Browser**:
   - Simply paste the URL in your browser address bar
   - The image will be displayed directly in the browser

The filename is the last part of the `imagePath` returned in the category response.

## Product Management

### Create Product (Admin or Product Manager)

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

### Get All Products

- **URL**: `GET http://localhost:8080/api/products`
- **Response**: List of all products.

### Get Product by ID

- **URL**: `GET http://localhost:8080/api/products/{id}`
- **Response**: Product details.

### Update Product (Admin or Product Manager)

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

### Delete Product (Admin or Product Manager)

- **URL**: `DELETE http://localhost:8080/api/products/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

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

### Update Order Status

- **URL**: `PUT http://localhost:8080/api/orders/{id}/status`
- **Headers**: 
  - Authorization: Bearer {admin_or_delivery_partner_jwt_token}
  - Content-Type: application/x-www-form-urlencoded
- **Body** (x-www-form-urlencoded):
  - status: PROCESSING (or SHIPPED, DELIVERED, CANCELLED)
- **Response**: Updated order details.
- **Note**: Only admins and delivery partners can update order status. If status is set to DELIVERED, the deliveredAt timestamp will be automatically set.

### Update Payment Status

- **URL**: `PUT http://localhost:8080/api/orders/{id}/payment-status`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
  - Content-Type: application/x-www-form-urlencoded
- **Body** (x-www-form-urlencoded):
  - status: PAID (or PENDING, FAILED, REFUNDED)
  - transactionId: TXN123456 (optional)
- **Response**: Updated order details.
- **Note**: Only admins can update payment status.



### Create Order (Simplified with Separate Delivery Address)

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


### Create Offer (Admin only)

- **URL**: `POST http://localhost:8080/api/offers`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - title: Summer Sale (required)
  - description: Get 20% off on all summer products (optional)
  - image: (file upload - JPEG, JPG, or PNG only) (required)
- **Response**: Created offer details.

### Update Offer (Admin only)

- **URL**: `PUT http://localhost:8080/api/offers/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Body** (form-data):
  - title: Summer Sale Extended (required)
  - description: Updated description for the offer (optional)
  - image: (file upload - JPEG, JPG, or PNG only) (optional)
  - active: true/false (optional)
- **Response**: Updated offer details.

### Delete Offer (Admin only)

- **URL**: `DELETE http://localhost:8080/api/offers/{id}`
- **Headers**: 
  - Authorization: Bearer {admin_jwt_token}
- **Response**: Success message.

- **URL**: `GET http://localhost:8080/api/files/offers/images`
- **Response**: List of all offer image URLs.
```json
[
  "http://localhost:8080/api/files/offers/summer-sale.jpg",
  "http://localhost:8080/api/files/offers/winter-discount.jpg"
]
```

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