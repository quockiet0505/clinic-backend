# 🏥 Clinic Management Backend System

Tài liệu này cung cấp cái nhìn tổng quan về hệ thống Backend của Clinic Management. 
Hệ thống được thiết kế dựa trên kiến trúc Microservices/Monolithic (tùy ngữ cảnh) sử dụng Spring Boot, đảm bảo khả năng mở rộng, bảo mật cao và hiệu suất xử lý tốt cho hàng ngàn lượt truy cập đồng thời từ Admin Web, Patient Web và Mobile App.

## 🌟 Tính năng cốt lõi
- **Quản lý danh tính (Identity & Access Management):** Phân quyền dựa trên RBAC (Role-Based Access Control) cho Admin, Doctor, Patient.
- **Quản lý Lịch hẹn (Appointments):** 4 mode đặt lịch (DOCTOR/EXPERTISE/SERVICE/DIRECT), slot theo khoa/dịch vụ, AI gợi ý `suggested_expertise_id`
- **Hồ sơ y tế điện tử (EMR):** Lưu trữ bệnh án, kết quả xét nghiệm, hình ảnh X-quang an toàn.

## 📚 Tài liệu chi tiết

- [Luồng nghiệp vụ (Business Flows)](business-flows.md) — đặt lịch, khám bệnh, lab, CRM; 4 booking mode
- [API Design](api-design.md) — chuẩn response + appointment endpoints
- [Luồng kỹ thuật Spring Boot (Architecture Flow)](architecture-flow.md)
- [Database](database.md)

## 🛠 Tech Stack
- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
- **Database:** MySQL 8.x / PostgreSQL 15+
- **Caching:** Redis (cho session, OTP, rate-limiting)
- **Message Broker (Tương lai):** RabbitMQ hoặc Kafka
