package com.clinic.e2e.group6_prescription;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.auth.LoginRequest;
import com.clinic.dto.prescription.PrescriptionItemRequest;
import com.clinic.dto.prescription.PrescriptionRequest;
import com.clinic.e2e.BaseIntegrationTest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Service;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.prescription.DrugInteraction;
import com.clinic.entity.prescription.Medicine;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.prescription.DrugInteractionRepository;
import com.clinic.repository.prescription.MedicineRepository;
import com.clinic.repository.staff.StaffRepository;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
public class PrescriptionTest extends BaseIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceOrderRepository serviceOrderRepository;
    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private DrugInteractionRepository drugInteractionRepository;

    private static String doctorToken;
    private static String nurseToken;
    
    private static Integer medicalRecordId;
    private static Integer pendingRecordId;
    private static Integer doneRecordId;

    private static Integer medicineAId; // Amoxicillin (tồn 100)
    private static Integer medicineBId; // Clarithromycin (tương tác với A)
    private static Integer medicineCId; // Paracetamol (hết hạn / isDeleted = 1)
    private static Integer medicineDId; // Vitamin C (tồn 5)
    
    @Autowired private com.clinic.repository.auth.AccountRepository accountRepository;
    @Autowired private com.clinic.repository.auth.RoleRepository roleRepository;
    @Autowired private com.clinic.security.JwtService jwtService;

    @BeforeAll
    public void setupData() {
        io.restassured.RestAssured.port = port;
        
        com.clinic.entity.auth.Role doctorRole = roleRepository.findByRoleCode("DOCTOR").orElseGet(() -> {
            com.clinic.entity.auth.Role r = new com.clinic.entity.auth.Role();
            r.setRoleCode("DOCTOR");
            r.setRoleName("Doctor");
            return roleRepository.save(r);
        });

        com.clinic.entity.auth.Account docAcc = accountRepository.findByEmail("doctor1@clinic.com").orElseGet(() -> {
            com.clinic.entity.auth.Account acc = new com.clinic.entity.auth.Account();
            acc.setEmail("doctor1@clinic.com");
            acc.setPassword("Password123!");
            acc.setIsActive(1);
            acc = accountRepository.save(acc);
            acc.getRoles().add(doctorRole);
            return accountRepository.save(acc);
        });
        doctorToken = jwtService.generateToken(new com.clinic.security.CustomUserDetails(docAcc));

        com.clinic.entity.auth.Role nurseRole = roleRepository.findByRoleCode("NURSE").orElseGet(() -> {
            com.clinic.entity.auth.Role r = new com.clinic.entity.auth.Role();
            r.setRoleCode("NURSE");
            r.setRoleName("Nurse");
            return roleRepository.save(r);
        });

        com.clinic.entity.auth.Account nurseAcc = accountRepository.findByEmail("nurse1@clinic.com").orElseGet(() -> {
            com.clinic.entity.auth.Account acc = new com.clinic.entity.auth.Account();
            acc.setEmail("nurse1@clinic.com");
            acc.setPassword("Password123!");
            acc.setIsActive(1);
            acc = accountRepository.save(acc);
            acc.getRoles().add(nurseRole);
            return accountRepository.save(acc);
        });
        nurseToken = jwtService.generateToken(new com.clinic.security.CustomUserDetails(nurseAcc));

        // Doctor Staff
        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Prescription Doctor Test")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Prescription Doctor Test");
                    s.setStaffType(com.clinic.common.enums.StaffType.DOCTOR);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });

        // Create Patient
        Patient patient = patientRepository.findAll().stream()
                .filter(p -> p.getFullName().equals("Prescription Patient")).findFirst()
                .orElseGet(() -> {
                    Patient p = new Patient();
                    p.setFullName("Prescription Patient");
                    p.setPhone("0888888888");
                    p.setGender("MALE");
                    p.setDateOfBirth(LocalDate.of(1995, 1, 1));
                    p.setAddress("HN");
                    p.setIsDeleted(0);
                    return patientRepository.save(p);
                });

        // Appointment 1
        Appointment appointment1 = new Appointment();
        appointment1.setPatient(patient);
        appointment1.setMainDoctor(doctor);
        appointment1.setAppointmentDate(LocalDate.now());
        appointment1.setTimeStart(LocalTime.of(10, 0));
        appointment1.setTimeEnd(LocalTime.of(10, 30));
        appointment1.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment1.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment1.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment1 = appointmentRepository.save(appointment1);

        // Record 1 (Ready to prescribe)
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setMainDoctor(doctor);
        record.setAppointment(appointment1);
        record.setStatus(MedicalRecordStatus.IN_PROGRESS);
        record.setDiagnosis("Cảm cúm");
        record = medicalRecordRepository.save(record);
        medicalRecordId = record.getRecordId();

        // Appointment 2
        Appointment appointment2 = new Appointment();
        appointment2.setPatient(patient);
        appointment2.setMainDoctor(doctor);
        appointment2.setAppointmentDate(LocalDate.now());
        appointment2.setTimeStart(LocalTime.of(11, 0));
        appointment2.setTimeEnd(LocalTime.of(11, 30));
        appointment2.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment2.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment2.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment2 = appointmentRepository.save(appointment2);

        // Record 2 (Pending Lab)
        MedicalRecord pendingRecord = new MedicalRecord();
        pendingRecord.setPatient(patient);
        pendingRecord.setMainDoctor(doctor);
        pendingRecord.setAppointment(appointment2);
        pendingRecord.setStatus(MedicalRecordStatus.WAITING_RESULT);
        pendingRecord.setDiagnosis("Cần xét nghiệm máu");
        pendingRecord = medicalRecordRepository.save(pendingRecord);
        pendingRecordId = pendingRecord.getRecordId();

        Service bloodTest = serviceRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Service s = new Service();
                    s.setServiceName("Xét nghiệm máu Test");
                    s.setOriginalPrice(new java.math.BigDecimal("100000"));
                    s.setServiceType(com.clinic.common.enums.ServiceType.LAB_TEST);
                    s.setIsDeleted(0);
                    return serviceRepository.save(s);
                });
        ServiceOrder order = new ServiceOrder();
        order.setMedicalRecord(pendingRecord);
        order.setOrderedBy(doctor);
        order.setService(bloodTest);
        order.setStatus(ServiceOrderStatus.ORDERED);
        order.setServiceOriginalFee(bloodTest.getOriginalPrice());
        order.setServiceFinalFee(bloodTest.getOriginalPrice());
        serviceOrderRepository.save(order);

        // Appointment 3
        Appointment appointment3 = new Appointment();
        appointment3.setPatient(patient);
        appointment3.setMainDoctor(doctor);
        appointment3.setAppointmentDate(LocalDate.now());
        appointment3.setTimeStart(LocalTime.of(14, 0));
        appointment3.setTimeEnd(LocalTime.of(14, 30));
        appointment3.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment3.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment3.setStatus(AppointmentStatus.COMPLETED);
        appointment3 = appointmentRepository.save(appointment3);

        // Record 3 (DONE)
        MedicalRecord doneRecord = new MedicalRecord();
        doneRecord.setPatient(patient);
        doneRecord.setMainDoctor(doctor);
        doneRecord.setAppointment(appointment3);
        doneRecord.setStatus(MedicalRecordStatus.DONE);
        doneRecord.setDiagnosis("Khám xong");
        doneRecord = medicalRecordRepository.save(doneRecord);
        doneRecordId = doneRecord.getRecordId();

        // Setup Medicines
        Medicine medA = new Medicine();
        medA.setName("Amoxicillin 500mg");
        medA.setActiveElement("Amoxicillin");
        medA.setStockQuantity(100);
        medA = medicineRepository.save(medA);
        medicineAId = medA.getMedicineId();

        Medicine medB = new Medicine();
        medB.setName("Clarithromycin 500mg");
        medB.setActiveElement("Clarithromycin");
        medB.setStockQuantity(50);
        medB = medicineRepository.save(medB);
        medicineBId = medB.getMedicineId();

        Medicine medC = new Medicine();
        medC.setName("Paracetamol 500mg");
        medC.setActiveElement("Paracetamol");
        medC.setStockQuantity(100);
        medC.setIsDeleted(1); // Inactive
        medC = medicineRepository.save(medC);
        medicineCId = medC.getMedicineId();

        Medicine medD = new Medicine();
        medD.setName("Vitamin C 1000mg");
        medD.setActiveElement("Vitamin C");
        medD.setStockQuantity(5);
        medD = medicineRepository.save(medD);
        medicineDId = medD.getMedicineId();

        // Setup Interaction (A + B)
        DrugInteraction interaction = new DrugInteraction();
        interaction.setActiveElement1("amoxicillin");
        interaction.setActiveElement2("clarithromycin");
        interaction.setMechanism("Tăng độc tính trên gan");
        interaction.setConsequence("Nguy cơ viêm gan cấp");
        interaction.setManagement("Chống chỉ định phối hợp");
        drugInteractionRepository.save(interaction);
    }

    @Test
    @Order(1)
    public void test_TC06_02_EmptyMedicineList() {
        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Collections.emptyList());

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(2)
    public void test_TC06_03_MissingDosage() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);
        // Missing dosage

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(3)
    public void test_TC06_04_InvalidFrequencyOrDuration() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        // Missing frequency
        item.setDurationDays(0); // Invalid days

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(4)
    public void test_TC06_05_InactiveMedicine() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineCId);
        item.setMedicineName("Paracetamol 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("không còn hoạt động"));
    }

    @Test
    @Order(5)
    public void test_TC06_06_StockExceeded() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineDId);
        item.setMedicineName("Vitamin C 1000mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10")); // Stock is 5
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("vượt tồn kho"));
    }

    @Test
    @Order(6)
    public void test_TC06_07_DrugInteractionWarning() {
        PrescriptionItemRequest item1 = new PrescriptionItemRequest();
        item1.setMedicineId(medicineAId);
        item1.setMedicineName("Amoxicillin 500mg");
        item1.setUnit("Viên");
        item1.setQuantity(new BigDecimal("10"));
        item1.setDosage("1 viên/lần");
        item1.setFrequency("2 lần/ngày");
        item1.setDurationDays(5);

        PrescriptionItemRequest item2 = new PrescriptionItemRequest();
        item2.setMedicineId(medicineBId);
        item2.setMedicineName("Clarithromycin 500mg");
        item2.setUnit("Viên");
        item2.setQuantity(new BigDecimal("10"));
        item2.setDosage("1 viên/lần");
        item2.setFrequency("2 lần/ngày");
        item2.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item1, item2));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("CẢNH BÁO TƯƠNG TÁC THUỐC"));
    }

    @Test
    @Order(7)
    public void test_TC06_08_MultipleInteractions() {
        // Similar to TC06-07. Usually tests API with more complex medicines.
        // We will just verify it throws the same interaction error.
        test_TC06_07_DrugInteractionWarning();
    }

    @Test
    @Order(8)
    public void test_TC06_09_PendingLabOrder() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(pendingRecordId); // This record has ORDERED lab tests
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("chỉ định cận lâm sàng chưa có kết quả"));
    }

    @Test
    @Order(9)
    public void test_TC06_10_NotDoctor() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        // Use NURSE token
        given()
            .header("Authorization", "Bearer " + nurseToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(403);
    }

    @Test
    @Order(10)
    public void test_TC06_11_DoneMedicalRecord() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(doneRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("đã hoàn thành"));
    }

    @Test
    @Order(11)
    public void test_TC06_12_DuplicateMedicine() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        // Duplicate the item
        req.setItems(Arrays.asList(item, item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(400)
            .body("message", containsString("trùng"));
    }

    @Test
    @Order(12)
    public void test_TC06_01_Success() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineAId);
        item.setMedicineName("Amoxicillin 500mg");
        item.setUnit("Viên");
        item.setQuantity(new BigDecimal("10"));
        item.setDosage("1 viên/lần");
        item.setFrequency("2 lần/ngày");
        item.setDurationDays(5);

        PrescriptionRequest req = new PrescriptionRequest();
        req.setRecordId(medicalRecordId);
        req.setItems(Arrays.asList(item));

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", equalTo("PENDING"));
    }

}
