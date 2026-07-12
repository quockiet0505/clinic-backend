-- Nguồn tham khảo dữ liệu:
-- 1. Cổng thông tin Ngân hàng Dữ liệu Ngành Dược (Drugbank Vietnam - https://drugbank.vn) của Cục Quản lý Dược - Bộ Y tế Việt Nam.
-- 2. Hệ thống Cơ sở dữ liệu Dược học Quốc tế DrugBank Online (https://go.drugbank.com).

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE medicine;

INSERT INTO medicine (medicine_id, name, active_element, packing_standard, base_unit, usage_note, is_deleted, created_at) VALUES 
(1, 'Hapacol 650', 'Paracetamol', 'Hộp 100 viên sủi', 'Viên', 'Uống sau ăn, cách nhau 4-6 tiếng nếu sốt hoặc đau đầu', 0, NOW()),
(2, 'Panadol Extra', 'Paracetamol, Caffeine', 'Hộp 120 viên nén', 'Viên', 'Uống sau ăn, không dùng quá 8 viên mỗi ngày', 0, NOW()),
(3, 'Efferalgan 500mg', 'Paracetamol', 'Hộp 16 viên sủi', 'Viên', 'Hòa tan vào nước trước khi uống, cách nhau ít nhất 4 tiếng', 0, NOW()),
(4, 'Siro Ceelin 120ml', 'Acid ascorbic', 'Chai 120ml', 'Chai', 'Siro bổ sung Vitamin C cho trẻ em, uống sau ăn sáng', 0, NOW()),
(5, 'Claritin 10mg', 'Loratadin', 'Hộp 10 viên nén', 'Viên', 'Uống 1 viên vào buổi tối trước khi đi ngủ', 0, NOW()),
(6, 'Telfast BD 180mg', 'Fexofenadin', 'Hộp 10 viên nén', 'Viên', 'Uống 1 viên mỗi ngày hỗ trợ giảm dị ứng mũi, nổi mề đay', 0, NOW()),
(7, 'Aerius 5mg', 'Desloratadin', 'Hộp 30 viên nén', 'Viên', 'Uống sau ăn, không gây buồn ngủ', 0, NOW()),
(8, 'Singulair 10mg', 'Montelukast', 'Hộp 28 viên nén', 'Viên', 'Uống buổi tối hỗ trợ hen suyễn và viêm mũi dị ứng', 0, NOW()),
(9, 'Glucophage 850mg', 'Metformin', 'Hộp 30 viên nén', 'Viên', 'Uống ngay trước hoặc trong bữa ăn chính để giảm kích ứng ruột', 0, NOW()),
(10, 'Diamicron MR 30mg', 'Gliclazide', 'Hộp 60 viên nén giải phóng kéo dài', 'Viên', 'Uống 1 lần duy nhất vào bữa ăn sáng', 0, NOW()),
(11, 'Nexium 40mg', 'Esomeprazol', 'Hộp 14 viên nén kháng dịch vị', 'Viên', 'Uống trước bữa ăn sáng 30-60 phút, không nhai viên thuốc', 0, NOW()),
(12, 'Losec MUPS 20mg', 'Omeprazol', 'Hộp 14 viên nén', 'Viên', 'Uống lúc bụng đói vào buổi sáng', 0, NOW()),
(13, 'Pantoloc 40mg', 'Pantoprazol', 'Hộp 7 viên nén', 'Viên', 'Uống trước ăn sáng 1 tiếng', 0, NOW()),
(14, 'Motilium-M 10mg', 'Domperidon', 'Hộp 100 viên nén', 'Viên', 'Uống trước bữa ăn 15-30 phút', 0, NOW()),
(15, 'Hỗn dịch Phosphalugel', 'Colloidal Aluminum Phosphate', 'Hộp 20 gói', 'Gói', 'Uống khi có cơn đau dạ dày hoặc sau bữa ăn 1-2 tiếng', 0, NOW()),
(16, 'Maalox', 'Aluminium hydroxide, Magnesium hydroxide', 'Hộp 40 viên nhai', 'Viên', 'Nhai kỹ trước khi nuốt, dùng sau ăn 1 tiếng hoặc khi đau', 0, NOW()),
(17, 'Smecta', 'Dioctahedral smectite', 'Hộp 30 gói bột pha hỗn dịch', 'Gói', 'Hòa tan vào nước ấm, uống giữa các bữa ăn', 0, NOW()),
(18, 'Enterogermina 2 tỷ/5ml', 'Bacillus clausii', 'Hộp 20 ống', 'Ống', 'Lắc kỹ ống trước khi uống trực tiếp', 0, NOW()),
(19, 'Siro Halixol 100ml', 'Ambroxol', 'Chai 100ml', 'Chai', 'Siro long đờm, uống sau khi ăn no', 0, NOW()),
(20, 'Acemuc 200mg', 'Acetylcystein', 'Hộp 30 gói bột', 'Gói', 'Hòa tan vào nước, tránh uống sát giờ đi ngủ', 0, NOW()),
(21, 'Alpha Choay', 'Chymotrypsin', 'Hộp 30 viên nén', 'Viên', 'Ngậm dưới lưỡi hoặc uống nhiều nước để giảm phù nề chấn thương', 0, NOW()),
(22, 'Calci Corbiere 10ml', 'Calcium glucoheptonate, Vitamin C, PP', 'Hộp 30 ống dung dịch', 'Ống', 'Uống vào buổi sáng hoặc trưa để hấp thụ tốt nhất', 0, NOW()),
(23, 'Maltofer Chewing Tablet', 'Sắt (III) hydroxyd polymaltose', 'Hộp 30 viên nhai', 'Viên', 'Nhai và nuốt trong hoặc ngay sau bữa ăn', 0, NOW()),
(24, 'Enervon', 'Vitamin C, B-complex', 'Hộp 100 viên nén bao phim', 'Viên', 'Uống sau bữa ăn sáng', 0, NOW()),
(25, 'Pharmaton Essential', 'Multivitamin, Khoáng chất', 'Hộp 30 viên nén', 'Viên', 'Uống 1 viên vào bữa ăn sáng để duy trì năng lượng', 0, NOW()),
(26, 'Ventolin Inhaler 100mcg', 'Salbutamol', 'Hộp 1 bình x 200 liều xịt', 'Bình', 'Hít xịt qua miệng khi lên cơn khó thở đột ngột', 0, NOW()),
(27, 'Pulmicort Respules 500mcg', 'Budesonide', 'Hộp 20 ống khí dung', 'Ống', 'Khí dung xông mũi họng, súc miệng sạch sau khi xông', 0, NOW()),
(28, 'Tanakan 40mg', 'Ginkgo biloba extract', 'Hộp 90 viên nén', 'Viên', 'Uống trong bữa ăn chính để tăng tuần hoàn não', 0, NOW()),
(29, 'Betaserc 16mg', 'Betahistin', 'Hộp 60 viên nén', 'Viên', 'Uống sau ăn để giảm chóng mặt, rối loạn tiền đình', 0, NOW()),
(30, 'Imodium 2mg', 'Loperamid', 'Hộp 100 viên nang', 'Viên', 'Uống sau mỗi lần tiêu chảy cấp, không dùng quá 8 viên/ngày', 0, NOW()),
(31, 'Decolgen Forte', 'Paracetamol, Phenylephrine, Chlorpheniramine', 'Hộp 100 viên nén', 'Viên', 'Uống sau ăn để điều trị cảm cúm, sổ mũi', 0, NOW()),
(32, 'Tiffy Dey', 'Paracetamol, Phenylephrine, Chlorpheniramine', 'Hộp 100 viên nén', 'Viên', 'Giảm triệu chứng cảm sốt, nghẹt mũi', 0, NOW()),
(33, 'Strepsils Cool Lozenges', 'Amylmetacresol, Dichlorobenzyl alcohol', 'Hộp 24 viên ngậm', 'Viên', 'Ngậm sát khuẩn và dịu cổ họng, cách 2-3 tiếng ngậm 1 viên', 0, NOW()),
(34, 'Eugica Green Capsule', 'Eucalyptol, Menthol, Tinh dầu Gừng, Tần', 'Hộp 100 viên nang mềm', 'Viên', 'Uống 1-2 viên/lần để trị ho, ấm họng', 0, NOW()),
(35, 'Berberin 10mg', 'Berberin', 'Lọ 100 viên nén', 'Viên', 'Uống khi bị kiết lỵ, nhiễm trùng đường ruột', 0, NOW()),
(36, 'Xịt mũi Otrivin 0.1%', 'Xylometazoline', 'Chai 10ml', 'Chai', 'Xịt co mạch giảm nghẹt mũi, không dùng quá 7 ngày liên tục', 0, NOW()),
(37, 'Tobrex 0.3%', 'Tobramycin', 'Chai 5ml', 'Chai', 'Nhỏ mắt điều trị nhiễm trùng ngoài nhãn cầu', 0, NOW()),
(38, 'Nước nhỏ mắt Maxitrol', 'Dexamethasone, Neomycin, Polymyxin B', 'Chai 5ml', 'Chai', 'Nhỏ mắt chống viêm nhiễm trùng, dùng theo chỉ định của Bác sĩ nhãn khoa', 0, NOW()),
(39, 'Natri Clorid 0.9%', 'Natri clorid', 'Chai 10ml nước nhỏ mắt sinh lý', 'Chai', 'Rửa mắt, mũi hàng ngày giúp làm sạch bụi bẩn', 0, NOW()),
(40, 'Miếng dán Salonpas', 'Methyl salicylate, L-menthol', 'Hộp 20 miếng dán', 'Miếng', 'Dán ngoài da giảm đau cơ xương khớp, không dán lên vết thương hở', 0, NOW()),
(41, 'Voltaren Emulgel 1%', 'Diclofenac', 'Tuýp 20g', 'Tuýp', 'Thoa nhẹ lên vùng khớp sưng đau 3-4 lần/ngày', 0, NOW()),
(42, 'Contractubex Gel 10g', 'Extractum cepae, Heparin sodium, Allantoin', 'Tuýp 10g gel trị sẹo', 'Tuýp', 'Bôi lên sẹo mới hình thành giúp làm mờ sẹo co rút', 0, NOW()),
(43, 'Canesten Cream 1%', 'Clotrimazole', 'Tuýp 20g kem bôi nấm', 'Tuýp', 'Thoa mỏng lên vùng da nhiễm nấm 2-3 lần/ngày', 0, NOW()),
(44, 'Nizoral Cream 2%', 'Ketoconazole', 'Tuýp 10g kem bôi nấm', 'Tuýp', 'Điều trị nấm da, hắc lào, lang ben', 0, NOW()),
(45, 'Nhũ dịch Biafine', 'Trolamine', 'Tuýp 46.5g nhũ dịch', 'Tuýp', 'Bôi dày lên vùng da bị bỏng nhẹ, cháy nắng', 0, NOW()),
(46, 'Duphalac', 'Lactulose', 'Hộp 20 gói dung dịch uống', 'Gói', 'Nhuận tràng điều trị táo bón, uống vào buổi sáng', 0, NOW()),
(47, 'Forlax 10g', 'Macrogol 4000', 'Hộp 20 gói bột pha uống', 'Gói', 'Hòa gói bột vào ly nước đầy, uống buổi sáng', 0, NOW()),
(48, 'Spasfon', 'Phloroglucinol', 'Hộp 30 viên nén', 'Viên', 'Chống co thắt cơ trơn dạ dày, ruột, đường mật và tiết niệu', 0, NOW()),
(49, 'Buscopan 10mg', 'Hyoscine butylbromide', 'Hộp 100 viên nén', 'Viên', 'Giảm co thắt cơ trơn đường tiêu hóa', 0, NOW()),
(50, 'Debridat 100mg', 'Trimebutine', 'Hộp 30 viên nén', 'Viên', 'Điều trị rối loạn chức năng đại tràng, hội chứng ruột kích thích', 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;
