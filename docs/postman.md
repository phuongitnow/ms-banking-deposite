## Hướng dẫn test bằng Postman (MVP)

### Chuẩn bị
- Stack đã chạy: `docker compose up -d`.
- Kong: `http://localhost:8000`.
- Kafka Connect: `http://localhost:18083`.
- Đã đăng ký Debezium connector `product-outbox-connector`.

### Import Postman Collection
- Import file: `postman/ms-banking-deposit.postman_collection.json`.
- (Tùy chọn) Tạo Environment `local`:
  - `baseUrl` = `http://localhost:8000`
  - `connectUrl` = `http://localhost:18083`

### Quy trình test gợi ý
1) Tạo sản phẩm
- POST `{{baseUrl}}/api/products`
```json
{
  "name": "Term-6M",
  "minAmount": 1000000,
  "maxAmount": 500000000,
  "termInMonths": 6,
  "ratePercent": 5.5
}
```
- Lưu `productId` từ response.

2) Tính lãi (tham khảo)
- GET `{{baseUrl}}/api/rates?term=6&amount=100000000&productId={{productId}}`

3) Tạo yêu cầu mở sổ
- POST `{{baseUrl}}/api/deposits`
```json
{
  "productId": "{{productId}}",
  "customerId": "00000000-0000-0000-0000-000000000001",
  "amount": 10000000
}
```
- Lưu `depositId`.

4) Phê duyệt yêu cầu
- POST `{{baseUrl}}/api/deposits/{{depositId}}/approve`

5) Tra cứu tài khoản/sổ
- GET `{{baseUrl}}/api/accounts/00000000-0000-0000-0000-000000000001`
- GET `{{baseUrl}}/api/accounts/00000000-0000-0000-0000-000000000001/deposits`

6) Gửi thông báo (mock)
- POST `{{baseUrl}}/api/notifications/test`
```json
{"to":"user@example.com","subject":"Hello","body":"Hi"}
```

### Kiểm tra sự kiện trong Kafka (tuỳ chọn)
- Cài `kafkacat`/`kcat` hoặc UI khác; connect `localhost:29092`.
- Subcribe topic `deposit.approved` để xem event sau phê duyệt.

### Lỗi thường gặp
- 404 qua Kong: kiểm tra `kong/kong.yml` đã mount, Kong healthy.
- 500 ở product/account: kiểm tra Postgres migrations (Liquibase) đã chạy, container logs.
- Không thấy event: kiểm tra Debezium connector (GET `{{connectUrl}}/connectors`).
