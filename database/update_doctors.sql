-- Cập nhật dữ liệu bác sĩ theo schema staff trong clinic_system.sql
-- LƯU Ý: is_deleted = 0 là đang hoạt động, is_deleted = 1 là đã xóa (soft delete)

USE clinic_system;
SET SQL_SAFE_UPDATES = 0;

-- Đảm bảo cột specialty_treatment
ALTER TABLE staff MODIFY COLUMN specialty_treatment VARCHAR(255);

-- Phân bổ bác sĩ vào các khoa (luân phiên)
SET @expertise_count = (SELECT COUNT(*) FROM expertise);

SET @row_num = 0;
DROP TEMPORARY TABLE IF EXISTS temp_doctor_rownum;
CREATE TEMPORARY TABLE temp_doctor_rownum AS
SELECT staff_id, @row_num := @row_num + 1 AS row_num
FROM staff
WHERE staff_type = 'DOCTOR' AND is_deleted = 0
ORDER BY staff_id;

SET @exp_row = 0;
DROP TEMPORARY TABLE IF EXISTS temp_expertise_rownum;
CREATE TEMPORARY TABLE temp_expertise_rownum AS
SELECT expertise_id, @exp_row := @exp_row + 1 AS exp_row
FROM expertise
ORDER BY expertise_id;

DROP TEMPORARY TABLE IF EXISTS temp_mapping;
CREATE TEMPORARY TABLE temp_mapping AS
SELECT d.staff_id, e.expertise_id
FROM temp_doctor_rownum d
JOIN temp_expertise_rownum e
  ON e.exp_row = ((d.row_num - 1) % @expertise_count) + 1;

UPDATE staff s
JOIN temp_mapping m ON s.staff_id = m.staff_id
SET s.expertise_id = m.expertise_id
WHERE s.staff_type = 'DOCTOR' AND s.is_deleted = 0;

DROP TEMPORARY TABLE IF EXISTS temp_doctor_rownum;
DROP TEMPORARY TABLE IF EXISTS temp_expertise_rownum;
DROP TEMPORARY TABLE IF EXISTS temp_mapping;

-- Cập nhật specialty_treatment theo expertise_id
UPDATE staff s
JOIN expertise e ON s.expertise_id = e.expertise_id
SET s.specialty_treatment = CASE e.expertise_id
    WHEN 1 THEN 'Khám và điều trị các bệnh lý thông thường, chăm sóc sức khỏe gia đình, tư vấn phòng bệnh'
    WHEN 2 THEN 'Khám và điều trị các bệnh lý về gan, mật, tụy, đường tiêu hóa, viêm gan, sỏi mật'
    WHEN 3 THEN 'Khám tổng quát, phát hiện sớm bệnh lý nội khoa, theo dõi sức khỏe định kỳ'
    WHEN 4 THEN 'Khám và điều trị các rối loạn nội tiết: tiểu đường, tuyến giáp, rối loạn mỡ máu'
    WHEN 5 THEN 'Khám và điều trị các bệnh về da, mụn, viêm da cơ địa, nấm, dị ứng da'
    WHEN 6 THEN 'Khám và điều trị bệnh tim mạch: tăng huyết áp, suy tim, rối loạn nhịp'
    WHEN 7 THEN 'Khám và điều trị các bệnh lý thần kinh: đau đầu, động kinh, tai biến mạch máu não'
    WHEN 8 THEN 'Khám và điều trị bệnh lý cơ, xương, khớp: thoái hóa khớp, viêm khớp, đau cơ'
    WHEN 9 THEN 'Khám và điều trị bệnh lý tai, mũi, họng: viêm xoang, viêm họng, ù tai'
    WHEN 10 THEN 'Khám và điều trị các bệnh về mắt: cận thị, đục thủy tinh thể, glôcôm'
    WHEN 11 THEN 'Khám và điều trị bệnh lý tiêu hóa: viêm dạ dày, đau bụng, táo bón, tiêu chảy'
    WHEN 12 THEN 'Khám và điều trị bệnh truyền nhiễm: sốt xuất huyết, tay chân miệng, nhiễm khuẩn'
    WHEN 13 THEN 'Khám và điều trị bệnh hô hấp: viêm phổi, hen suyễn, COPD'
    WHEN 14 THEN 'Khám và điều trị bệnh lý tiết niệu, thận, bàng quang, nhiễm trùng đường tiết niệu'
    WHEN 15 THEN 'Phẫu thuật và điều trị bệnh lý cơ xương khớp: gãy xương, thoát vị đĩa đệm'
    WHEN 16 THEN 'Khám và điều trị bệnh phụ khoa, thai sản, kế hoạch hóa gia đình'
    WHEN 17 THEN 'Phẫu thuật đường tiêu hóa, gan mật, cắt ruột thừa, sỏi mật'
    WHEN 18 THEN 'Phẫu thuật và điều trị bệnh lý tiết niệu, nam khoa, sỏi thận'
    WHEN 19 THEN 'Tư vấn tâm lý, điều trị stress, lo âu, trầm cảm, rối loạn giấc ngủ'
    WHEN 20 THEN 'Khám và điều trị các rối loạn tâm thần: trầm cảm, lo âu, rối loạn nhân cách'
    WHEN 21 THEN 'Phẫu thuật bệnh lý hô hấp: ung thư phổi, tràn khí màng phổi'
    WHEN 22 THEN 'Phẫu thuật thần kinh: chấn thương sọ não, u não, thoát vị đĩa đệm cột sống'
    WHEN 23 THEN 'Phẫu thuật lồng ngực và mạch máu, bệnh lý tim mạch, phình động mạch'
    WHEN 24 THEN 'Tư vấn dinh dưỡng, xây dựng chế độ ăn cho người bệnh tiểu đường, béo phì'
    WHEN 25 THEN 'Điều trị thẩm mỹ da: xóa nếp nhăn, laser, trị sẹo, nâng cơ'
    WHEN 26 THEN 'Phẫu thuật tổng quát: các bệnh ngoại khoa thông thường'
    WHEN 27 THEN 'Khám và điều trị dị ứng, miễn dịch: viêm mũi dị ứng, nổi mề đay, hen'
    WHEN 28 THEN 'Khám và điều trị bệnh răng miệng: sâu răng, viêm nướu, niềng răng'
    WHEN 29 THEN 'Điều trị chấn thương và chỉnh hình: gãy xương, bó bột, phục hồi chức năng'
    WHEN 30 THEN 'Khám và điều trị vô sinh, hiếm muộn, hỗ trợ sinh sản'
    WHEN 31 THEN 'Phẫu thuật điều trị ung thư, u bướu'
    WHEN 32 THEN 'Điều trị ung thư bằng hóa trị, xạ trị, điều trị giảm nhẹ'
    WHEN 33 THEN 'Khám và điều trị các bệnh lý nam khoa: viêm tuyến tiền liệt, rối loạn cương dương'
    WHEN 34 THEN 'Khám và điều trị bệnh lý người cao tuổi: tăng huyết áp, tiểu đường, loãng xương'
    WHEN 35 THEN 'Phục hồi chức năng, vật lý trị liệu cho bệnh nhân sau chấn thương, tai biến'
    WHEN 36 THEN 'Điều trị bằng y học cổ truyền: châm cứu, bấm huyệt, xoa bóp, thuốc nam'
    WHEN 37 THEN 'Can thiệp tim mạch: đặt stent, can thiệp mạch vành'
    WHEN 38 THEN 'Phẫu thuật tạo hình thẩm mỹ: nâng mũi, cắt mí, hút mỡ'
    WHEN 39 THEN 'Điều trị bệnh cơ xương khớp bằng phương pháp chiropractic, nắn chỉnh cột sống'
    WHEN 40 THEN 'Khám và điều trị răng miệng tổng quát: trám răng, nhổ răng, vệ sinh răng'
    WHEN 41 THEN 'Khám và điều trị bệnh lý trẻ em: sốt, ho, tiêu chảy, dinh dưỡng nhi'
    ELSE 'Khám và tư vấn sức khỏe tổng quát'
END
WHERE s.staff_type = 'DOCTOR' AND s.is_deleted = 0;

-- Điền thông tin còn thiếu (chỉ khi NULL/rỗng)
UPDATE staff SET experience = '5 năm kinh nghiệm'
WHERE staff_type = 'DOCTOR' AND is_deleted = 0 AND (experience IS NULL OR experience = '');

UPDATE staff SET gender = 'Nam'
WHERE staff_type = 'DOCTOR' AND is_deleted = 0 AND (gender IS NULL OR gender = '');

-- Dữ liệu mẫu ngẫu nhiên (tùy chọn — bỏ comment nếu cần)
-- UPDATE staff SET date_of_birth = DATE_ADD('1970-01-01', INTERVAL FLOOR(RAND() * (365 * 30)) DAY) WHERE staff_type = 'DOCTOR' AND is_deleted = 0;
-- UPDATE staff SET phone = CONCAT('090', LPAD(FLOOR(RAND() * 10000000), 7, '0')) WHERE staff_type = 'DOCTOR' AND is_deleted = 0;

-- Đảm bảo bác sĩ đang hoạt động (KHÔNG set is_deleted = 1)
UPDATE staff SET is_deleted = 0 WHERE staff_type = 'DOCTOR' AND is_deleted IS NULL;

-- Kiểm tra
SELECT e.expertise_name, COUNT(s.staff_id) AS so_luong_bac_si
FROM staff s
JOIN expertise e ON s.expertise_id = e.expertise_id
WHERE s.staff_type = 'DOCTOR' AND s.is_deleted = 0
GROUP BY e.expertise_name
ORDER BY so_luong_bac_si DESC;

SELECT staff_id, full_name, e.expertise_name, specialty_treatment, image_url, is_deleted
FROM staff s
LEFT JOIN expertise e ON s.expertise_id = e.expertise_id
WHERE s.staff_type = 'DOCTOR' AND s.is_deleted = 0
LIMIT 20;
