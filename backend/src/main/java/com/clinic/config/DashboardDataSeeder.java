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
import com.clinic.entity.patient.Patient;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.repository.medical.*;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.prescription.MedicineRepository;
import com.clinic.repository.prescription.PrescriptionRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Preparing complete clinic workflow mock data...");

        // 1. Seed Expertise
        List<Expertise> expertises = new ArrayList<>();
        expertises.add(createExpertise("Nội tổng quát"));
        expertises.add(createExpertise("Tai Mũi Họng"));
        expertises.add(createExpertise("Răng Hàm Mặt"));

        // 2. Seed Services
        List<Service> services = new ArrayList<>();
        services.add(createService("Khám nội tổng quát", ServiceType.EXAM, new BigDecimal("150000")));
        services.add(createService("Khám răng hàm mặt", ServiceType.EXAM, new BigDecimal("200000")));
        services.add(createService("Xét nghiệm tổng công thức máu", ServiceType.LAB_TEST, new BigDecimal("250000")));
        services.add(createService("Siêu âm bụng tổng quát", ServiceType.ULTRASOUND, new BigDecimal("350000")));
        services.add(createService("Chụp X-Quang phổi", ServiceType.X_RAY, new BigDecimal("200000")));

        // 3. Seed Medicines
        List<Medicine> medicines = new ArrayList<>();
        medicines.add(createMedicine("Paracetamol 500mg", "Paracetamol", "Hộp 10 vỉ x 10 viên", "Viên", 100));
        medicines.add(createMedicine("Amoxicillin 500mg", "Amoxicillin", "Hộp 10 vỉ x 10 viên", "Viên", 200));
        medicines.add(createMedicine("Ibuprofen 400mg", "Ibuprofen", "Hộp 10 vỉ x 10 viên", "Viên", 150));
        medicines.add(createMedicine("Decolgen Forte", "Paracetamol + Phenylephrine", "Hộp 25 vỉ x 4 viên", "Viên", 120));
        medicines.add(createMedicine("Cetirizine 10mg", "Cetirizine dihydrochloride", "Hộp 10 vỉ x 10 viên", "Viên", 80));

        // 4. Seed Staff & link to seeded system accounts
        Staff doctorA = createStaff("doctor@clinic.com", "BS. Nguyễn Văn A", StaffType.DOCTOR, expertises.get(0));
        Staff doctorB = createStaff("doctor2@clinic.com", "BS. Trần Thị B", StaffType.DOCTOR, expertises.get(1));
        Staff doctorC = createStaff("doctor3@clinic.com", "BS. Phạm Minh C", StaffType.DOCTOR, expertises.get(2));

        Staff labTech = createStaff("lab_tech@clinic.com", "KTV. Lê Văn Lab", StaffType.LAB_TECH, null);
        Staff nurse = createStaff("nurse@clinic.com", "ĐD. Phạm Thị Điều Dưỡng", StaffType.NURSE, null);
        Staff receptionist = createStaff("receptionist@clinic.com", "LT. Trần Thị Lễ Tân", StaffType.RECEPTIONIST, null);

        List<Staff> doctorsList = List.of(doctorA, doctorB, doctorC);

        // 5. Seed Patients
        List<Patient> patients = new ArrayList<>();
        patients.add(createPatient("patient1@clinic.com", "Dương Quốc Kiệt", "0912345678"));
        patients.add(createPatient("patient2@clinic.com", "Nguyễn Văn Bình", "0901234567"));
        patients.add(createPatient("patient3@clinic.com", "Lê Thị Hoa", "0902345678"));
        patients.add(createPatient("patient4@clinic.com", "Trần Văn Cường", "0903456789"));
        patients.add(createPatient("patient5@clinic.com", "Phạm Thị Mai", "0904567890"));

        // 6. Seed Complete Clinic Workflows from August 1 to August 15, 2026
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 15);
        Random rand = new Random();
        int index = 1;

        log.info("Generating transaction records from Aug 1 to Aug 15...");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Seed 2 completed appointments + workflows per day
            for (int i = 0; i < 2; i++) {
                Patient patient = patients.get(rand.nextInt(patients.size()));
                Staff doctor = doctorsList.get(rand.nextInt(doctorsList.size()));
                Service examService = (doctor.getExpertise().getExpertiseName().equals("Răng Hàm Mặt")) ? services.get(1) : services.get(0);
                Service labService = rand.nextBoolean() ? services.get(2) : services.get(3); // Blood test or Ultrasound

                LocalTime timeStart = LocalTime.of(8 + i * 2, 0);
                LocalTime timeEnd = timeStart.plusMinutes(30);

                seedCompleteWorkflow(date, timeStart, timeEnd, patient, doctor, labTech, nurse, examService, labService, medicines, index++, rand);
            }

            // Seed 1 Cancelled appointment per day (15% rate)
            if (date.getDayOfMonth() % 3 == 0) {
                Patient patient = patients.get(rand.nextInt(patients.size()));
                Staff doctor = doctorsList.get(rand.nextInt(doctorsList.size()));
                Service examService = services.get(0);

                Appointment cancelAppt = new Appointment();
                cancelAppt.setPatient(patient);
                cancelAppt.setMainDoctor(doctor);
                cancelAppt.setService(examService);
                cancelAppt.setExpertise(doctor.getExpertise());
                cancelAppt.setAppointmentDate(date);
                cancelAppt.setTimeStart(LocalTime.of(15, 0));
                cancelAppt.setTimeEnd(LocalTime.of(15, 30));
                cancelAppt.setAppointmentType(AppointmentType.ONLINE);
                cancelAppt.setStatus(AppointmentStatus.CANCELLED);
                cancelAppt.setCreatedBy(CreatedByType.PATIENT);
                cancelAppt.setBookingMode(BookingMode.DOCTOR);
                cancelAppt.setCancelledBy(CancelledByType.PATIENT);
                cancelAppt.setCancelReason("Bận việc gia đình đột xuất");
                cancelAppt.setIsDeleted(0);
                appointmentRepository.save(cancelAppt);
            }

            // Seed 1 Rescheduled appointment (Dời lịch)
            if (date.getDayOfMonth() % 4 == 0) {
                Patient patient = patients.get(rand.nextInt(patients.size()));
                Staff doctor = doctorsList.get(rand.nextInt(doctorsList.size()));
                Service examService = services.get(0);

                Appointment reschedAppt = new Appointment();
                reschedAppt.setPatient(patient);
                reschedAppt.setMainDoctor(doctor);
                reschedAppt.setService(examService);
                reschedAppt.setExpertise(doctor.getExpertise());
                reschedAppt.setAppointmentDate(date);
                reschedAppt.setTimeStart(LocalTime.of(16, 0));
                reschedAppt.setTimeEnd(LocalTime.of(16, 30));
                reschedAppt.setAppointmentType(AppointmentType.ONLINE);
                reschedAppt.setStatus(AppointmentStatus.CONFIRMED);
                reschedAppt.setCreatedBy(CreatedByType.RECEPTIONIST);
                reschedAppt.setBookingMode(BookingMode.DOCTOR);
                reschedAppt.setRescheduleCount(1);
                reschedAppt.setRescheduleReason("Bệnh nhân xin dời lịch do kẹt xe");
                reschedAppt.setIsDeleted(0);
                appointmentRepository.save(reschedAppt);
            }
        }

        // 7. Seed Calendar appointments for the future (Aug 17 to Aug 30, 2026)
        log.info("Generating future calendar appointments...");
        LocalDate futureStart = LocalDate.of(2026, 8, 17);
        LocalDate futureEnd = LocalDate.of(2026, 8, 30);
        for (LocalDate date = futureStart; !date.isAfter(futureEnd); date = date.plusDays(1)) {
            // Seed 1-2 upcoming appointments per day
            int count = 1 + rand.nextInt(2);
            for (int i = 0; i < count; i++) {
                Patient patient = patients.get(rand.nextInt(patients.size()));
                Staff doctor = doctorsList.get(rand.nextInt(doctorsList.size()));
                Service service = services.get(rand.nextInt(2)); // Clinical exam

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
        }

        log.info("Dashboard mock seeder finished successfully!");
    }

    private void seedCompleteWorkflow(
            LocalDate date, LocalTime start, LocalTime end,
            Patient patient, Staff doctor, Staff labTech, Staff nurse,
            Service examService, Service labService, List<Medicine> medicines,
            int index, Random rand) {

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
        ServiceOrder order = new ServiceOrder();
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

        // Item 2: Lab service fee
        InvoiceItem item2 = new InvoiceItem();
        item2.setInvoice(invoice);
        item2.setItemType(InvoiceItemType.SERVICE);
        item2.setReferenceId(order.getOrderId());
        item2.setDescription("Chi phí chỉ định xét nghiệm (" + labService.getServiceName() + ")");
        item2.setPriceAtTime(labService.getOriginalPrice());
        invoice.getItems().add(item2);

        BigDecimal totalPrice = examService.getOriginalPrice().add(labService.getOriginalPrice());
        invoice.setTotalPrice(totalPrice);
        invoiceRepository.save(invoice);

        // H. Doctor Review
        DoctorReview review = new DoctorReview();
        review.setDoctor(doctor);
        review.setPatient(patient);
        review.setAppointment(appt);
        review.setRating(4 + rand.nextInt(2)); // 4 or 5 stars
        review.setComment(rand.nextBoolean() ? "Bác sĩ rất thân thiện, tư vấn chi tiết" : "Dịch vụ phòng khám nhanh chóng, sạch sẽ");
        review.setCreatedAt(date.atTime(start));
        review.setAiStatus("APPROVED");
        doctorReviewRepository.save(review);

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
        return staffRepository.save(staff);
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
        patient.setGender("Nam");
        patient.setPhone(phone);
        patient.setIsDeleted(0);
        return patientRepository.save(patient);
    }
}
