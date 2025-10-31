# Hướng dẫn kết nối DBeaver - ms-banking-deposit

## Tổng quan

DBeaver là công cụ quản lý database GUI hỗ trợ PostgreSQL. Hướng dẫn này giúp bạn kết nối và query data trong hệ thống ms-banking-deposit.

## Cài đặt DBeaver

### Windows/Mac/Linux
Download từ: https://dbeaver.io/download/

**Hoặc cài qua package manager:**
```bash
# Ubuntu/Debian
sudo snap install dbeaver-ce

# macOS
brew install --cask dbeaver-community

# Hoặc download portable version
```

## Kết nối Database

### Bước 1: Khởi động hệ thống

```bash
# Đảm bảo containers đang chạy
docker compose ps

# Nếu chưa chạy, start
docker compose up -d
```

### Bước 2: Tạo connection trong DBeaver

#### 2.1. Mở DBeaver

- Click **Database** → **New Database Connection** (hoặc icon **New Connection** trên toolbar)

#### 2.2. Chọn PostgreSQL

- Trong danh sách drivers, chọn **PostgreSQL**
- Click **Next**

#### 2.3. Nhập thông tin connection

**Product Database:**

| Field | Value |
|-------|-------|
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database** | `product_db` |
| **Username** | `product` |
| **Password** | `product` |

**Account Database:**

| Field | Value |
|-------|-------|
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database** | `account_db` |
| **Username** | `account` |
| **Password** | `account` |

#### 2.4. Test Connection

- Click **Test Connection**
- Nếu lần đầu, DBeaver sẽ tải PostgreSQL driver → chọn **Download**
- Đợi download xong → click **Test Connection** lại
- Thông báo **Success** nếu thành công

#### 2.5. Lưu Connection

- Nhập **Connection name**: `product-db` (hoặc `account-db`)
- Click **Finish**

### Bước 3: Lặp lại cho database thứ 2

Tạo thêm connection cho `account_db` với các thông tin tương tự (sửa database, username, password).

---

## Sử dụng DBeaver

### Xem Database Schema

1. Trong **Database Navigator** (sidebar trái), expand connection
2. Expand **Schemas** → **public**
3. Expand **Tables** để xem danh sách tables
4. Right-click table → **View Data** để xem data

### Run SQL Queries

#### 1. Mở SQL Editor

- Right-click database/table → **Open SQL Script**
- Hoặc click icon **SQL Editor** trên toolbar

#### 2. Viết và chạy query

**Ví dụ 1: Xem tất cả products**
```sql
SELECT * FROM product
ORDER BY created_at DESC;
```

**Ví dụ 2: Xem deposits đã approved**
```sql
SELECT dr.*, p.name as product_name
FROM deposit_request dr
JOIN product p ON dr.product_id = p.id
WHERE dr.status = 'APPROVED'
ORDER BY dr.created_at DESC;
```

**Ví dụ 3: Xem outbox events**
```sql
SELECT id, aggregate_type, event_type, occurred_at
FROM outbox_event
ORDER BY occurred_at DESC
LIMIT 10;
```

**Ví dụ 4: Xem accounts và deposits**
```sql
SELECT 
  a.id as account_id,
  a.customer_id,
  a.balance,
  COUNT(ad.id) as num_deposits,
  SUM(ad.amount) as total_deposited
FROM account a
LEFT JOIN account_deposit ad ON a.id = ad.account_id
GROUP BY a.id, a.customer_id, a.balance;
```

#### 3. Execute Query

- Click **Execute SQL** (F5) hoặc **Execute SQL statement** (Ctrl+Enter)

### Export Data

1. Right-click table → **Export Data**
2. Chọn format: CSV, JSON, Excel, SQL, ...
3. Chọn destination file
4. Click **Start**

### Import Data (nếu cần)

1. Right-click table → **Import Data**
2. Chọn file nguồn (CSV, Excel, ...)
3. Map columns
4. Click **Start**

---

## Quick Queries Reference

### Product Database

**List all products**
```sql
SELECT * FROM product;
```

**Products created today**
```sql
SELECT * FROM product 
WHERE DATE(created_at) = CURRENT_DATE;
```

**Pending deposits**
```sql
SELECT dr.*, p.name 
FROM deposit_request dr
JOIN product p ON dr.product_id = p.id
WHERE dr.status = 'REQUESTED';
```

**Outbox events chưa được publish (nếu cần)**
```sql
SELECT * FROM outbox_event
ORDER BY occurred_at DESC;
```

### Account Database

**All accounts**
```sql
SELECT * FROM account;
```

**Accounts với deposits**
```sql
SELECT a.*, COUNT(ad.id) as num_deposits
FROM account a
LEFT JOIN account_deposit ad ON a.id = ad.account_id
GROUP BY a.id;
```

**Deposits của một customer**
```sql
SELECT ad.*, p.name as product_name
FROM account_deposit ad
JOIN account a ON ad.account_id = a.id
-- Replace với actual customer_id
WHERE a.customer_id = '00000000-0000-0000-0000-000000000001';
```

**Total deposited by status**
```sql
SELECT 
  status,
  COUNT(*) as count,
  SUM(amount) as total_amount
FROM account_deposit
GROUP BY status;
```

---

## Troubleshooting

### 1. Connection Failed: Connection refused

**Nguyên nhân**: PostgreSQL container chưa chạy hoặc port 5432 bị chiếm.

**Giải pháp**:
```bash
# Check containers
docker compose ps postgres

# Check port
netstat -an | grep 5432  # Linux/Mac
netstat -an | findstr 5432  # Windows

# Restart container
docker compose restart postgres
```

### 2. Authentication Failed

**Nguyên nhân**: Sai username/password.

**Giải pháp**: Kiểm tra lại credentials trong `infra/postgres/init.sql` hoặc tạo lại database:
```bash
docker compose down -v  # Xóa volumes
docker compose up -d
```

### 3. Driver Not Found

**Nguyên nhân**: PostgreSQL driver chưa được download.

**Giải pháp**: 
- Click **Test Connection** → DBeaver tự download
- Hoặc **Edit Connection** → **Edit Driver Settings** → **Download/Update**

### 4. SSL Connection Error

**Nguyên nhân**: PostgreSQL config SSL, DBeaver config không match.

**Giải pháp**: 
- **Edit Connection** → tab **SSL**
- Uncheck **Use SSL** (local development)

### 5. Table Not Found

**Nguyên nhân**: Chưa chọn đúng schema.

**Giải pháp**:
```sql
-- Set schema
SET search_path TO public;
```

---

## Tips & Tricks

### 1. Dark Theme

**Preferences** → **Appearance** → Chọn theme tối

### 2. Auto-complete

Enable trong **Preferences** → **SQL Editor** → **Auto completion**

### 3. Format SQL

Right-click trong SQL editor → **Format SQL** hoặc Ctrl+Shift+F

### 4. Export result set

Click icon **Export Data** (trong result tab) để export kết quả query

### 5. Bookmarks

Right-click database/table → **Create Bookmark** để dễ truy cập

### 6. Split View

**Window** → **New Editor** để mở nhiều SQL editor

### 7. Search trong Database

Right-click database → **Tools** → **Search Objects**

---

## ER Diagram

Tạo ER diagram tự động trong DBeaver:

1. Right-click database → **View Diagram**
2. Chọn tables muốn hiển thị
3. Click **OK**
4. DBeaver sẽ generate ER diagram với relationships

**Export ER Diagram**:
- Right-click diagram → **Export Diagram** → chọn format (PNG, PDF, ...)

---

## Performance Tips

### 1. Giới hạn rows khi view data

**Preferences** → **SQL Editor** → **Data Editor** → **Default rows limit**: 1000

### 2. Cache settings

**Preferences** → **Connections** → **Cache settings** để tối ưu performance

### 3. Connection pooling

**Edit Connection** → tab **Connection pools** → Configure pool settings

---

## Backup & Restore

### Backup

**Export Data** → chọn **SQL** format → chọn **Include CREATE/DROP statements**

### Restore

**Tools** → **Execute Script** → chọn SQL file backup

---

## Kết nối từ Docker Container (Optional)

Nếu muốn kết nối từ trong container:

```bash
# Vào container
docker compose exec postgres bash

# Connect bằng psql
psql -U product -d product_db
psql -U account -d account_db

# Run queries
SELECT * FROM product;
\q  # Quit
```

---

## Tài liệu tham khảo

- [DBeaver Documentation](https://dbeaver.com/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Data Architecture](./data-architecture.md)
- [Architecture Overview](./architecture.md)

