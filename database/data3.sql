use clinic_system;
select * from account;

-- Lấy patient_id và doctor_id mẫu
SET @patient_id = (SELECT patient_id FROM patient WHERE account_id = 15);
SET @doctor_id = (SELECT staff_id FROM staff WHERE staff_type = 'DOCTOR' LIMIT 1);

-- Tạo appointment (lịch khám)
INSERT INTO appointment (patient_id, main_doctor_id, appointment_date, time_start, time_end, appointment_type, status, created_by)
VALUES (@patient_id, @doctor_id, '2026-05-10', '09:00:00', '09:30:00', 'ONLINE', 'COMPLETED', 'PATIENT');
SET @appointment_id = LAST_INSERT_ID();

-- Tạo medical record
INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, status)
VALUES (@patient_id, @appointment_id, @doctor_id, 
        'Thiếu máu cơ tim cục bộ nhẹ / Rối loạn lipid máu',
        'Atorvastatin 20mg 1 viên/ngày, Aspirin 81mg 1 viên/ngày, kiêng mỡ động vật, tập thể dục 30p/ngày',
        'DONE');
SET @record_id = LAST_INSERT_ID();

-- Tạo prescription (đơn thuốc)
INSERT INTO prescription (record_id) VALUES (@record_id);
SET @prescription_id = LAST_INSERT_ID();

-- Thêm thuốc (nếu chưa có)
INSERT INTO medicine (name, base_unit, sell_price) VALUES 
('Atorvastatin 20mg', 'Viên', 150000),
('Aspirin 81mg', 'Viên', 50000);
SET @med1 = (SELECT medicine_id FROM medicine WHERE name = 'Atorvastatin 20mg');
SET @med2 = (SELECT medicine_id FROM medicine WHERE name = 'Aspirin 81mg');

-- Thêm chi tiết đơn thuốc
INSERT INTO prescription_item (prescription_id, medicine_id, unit, quantity, dosage, price)
VALUES (@prescription_id, @med1, 'Viên', 30, 'Uống buổi tối sau ăn', 150000),
       (@prescription_id, @med2, 'Viên', 30, 'Uống sau ăn sáng', 50000);

-- Tạo service (xét nghiệm) nếu chưa có
INSERT INTO service (service_name, service_type, price) VALUES 
('Cholesterol toàn phần', 'LAB_TEST', 100000),
('Triglyceride', 'LAB_TEST', 80000),
('Điện tâm đồ (ECG)', 'IMAGING', 200000);
SET @service1 = (SELECT service_id FROM service WHERE service_name = 'Cholesterol toàn phần');
SET @service2 = (SELECT service_id FROM service WHERE service_name = 'Triglyceride');
SET @service3 = (SELECT service_id FROM service WHERE service_name = 'Điện tâm đồ (ECG)');

-- Tạo service orders
INSERT INTO service_order (record_id, service_id, ordered_by, status)
VALUES (@record_id, @service1, @doctor_id, 'DONE'),
       (@record_id, @service2, @doctor_id, 'DONE'),
       (@record_id, @service3, @doctor_id, 'DONE');

-- Lấy order_id vừa tạo
SET @order1 = (SELECT order_id FROM service_order WHERE record_id = @record_id AND service_id = @service1);
SET @order2 = (SELECT order_id FROM service_order WHERE record_id = @record_id AND service_id = @service2);
SET @order3 = (SELECT order_id FROM service_order WHERE record_id = @record_id AND service_id = @service3);

-- Nhập kết quả xét nghiệm
INSERT INTO service_result (order_id, result_data, conclusion, entered_by)
VALUES (@order1, '6.2 mmol/L', 'Bất thường', @doctor_id),
       (@order2, '2.5 mmol/L', 'Bất thường', @doctor_id),
       (@order3, 'Nhịp xoang đều, ST chênh xuống nhẹ', 'Bình thường', @doctor_id);