## Kiến trúc tổng thể (MVP)

- Tech stack: Java 17, Spring Boot 3, Gradle 8.12, PostgreSQL, Liquibase, Kafka, Debezium, Kong, Docker Compose.
- Mẫu kiến trúc: Clean Architecture trong từng service; CQRS ở `product-service`; Outbox Pattern + Debezium CDC; Global Exception Handling; Bean Validation; Actuator.
- API Gateway: Kong (DB-less) định tuyến `/api/*` tới các service.

### Microservices
- product-service: quản lý sản phẩm tiền gửi và yêu cầu mở sổ; ghi sự kiện domain vào `outbox_event` (Postgres) → Debezium → Kafka.
- account-service: read model tài khoản/sổ; tiêu thụ event `deposit.approved|deposit.rejected` để cập nhật bảng đọc; expose query.
- calculation-service: cung cấp tính toán lãi/rate (read-only endpoint) cho mobile.
- notify-service: tiêu thụ `notification.requested` và mô phỏng gửi email (log), cung cấp REST test.
- mock-api-service: giả lập provider email ngoài (REST) nếu cần.

### Dữ liệu và sự kiện
- Postgres per-service (MVP hiện có DB sản phẩm và account). Liquibase quản lý schema.
- Outbox table (product-service): `outbox_event(id, aggregate_type, aggregate_id, event_type, payload, occurred_at)`.
- Kafka topics (MVP): `deposit.product.created`, `deposit.requested`, `deposit.approved`, `deposit.rejected`, `notification.requested`.
- Debezium Postgres Connector (Kafka Connect): đọc `public.outbox_event`, dùng `EventRouter` route theo `event_type` → topic tương ứng.

### CQRS (product-service)
- Command: tạo sản phẩm, tạo yêu cầu mở sổ, phê duyệt/từ chối.
- Query: lấy sản phẩm theo id, lấy yêu cầu theo id.
- Transactional outbox: ghi entity + outbox cùng transaction.

### Luồng nghiệp vụ chính
1) Tạo sản phẩm: `POST /api/products` → persist + outbox `deposit.product.created`.
2) Mở sổ: `POST /api/deposits` → persist request + outbox `deposit.requested` (có amount, productId, customerId).
3) Phê duyệt/từ chối: `POST /api/deposits/{id}/approve|reject` → update status + outbox `deposit.approved|deposit.rejected`.
4) account-service tiêu thụ event phê duyệt/từ chối, cập nhật read model `account` và `account_deposit`.
5) notify-service (tùy luồng) lắng nghe `notification.requested` để gửi email (mock/log).

### Triển khai & Kết nối
- Docker Compose: Postgres, Zookeeper, Kafka, Kafka Connect (Debezium), Kong, các service.
- Kafka listeners (dev): Internal `PLAINTEXT://kafka:9092`, Host `PLAINTEXT_HOST://localhost:29092`.
- Kafka Connect: mapped `18083:8083`.
- Kong: mapped `8000:8000` (proxy), `8001:8001` (admin).

### Routes qua Kong (DB-less)
- product-service: `/api/products`, `/api/deposits`, `/api/deposits/{id}`, `/api/deposits/{id}/approve|reject`.
- account-service: `/api/accounts/{id}`, `/api/accounts/{id}/deposits`.
- calculation-service: `/api/rates`.
- notify-service: `/api/notifications/test`.

### Observability & Resilience (MVP)
- Actuator mở toàn bộ endpoints nội bộ (dev).
- Idempotency đề xuất cho `POST /api/deposits`.

### Bảo mật (MVP)
- Kong chưa bật auth để thuận tiện local. Production: Key-Auth/OIDC, TLS, secrets.

### Nâng cấp tương lai
- Projection/read-model chuyên biệt, Saga/Orchestration, K8s manifests/Helm.
