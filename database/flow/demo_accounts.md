# Tài khoản demo — luồng khám & tái khám

Mật khẩu chung: **12345678**

## Chạy seed SQL

```powershell
$env:MYSQL_PWD='12345678'
Get-Content database/flow/seed_clinical_flow_demo.sql -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root clinic_system
```

Script bỏ qua nếu đã có `bn1@gmail.com`.

## Nhân viên / Bác sĩ

| Email | Vai trò | Dùng để test |
|-------|---------|--------------|
| `bacsi@gmail.com` | Bác sĩ | Khám, chỉ định CLS, hoàn tất, hẹn tái khám |
| `letan@gmail.com` | Lễ tân | Check-in, gọi khám, quản lý hàng chờ |
| `lab@gmail.com` | KTV Lab | Nhập kết quả xét nghiệm |

## Bệnh nhân (patient-web / mobile-app)

| Email | Trạng thái demo | Luồng test |
|-------|----------------|------------|
| `bn1@gmail.com` | CHECKED_IN, queue #1 | Chờ gọi khám — không CLS |
| `bn2@gmail.com` | IN_PROGRESS + chỉ định ORDERED | Tạo chỉ định → Chuyển chờ KQ |
| `bn3@gmail.com` | WAITING_RESULT, Lab chưa nhập KQ | Lab nhập KQ → auto về hàng chờ đọc KQ |
| `bn4@gmail.com` | CHECKED_IN queue=0, đã có KQ | Nút **Đọc kết quả** trên admin |
| `bn5@gmail.com` | COMPLETED + follow_up PENDING | Thông báo tái khám — xác nhận/từ chối trên app |
| `bn6@gmail.com` | follow_up ngày mai | Job nhắc D-1 lúc 09:00 |
| `bn7@gmail.com` | follow_up CONFIRMED + nhiều thông báo | Xem danh sách thông báo |
