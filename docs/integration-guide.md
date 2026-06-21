# 🔗 Hướng dẫn Tích hợp (Integration Guide)

Tài liệu hướng dẫn kỹ thuật khi tích hợp hệ thống backend với các nhà cung cấp dịch vụ bên thứ ba (Third-party services).

## 1. Tích hợp Cổng thanh toán VNPay
- **Tài liệu tham khảo:** Sandbox VNPay.
- **Luồng hoạt động:**
  1. Client gọi API Backend `/api/v1/payments/vnpay/create-url`.
  2. Backend sinh URL mã hóa bằng thuật toán `HMAC-SHA512` kết hợp `vnp_HashSecret`.
  3. Client redirect người dùng đến URL của VNPay.
  4. Người dùng thanh toán, VNPay gọi về **Webhook** (IPN Callback) `/api/v1/payments/vnpay/ipn`.
  5. Backend verify chữ ký và cập nhật trạng thái `PAID` cho lịch hẹn.

## 2. Tích hợp Gửi Email (SMTP / SendGrid)
- Hệ thống gửi email bất đồng bộ bằng `@Async` để không block API chính.
- Sử dụng Thymeleaf để render giao diện HTML cho email.
- Thư mục template: `src/main/resources/templates/emails/`.

## 3. Tích hợp Lưu trữ Ảnh (Cloudinary / AWS S3)
- Tất cả Avatar bác sĩ, Bệnh án hình ảnh (X-quang) đều phải đẩy lên Cloud.
- Frontend truyền ảnh dưới dạng `MultipartFile`.
- Backend upload lên Cloudinary, lấy URL trả về lưu vào database.
