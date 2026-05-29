
USE clinic_system;

-- Tắt khóa ngoại để Insert đồng loạt không bị lỗi ràng buộc
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. INSERT FULL DATA CHUYÊN KHOA (EXPERTISE)
-- =====================================================================
TRUNCATE TABLE expertise;
INSERT INTO expertise (expertise_id, expertise_name, icon_url) VALUES
(1, 'Bác sĩ Gia Đình', '/icons/specialties/bac-si-gia-dinh.png'),
(2, 'Tiêu Hóa Gan Mật', '/icons/specialties/tieu-hoa-gan-mat.png'),
(3, 'Nội Tổng Quát', '/icons/specialties/noi-tong-quat.png'),
(4, 'Nội Tiết', '/icons/specialties/noi-tiet.png'),
(5, 'Da liễu', '/icons/specialties/da-lieu.png'),
(6, 'Nội Tim Mạch', '/icons/specialties/noi-tim-mach.png'),
(7, 'Nội Thần Kinh', '/icons/specialties/noi-than-kinh.png'),
(8, 'Nội Cơ Xương Khớp', '/icons/specialties/noi-co-xuong-khop.png'),
(9, 'Tai Mũi Họng', '/icons/specialties/tai-mui-hong.png'),
(10, 'Mắt', '/icons/specialties/mat.png'),
(11, 'Nội Tiêu Hoá', '/icons/specialties/noi-tieu-hoa.png'),
(12, 'Nội Truyền Nhiễm', '/icons/specialties/noi-truyen-nhiem.png'),
(13, 'Nội Hô Hấp', '/icons/specialties/noi-ho-hap.png'),
(14, 'Nội Tiết Niệu', '/icons/specialties/noi-tiet-nieu.png'),
(15, 'Ngoại Cơ Xương Khớp', '/icons/specialties/ngoai-co-xuong-khop.png'),
(16, 'Sản - Phụ Khoa', '/icons/specialties/san-phu-khoa.png'),
(17, 'Ngoại Tiêu Hoá', '/icons/specialties/ngoai-tieu-hoa.png'),
(18, 'Ngoại Tiết Niệu', '/icons/specialties/ngoai-tiet-nieu.png'),
(19, 'Tâm Lý', '/icons/specialties/tam-ly.png'),
(21, 'Tâm Thần Kinh', '/icons/specialties/tam-than-kinh.png'),
(22, 'Ngoại Hô Hấp', '/icons/specialties/ngoai-ho-hap.png'),
(23, 'Ngoại Thần Kinh', '/icons/specialties/ngoai-than-kinh.png'),
(24, 'Lồng Ngực - Mạch Máu', '/icons/specialties/long-nguc-mach-mau.png'),
(25, 'Dinh Dưỡng', '/icons/specialties/dinh-duong.png'),
(26, 'Thẩm mỹ da', '/icons/specialties/tham-my-da.png'),
(27, 'Ngoại Tổng Quát', '/icons/specialties/ngoai-tong-quat.png'),
(28, 'Dị Ứng - Miễn Dịch Lâm Sàng', '/icons/specialties/di-ung-mien-dich-lam-sang.png'),
(29, 'Răng Hàm Mặt', '/icons/specialties/rang-ham-mat.png'),
(30, 'Chấn Thương Chỉnh Hình', '/icons/specialties/chan-thuong-chinh-hinh.png'),
(31, 'Vô Sinh - Hiếm Muộn', '/icons/specialties/vo-sinh-hiem-muon.png'),
(32, 'Ngoại Ung Bướu', '/icons/specialties/ngoai-ung-buou.png'),
(33, 'Ung Bướu', '/icons/specialties/ung-buou.png'),
(34, 'Nam Khoa', '/icons/specialties/nam-khoa.png'),
(35, 'Lão Khoa', '/icons/specialties/lao-khoa.png'),
(36, 'Vật Lý Trị Liệu - Phục Hồi Chức Năng', '/icons/specialties/vat-ly-tri-lieu-phuc-hoi-chuc-nang.png'),
(37, 'Y Học Cổ Truyền', '/icons/specialties/y-hoc-co-truyen.png'),
(38, 'Tim Mạch Can Thiệp', '/icons/specialties/tim-mach-can-thiep.png'),
(39, 'Tạo hình thẩm mỹ', '/icons/specialties/tao-hinh-tham-my.png'),
(40, 'Cơ Xương Khớp - Chiropractic', '/icons/specialties/co-xuong-khop-chiropractic.png'),
(41, 'Nha Khoa', '/icons/specialties/nha-khoa.png'),
(42, 'Nhi Khoa', '/icons/specialties/nhi-khoa.png');


-- =====================================================================
-- 2. INSERT FULL DATA BÁC SĨ (STAFF)
-- =====================================================================
TRUNCATE TABLE staff;
INSERT INTO staff (staff_id, staff_type, full_name, expertise_id, gender, image_url) VALUES
(1, 'DOCTOR', 'BS CKII. Ngô Trung Nam', 16, 'MALE', '/images/doctors/doctor-digimed_NAMNT.jpg'),
(2, 'DOCTOR', 'BS. Lê Tuấn', 3, 'MALE', '/images/doctors/doctor-digimed_TUANL715.jpg'),
(3, 'DOCTOR', 'BS. Nguyễn Trường Mạnh', 31, 'MALE', '/images/doctors/doctor-digimed_MANHNT505.jpg'),
(4, 'DOCTOR', 'Bác sĩ Trần Thị Mây', 19, 'FEMALE', '/images/doctors/doctor-digimed_MAYTT869.jpg'),
(5, 'DOCTOR', 'Bác sĩ Vũ Đình Khôi', 12, 'MALE', '/images/doctors/doctor-digimed_KHOIVD039.jpg'),
(6, 'DOCTOR', 'BS.CKI. Đỗ Đăng Khoa', 38, 'MALE', '/images/doctors/doctor-digimed_KHOADD.jpg'),
(7, 'DOCTOR', 'BS CKI. Vũ Thị Hà', 10, 'FEMALE', '/images/doctors/doctor-digimed_HAVUT.jpg'),
(8, 'DOCTOR', 'BS CKI. Nguyễn Thị Sen', 5, 'FEMALE', '/images/doctors/doctor-digimed_SENNT816.jpg'),
(9, 'DOCTOR', 'Ths BS. Lê Hoàng Thiên', 3, 'MALE', '/images/doctors/doctor-digimed_THIENLH.jpg'),
(10, 'DOCTOR', 'BS. Đặng Quốc Bảo', 42, 'MALE', '/images/doctors/doctor-digimed_BAODQ.jpg'),
(11, 'DOCTOR', 'BS. Hoàng Thị Anh Thư', 19, 'FEMALE', '/images/doctors/doctor-digimed_THUHTA.jpg'),
(12, 'DOCTOR', 'BS CKI. Nguyễn Phúc Thiện', 6, 'MALE', '/images/doctors/doctor-digimed_THIENNP410.jpg'),
(13, 'DOCTOR', 'Bác sĩ Nguyễn Thị Hường', 10, 'FEMALE', '/images/doctors/doctor-digimed_HUONGNT195.jpg'),
(14, 'DOCTOR', 'BS. Vũ Thị Thư', 21, 'FEMALE', '/images/doctors/doctor-digimed_THUVT739.jpg'),
(15, 'DOCTOR', 'BS CKI. Lê Ngọc Hồng Hạnh', 42, 'FEMALE', '/images/doctors/doctor-digimed_HANHLHN.jpg'),
(16, 'DOCTOR', 'Ths BS. Lê Vũ Tân', 34, 'MALE', '/images/doctors/doctor-digimed_TANVUL.jpg'),
(17, 'DOCTOR', 'Ths BS. Trần Thị Oanh', 16, 'FEMALE', '/images/doctors/doctor-digimed_HUONGTH.jpg'),
(18, 'DOCTOR', 'Ths BS. Trịnh Xuân Quân', 9, 'MALE', '/images/doctors/doctor-20de90c054c24de6b0c364316e394aa8.jpg'),
(19, 'DOCTOR', 'Bác sĩ Phan Thị Nga', 42, 'FEMALE', '/images/doctors/doctor-digimed_NGAPT462.jpg'),
(20, 'DOCTOR', 'ThS CKI BSNT. Lê Chí Hiếu', 42, 'MALE', '/images/doctors/doctor-digimed_HIEULC.jpg'),
(21, 'DOCTOR', 'Ths BS. Nguyễn Ngọc Bách', 42, 'MALE', '/images/doctors/doctor-2f3caacda3124217a9c4be86b846f19d.jpg'),
(22, 'DOCTOR', 'Bác sĩ Ca Thị Lan Nhi', 16, 'FEMALE', '/images/doctors/doctor-digimed_NHICTL482.jpg'),
(23, 'DOCTOR', 'Ths BS. Nguyễn Thị Mỹ Linh', 8, 'FEMALE', '/images/doctors/doctor-digimed_LINHNTM.jpg'),
(24, 'DOCTOR', 'Bác sĩ Huỳnh Công', 31, 'MALE', '/images/doctors/doctor-digimed_CONGH894.jpg'),
(25, 'DOCTOR', 'Ths BS. Nguyễn Đức Bảo', 9, 'MALE', '/images/doctors/doctor-digimed_BAOND.jpg'),
(26, 'DOCTOR', 'Bác sĩ Nguyễn Quốc Duy', 42, 'MALE', '/images/doctors/doctor-digimed_DUYNQ.jpg'),
(27, 'DOCTOR', 'Bác sĩ Ngô Tài Dũng', 42, 'MALE', '/images/doctors/doctor-digimed_DUNGNT.jpg'),
(28, 'DOCTOR', 'BS.CKI. Phan Bá Hà', 3, 'MALE', '/images/doctors/doctor-digimed_HAPB.jpg'),
(29, 'DOCTOR', 'Ths BS. Đỗ Thanh Tân', 42, 'MALE', '/images/doctors/doctor-digimed_TANDT.jpg'),
(30, 'DOCTOR', 'TS BS. Phạm Minh Triết', 19, 'MALE', '/images/doctors/doctor-f3bc60c501e4497897a6b7ced212af41.jpg');


-- =====================================================================
-- 3. INSERT FULL DATA GÓI XÉT NGHIỆM / DỊCH VỤ (SERVICE)
-- (Đã xóa trùng lặp giữa Home và List, chuyển tất cả thành dịch vụ)
-- =====================================================================
TRUNCATE TABLE service;

-- Dịch vụ mặc định để gắn giá cho chức năng "Đặt khám Bác sĩ"
INSERT INTO service (service_id, service_name, service_type, price, discount_price) VALUES 
(1, 'Khám Bệnh Chuyên Khoa Cơ Bản', 'EXAM', 0, 0);

-- Các gói xét nghiệm từ JSON (Bắt đầu từ ID 2)
INSERT INTO service (service_name, service_type, price, discount_price, image_url) VALUES
('Gói Xét Nghiệm Sinh Hóa Cơ Bản Tại Nhà', 'LAB_TEST', 1015000, 885000, '/images/services/home/home-pkg-c09a907bb4fb4123913d67fd71b13bd1.jpg'),
('Gói xét nghiệm sức khỏe sinh sản nữ - nâng cao (6 test)', 'LAB_TEST', 1338000, 1338000, '/images/services/home/home-pkg-diag005_DV008.jpg'),
('Gói XN Viêm gan - cơ bản (3 test)', 'LAB_TEST', 420000, 420000, '/images/services/home/home-pkg-diag005_DV011.jpg'),
('Gói XN đái tháo đường - cơ bản (3 test)', 'LAB_TEST', 159000, 159000, '/images/services/home/home-pkg-diag005_DV013.jpg'),
('Gói xét nghiệm máu cơ bản - Nam/Nữ', 'LAB_TEST', 2110000, 2110000, '/images/services/home/home-pkg-pkvietuc_GK007.jpg'),
('Gói XN Viêm gan - nâng cao (6 test)', 'LAB_TEST', 1117000, 1117000, '/images/services/home/home-pkg-diag005_DV012.jpg'),
('Gói Xét Nghiệm Tổng Quát', 'EXAM', 2300000, 2300000, '/images/services/list/list-pkg-default-pkdkvigor_B1.png'),
('Gói Xét Nghiệm Tầm Soát Ung Thư ở Nữ Giới', 'LAB_TEST', 1930000, 1930000, '/images/services/list/list-pkg-default-pkdkvigor_B3.png'),
('Gói xét nghiệm máu chuyên sâu (Tầm soát ung thư) - Nữ', 'LAB_TEST', 4000000, 4000000, '/images/services/list/list-pkg-default-pkvietuc_GK009.png'),
('Gói xét nghiệm máu chuyên sâu (Tầm soát ung thư) - Nam', 'LAB_TEST', 3740000, 3740000, '/images/services/list/list-pkg-default-pkvietuc_GK008.png'),
('Anti HIV Test', 'LAB_TEST', 250000, 250000, '/images/services/list/list-pkg-default-pkvhc_XN03.png'),
('Gói khám thẻ hồng', 'EXAM', 2225000, 2225000, '/images/services/list/list-pkg-default-pkdkvigor_GK04.png'),
('Gói Xét Nghiệm Về Bệnh Truyền Nhiễm', 'LAB_TEST', 1230000, 1069500, '/images/services/list/list-pkg-default-27b0a86d2aa74820bca914a9db126499.png'),
('Gói Xét Nghiệm Tầm Soát Huyết Khối Tại Nhà', 'LAB_TEST', 1390000, 1159000, '/images/services/list/list-pkg-default-pkdkphapanh_DV_XNMedpro_PA_010.png'),
('Gói Xét Nghiệm Chức Năng Gan Chuyên Sâu', 'LAB_TEST', 1478000, 1285500, '/images/services/list/list-pkg-default-efacb250c7ba48d3b3cbd62dfc08aaff.png'),
('Gói Xét Nghiệm Ung Thư Chuyên Sâu', 'LAB_TEST', 1550000, 1398000, '/images/services/list/list-pkg-default-c56ab37db3744fdca76270ddfd3ea536.png'),
('Gói Xét Nghiệm Cao Cấp Cho Nhân Viên Văn Phòng - Nam', 'LAB_TEST', 3180000, 2895000, '/images/services/list/list-pkg-default-78d640068c3949c892bd2900c286c5b1.png'),
('Gói Xét Nghiệm Cao Cấp Cho Nhân Viên Văn Phòng - Nữ', 'LAB_TEST', 3700000, 3218000, '/images/services/list/list-pkg-default-f2bd26f6913045c6804512d585d47ee4.png'),
('Gói Xét Nghiệm Tổng Quát Toàn Diện dành cho Nam', 'LAB_TEST', 4372000, 3801000, '/images/services/list/list-pkg-default-6150c88c3ce040dba9b1cde4a4a8d941.png'),
('Gói Xét Nghiệm Tổng Quát Toàn Diện dành cho Nữ', 'LAB_TEST', 4526000, 3963000, '/images/services/list/list-pkg-default-611aa3c0a52e4b84a42c99456e4aed75.png'),
('Lấy mẫu tại nhà - Xét nghiệm ADN Cha con Thai nhi', 'LAB_TEST', 25000000, 25000000, '/images/services/list/list-pkg-default-cc65a5ce4f6649a699b323c687e82962.png'),
('Tiêu chuẩn Xét nghiệm ADN Hài cốt liệt sĩ', 'LAB_TEST', 12000000, 12000000, '/images/services/list/list-pkg-default-cf9db29f176e45d6b9cf2486c4e63256.png'),
('Tầm soát tiền ung thư bằng xét nghiệm máu Nam', 'LAB_TEST', 2153000, 2153000, '/images/services/list/list-pkg-default-pkvhc_Nam005.png'),
('Gói Xét Nghiệm Tầm Soát Viêm Gan', 'LAB_TEST', 820000, 820000, '/images/services/list/list-pkg-default-pkdkvigor_B4.png'),
('Xét nghiệm tim mạch chuyên khoa', 'LAB_TEST', 4955000, 4955000, '/images/services/list/list-pkg-default-420bcbbc15be4b8fb75344d8feba7e14.png'),
('Gói xét nghiệm KÝ SINH TRÙNG (Nam)', 'LAB_TEST', 2500000, 2500000, '/images/services/list/list-pkg-default-pkdksp79_GOI009.png'),
('Gói xét nghiệm TỔNG QUÁT PREMIUM (Nữ độc thân)', 'LAB_TEST', 8800000, 8800000, '/images/services/list/list-pkg-default-pkdksp79_GOI012.png'),
('Gói xét nghiệm TỔNG QUÁT PREMIUM (Nữ có gia đình)', 'LAB_TEST', 9800000, 9800000, '/images/services/list/list-pkg-default-pkdksp79_GOI013.png'),
('Gói xét nghiệm KÝ SINH TRÙNG (Nữ)', 'LAB_TEST', 2500000, 2500000, '/images/services/list/list-pkg-default-pkdksp79_GOI010.png'),
('Gói xét nghiệm TỔNG QUÁT PREMIUM (Nam)', 'LAB_TEST', 11500000, 11500000, '/images/services/list/list-pkg-default-pkdksp79_GOI011.png'),
('Gói xét nghiệm Tuyến thận - Tiết niệu', 'LAB_TEST', 950000, 950000, '/images/services/list/list-pkg-default-thanhvubl_COVID6.png'),
('Gói xét nghiệm Tuyến giáp - Cận giáp', 'LAB_TEST', 2150000, 2150000, '/images/services/list/list-pkg-default-thanhvubl_COVID3.png'),
('Gói xét nghiệm Gan Mật Tụy', 'LAB_TEST', 1960000, 1960000, '/images/services/list/list-pkg-default-thanhvubl_COVID2.png'),
('Gói xét nghiệm xương khớp', 'LAB_TEST', 1280000, 1280000, '/images/services/list/list-pkg-default-thanhvubl_COVID1.png'),
('Gói xét nghiệm tiền phẫu', 'LAB_TEST', 4450000, 4450000, '/images/services/list/list-pkg-default-bvphusanquoctesaigon_D016.png'),
('Gói xét nghiệm dấu ấn ung thư', 'LAB_TEST', 720000, 720000, '/images/services/list/list-pkg-drcheck_011.png'),
('Gói chỉ định xét nghiệm kiểm tra mỡ máu và Bác sĩ tư vấn', 'LAB_TEST', 525000, 525000, '/images/services/list/list-pkg-default-1574_XN003.png'),
('Gói xét nghiệm Vitamin và khoáng chất cho trẻ em', 'LAB_TEST', 1645000, 1645000, '/images/services/list/list-pkg-default-1574_PKG010.png'),
('Gói xét nghiệm tổng quát Diag Wellness - vàng (27 test)', 'LAB_TEST', 1605000, 1605000, '/images/services/list/list-pkg-default-eff8e3ab8e59446bad4a49e7a0079b29.png');


-- =====================================================================
-- 4. INSERT BẢNG GIÁ KHÁM CỦA 30 BÁC SĨ (DOCTOR_SERVICE_PRICE)
-- (Map giá ConsultationFee từ file JSON của sếp vào service_id = 1)
-- =====================================================================
TRUNCATE TABLE doctor_service_price;
INSERT INTO doctor_service_price (staff_id, service_id, price) VALUES
(1, 1, 200000),
(2, 1, 150000),
(3, 1, 200000),
(4, 1, 300000),
(5, 1, 450000),
(6, 1, 200000),
(7, 1, 150000),
(8, 1, 200000),
(9, 1, 149000),
(10, 1, 200000),
(11, 1, 650000), -- Chỉnh sửa lỗi gõ nhầm 250000650000 thành 650.000đ
(12, 1, 300000),
(13, 1, 150000),
(14, 1, 300000),
(15, 1, 200000),
(16, 1, 200000),
(17, 1, 150000),
(18, 1, 150000),
(19, 1, 150000),
(20, 1, 300000),
(21, 1, 150000),
(22, 1, 200000),
(23, 1, 220000),
(24, 1, 150000),
(25, 1, 150000),
(26, 1, 200000),
(27, 1, 250000),
(28, 1, 150000),
(29, 1, 150000),
(30, 1, 1100000);

-- Bật lại khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;