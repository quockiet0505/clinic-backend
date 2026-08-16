-- Nguồn tham khảo dữ liệu:
-- 1. Cổng thông tin Ngân hàng Dữ liệu Ngành Dược (Drugbank Vietnam - https://drugbank.vn) của Cục Quản lý Dược - Bộ Y tế Việt Nam.
-- 2. Hệ thống Cơ sở dữ liệu Dược học Quốc tế DrugBank Online (https://go.drugbank.com).

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM medicine WHERE medicine_id BETWEEN 51 AND 100;

INSERT INTO medicine (medicine_id, name, active_element, packing_standard, base_unit, usage_note, is_deleted, created_at) VALUES 
(51, 'ccc 100mg', 'Aceclofenac', 'Hộp 60 viên nén', 'Viên', 'Uống sau bữa ăn no để tránh kích ứng dạ dày. CHỐNG CHỈ ĐỊNH phối hợp với Ketorolac.', 0, NOW()),
(52, 'Toradol 10mg', 'Ketorolac', 'Hộp 20 viên nén', 'Viên', 'Dùng giảm đau ngắn hạn. CHỐNG CHỈ ĐỊNH dùng chung với các NSAID khác như Aceclofenac/Ibuprofen.', 0, NOW()),
(53, 'Soriatane 10mg', 'Acitretin', 'Hộp 30 viên nang', 'Viên', 'Điều trị vảy nến nặng. CHỐNG CHỈ ĐỊNH dùng chung với Tetracyclin/Doxycyclin.', 0, NOW()),
(54, 'Doxycyclin 100mg DHG', 'Doxycyclin', 'Hộp 100 viên nang', 'Viên', 'Uống with nhiều nước, không nằm ngay sau khi uống. Không dùng chung với Acitretin.', 0, NOW()),
(55, 'Cordarone 200mg', 'Amiodaron', 'Hộp 30 viên nén', 'Viên', 'Thuốc chống loạn nhịp tim. CHỐNG CHỈ ĐỊNH phối hợp với Domperidon/Colchicin/Levofloxacin.', 0, NOW()),
(56, 'Motilium 10mg', 'Domperidon', 'Hộp 100 viên nén', 'Viên', 'Uống trước ăn 15-30 phút. Không phối hợp với Amiodaron vì nguy cơ kéo dài khoảng QT.', 0, NOW()),
(57, 'Colchicin 1mg Traphaco', 'Colchicin', 'Hộp 20 viên nén', 'Viên', 'Điều trị cơn Gút cấp. Không phối hợp với Amiodaron vì gây ngộ độc Colchicin.', 0, NOW()),
(58, 'Valdoxan 25mg', 'Agomelatin', 'Hộp 28 viên nén', 'Viên', 'Điều trị trầm cảm. CHỐNG CHỈ ĐỊNH phối hợp với kháng sinh Ciprofloxacin.', 0, NOW()),
(59, 'Ciprobay 500mg', 'Ciprofloxacin', 'Hộp 10 viên nén', 'Viên', 'Kháng sinh nhóm quinolone. Tránh dùng cùng Agomelatin.', 0, NOW()),
(60, 'Rasilez 150mg', 'Aliskiren', 'Hộp 28 viên nén', 'Viên', 'Điều trị cao huyết áp. CHỐNG CHỈ ĐỊNH phối hợp với ACEI/ARB ở người suy thận hoặc đái tháo đường.', 0, NOW()),
(61, 'Lopril 25mg', 'Captopril', 'Hộp 100 viên nén', 'Viên', 'Uống trước bữa ăn 1 tiếng. Không phối hợp với Aliskiren.', 0, NOW()),
(62, 'Zestril 10mg', 'Lisinopril', 'Hộp 28 viên nén', 'Viên', 'Thuốc ức chế men chuyển. Tránh dùng chung với Aliskiren.', 0, NOW()),
(63, 'Xanax 0.5mg', 'Alprazolam', 'Hộp 30 viên nén', 'Viên', 'Thuốc an thần. CHỐNG CHỈ ĐỊNH phối hợp với Indinavir.', 0, NOW()),
(64, 'Crixivan 400mg', 'Indinavir', 'Hộp 180 viên nang', 'Viên', 'Thuốc kháng virus HIV. CHỐNG CHỈ ĐỊNH phối hợp với Alprazolam/Amiodaron.', 0, NOW()),
(65, 'Viagra 50mg', 'Sildenafil', 'Hộp 4 viên nén', 'Viên', 'Điều trị rối loạn cương dương. CHỐNG CHỈ ĐỊNH dùng chung với các thuốc Nitrat (Nitroglycerin/Isosorbid).', 0, NOW()),
(66, 'Nitromint 2.6mg', 'Nitroglycerin', 'Hộp 30 viên nén giải phóng chậm', 'Viên', 'Điều trị đau thắt ngực. CHỐNG CHỈ ĐỊNH dùng chung với Sildenafil.', 0, NOW()),
(67, 'Zocor 20mg', 'Simvastatin', 'Hộp 30 viên nén', 'Viên', 'Thuốc hạ mỡ máu. CHỐNG CHỈ ĐỊNH phối hợp với Clarithromycin/Erythromycin/Itraconazole/Ketoconazole.', 0, NOW()),
(68, 'Klacid 500mg', 'Clarithromycin', 'Hộp 14 viên nén', 'Viên', 'Kháng sinh macrolide. CHỐNG CHỈ ĐỊNH dùng chung với Simvastatin.', 0, NOW()),
(69, 'Erythromycin 250mg', 'Erythromycin', 'Hộp 100 viên nén', 'Viên', 'Kháng sinh macrolide. CHỐNG CHỈ ĐỊNH dùng chung với Simvastatin.', 0, NOW()),
(70, 'Sporal 100mg', 'Itraconazole', 'Hộp 4 viên nang', 'Viên', 'Thuốc kháng nấm. CHỐNG CHỈ ĐỊNH phối hợp với Simvastatin.', 0, NOW()),
(71, 'Nizoral 200mg', 'Ketoconazole', 'Hộp 10 viên nén', 'Viên', 'Thuốc kháng nấm đường uống. CHỐNG CHỈ ĐỊNH phối hợp với Simvastatin.', 0, NOW()),
(72, 'Gofen 400', 'Ibuprofen', 'Hộp 50 viên nang mềm', 'Viên', 'Giảm đau chống viêm. Thận trọng khi dùng cùng Aspirin hoặc Methotrexate.', 0, NOW()),
(73, 'Aspirin pH8 81mg', 'Aspirin', 'Hộp 100 viên nén kháng dịch vị', 'Viên', 'Dự phòng huyết khối. Tránh phối hợp liều cao với Ibuprofen/Warfarin.', 0, NOW()),
(74, 'Aldactone 25mg', 'Spironolactone', 'Hộp 100 viên nén', 'Viên', 'Thuốc lợi tiểu giữ kali. Thận trọng nguy cơ tăng kali máu khi dùng cùng Enalapril/Valsartan.', 0, NOW()),
(75, 'Renitec 5mg', 'Enalapril', 'Hộp 30 viên nén', 'Viên', 'Thuốc hạ huyết áp nhóm ức chế men chuyển. Tránh dùng cùng Spironolactone.', 0, NOW()),
(76, 'Diovan 80mg', 'Valsartan', 'Hộp 28 viên nén', 'Viên', 'Thuốc chẹn thụ thể Angiotensin II. Tránh dùng cùng Spironolactone.', 0, NOW()),
(77, 'Trexall 2.5mg', 'Methotrexate', 'Hộp 30 viên nén', 'Viên', 'Điều trị ung thư/khớp tự miễn. Tránh phối hợp với NSAID (Ibuprofen) vì tăng độc tính.', 0, NOW()),
(78, 'Lanoxin 0.25mg', 'Digoxin', 'Hộp 30 viên nén', 'Viên', 'Điều trị suy tim. Cần giảm liều Digoxin khi phối hợp với Amiodaron/Verapamil.', 0, NOW()),
(79, 'Isoptin 80mg', 'Verapamil', 'Hộp 100 viên nén', 'Viên', 'Thuốc chẹn kênh calci trị loạn nhịp. Tránh dùng chung hoặc cần giảm liều Digoxin.', 0, NOW()),
(80, 'Coumadin 5mg', 'Warfarin', 'Hộp 28 viên nén', 'Viên', 'Thuốc chống đông máu. Rất dễ tương tác gây chảy máu với Aspirin/Ibuprofen/Tamoxifen.', 0, NOW()),
(81, 'Zyloric 300mg', 'Allopurinol', 'Hộp 28 viên nén', 'Viên', 'Điều trị Gút (giảm acid uric). Cần giảm liều mạnh Azathioprine khi phối hợp.', 0, NOW()),
(82, 'Imuran 50mg', 'Azathioprine', 'Hộp 100 viên nén', 'Viên', 'Thuốc ức chế miễn dịch. Tương tác mạnh với Allopurinol gây suy tủy xương.', 0, NOW()),
(83, 'Tavanic 500mg', 'Levofloxacin', 'Hộp 5 viên nén', 'Viên', 'Kháng sinh fluoroquinolone. Tránh phối hợp với Amiodaron vì nguy cơ kéo dài khoảng QT.', 0, NOW()),
(84, 'Isoket 20mg', 'Isosorbid dinitrat', 'Hộp 50 viên nén', 'Viên', 'Thuốc giãn mạch điều trị đau thắt ngực. CHỐNG CHỈ ĐỊNH phối hợp với Sildenafil.', 0, NOW()),
(85, 'Nolvadex 20mg', 'Tamoxifen', 'Hộp 30 viên nén', 'Viên', 'Điều trị ung thư vú. Tương tác gây tăng xuất huyết khi dùng cùng Warfarin.', 0, NOW()),
(86, 'Minocyclin 50mg', 'Minocyclin', 'Hộp 100 viên nang', 'Viên', 'Kháng sinh nhóm tetracyclin. Không dùng chung với Acitretin vì nguy cơ tăng áp nội sọ.', 0, NOW()),
(87, 'Tetracyclin 250mg', 'Tetracyclin', 'Hộp 100 viên nang', 'Viên', 'Kháng sinh giá rẻ. Không phối hợp với Acitretin.', 0, NOW()),
(88, 'Cezin 10mg', 'Cetirizin', 'Hộp 100 viên nén', 'Viên', 'Kháng histamine H1 trị dị ứng.', 0, NOW()),
(89, 'Telfast 60mg', 'Fexofenadin', 'Hộp 20 viên nén', 'Viên', 'Kháng histamine trị dị ứng.', 0, NOW()),
(90, 'Medrol 4mg', 'Methylprednisolone', 'Hộp 30 viên nén', 'Viên', 'Thuốc kháng viêm steroid.', 0, NOW()),
(91, 'Medrol 16mg', 'Methylprednisolone', 'Hộp 30 viên nén', 'Viên', 'Thuốc kháng viêm steroid liều cao.', 0, NOW()),
(92, 'Fugacar vị Sô-cô-la', 'Mebendazol', 'Hộp 1 viên nén nhai', 'Viên', 'Thuốc tẩy giun.', 0, NOW()),
(93, 'Glucophage XR 500mg', 'Metformin', 'Hộp 30 viên nén kéo dài', 'Viên', 'Uống trong bữa tối.', 0, NOW()),
(94, 'Lipitor 10mg', 'Atorvastatin', 'Hộp 30 viên nén', 'Viên', 'Thuốc hạ cholesterol.', 0, NOW()),
(95, 'Lipitor 20mg', 'Atorvastatin', 'Hộp 30 viên nén', 'Viên', 'Thuốc hạ cholesterol.', 0, NOW()),
(96, 'Crestor 10mg', 'Rosuvastatin', 'Hộp 28 viên nén', 'Viên', 'Thuốc hạ cholesterol thế hệ mới.', 0, NOW()),
(97, 'Concor 5mg', 'Bisoprolol', 'Hộp 100 viên nén', 'Viên', 'Thuốc chẹn beta trị tăng huyết áp/suy tim.', 0, NOW()),
(98, 'Amlor 5mg', 'Amlodipine', 'Hộp 30 viên nang', 'Viên', 'Thuốc chẹn kênh calci trị cao huyết áp.', 0, NOW()),
(99, 'Tobrex 5ml', 'Tobramycin', 'Chai 5ml thuốc nhỏ mắt', 'Chai', 'Kháng sinh nhỏ mắt.', 0, NOW()),
(100, 'Maalox Plus', 'Aluminium hydroxide, Magnesium hydroxide, Simethicone', 'Hộp 40 viên nhai', 'Viên', 'Giảm đầy hơi và đau dạ dày.', 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;
