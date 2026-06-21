# 💻 Coding Conventions & Best Practices

Để duy trì mã nguồn sạch (Clean Code), đội ngũ phát triển cần tuân thủ nghiêm ngặt các quy tắc sau:

## 1. Nguyên tắc thiết kế (Design Principles)
- **SOLID Principles:** Mỗi class chỉ chịu một trách nhiệm (Single Responsibility).
- **DRY (Don't Repeat Yourself):** Tuyệt đối không copy-paste logic. Đưa logic dùng chung vào `Utils` hoặc `BaseService`.
- **KISS (Keep It Simple, Stupid):** Code đơn giản, dễ hiểu, tránh các cấu trúc phức tạp không cần thiết.

## 2. Quy tắc Đặt tên (Naming Rules)
- **Package:** Tất cả chữ cái viết thường, phân cách bởi dấu chấm (ví dụ: `com.clinic.userservice`).
- **Class/Interface:** `PascalCase` (ví dụ: `UserServiceImpl`, `AppointmentRepository`).
- **Method/Variable:** `camelCase` (ví dụ: `getUserById`, `totalPrice`).
- **Constant:** `UPPER_SNAKE_CASE` (ví dụ: `MAX_RETRY_COUNT = 3`).

## 3. Cấu trúc Spring Components
### Controller Layer
- Chỉ xử lý HTTP Request/Response. Không chứa Business Logic.
- Mọi hàm phải trả về đối tượng chuẩn (ví dụ: `ApiResponse<T>`).
```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(new ApiResponse<>(userService.getUserById(id)));
}
```

### Service Layer
- Phải có Interface định nghĩa hợp đồng (Contract) và Class Impl.
- Sử dụng `@Transactional` đúng mức độ cần thiết (tránh đặt trên hàm chỉ có lệnh SELECT).

### Repository Layer
- Chỉ gọi Database. Cố gắng sử dụng Query Method của Spring Data JPA. Tránh viết Native Query trừ khi thật sự tối ưu hiệu năng.
