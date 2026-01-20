# Test Script for E-Commerce API
$baseUrl = "http://localhost:8080/api"
$userId = "user123"

# 1. Create Product
Write-Host "1. Creating Product..."
$productBody = @{
    name = "Laptop"
    description = "Gaming Laptop"
    price = 50000.0
    stock = 10
} | ConvertTo-Json
$product = Invoke-RestMethod -Uri "$baseUrl/products" -Method Post -Body $productBody -ContentType "application/json"
$productId = $product.id
Write-Host "Product Created: $productId"

# 2. Add to Cart
Write-Host "2. Adding to Cart..."
$cartBody = @{
    userId = $userId
    productId = $productId
    quantity = 2
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/cart/add" -Method Post -Body $cartBody -ContentType "application/json" | Out-Null
Write-Host "Item added to cart."

# 3. Create Order
Write-Host "3. Creating Order..."
$orderBody = @{
    userId = $userId
} | ConvertTo-Json
$order = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Body $orderBody -ContentType "application/json"
$orderId = $order.id
Write-Host "Order Created: $orderId (Status: $($order.status))"

# 4. Initiate Payment
Write-Host "4. Initiating Payment..."
$paymentBody = @{
    orderId = $orderId
    amount = $order.totalAmount
} | ConvertTo-Json
$payment = Invoke-RestMethod -Uri "$baseUrl/payments/create" -Method Post -Body $paymentBody -ContentType "application/json"
Write-Host "Payment Initiated: $($payment.paymentId) (Status: $($payment.status))"

# 5. Wait for Webhook (Mock Service Delay)
Write-Host "Waiting 5 seconds for Mock Webhook..."
Start-Sleep -Seconds 5

# 6. Verify Order Status
Write-Host "6. Verifying Order Status..."
$updatedOrder = Invoke-RestMethod -Uri "$baseUrl/orders/$orderId" -Method Get
Write-Host "Updated Order Status: $($updatedOrder.status)"

if ($updatedOrder.status -eq "PAID") {
    Write-Host "SUCCESS: Flow completed successfully!" -ForegroundColor Green
} else {
    Write-Host "FAILED: Order status did not update to PAID." -ForegroundColor Red
}
