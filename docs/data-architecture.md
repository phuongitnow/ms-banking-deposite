# Kiến trúc dữ liệu - ms-banking-deposit MVP

## Tổng quan

Hệ thống sử dụng mô hình **Database per Service** với PostgreSQL, áp dụng **CQRS** và **Outbox Pattern** để đảm bảo eventual consistency giữa các microservices thông qua event streaming (Kafka).

## Cấu trúc Database

### 1. product_db

Database quản lý sản phẩm tiền gửi và yêu cầu mở sổ, đồng thời lưu events để publish qua Debezium CDC.

#### Table: `product`

Lưu thông tin sản phẩm tiền gửi.

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `id` | UUID | PK, NOT NULL | ID duy nhất của sản phẩm |
| `name` | VARCHAR(200) | NOT NULL | Tên sản phẩm (VD: "Term-6M") |
| `min_amount` | NUMERIC(18,2) | NOT NULL | Số tiền tối thiểu (VND) |
| `max_amount` | NUMERIC(18,2) | NOT NULL | Số tiền tối đa (VND) |
| `term_in_months` | INT | NOT NULL | Kỳ hạn (tháng) |
| `rate_percent` | NUMERIC(5,3) | NOT NULL | Lãi suất (%) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

**Ví dụ data:**
```sql
INSERT INTO product VALUES (
  'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
  'Term-6M',
  1000000.00,      -- 1 triệu VND
  500000000.00,    -- 500 triệu VND
  6,               -- 6 tháng
  5.500,           -- 5.5% năm
  '2025-10-30 10:00:00'
);
```

#### Table: `deposit_request`

Lưu yêu cầu mở sổ tiết kiệm của khách hàng.

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `id` | UUID | PK, NOT NULL | ID duy nhất của yêu cầu |
| `product_id` | UUID | FK → product.id, NOT NULL | ID sản phẩm |
| `customer_id` | UUID | NOT NULL | ID khách hàng |
| `amount` | NUMERIC(18,2) | NOT NULL | Số tiền gửi (VND) |
| `status` | VARCHAR(30) | NOT NULL | Trạng thái: REQUESTED, APPROVED, REJECTED |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

**Ví dụ data:**
```sql
INSERT INTO deposit_request VALUES (
  'd1e2f3a4-b5c6-7890-def1-234567890abc',
  'a1b2c3d4-e5f6-7890-abcd-ef1234567890',  -- product_id
  '00000000-0000-0000-0000-000000000001',  -- customer_id
  10000000.00,                              -- 10 triệu VND
  'APPROVED',
  '2025-10-30 10:05:00'
);
```

#### Table: `outbox_event`

**Outbox Pattern**: Lưu events để publish ra Kafka qua Debezium CDC.

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `id` | UUID | PK, NOT NULL | ID duy nhất của event |
| `aggregate_type` | VARCHAR(100) | NOT NULL | Loại aggregate (VD: "Product", "Deposit") |
| `aggregate_id` | VARCHAR(100) | NOT NULL | ID của aggregate |
| `event_type` | VARCHAR(150) | NOT NULL | Loại event (VD: "deposit.approved") |
| `payload` | TEXT | NOT NULL | JSON payload của event |
| `occurred_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian xảy ra |

**Index:**
- `idx_outbox_event_type_time` on (`event_type`, `occurred_at`) - Tối ưu query và Debezium polling

**Ví dụ data:**
```sql
INSERT INTO outbox_event VALUES (
  'o1e2v3n4-t5i6-d7e8-b9e0-a1b2c3d4e5f6',
  'Deposit',
  'd1e2f3a4-b5c6-7890-def1-234567890abc',
  'deposit.approved',
  '{"depositId":"d1e2f3a4-b5c6-7890-def1-234567890abc","productId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","customerId":"00000000-0000-0000-0000-000000000001","amount":"10000000","approvedAt":"2025-10-30T10:05:00Z"}',
  '2025-10-30 10:05:00'
);
```

**Events được publish:**
- `deposit.product.created` - Sản phẩm mới được tạo
- `deposit.requested` - Yêu cầu mở sổ được tạo
- `deposit.approved` - Yêu cầu được phê duyệt
- `deposit.rejected` - Yêu cầu bị từ chối

**Debezium Flow:**
1. Application ghi event vào `outbox_event` trong cùng transaction với business data
2. Debezium Postgres Connector detect thay đổi (WAL)
3. Publish event sang Kafka topic tương ứng (route bằng `event_type`)

---

### 2. account_db

Database read model quản lý tài khoản và lịch sử sổ tiết kiệm.

#### Table: `account`

Quản lý thông tin tài khoản khách hàng (1-1 với customer_id trong MVP).

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `id` | UUID | PK, NOT NULL | ID tài khoản (= customer_id trong MVP) |
| `customer_id` | UUID | NOT NULL | ID khách hàng |
| `balance` | NUMERIC(18,2) | NOT NULL | Số dư hiện tại (VND) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

**Ví dụ data:**
```sql
INSERT INTO account VALUES (
  '00000000-0000-0000-0000-000000000001',  -- id = customer_id
  '00000000-0000-0000-0000-000000000001',  -- customer_id
  10000000.00,                              -- balance
  '2025-10-30 10:10:00'
);
```

#### Table: `account_deposit`

Read model lưu lịch sử sổ tiết kiệm của khách hàng (được update khi có event `deposit.approved` hoặc `deposit.rejected`).

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `id` | UUID | PK, NOT NULL | ID duy nhất của record |
| `account_id` | UUID | FK → account.id, NOT NULL | ID tài khoản |
| `deposit_id` | UUID | NOT NULL | ID deposit request gốc |
| `product_id` | UUID | NOT NULL | ID sản phẩm |
| `amount` | NUMERIC(18,2) | NOT NULL | Số tiền gửi (VND) |
| `status` | VARCHAR(30) | NOT NULL | APPROVED hoặc REJECTED |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

**Ví dụ data:**
```sql
INSERT INTO account_deposit VALUES (
  'e1f2a3b4-c5d6-7890-ef12-345678901234',
  '00000000-0000-0000-0000-000000000001',  -- account_id
  'd1e2f3a4-b5c6-7890-def1-234567890abc',  -- deposit_id
  'a1b2c3d4-e5f6-7890-abcd-ef1234567890',  -- product_id
  10000000.00,                              -- amount
  'APPROVED',                               -- status
  '2025-10-30 10:10:00'
);
```

---

## Event Flow & CQRS

### Write Side (product-service)

1. **Tạo Product**
   - Ghi vào table `product`
   - Ghi event `deposit.product.created` vào `outbox_event`
   - Debezium publish → Kafka topic `deposit.product.created`

2. **Tạo Deposit Request**
   - Ghi vào table `deposit_request` với status=REQUESTED
   - Ghi event `deposit.requested` vào `outbox_event`
   - Debezium publish → Kafka topic `deposit.requested`

3. **Phê duyệt Deposit**
   - Update `deposit_request.status` = APPROVED
   - Ghi event `deposit.approved` vào `outbox_event`
   - Debezium publish → Kafka topic `deposit.approved`

### Read Side (account-service)

- **Consumer** lắng nghe topics: `deposit.approved`, `deposit.rejected`
- Khi nhận event:
  - Tạo/update `account` nếu chưa tồn tại
  - Insert record vào `account_deposit`
  - Update `account.balance` nếu cần (tùy business logic)

---

## Data Model Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                         product_db                               │
├─────────────────────────────────────────────────────────────────┤
│  product                                                         │
│  ├── id (PK)                                                    │
│  └── ...                                                        │
│                                                                   │
│  deposit_request                                                 │
│  ├── id (PK)                                                    │
│  ├── product_id (FK → product.id)                               │
│  └── ...                                                        │
│                                                                   │
│  outbox_event (CDC)                                              │
│  ├── id (PK)                                                    │
│  ├── event_type                                                  │
│  └── payload → Kafka topics                                      │
└─────────────────────────────────────────────────────────────────┘
                           ↓
                   (Debezium CDC)
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                            Kafka                                  │
│  Topics: deposit.approved, deposit.rejected, ...                 │
└─────────────────────────────────────────────────────────────────┘
                           ↓
                    (Consumers)
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                        account_db                                 │
├─────────────────────────────────────────────────────────────────┤
│  account                                                         │
│  ├── id (PK) = customer_id                                      │
│  └── ...                                                        │
│                                                                   │
│  account_deposit                                                 │
│  ├── id (PK)                                                    │
│  ├── account_id (FK → account.id)                               │
│  └── ...                                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Database Credentials

### product_db
- **Host**: localhost:5432
- **Database**: product_db
- **Username**: product
- **Password**: product

### account_db
- **Host**: localhost:5432
- **Database**: account_db
- **Username**: account
- **Password**: account

---

## Migration Management

Hệ thống sử dụng **Liquibase** để quản lý schema versioning.

- Changelog master: `db/changelog/db.changelog-master.yaml`
- Changelog files: `001-baseline.yaml`, `010-outbox.yaml`, ...
- Auto-run khi Spring Boot startup

**Ví dụ tạo migration mới:**

```yaml
# services/product-service/src/main/resources/db/changelog/020-add-column.yaml
databaseChangeLog:
  - changeSet:
      id: "020-1"
      author: dev
      changes:
        - addColumn:
            tableName: product
            columns:
              - column:
                  name: description
                  type: text
                  constraints:
                    nullable: true
```

---

## Data Types Summary

| Java Type | PostgreSQL Type | Use Case |
|-----------|----------------|----------|
| `UUID` | UUID | Primary keys, foreign keys |
| `BigDecimal` | NUMERIC(18,2) | Money amounts (VND) |
| `BigDecimal` | NUMERIC(5,3) | Percentages (rate) |
| `Integer` | INT | Months, counts |
| `String` | VARCHAR(n) | Text fields |
| `String` | TEXT | JSON payloads |
| `Instant` | TIMESTAMP | Timestamps |

---

## Best Practices

1. **Transactional Outbox**: Luôn ghi event trong cùng transaction với business data
2. **Idempotency**: Consumer nên check duplicate events (deposit_id)
3. **Eventual Consistency**: Accept delay giữa write và read model updates
4. **Monitoring**: Track Outbox table size, Consumer lag, Debezium connector status

