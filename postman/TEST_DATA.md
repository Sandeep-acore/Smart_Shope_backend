# Smart Shop API Test Data

This file contains sample test data for testing the Smart Shop API with Postman.

## Authentication & Security

### Admin User
```json
{
  "email": "admin@smartshop.com",
  "password": "admin123"
}
```

### Regular User
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "Password123",
  "phone": "1234567890",
  "roles": ["user"]
}
```

### Product Manager
```json
{
  "name": "Product Manager",
  "email": "pm@example.com",
  "password": "Password123",
  "phone": "2345678901",
  "roles": ["product_manager"]
}
```

### Delivery Partner
```json
{
  "name": "Delivery Partner",
  "email": "delivery@example.com",
  "password": "Password123",
  "phone": "3456789012",
  "roles": ["delivery_partner"]
}
```

## Product Management

### Sample Product
```json
{
  "name": "Smartphone X",
  "description": "Latest smartphone with advanced features",
  "price": 999.99,
  "categoryId": 1,
  "stockQuantity": 100
}
```

### Updated Product
```json
{
  "name": "Smartphone X Pro",
  "description": "Updated description with new features",
  "price": 1099.99,
  "stockQuantity": 50,
  "discountPercentage": 10
}
```

## Category Management

### Sample Category
```json
{
  "name": "Electronics",
  "description": "Electronic devices and gadgets"
}
```

### Updated Category
```json
{
  "name": "Electronics & Gadgets",
  "description": "Updated description for electronics category"
}
```

## Order & Payment Management

### Sample Order
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
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
  "paymentMethod": "CREDIT_CARD",
  "couponCode": "SUMMER10"
}
```

### Order Status Update
```json
{
  "status": "SHIPPED"
}
```

### Payment Status Update
```json
{
  "status": "PAID",
  "transactionId": "txn_123456789"
}
```

## Cart & Wishlist

### Add to Cart
```json
{
  "productId": 1,
  "quantity": 2
}
```

### Add to Wishlist
```json
{
  "productId": 1
}
```

## Coupons & Offers

### Sample Coupon
```json
{
  "code": "SUMMER10",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "minPurchaseAmount": 50,
  "maxDiscountAmount": 100,
  "validFrom": "2023-06-01",
  "validTo": "2023-08-31",
  "isActive": true
}
```

## Testing Flow

1. Register users with different roles
2. Login with admin credentials
3. Create categories
4. Add products
5. Test product search and filtering
6. Create coupons
7. Add products to cart and wishlist
8. Place orders
9. Update order and payment status
10. Generate invoices
11. Test referral system 