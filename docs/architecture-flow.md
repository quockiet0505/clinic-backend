# Luồng hoạt động của Backend (Architecture Flow)

Dự án sử dụng kiến trúc phân tầng (Layered Architecture) kết hợp với Spring Boot.

## 1. Cấu trúc các tầng (Layers)

- **Entity (`com.clinic.entity`)**: Định nghĩa các lớp ánh xạ (map) 1-1 với các bảng trong cơ sở dữ liệu.
- **Repository (`com.clinic.repository`)**: Chứa các interface kế thừa từ `JpaRepository` hoặc `JpaSpecificationExecutor`, chịu trách nhiệm giao tiếp trực tiếp với Database (CRUD).
- **Specification (`com.clinic.specification`)**: Nơi chứa các logic lọc (filter), tìm kiếm động và phân trang. Thay vì viết các câu query SQL phức tạp, ta dùng `Specification` (kết hợp với `BaseSpecification`) để build query linh hoạt dựa trên các class `FilterRequest`.
- **Service (`com.clinic.service`)**: Chứa logic nghiệp vụ (Business Logic). Tầng này gọi đến Repository hoặc Specification để lấy dữ liệu, sau đó xử lý và chuyển đổi sang DTO.
- **Controller (`com.clinic.controller`)**: Tiếp nhận các HTTP Request từ Client (Frontend/Mobile), gọi đến Service tương ứng và trả về HTTP Response (thường được bọc trong `ApiResponse`).
- **DTO (`com.clinic.dto`)**: Data Transfer Object. Các đối tượng dùng để truyền tải dữ liệu giữa Client và Server (Request/Response), giúp ẩn đi cấu trúc thực sự của Entity và bảo mật dữ liệu.

## 2. Luồng xử lý một Request tiêu biểu

Ví dụ: Lấy danh sách nhân viên có phân trang và tìm kiếm.

1. **Client (Admin Web)** gửi request: `GET /api/v1/staffs?search=Nguyen&page=0&size=20`
2. **Controller (`StaffController`)**: Nhận request, Spring Boot tự động map các tham số vào object `StaffFilterRequest`. Controller gọi `staffService.getAllStaffs(filterRequest)`.
3. **Service (`StaffService`)**:
   - Gọi `StaffSpecification.filterBy(filterRequest)` để tạo ra câu điều kiện tìm kiếm động (Specification).
   - Gọi `staffRepository.findAll(spec, pageable)` để lấy dữ liệu từ DB.
   - Chuyển đổi danh sách `Staff` (Entity) thành `StaffResponse` (DTO).
   - Đóng gói kết quả vào `PageResponse`.
4. **Repository & Specification**: Hibernate tự động sinh ra câu lệnh SQL tương ứng (ví dụ có `WHERE full_name LIKE %Nguyen%` và `LIMIT 20 OFFSET 0`) và truy vấn Database.
5. **Controller**: Trả về JSON (bọc trong `ApiResponse`) cho Client.
