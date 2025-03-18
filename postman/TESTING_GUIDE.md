# Smart Shop API Testing Guide

This guide will help you test the Smart Shop API using Postman. The API is developed in modular blocks, each independently testable.

## Setup

1. Import the Postman collection: `Smart_Shop_API.postman_collection.json`
2. Import the Postman environment: `Smart_Shop_API.postman_environment.json`
3. Select the "Smart Shop API Environment" from the environment dropdown in Postman

## Testing Blocks

### 1. Authentication & Security

1. **Register User**:
   - Send the "Register User" request
   - Verify that a new user is created successfully
   - Note the response message and status code (should be 201 Created)

2. **Login**:
   - Send the "Login" request with the credentials of the user you just registered
   - Verify that you receive a JWT token in the response
   - The token will be automatically saved to the `token` environment variable

3. **Reset Password**:
   - Send the "Reset Password Request" with a registered email
   - Check that an OTP is sent (in development, this will be logged in the console)
   - Use the OTP to verify and set a new password with the "Verify OTP & Set New Password" request

4. **Get User Profile**:
   - Send the "Get User Profile" request
   - Verify that you can see the user's profile information
   - The JWT token should be automatically included in the Authorization header

5. **Update User Profile**:
   - Send the "Update User Profile" request with updated information
   - Verify that the profile is updated successfully

### 2. Product Management

1. **Add Product** (requires Admin role):
   - Login as an admin user
   - Send the "Add Product" request with product details
   - Verify that the product is created successfully
   - The product ID will be automatically saved to the `productId` environment variable

2. **Get Product by ID**:
   - Send the "Get Product by ID" request
   - Verify that you can see the product details

3. **Search Products by Name**:
   - Send the "Search Products by Name" request
   - Verify that you can find products by name

4. **Update Product** (requires Admin role):
   - Send the "Update Product" request with updated details
   - Verify that the product is updated successfully

5. **Delete Product** (requires Admin role):
   - Send the "Delete Product" request
   - Verify that the product is deleted successfully

### 3. Category Management

1. **Create Category** (requires Admin role):
   - Send the "Create Category" request
   - Verify that the category is created successfully
   - The category ID will be automatically saved to the `categoryId` environment variable

2. **Get All Categories**:
   - Send the "Get All Categories" request
   - Verify that you can see all categories

3. **Get Category by ID**:
   - Send the "Get Category by ID" request
   - Verify that you can see the category details

4. **Update Category** (requires Admin role):
   - Send the "Update Category" request with updated details
   - Verify that the category is updated successfully

5. **Delete Category** (requires Admin role):
   - Send the "Delete Category" request
   - Verify that the category is deleted successfully

### 4. Order & Payment Management

1. **Place Order**:
   - Send the "Place Order" request with order details
   - Verify that the order is created successfully
   - The order ID will be automatically saved to the `orderId` environment variable

2. **Get Order by ID**:
   - Send the "Get Order by ID" request
   - Verify that you can see the order details

3. **Update Order Status** (requires Admin or Delivery Partner role):
   - Send the "Update Order Status" request
   - Verify that the order status is updated successfully

4. **Update Payment Status** (requires Admin role):
   - Send the "Update Payment Status" request
   - Verify that the payment status is updated successfully

### 5. Cart & Wishlist

1. **Add to Cart**:
   - Send the "Add to Cart" request
   - Verify that the product is added to the cart

2. **View Cart**:
   - Send the "View Cart" request
   - Verify that you can see the products in the cart

3. **Remove from Cart**:
   - Send the "Remove from Cart" request
   - Verify that the product is removed from the cart

4. **Add to Wishlist**:
   - Send the "Add to Wishlist" request
   - Verify that the product is added to the wishlist

5. **View Wishlist**:
   - Send the "View Wishlist" request
   - Verify that you can see the products in the wishlist

6. **Remove from Wishlist**:
   - Send the "Remove from Wishlist" request
   - Verify that the product is removed from the wishlist

### 6. Coupons & Offers

1. **Create Coupon** (requires Admin role):
   - Send the "Create Coupon" request
   - Verify that the coupon is created successfully
   - The coupon code will be automatically saved to the `couponCode` environment variable

2. **Validate Coupon**:
   - Send the "Validate Coupon" request
   - Verify that you can validate the coupon and see its details

### 7. Auto Invoice Generation

1. **Download Invoice**:
   - Place an order first
   - Send the "Download Invoice" request
   - Verify that you can download the invoice as a PDF

### 8. Referral System

1. **Generate Referral Code**:
   - Send the "Generate Referral Code" request
   - Verify that a referral code is generated
   - The referral code will be automatically saved to the `referralCode` environment variable

2. **Validate Referral Code**:
   - Send the "Validate Referral Code" request
   - Verify that you can validate the referral code

### 9. Search & Filters

1. **Search Products**:
   - Send the "Search Products" request
   - Verify that you can search for products

2. **Filter Products**:
   - Send the "Filter Products" request
   - Verify that you can filter products by various criteria

### 10. Multi-Admin Support

1. **Add Admin** (requires Admin role):
   - Send the "Add Admin" request
   - Verify that a new admin user is created

2. **Remove Admin** (requires Admin role):
   - Send the "Remove Admin" request
   - Verify that the admin user is removed

## Troubleshooting

- If you encounter authentication issues, make sure your JWT token is valid
- If you get a 403 Forbidden error, check that you have the required role for the operation
- For any 400 Bad Request errors, check the request body for missing or invalid fields

## Automated Testing

The collection includes test scripts that automatically validate responses and set environment variables. You can run the entire collection as a test suite:

1. Click on the collection name
2. Click the "Run" button
3. Select the requests you want to run
4. Click "Run Smart Shop API"

This will execute the requests in sequence and show you the test results. 