# Minimal E-Commerce Backend API

A Spring Boot application implementing a minimal e-commerce backend with MongoDB and Mock Payment processing.

## Prerequisites
- Java 17+
- Maven
- MongoDB (running on `localhost:27017`)

## Setup
1.  **Clone/Download** the project.
2.  **Start MongoDB**.
3.  **Build** the project:
    ```bash
    mvn clean install
    ```
4.  **Run** the application:
    ```bash
    mvn spring-boot:run
    ```

## API Documentation
The API follows the assignment requirements.

### Products
- `POST /api/products` - Create Product
- `GET /api/products` - List Products

### Cart
- `POST /api/cart/add` - Add to Cart
- `GET /api/cart/{userId}` - Get Cart
- `DELETE /api/cart/{userId}/clear` - Clear Cart

### Orders
- `POST /api/orders` - Create Order (from Cart)
- `GET /api/orders/{orderId}` - Get Order Details

### Payments
- `POST /api/payments/create` - Initiate Payment (Mock)
- `POST /api/webhooks/payment` - Payment Webhook (Internal)

## Testing
You can use the provided `test_api.ps1` script to verify the flow or use the Postman Collection.
