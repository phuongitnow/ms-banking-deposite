## Tài liệu BA (MVP)

### Mục tiêu
Xây dựng backend hỗ trợ mobile app cho sản phẩm tiền gửi (deposit) với các luồng: tạo sản phẩm, mở sổ, phê duyệt, thông báo.

### Phạm vi MVP
- Quản lý sản phẩm gửi tiết kiệm (tên, kỳ hạn, mức tiền, lãi suất).
- Tạo yêu cầu mở sổ cho khách hàng (productId, customerId, amount).
- Phê duyệt/từ chối yêu cầu mở sổ.
- Cập nhật read model tài khoản/sổ để mobile tra cứu.
- Gửi thông báo (mock) theo sự kiện.

### Đối tượng sử dụng
- Khách hàng (qua mobile app).
- Nhân viên phê duyệt (giả lập qua API trong MVP).

### Yêu cầu chức năng
1) Sản phẩm
- Tạo sản phẩm: tên, kỳ hạn (tháng), min/max amount, rate%.
- Tra cứu sản phẩm theo id.

2) Yêu cầu mở sổ
- Tạo yêu cầu: productId, customerId, amount.
- Tra cứu theo id.
- Phê duyệt hoặc từ chối.

3) Tài khoản/sổ (read model)
- Tra cứu tài khoản theo `customerId` (MVP dùng `accountId == customerId`).
- Tra cứu danh sách sổ theo `accountId`.

4) Tính lãi (read-only)
- Tính rate và lãi ước tính theo `term`, `amount`, `productId` (tùy chọn).

5) Thông báo (mock)
- Gửi thông báo theo sự kiện hoặc qua endpoint test.

### Yêu cầu phi chức năng (MVP)
- Độ tin cậy: Outbox + Debezium đảm bảo phát tán sự kiện.
- Khả năng mở rộng: tách microservices; Kafka làm event backbone.
- Quan sát: Actuator; logs.

### Use cases và Tiêu chí nghiệm thu (AC)
- UC1: Tạo sản phẩm
  - AC: 201/200, trả `id` sản phẩm; outbox có `deposit.product.created`.
- UC2: Mở sổ
  - AC: 201/200, trả `depositId`; outbox có `deposit.requested`.
- UC3: Phê duyệt yêu cầu
  - AC: 202/200; event `deposit.approved`; account-service ghi `account_deposit` với status APPROVED.
- UC4: Từ chối yêu cầu
  - AC: 202/200; event `deposit.rejected`; account-service ghi status REJECTED.
- UC5: Tra cứu tài khoản/sổ
  - AC: 200, trả thông tin account/sổ đúng với event đã xử lý.
- UC6: Tính lãi
  - AC: 200, trả `ratePercent`, `estimatedInterest`.

### Ràng buộc & Giả định
- `accountId == customerId` trong MVP.
- Auth chưa bật qua Kong (dev local).
- Phê duyệt do API gọi thẳng (mock role approver).
