package com.clinic.config;

import com.clinic.common.enums.*;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.crm.FollowUp;
import com.clinic.entity.medical.*;
import com.clinic.entity.prescription.Medicine;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.LeaveRequest;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.crm.NotificationRepository;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.entity.crm.Feedback;
import com.clinic.entity.crm.Notification;
import com.clinic.repository.medical.*;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.patient.PatientVitalProfileRepository;
import com.clinic.entity.patient.PatientVitalProfile;
import com.clinic.repository.prescription.MedicineRepository;
import com.clinic.repository.prescription.PrescriptionRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.service.staff.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DashboardDataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final ExpertiseRepository expertiseRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordVitalRepository medicalRecordVitalRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceResultRepository serviceResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final InvoiceRepository invoiceRepository;
    private final FollowUpRepository followUpRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorServicePriceRepository doctorServicePriceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PatientVitalProfileRepository patientVitalProfileRepository;
    private final StaffScheduleService staffScheduleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Preparing complete clinic workflow mock data...");

        // 1. Fetch or Seed Expertise
        List<Expertise> expertises = expertiseRepository.findAll();
        if (expertises.isEmpty()) {
            expertises = new ArrayList<>();
            expertises.add(createExpertise("Nội tổng quát"));
            expertises.add(createExpertise("Tai Mũi Họng"));
            expertises.add(createExpertise("Răng Hàm Mặt"));
        }

        // 2. Fetch or Seed Services
        List<Service> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            services = new ArrayList<>();
            services.add(createService("Khám nội tổng quát", ServiceType.EXAM, new BigDecimal("150000")));
            services.add(createService("Khám răng hàm mặt", ServiceType.EXAM, new BigDecimal("200000")));
            services.add(createService("Xét nghiệm tổng công thức máu", ServiceType.LAB_TEST, new BigDecimal("250000")));
            services.add(createService("Siêu âm bụng tổng quát", ServiceType.ULTRASOUND, new BigDecimal("350000")));
            services.add(createService("Chụp X-Quang phổi", ServiceType.X_RAY, new BigDecimal("200000")));
        }

        // 3. Fetch or Seed Medicines
        List<Medicine> medicines = medicineRepository.findAll();
        if (medicines.isEmpty()) {
            medicines = new ArrayList<>();
            medicines.add(createMedicine("Paracetamol 500mg", "Paracetamol", "Hộp 10 vỉ x 10 viên", "Viên", 100));
            medicines.add(createMedicine("Amoxicillin 500mg", "Amoxicillin", "Hộp 10 vỉ x 10 viên", "Viên", 200));
            medicines.add(createMedicine("Ibuprofen 400mg", "Ibuprofen", "Hộp 10 vỉ x 10 viên", "Viên", 150));
            medicines.add(createMedicine("Decolgen Forte", "Paracetamol + Phenylephrine", "Hộp 25 vỉ x 4 viên", "Viên", 120));
            medicines.add(createMedicine("Cetirizine 10mg", "Cetirizine dihydrochloride", "Hộp 10 vỉ x 10 viên", "Viên", 80));
        }

        // 4. Fetch or Seed Staff
        List<Staff> doctorsList = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);
        if (doctorsList.isEmpty()) {
            doctorsList = new ArrayList<>();
            doctorsList.add(createStaff("doctor@clinic.com", "BS. Nguyễn Văn A", StaffType.DOCTOR, expertises.get(0)));
            doctorsList.add(createStaff("doctor2@clinic.com", "BS. Trần Thị B", StaffType.DOCTOR, expertises.get(1)));
            doctorsList.add(createStaff("doctor3@clinic.com", "BS. Phạm Minh C", StaffType.DOCTOR, expertises.get(2)));
        }

        Staff labTech = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0).stream().findFirst().orElse(null);
        if (labTech == null) {
            labTech = createStaff("lab_tech@clinic.com", "KTV. Lê Văn Lab", StaffType.LAB_TECH, null);
        }
        Staff nurse = staffRepository.findByStaffTypeAndIsDeleted(StaffType.NURSE, 0).stream().findFirst().orElse(null);
        if (nurse == null) {
            nurse = createStaff("nurse@clinic.com", "ĐD. Phạm Thị Điều Dưỡng", StaffType.NURSE, null);
        }

        // 5. Fetch or Seed Patients (Always seed exactly 20 clean patients)
        List<Patient> patients = patientRepository.findByIsDeleted(0);
        if (patients.isEmpty()) {
            patients = new ArrayList<>();
            String[] names = {
                "Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", "Phạm Minh Đức", "Huỳnh Thu Thảo",
                "Võ Văn Nam", "Đặng Thị Mai", "Bùi Quốc Anh", "Đỗ Kim Chi", "Ngô Thanh Tùng",
                "Hoàng Diệu Hương", "Phan Văn Khải", "Vũ Thị Lành", "Tống Mỹ Linh", "Lý Khánh An",
                "Trương Ngọc Hải", "Dương Quốc Kiệt", "Nguyễn Thị Kim", "Trần Bảo Long", "Nguyễn Minh Triết"
            };
            for (int i = 0; i < names.length; i++) {
                patients.add(createPatient("patient" + (i + 1) + "@clinic.com", names[i], String.format("090%07d", 1234560 + i)));
            }
        }

        Random rand = new Random();
        int index = 1;
        java.util.Set<String> reviewedPairs = new java.util.HashSet<>();

        // 6. Seed exactly 20 completed workflows in the past/today
        log.info("Generating 20 past transaction records...");
        for (int i = 0; i < 20; i++) {
            LocalDate apptDate = LocalDate.of(2026, 8, 1).plusDays(i % 15);
            Patient patient = patients.get(i % patients.size());
            Staff doctor = doctorsList.get(i % doctorsList.size());
            Service examService = (doctor.getExpertise().getExpertiseName().equals("Răng Hàm Mặt")) ? services.get(1) : services.get(0);
            
            // exactly 10 patients have lab services
            boolean hasLab = (i < 10);
            Service labService = hasLab ? services.get(2) : null; 

            LocalTime timeStart = LocalTime.of(8 + (i % 8), (i % 2 == 0) ? 0 : 30);
            LocalTime timeEnd = timeStart.plusMinutes(30);

            seedCompleteWorkflow(apptDate, timeStart, timeEnd, patient, doctor, labTech, nurse, examService, labService, medicines, index++, rand, reviewedPairs);
        }

        // 7. Seed exactly 5 Calendar appointments for the future
        log.info("Generating 5 future calendar appointments...");
        for (int i = 0; i < 5; i++) {
            LocalDate date = LocalDate.of(2026, 8, 17).plusDays(i);
            Patient patient = patients.get(rand.nextInt(patients.size()));
            Staff doctor = doctorsList.get(rand.nextInt(doctorsList.size()));
            Service service = services.get(0); 

            Appointment upcoming = new Appointment();
            upcoming.setPatient(patient);
            upcoming.setMainDoctor(doctor);
            upcoming.setService(service);
            upcoming.setExpertise(doctor.getExpertise());
            upcoming.setAppointmentDate(date);
            upcoming.setTimeStart(LocalTime.of(9 + i, 30));
            upcoming.setTimeEnd(LocalTime.of(10 + i, 0));
            upcoming.setAppointmentType(AppointmentType.ONLINE);
            upcoming.setStatus(AppointmentStatus.CONFIRMED);
            upcoming.setCreatedBy(CreatedByType.PATIENT);
            upcoming.setBookingMode(BookingMode.DOCTOR);
            upcoming.setIsDeleted(0);
            appointmentRepository.save(upcoming);
        }

        // 7.5 Seed active workflows for today (so the active tabs: Chuẩn bị khám, Đang khám, Chờ kết quả, Đọc kết quả are populated)
        log.info("Generating active today's workflows for demo...");
        LocalDate today = LocalDate.now();
        
        // 2 in Chuẩn bị khám (CHECKED_IN, 1 with vitals, 1 without)
        // Patient 14 (Index 13) and Patient 15 (Index 14)
        seedActiveWorkflow(today, LocalTime.of(9, 0), LocalTime.of(9, 30), patients.get(13), doctorsList.get(0), labTech, nurse, services.get(0), null, medicines, 1, rand, AppointmentStatus.CHECKED_IN, MedicalRecordStatus.PENDING, null);
        seedActiveWorkflow(today, LocalTime.of(9, 30), LocalTime.of(10, 0), patients.get(14), doctorsList.get(0), labTech, nurse, services.get(0), null, medicines, 2, rand, AppointmentStatus.CHECKED_IN, MedicalRecordStatus.PENDING, null);
        
        // 2 in Đang khám (IN_PROGRESS)
        // Patient 16 and 17
        seedActiveWorkflow(today, LocalTime.of(10, 0), LocalTime.of(10, 30), patients.get(15), doctorsList.get(1), labTech, nurse, services.get(0), null, medicines, 3, rand, AppointmentStatus.IN_PROGRESS, MedicalRecordStatus.IN_PROGRESS, null);
        seedActiveWorkflow(today, LocalTime.of(10, 30), LocalTime.of(11, 0), patients.get(16), doctorsList.get(1), labTech, nurse, services.get(0), null, medicines, 4, rand, AppointmentStatus.IN_PROGRESS, MedicalRecordStatus.IN_PROGRESS, null);
        
        // 2 in Chờ kết quả (WAITING_RESULT, ORDERED lab test)
        // Patient 18 and 19
        seedActiveWorkflow(today, LocalTime.of(11, 0), LocalTime.of(11, 30), patients.get(17), doctorsList.get(2), labTech, nurse, services.get(0), services.get(2), medicines, 5, rand, AppointmentStatus.WAITING_RESULT, MedicalRecordStatus.WAITING_RESULT, ServiceOrderStatus.ORDERED);
        seedActiveWorkflow(today, LocalTime.of(11, 30), LocalTime.of(12, 0), patients.get(18), doctorsList.get(2), labTech, nurse, services.get(0), services.get(2), medicines, 6, rand, AppointmentStatus.WAITING_RESULT, MedicalRecordStatus.WAITING_RESULT, ServiceOrderStatus.ORDERED);
        
        // 1 in Đọc kết quả (WAITING_RESULT, DONE lab test)
        // Patient 20
        seedActiveWorkflow(today, LocalTime.of(13, 30), LocalTime.of(14, 0), patients.get(19), doctorsList.get(0), labTech, nurse, services.get(0), services.get(2), medicines, 7, rand, AppointmentStatus.WAITING_RESULT, MedicalRecordStatus.WAITING_RESULT, ServiceOrderStatus.DONE);

        // 8. Seed exactly 5 Leave Requests
        log.info("Generating mock leave requests...");
        if (!doctorsList.isEmpty()) {
            Staff manager = staffRepository.findByStaffTypeAndIsDeleted(StaffType.ADMIN, 0).stream().findFirst().orElse(null);

            // Request 1: Approved annual leave for doctor 0
            LeaveRequest req1 = new LeaveRequest();
            req1.setStaff(doctorsList.get(0));
            req1.setLeaveType(LeaveType.ANNUAL);
            req1.setFromDate(LocalDate.of(2026, 8, 5));
            req1.setToDate(LocalDate.of(2026, 8, 7));
            req1.setReason("Nghỉ phép năm đi du lịch cùng gia đình");
            req1.setStatus(LeaveStatus.APPROVED);
            req1.setApprovedBy(manager);
            req1.setReviewedAt(LocalDateTime.of(2026, 8, 1, 9, 0));
            leaveRequestRepository.save(req1);

            // Request 2: Rejected sick leave for doctor 1
            LeaveRequest req2 = new LeaveRequest();
            req2.setStaff(doctorsList.get(1));
            req2.setLeaveType(LeaveType.SICK);
            req2.setFromDate(LocalDate.of(2026, 8, 10));
            req2.setToDate(LocalDate.of(2026, 8, 10));
            req2.setReason("Nghỉ ốm đi khám bệnh");
            req2.setStatus(LeaveStatus.REJECTED);
            req2.setApprovedBy(manager);
            req2.setRejectionReason("Không có giấy xác nhận của bác sĩ");
            req2.setReviewedAt(LocalDateTime.of(2026, 8, 9, 14, 0));
            leaveRequestRepository.save(req2);

            // Request 3: Pending annual leave for doctor 2
            LeaveRequest req3 = new LeaveRequest();
            req3.setStaff(doctorsList.get(2));
            req3.setLeaveType(LeaveType.ANNUAL);
            req3.setFromDate(LocalDate.of(2026, 8, 20));
            req3.setToDate(LocalDate.of(2026, 8, 22));
            req3.setReason("Nghỉ phép giải quyết việc gia đình");
            req3.setStatus(LeaveStatus.PENDING);
            leaveRequestRepository.save(req3);

            // Request 4: Pending annual leave for nurse in the future
            if (nurse != null) {
                LeaveRequest req4 = new LeaveRequest();
                req4.setStaff(nurse);
                req4.setLeaveType(LeaveType.ANNUAL);
                req4.setFromDate(LocalDate.of(2026, 8, 25));
                req4.setToDate(LocalDate.of(2026, 8, 26));
                req4.setReason("Nghỉ phép chăm sóc người thân ốm");
                req4.setStatus(LeaveStatus.PENDING);
                leaveRequestRepository.save(req4);
            }

            // Request 5: Pending sick leave for lab tech in the future
            if (labTech != null) {
                LeaveRequest req5 = new LeaveRequest();
                req5.setStaff(labTech);
                req5.setLeaveType(LeaveType.SICK);
                req5.setFromDate(LocalDate.of(2026, 8, 23));
                req5.setToDate(LocalDate.of(2026, 8, 23));
                req5.setReason("Đi tiêm ngừa cảm cúm định kỳ");
                req5.setStatus(LeaveStatus.PENDING);
                leaveRequestRepository.save(req5);
            }
        }

        // 8.5 Seed Feedbacks, DoctorReviews (Pending/Rejected), and Notifications
        log.info("Seeding feedbacks, pending reviews, and notifications...");
        seedExtraFeedbacksAndReviews(patients, doctorsList);
        seedNotifications(patients);

        // 9. Generate Staff Schedules for rolling 7 days
        log.info("Generating staff schedules...");
        staffScheduleService.maintainRollingSchedule();

        log.info("Dashboard mock seeder finished successfully!");
    }

    private void seedCompleteWorkflow(
            LocalDate date, LocalTime start, LocalTime end,
            Patient patient, Staff doctor, Staff labTech, Staff nurse,
            Service examService, Service labService, List<Medicine> medicines,
            int index, Random rand, java.util.Set<String> reviewedPairs) {

        // A. Appointment
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setMainDoctor(doctor);
        appt.setService(examService);
        appt.setExpertise(doctor.getExpertise());
        appt.setAppointmentDate(date);
        appt.setTimeStart(start);
        appt.setTimeEnd(end);
        appt.setAppointmentType(AppointmentType.WALK_IN);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.setCreatedBy(CreatedByType.RECEPTIONIST);
        appt.setBookingMode(BookingMode.DOCTOR);
        appt.setCheckinTime(date.atTime(start.minusMinutes(10)));
        appt.setCheckoutTime(date.atTime(end.plusMinutes(45)));
        appt.setIsDeleted(0);
        appt = appointmentRepository.save(appt);

        // B. Medical Record
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setAppointment(appt);
        record.setMainDoctor(doctor);
        record.setDiagnosis("Đau dạ dày / Viêm họng cấp");
        record.setTreatment("Uống thuốc theo đơn và hạn chế đồ cay nóng");
        record.setNote("Tái khám nếu không giảm triệu chứng");
        record.setStatus(MedicalRecordStatus.DONE);
        record.setConsultationOriginalFee(examService.getOriginalPrice());
        record.setConsultationFinalFee(examService.getOriginalPrice());
        record.setVitalsTaken(true);
        record = medicalRecordRepository.save(record);

        // C. Vitals (MedicalRecordVital)
        MedicalRecordVital vitals = new MedicalRecordVital();
        vitals.setMedicalRecord(record);
        vitals.setWeight(new BigDecimal(50 + rand.nextInt(35)));
        vitals.setBloodPressure((110 + rand.nextInt(20)) + "/" + (70 + rand.nextInt(15)));
        vitals.setPulse(70 + rand.nextInt(25));
        vitals.setRecordedBy(nurse);
        vitals.setStatus("DONE");
        medicalRecordVitalRepository.save(vitals);

        // D. Prescription
        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(record);
        prescription.setStatus("COMPLETED");
        prescription = prescriptionRepository.save(prescription);

        // Add 2 medicines to prescription
        for (int m = 0; m < 2; m++) {
            Medicine med = medicines.get((index + m) % medicines.size());
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicine(med);
            item.setMedicineName(med.getName());
            item.setUnit(med.getBaseUnit());
            item.setQuantity(new BigDecimal("10"));
            item.setDosage("Uống 1 viên sau ăn");
            item.setFrequency("2 lần/ngày (Sáng, Tối)");
            item.setDurationDays(5);
            prescription.addItem(item);
        }
        prescriptionRepository.save(prescription);

        // E. Service Order (Xét nghiệm được chỉ định khi khám)
        ServiceOrder order = null;
        if (labService != null) {
            order = new ServiceOrder();
            order.setMedicalRecord(record);
            order.setService(labService);
            order.setCustomServiceName(labService.getServiceName());
            order.setDoctorNote("Lấy máu kiểm tra chỉ số");
            order.setOrderedBy(doctor);
            order.setServiceOriginalFee(labService.getOriginalPrice());
            order.setServiceFinalFee(labService.getOriginalPrice());
            order.setStatus(ServiceOrderStatus.DONE);
            order.setSampleCollectedAt(date.atTime(start.plusMinutes(20)));
            order.setSampleCollectedBy(nurse);
            order = serviceOrderRepository.save(order);

            // F. Service Result
            ServiceResult result = new ServiceResult();
            result.setServiceOrder(order);
            result.setResultData("{\"Hồng cầu\": \"4.6 M/uL\", \"Bạch cầu\": \"7.5 K/uL\", \"Tiểu cầu\": \"280 K/uL\"}");
            result.setConclusion("Các chỉ số huyết học nằm trong giới hạn bình thường.");
            result.setEnteredBy(labTech);
            result.setEnteredAt(date.atTime(start.plusMinutes(50)));
            serviceResultRepository.save(result);
        }

        // G. Invoice
        Invoice invoice = new Invoice();
        invoice.setMedicalRecord(record);
        invoice.setPatient(patient);
        invoice.setStatus(rand.nextInt(100) < 80 ? InvoiceStatus.PAID : InvoiceStatus.UNPAID);
        invoice.setPaymentMethod(rand.nextBoolean() ? PaymentMethod.CASH : PaymentMethod.TRANSFER);
        invoice = invoiceRepository.save(invoice);

        // Item 1: Consultation fee
        InvoiceItem item1 = new InvoiceItem();
        item1.setInvoice(invoice);
        item1.setItemType(InvoiceItemType.CONSULTATION);
        item1.setReferenceId(appt.getAppointmentId());
        item1.setDescription("Phí khám bệnh chuyên khoa (" + examService.getServiceName() + ")");
        item1.setPriceAtTime(examService.getOriginalPrice());
        invoice.getItems().add(item1);

        // Item 2: Lab service fee (Optional)
        if (labService != null && order != null) {
            InvoiceItem item2 = new InvoiceItem();
            item2.setInvoice(invoice);
            item2.setItemType(InvoiceItemType.SERVICE);
            item2.setReferenceId(order.getOrderId());
            item2.setDescription("Chi phí chỉ định xét nghiệm (" + labService.getServiceName() + ")");
            item2.setPriceAtTime(labService.getOriginalPrice());
            invoice.getItems().add(item2);
        }

        BigDecimal totalPrice = examService.getOriginalPrice().add(labService != null ? labService.getOriginalPrice() : BigDecimal.ZERO);
        invoice.setTotalPrice(totalPrice);
        invoiceRepository.save(invoice);

        // H. Doctor Review (Only if not already reviewed)
        String reviewKey = patient.getPatientId() + "-" + doctor.getStaffId();
        if (!reviewedPairs.contains(reviewKey)) {
            DoctorReview review = new DoctorReview();
            review.setDoctor(doctor);
            review.setPatient(patient);
            review.setAppointment(appt);
            review.setRating(4 + rand.nextInt(2)); // 4 or 5 stars
            review.setComment(rand.nextBoolean() ? "Bác sĩ rất thân thiện, tư vấn chi tiết" : "Dịch vụ phòng khám nhanh chóng, sạch sẽ");
            review.setCreatedAt(date.atTime(start));
            review.setAiStatus("APPROVED");
            doctorReviewRepository.save(review);
            reviewedPairs.add(reviewKey);
        }

        // I. Follow Up
        FollowUp followUp = new FollowUp();
        followUp.setMedicalRecord(record);
        followUp.setPatient(patient);
        followUp.setDoctor(doctor);
        followUp.setAppointment(appt);
        followUp.setScheduledDatetime(date.plusDays(7).atTime(LocalTime.of(9, 0)));
        followUp.setNote("Tái khám kiểm tra tình trạng đau dạ dày");
        followUp.setStatus(FollowUpStatus.PENDING);
        followUpRepository.save(followUp);
    }

    private void seedActiveWorkflow(
            LocalDate date, LocalTime start, LocalTime end,
            Patient patient, Staff doctor, Staff labTech, Staff nurse,
            Service examService, Service labService, List<Medicine> medicines,
            int index, Random rand, AppointmentStatus apptStatus, MedicalRecordStatus recStatus,
            ServiceOrderStatus orderStatus) {

        // A. Appointment
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setMainDoctor(doctor);
        appt.setService(examService);
        appt.setExpertise(doctor.getExpertise());
        appt.setAppointmentDate(date);
        appt.setTimeStart(start);
        appt.setTimeEnd(end);
        appt.setAppointmentType(AppointmentType.WALK_IN);
        appt.setStatus(apptStatus);
        appt.setCreatedBy(CreatedByType.RECEPTIONIST);
        appt.setBookingMode(BookingMode.DOCTOR);
        appt.setCheckinTime(date.atTime(start.minusMinutes(10)));
        appt.setIsDeleted(0);
        
        if (apptStatus == AppointmentStatus.CHECKED_IN || apptStatus == AppointmentStatus.IN_PROGRESS || apptStatus == AppointmentStatus.WAITING_RESULT) {
            appt.setQueueNumber(index);
        } else {
            appt.setQueueNumber(0);
        }
        
        appt = appointmentRepository.save(appt);

        // B. Medical Record (Created when not PENDING/CONFIRMED)
        if (apptStatus != AppointmentStatus.PENDING && apptStatus != AppointmentStatus.CONFIRMED) {
            MedicalRecord record = new MedicalRecord();
            record.setPatient(patient);
            record.setAppointment(appt);
            record.setMainDoctor(doctor);
            record.setStatus(recStatus);
            record.setConsultationOriginalFee(examService.getOriginalPrice());
            record.setConsultationFinalFee(examService.getOriginalPrice());
            
            // Alternately take vitals
            boolean vitalsTaken = (index % 2 == 0); 
            record.setVitalsTaken(vitalsTaken);
            record = medicalRecordRepository.save(record);

            if (vitalsTaken) {
                MedicalRecordVital vitals = new MedicalRecordVital();
                vitals.setMedicalRecord(record);
                vitals.setWeight(new BigDecimal(50 + rand.nextInt(35)));
                vitals.setBloodPressure((110 + rand.nextInt(20)) + "/" + (70 + rand.nextInt(15)));
                vitals.setPulse(70 + rand.nextInt(25));
                vitals.setRecordedBy(nurse);
                vitals.setStatus("DONE");
                medicalRecordVitalRepository.save(vitals);
            }

            // C. Service Order (If WAITING_RESULT and lab service ordered)
            if (labService != null && (apptStatus == AppointmentStatus.WAITING_RESULT || orderStatus != null)) {
                ServiceOrder order = new ServiceOrder();
                order.setMedicalRecord(record);
                order.setService(labService);
                order.setCustomServiceName(labService.getServiceName());
                order.setDoctorNote("Lấy máu xét nghiệm sinh hóa");
                order.setOrderedBy(doctor);
                order.setServiceOriginalFee(labService.getOriginalPrice());
                order.setServiceFinalFee(labService.getOriginalPrice());
                order.setStatus(orderStatus);
                if (orderStatus == ServiceOrderStatus.DONE) {
                    order.setSampleCollectedAt(date.atTime(start.plusMinutes(20)));
                    order.setSampleCollectedBy(nurse);
                    order = serviceOrderRepository.save(order);

                    // Service Result
                    ServiceResult result = new ServiceResult();
                    result.setServiceOrder(order);
                    result.setResultData("{\"Hồng cầu\": \"4.2 M/uL\", \"Bạch cầu\": \"6.8 K/uL\", \"Tiểu cầu\": \"250 K/uL\"}");
                    result.setConclusion("Các chỉ số trong giới hạn bình thường.");
                    result.setEnteredBy(labTech);
                    result.setEnteredAt(date.atTime(start.plusMinutes(40)));
                    serviceResultRepository.save(result);
                } else {
                    serviceOrderRepository.save(order);
                }
            }
        }
    }

    private Expertise createExpertise(String name) {
        Expertise exp = new Expertise();
        exp.setExpertiseName(name);
        return expertiseRepository.save(exp);
    }

    private Service createService(String name, ServiceType type, BigDecimal price) {
        Service s = new Service();
        s.setServiceName(name);
        s.setServiceType(type);
        s.setOriginalPrice(price);
        s.setDiscountAmount(BigDecimal.ZERO);
        s.setIsFeatured(true);
        return serviceRepository.save(s);
    }

    private Medicine createMedicine(String name, String active, String pack, String unit, int stock) {
        Medicine m = new Medicine();
        m.setName(name);
        m.setActiveElement(active);
        m.setPackingStandard(pack);
        m.setBaseUnit(unit);
        m.setStockQuantity(stock);
        return medicineRepository.save(m);
    }

    private Staff createStaff(String email, String name, StaffType type, Expertise expertise) {
        Role role = roleRepository.findByRoleCode(type.name())
                .orElseThrow(() -> new RuntimeException("Role " + type.name() + " not found"));

        Account acc = accountRepository.findByEmail(email).orElse(new Account());
        acc.setEmail(email);
        acc.setPassword(passwordEncoder.encode("12345678"));
        acc.setIsActive(1);
        acc.getRoles().clear();
        acc.getRoles().add(role);
        acc = accountRepository.save(acc);

        Staff staff = new Staff();
        staff.setAccount(acc);
        staff.setFullName(name);
        staff.setGender("Nam");
        staff.setStaffType(type);
        staff.setExpertise(expertise);
        staff.setIsDeleted(0);
        staff = staffRepository.save(staff);

        // Seed doctor consultation fee if the staff is a DOCTOR
        if (type == StaffType.DOCTOR) {
            DoctorServicePrice price = doctorServicePriceRepository.findByStaff_StaffId(staff.getStaffId())
                    .orElse(new DoctorServicePrice());
            price.setStaff(staff);
            price.setOriginalPrice(new BigDecimal("150000"));
            price.setDiscountAmount(BigDecimal.ZERO);
            doctorServicePriceRepository.save(price);
        }
        return staff;
    }

    private Patient createPatient(String email, String name, String phone) {
        Role role = roleRepository.findByRoleCode("PATIENT")
                .orElseThrow(() -> new RuntimeException("PATIENT role not found"));

        Account acc = accountRepository.findByEmail(email).orElse(new Account());
        acc.setEmail(email);
        acc.setPassword(passwordEncoder.encode("12345678"));
        acc.setIsActive(1);
        acc.getRoles().clear();
        acc.getRoles().add(role);
        acc = accountRepository.save(acc);

        Patient patient = new Patient();
        patient.setAccount(acc);
        patient.setFullName(name);
        // Clean matching of gender for patient names if possible, else random
        String gender = "Nam";
        if (name.contains("Thị") || name.contains("Chi") || name.contains("Thảo") || name.contains("Mai") || name.contains("Hương") || name.contains("Lành") || name.contains("Linh") || name.contains("An") || name.contains("Bình")) {
            gender = "Nữ";
        }
        patient.setGender(gender);
        patient.setPhone(phone);
        
        // Seed address and birth date
        String[] addresses = {"Cần Thơ", "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "An Giang", "Bình Dương", "Đồng Nai", "Vĩnh Long"};
        patient.setAddress(addresses[new Random().nextInt(addresses.length)]);
        patient.setDateOfBirth(LocalDate.of(1975 + new Random().nextInt(30), 1 + new Random().nextInt(11), 1 + new Random().nextInt(27)));
        
        patient.setIsDeleted(0);
        patient = patientRepository.save(patient);

        // Seed PatientVitalProfile
        PatientVitalProfile pvp = new PatientVitalProfile();
        pvp.setPatient(patient);
        pvp.setHeight(160 + new Random().nextInt(25));
        pvp.setWeight(new BigDecimal(50 + new Random().nextInt(30)));
        pvp.setBloodPressure((110 + new Random().nextInt(15)) + "/" + (70 + new Random().nextInt(10)));
        pvp.setPulse(70 + new Random().nextInt(20));
        pvp.setBloodType(new String[]{"A", "B", "AB", "O"}[new Random().nextInt(4)]);
        pvp.setAllergies("Không dị ứng");
        pvp.setChronicDiseases("Không");
        pvp.setMedicalHistory("Bình thường");
        pvp.setUpdatedAt(LocalDateTime.now());
        patientVitalProfileRepository.save(pvp);

        return patient;
    }

    private void seedExtraFeedbacksAndReviews(List<Patient> patients, List<Staff> doctorsList) {
        if (patients.size() < 10 || doctorsList.isEmpty()) return;

        List<MedicalRecord> records = medicalRecordRepository.findAll();
        if (records.size() < 5) return;

        // A. Seed Clinic Feedbacks (Phòng khám)
        // 2 APPROVED
        Feedback f1 = new Feedback();
        f1.setMedicalRecord(records.get(0));
        f1.setRating(5);
        f1.setComment("Phòng khám rất sạch sẽ, khang trang. Đội ngũ y tá và bác sĩ đón tiếp rất chu đáo, nhiệt tình.");
        f1.setAiStatus("APPROVED");
        f1.setCreatedAt(LocalDateTime.now().minusDays(3));
        feedbackRepository.save(f1);

        Feedback f2 = new Feedback();
        f2.setMedicalRecord(records.get(1));
        f2.setRating(4);
        f2.setComment("Dịch vụ tốt, thời gian lấy kết quả xét nghiệm nhanh chóng. Tuy nhiên bãi gửi xe hơi nhỏ.");
        f2.setAiStatus("APPROVED");
        f2.setCreatedAt(LocalDateTime.now().minusDays(2));
        feedbackRepository.save(f2);

        // 1 PENDING
        Feedback f3 = new Feedback();
        f3.setMedicalRecord(records.get(2));
        f3.setRating(3);
        f3.setComment("Tôi thấy mức giá dịch vụ cận lâm sàng hơi cao hơn so với các phòng khám xung quanh.");
        f3.setAiStatus("PENDING");
        f3.setCreatedAt(LocalDateTime.now().minusHours(5));
        feedbackRepository.save(f3);

        // 1 REJECTED (Violating comment)
        Feedback f4 = new Feedback();
        f4.setMedicalRecord(records.get(3));
        f4.setRating(1);
        f4.setComment("Phòng khám này làm ăn lừa đảo khách hàng! Đừng ai đến đây khám kẻo bị mất tiền oan uổng!!!");
        f4.setAiStatus("REJECTED");
        f4.setAiModerationNote("Chứa từ ngữ nhạy cảm quy chụp không bằng chứng (lừa đảo)");
        f4.setCreatedAt(LocalDateTime.now().minusHours(2));
        feedbackRepository.save(f4);

        // B. Seed Extra Doctor Reviews (Bác sĩ)
        // 2 PENDING
        DoctorReview dr1 = new DoctorReview();
        dr1.setDoctor(doctorsList.get(0));
        dr1.setPatient(patients.get(4));
        dr1.setRating(5);
        dr1.setComment("Bác sĩ tư vấn cực kỳ tận tâm, giải thích cặn kẽ bệnh lý. Tôi rất an tâm khi khám ở đây.");
        dr1.setAiStatus("PENDING");
        dr1.setCreatedAt(LocalDateTime.now().minusHours(6));
        doctorReviewRepository.save(dr1);

        DoctorReview dr2 = new DoctorReview();
        dr2.setDoctor(doctorsList.get(1 % doctorsList.size()));
        dr2.setPatient(patients.get(5));
        dr2.setRating(4);
        dr2.setComment("Khám bệnh kỹ càng, đơn thuốc uống hiệu quả nhanh. Mong bác sĩ tiếp tục phát huy.");
        dr2.setAiStatus("PENDING");
        dr2.setCreatedAt(LocalDateTime.now().minusHours(4));
        doctorReviewRepository.save(dr2);

        // 1 REJECTED (Violating doctor review)
        DoctorReview dr3 = new DoctorReview();
        dr3.setDoctor(doctorsList.get(0));
        dr3.setPatient(patients.get(6));
        dr3.setRating(1);
        dr3.setComment("Bác sĩ này khám ngu như bò, thái độ thì khinh khỉnh coi thường người nghèo!!! Tẩy chay đi bà con!");
        dr3.setAiStatus("REJECTED");
        dr3.setAiModerationNote("Chứa từ ngữ tục tĩu xúc phạm bác sĩ (ngu như bò)");
        dr3.setCreatedAt(LocalDateTime.now().minusHours(1));
        doctorReviewRepository.save(dr3);
    }

    private void seedNotifications(List<Patient> patients) {
        if (patients.isEmpty()) return;

        String[] messages = {
            "Lịch hẹn khám #APT-26 của bạn đã được xác nhận thành công.",
            "Yêu cầu đổi lịch khám của bạn sang 17/08/2026 đã được phê duyệt.",
            "Kết quả xét nghiệm sinh hóa máu đã sẵn sàng. Vui lòng xem trên ứng dụng hoặc liên hệ bác sĩ.",
            "Nhắc nhở: Lịch khám của bạn với BS CKII. Ngô Trung Nam sẽ bắt đầu sau 30 phút nữa.",
            "Hệ thống vừa cập nhật giờ làm việc mới áp dụng từ ngày 20/08/2026."
        };

        for (int i = 0; i < messages.length; i++) {
            Patient p = patients.get(i % patients.size());
            Notification n = new Notification();
            n.setAccount(p.getAccount());
            n.setType(Notification.Type.SYSTEM);
            n.setContent(messages[i]);
            n.setSentAt(LocalDateTime.now().minusDays(i).minusHours(2));
            notificationRepository.save(n);
        }
    }
}
