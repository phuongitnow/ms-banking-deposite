# Hướng dẫn test bằng Postman - ms-banking-deposit MVP

## Tổng quan

Tài liệu này hướng dẫn test hệ thống microservices banking deposit qua Kong API Gateway sử dụng Postman Collection với scripts tự động.

## Chuẩn bị

### 1. Khởi động hệ thống

```bash
# Build và start toàn bộ stack
docker compose up -d

# Kiểm tra tất cả containers đã running
docker compose ps

# Xem logs nếu cần
docker compose logs -f [service-name]
```

### 2. Services và Ports

| Service | URL | Description |
|---------|-----|-------------|
| Kong Gateway | http://localhost:8000 | Entry point cho tất cả API |
| Kong Admin | http://localhost:8001 | Admin API |
| Product Service | http://localhost:8081 | Quản lý sản phẩm và deposit request |
| Account Service | http://localhost:8082 | Quản lý tài khoản |
| Calculation Service | http://localhost:8083 | Tính toán lãi suất |
| Notify Service | http://localhost:8084 | Gửi thông báo |
| Mock API Service | http://localhost:8085 | Mock email API |
| Kafka Connect | http://localhost:18083 | Debezium CDC |

### 3. Import Postman Collection

#### Cách 1: Import trực tiếp
1. Mở Postman
2. Click **Import** (góc trên trái)
3. Chọn file `postman/ms-banking-deposit.postman_collection.json`
4. Click **Import**

#### Cách 2: Import qua URL (nếu có)
1. Mở Postman
2. Click **Import** → tab **Link**
3. Paste link đến file JSON trên repo
4. Click **Continue** → **Import**

## Collection Variables

Collection đã được cấu hình sẵn với các variables:

| Variable | Value | Mô tả |
|----------|-------|-------|
| `baseUrl` | http://localhost:8000 | Kong gateway URL |
| `connectUrl` | http://localhost:18083 | Kafka Connect URL |
| `customerId` | 00000000-0000-0000-0000-000000000001 | Customer ID mặc định |
| `productId` | *(auto)* | Tự động lưu khi tạo product |
| `depositId` | *(auto)* | Tự động lưu khi tạo deposit |

**Lưu ý**: `productId` và `depositId` được tự động lưu bởi test scripts, không cần set thủ công.

## Quy trình test E2E

### 1. Tạo sản phẩm (Product)

**Request**: `1. Create Product`  
**Method**: POST  
**URL**: `{{baseUrl}}/api/products`

**Body**:
```json
{
  "name": "Term-6M",
  "minAmount": 1000000,
  "maxAmount": 500000000,
  "termInMonths": 6,
  "ratePercent": 5.5
}
```

**Test Script** (tự động):
- Validate status 200/201
- Lưu `productId` vào collection variable
- Auto-chuyển sang request tiếp theo

**Response example**:
```json
{
  "id": "50837277-60a6-49de-bff5-7ef5b7105d9a"
}
```

---

### 2. Tính lãi suất (Rate Calculation)

**Request**: `2. Get Rates`  
**Method**: GET  
**URL**: `{{baseUrl}}/api/rates?term=6&amount=100000000&productId={{productId}}`

**Query Params**:
- `term`: Số tháng (6)
- `amount`: Số tiền (100000000)
- `productId`: Tự động dùng `{{productId}}`

**Test Script** (tự động):
- Validate status 200
- Auto-chuyển sang request tiếp theo

**Response example**:
```json
{
  "term": 6,
  "amount": 100000000,
  "ratePercent": 5.5,
  "totalInterest": 2750000
}
```

---

### 3. Tạo yêu cầu mở sổ (Deposit Request)

**Request**: `3. Create Deposit Request`  
**Method**: POST  
**URL**: `{{baseUrl}}/api/deposits`

**Body**:
```json
{
  "productId": "{{productId}}",
  "customerId": "{{customerId}}",
  "amount": 10000000
}
```

**Test Script** (tự động):
- Validate status 200/201
- Lưu `depositId` vào collection variable
- Auto-chuyển sang request tiếp theo

**Response example**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### 4. Phê duyệt yêu cầu (Approve Deposit)

**Request**: `4. Approve Deposit`  
**Method**: POST  
**URL**: `{{baseUrl}}/api/deposits/{{depositId}}/approve`

**Test Script** (tự động):
- Validate status 200/202
- Auto-chuyển sang request tiếp theo

**Side effects** (tự động):
- Tạo event `deposit.approved` → Kafka topic
- Account service consume event → tạo account/deposit entry
- Notify service consume event → gửi email thông báo

---

### 5. Tra cứu tài khoản

**Request**: `5. Get Account`  
**Method**: GET  
**URL**: `{{baseUrl}}/api/accounts/{{customerId}}`

**Test Script** (tự động):
- Validate status 200
- Auto-chuyển sang request tiếp theo

**Response example**:
```json
{
  "id": "00000000-0000-0000-0000-000000000001",
  "customerId": "00000000-0000-0000-0000-000000000001",
  "balance": 10000000
}
```

---

### 6. Lấy danh sách sổ tiết kiệm

**Request**: `6. Get Account Deposits`  
**Method**: GET  
**URL**: `{{baseUrl}}/api/accounts/{{customerId}}/deposits`

**Test Script** (tự động):
- Validate status 200
- Kết thúc chuỗi E2E

**Response example**:
```json
[
  {
    "id": "deposit-uuid-here",
    "accountId": "00000000-0000-0000-0000-000000000001",
    "depositId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "productId": "50837277-60a6-49de-bff5-7ef5b7105d9a",
    "amount": 10000000,
    "status": "APPROVED"
  }
]
```

---

### 7. Test thông báo (Optional)

**Request**: `7. Notify Test`  
**Method**: POST  
**URL**: `{{baseUrl}}/api/notifications/test`

**Body**:
```json
{
  "to": "user@example.com",
  "subject": "Hello",
  "body": "Hi"
}
```

**Test Script** (tự động):
- Validate status 200/202

---

### 8. Kiểm tra Debezium Connector (Optional)

**Request**: `8. Connect: List Connectors`  
**Method**: GET  
**URL**: `{{connectUrl}}/connectors`

**Test Script** (tự động):
- Validate status 200

**Response example**:
```json
["product-outbox-connector"]
```

---

## Cách chạy toàn bộ Collection

### Option 1: Chạy thủ công (Recommended cho lần đầu)

1. Mở Collection trong Postman
2. Chạy từng request theo thứ tự 1→6
3. Kiểm tra response và variables được lưu tự động
4. Verify data trong database (tùy chọn)

### Option 2: Chạy tự động (Runner)

1. Click **...** menu trên Collection → **Run collection**
2. Chọn `1. Create Product` → `6. Get Account Deposits` (bỏ tick các test khác nếu muốn)
3. Click **Run**
4. Xem test results và logs

**Lưu ý**: Collection đã có scripts tự động chain requests nên chỉ cần chạy request đầu tiên, các request sau sẽ tự động chạy theo.

---

## Kiểm tra Events trong Kafka (Tùy chọn)

### Cách 1: Dùng kcat (kafkacat)

```bash
# Install kcat
sudo apt-get install kcat  # Ubuntu/Debian

# Subcribe topic để xem events
kcat -b localhost:29092 -t deposit.approved -C

# Hoặc xem tất cả messages
kcat -b localhost:29092 -t deposit.approved -C -o beginning
```

### Cách 2: Dùng Kafka UI (tùy chọn)

```bash
# Start Kafka UI container
docker run -p 8080:8080 -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=localhost:29092 provectuslabs/kafka-ui:latest

# Mở browser: http://localhost:8080
```

### Events được tạo:

Sau khi chạy `4. Approve Deposit`, các events sau sẽ được tạo:

**Topic**: `deposit.approved`
```json
{
  "depositId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "productId": "50837277-60a6-49de-bff5-7ef5b7105d9a",
  "customerId": "00000000-0000-0000-0000-000000000001",
  "amount": "10000000",
  "approvedAt": "2025-10-30T10:00:00Z"
}
```

Consumers:
- **Account Service**: Listen `deposit.approved` → tạo account và deposit entry
- **Notify Service**: Listen `deposit.approved` → gửi email approval notification

---

## Troubleshooting

### 1. Lỗi 503 Service Temporarily Unavailable

**Nguyên nhân**: Kong không phân giải DNS tên service.

**Giải pháp**:
```bash
# Kiểm tra network
docker compose ps
docker network inspect ms-banking-deposit_banking | grep -A 5 "Containers"

# Kiểm tra Kong logs
docker compose logs kong | grep -i "name resolution"
```

### 2. Lỗi 404 Not Found

**Nguyên nhân**: Route không được configure trong Kong.

**Giải pháp**:
- Kiểm tra `kong/kong.yml` đã mount đúng
- Verify Kong đã load config: `curl http://localhost:8001/services`

### 3. Lỗi 500 Internal Server Error

**Nguyên nhân**: Database migrations chưa chạy hoặc sai cấu hình.

**Giải pháp**:
```bash
# Check service logs
docker compose logs product-service
docker compose logs account-service

# Check database
docker compose exec postgres psql -U product -d product_db -c "\dt"
docker compose exec postgres psql -U account -d account_db -c "\dt"
```

### 4. Không thấy Events trong Kafka

**Nguyên nhân**: Outbox publisher hoặc Debezium connector chưa chạy.

**Giải pháp**:
```bash
# Check Kafka Connect logs
docker compose logs connect

# Check connector status
curl http://localhost:18083/connectors/product-outbox-connector/status

# List all connectors
curl http://localhost:18083/connectors
```

### 5. productId/depositId không được lưu

**Nguyên nhân**: Test script lỗi hoặc response format sai.

**Giải pháp**:
- Mở tab **Tests** trong Postman request
- Xem console logs: **View** → **Show Postman Console**
- Verify response format có field `id`

---

## Best Practices

1. **Chạy thủ công lần đầu** để hiểu flow và verify từng bước
2. **Kiểm tra logs** nếu có lỗi: `docker compose logs [service]`
3. **Verify data** trong database sau mỗi request quan trọng
4. **Sử dụng collection variables** thay vì hardcode IDs
5. **Chạy toàn bộ E2E flow** để test integration giữa services

---

## Tài liệu liên quan

- [Architecture Document](architecture.md)
- [Business Analysis](ba.md)
- [README](../README.md)

---

## Support

Nếu gặp vấn đề, check:
1. Logs: `docker compose logs -f`
2. Service health: `docker compose ps`
3. Kong admin: http://localhost:8001
4. Postgres: `docker compose exec postgres psql -U postgres`
