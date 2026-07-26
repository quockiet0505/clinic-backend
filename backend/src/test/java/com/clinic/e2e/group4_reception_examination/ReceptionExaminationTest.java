package com.clinic.e2e.group4_reception_examination;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.TriageRequest;
import com.clinic.e2e.BaseIntegrationTest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Service;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.common.enums.StaffType;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.security.CustomUserDetails;
import com.clinic.security.JwtService;

import io.restassured.http.ContentType;

import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReceptionExaminationTest extends BaseIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private static String receptionistToken;
    private static String doctorToken;
    private static String adminToken;
    private static Integer patientId;
    private static Integer doctorId;
    private static Integer expertiseId;
    private static Integer serviceId;

    private static Integer validApptId;
    private static Integer cancelledApptId;
    private static Integer pendingApptId;
    private static Integer medicalRecordId;

    @BeforeAll
    void setupData() {
        // Setup Accounts and Roles
        Role adminRole = roleRepository.findByRoleCode("ADMIN").orElseThrow();
        Role doctorRole = roleRepository.findByRoleCode("DOCTOR").orElseThrow();
        Role repRole = roleRepository.findByRoleCode("RECEPTIONIST").orElseThrow();
        Role patientRole = roleRepository.findByRoleCode("PATIENT").orElseThrow();

        // 1. Admin Account
        Account adminAcc = accountRepository.findByEmail("admin_test@gmail.com").orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail("admin_test@gmail.com");
            acc.setPassword("Test@123");
            acc.setIsActive(1);
            acc.getRoles().add(adminRole);
            return accountRepository.save(acc);
        });
        adminToken = jwtService.generateToken(new CustomUserDetails(adminAcc));

        // 2. Doctor Account & Staff
        Account docAcc = accountRepository.findByEmail("doctor_reception@gmail.com").orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail("doctor_reception@gmail.com");
            acc.setPassword("Test@123");
            acc.setIsActive(1);
            acc.getRoles().add(doctorRole);
            return accountRepository.save(acc);
        });

        Expertise expertise = expertiseRepository.findAll().stream()
                .filter(e -> e.getExpertiseName().equals("Cardiology"))
                .findFirst()
                .orElseGet(() -> {
                    Expertise e = new Expertise();
                    e.setExpertiseName("Cardiology");
                    e.setIconUrl("heart");
                    return expertiseRepository.save(e);
                });
        expertiseId = expertise.getExpertiseId();

        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Dr. Reception Test")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Dr. Reception Test");
                    s.setStaffType(StaffType.DOCTOR);
                    s.setExpertise(expertise);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });
        doctorId = doctor.getStaffId();
        doctorToken = jwtService.generateToken(new CustomUserDetails(docAcc));

        // 3. Receptionist Account
        Account repAcc = accountRepository.findByEmail("receptionist_test@gmail.com").orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail("receptionist_test@gmail.com");
            acc.setPassword("Test@123");
            acc.setIsActive(1);
            acc.getRoles().add(repRole);
            return accountRepository.save(acc);
        });
        receptionistToken = jwtService.generateToken(new CustomUserDetails(repAcc));

        // 4. Patient Account & Patient
        Account patientAcc = accountRepository.findByEmail("patient_reception@gmail.com").orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail("patient_reception@gmail.com");
            acc.setPassword("Test@123");
            acc.setIsActive(1);
            acc.getRoles().add(patientRole);
            return accountRepository.save(acc);
        });

        Patient patient = patientRepository.findByAccount_AccountId(patientAcc.getAccountId()).orElseGet(() -> {
            Patient p = new Patient();
            p.setAccount(patientAcc);
            p.setFullName("Patient Reception");
            p.setPhone("0987654321");
            return patientRepository.save(p);
        });
        patientId = patient.getPatientId();

        // 5. Service
        Service srv = serviceRepository.findAll().stream()
                .filter(s -> s.getServiceName().equals("Test Blood Check"))
                .findFirst()
                .orElseGet(() -> {
                    Service s = new Service();
                    s.setServiceName("Test Blood Check");
                    s.setOriginalPrice(new BigDecimal("100000"));
                    s.setServiceType(com.clinic.common.enums.ServiceType.LAB_TEST);
                    s.setIsDeleted(0);
                    return serviceRepository.save(s);
                });
        serviceId = srv.getServiceId();

        // 6. Appointments
        validApptId = createTestAppointment(AppointmentStatus.PENDING);
        cancelledApptId = createTestAppointment(AppointmentStatus.CANCELLED);
        pendingApptId = createTestAppointment(AppointmentStatus.PENDING);
    }

    private Integer createTestAppointment(AppointmentStatus status) {
        Appointment app = new Appointment();
        app.setPatient(patientRepository.findById(patientId).get());
        app.setMainDoctor(staffRepository.findById(doctorId).get());
        app.setExpertise(expertiseRepository.findById(expertiseId).get());
        app.setAppointmentDate(LocalDate.now().plusDays(1));
        app.setTimeStart(LocalTime.of(9, 0));
        app.setAppointmentType(AppointmentType.ONLINE);
        app.setBookingMode(BookingMode.DOCTOR);
        app.setCreatedBy(com.clinic.common.enums.CreatedByType.PATIENT);
        app.setStatus(status);
        app.setIsDeleted(0);
        return appointmentRepository.save(app).getAppointmentId();
    }

    // =========================================================================================
    // Nhóm 4: Tiếp nhận, khám bệnh và quản lý hàng đợi
    // =========================================================================================

    @Test
    @Order(1)
    void test_TC04_01_CheckIn_Success() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
        .when()
            .patch("/api/v1/appointments/" + validApptId + "/status?status=CHECKED_IN")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", is("CHECKED_IN"))
            .body("data.queueNumber", notNullValue())
            .body("data.checkinTime", notNullValue());
    }

    @Test
    @Order(2)
    void test_TC04_02_CheckIn_NotFound() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
        .when()
            .patch("/api/v1/appointments/99999/status?status=CHECKED_IN")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(3)
    void test_TC04_03_CheckIn_Cancelled() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
        .when()
            .patch("/api/v1/appointments/" + cancelledApptId + "/status?status=CHECKED_IN")
        .then()
            .statusCode(400)
            .body("success", is(false))
            .body("message", containsString("Chỉ có thể Check-in"));
    }

    @Test
    @Order(4)
    void test_TC04_04_CheckIn_Twice() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
        .when()
            .patch("/api/v1/appointments/" + validApptId + "/status?status=CHECKED_IN")
        .then()
            .statusCode(400)
            .body("success", is(false))
            .body("message", containsString("đã được Check-in trước đó"));
    }

    @Test
    @Order(5)
    void test_TC04_05_SaveVitals_Success() {
        // Create MedicalRecord first via Admin (since Nurse can't create it directly)
        MedicalRecordRequest recordReq = new MedicalRecordRequest();
        recordReq.setPatientId(patientId);
        recordReq.setAppointmentId(validApptId);
        recordReq.setMainDoctorId(doctorId);

        medicalRecordId = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(recordReq)
        .when()
            .post("/api/v1/medical-records")
        .then()
            .statusCode(200)
            .extract().path("data.recordId");

        // Now Nurse/Receptionist triages
        TriageRequest triage = new TriageRequest();
        triage.setHeight(170);
        triage.setWeight(new BigDecimal("65.5"));
        triage.setBloodPressure("120/80");
        triage.setPulse(80);
        triage.setTemperature(37.0);

        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .body(triage)
        .when()
            .post("/api/v1/medical-records/" + medicalRecordId + "/triage")
        .then()
            .log().all()
            .statusCode(200)
            .body("success", is(true))
            .body("data.vitalsTaken", is(true));
    }

    @Test
    @Order(6)
    void test_TC04_06_SaveVitals_MissingRequired() {
        TriageRequest triage = new TriageRequest();
        // Missing height, weight, etc.

        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .body(triage)
        .when()
            .post("/api/v1/medical-records/" + medicalRecordId + "/triage")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(7)
    void test_TC04_07_SaveVitals_InvalidFormat() {
        // Negative weight, etc. But wait, TriageRequest only has @NotNull annotations for bigdecimal, no @Positive.
        // Let's just provide a valid string instead of bigdecimal to trigger type mismatch, or a blank blood pressure.
        TriageRequest triage = new TriageRequest();
        triage.setHeight(170);
        triage.setWeight(new BigDecimal("65.5"));
        triage.setBloodPressure(""); // @NotBlank validation will fail
        triage.setPulse(80);
        triage.setTemperature(37.0);

        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .body(triage)
        .when()
            .post("/api/v1/medical-records/" + medicalRecordId + "/triage")
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(8)
    void test_TC04_08_DoctorStartExam_Success() {
        given()
            .header("Authorization", "Bearer " + doctorToken)
        .when()
            .patch("/api/v1/appointments/" + validApptId + "/queue/call")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", is("IN_PROGRESS"));
    }

    @Test
    @Order(9)
    void test_TC04_09_DoctorStartExam_NotCheckedIn() {
        // pendingApptId is not checked in
        given()
            .header("Authorization", "Bearer " + doctorToken)
        .when()
            .patch("/api/v1/appointments/" + pendingApptId + "/queue/call")
        .then()
            .statusCode(400)
            .body("success", is(false))
            .body("message", containsString("CHECKED_IN"));
    }

    @Test
    @Order(10)
    void test_TC04_10_SaveDiagnosis_Success() {
        MedicalRecordRequest req = new MedicalRecordRequest();
        req.setPatientId(patientId);
        req.setMainDoctorId(doctorId);
        req.setDiagnosis("Cảm cúm");
        req.setTreatment("Nghỉ ngơi, uống nhiều nước");

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .put("/api/v1/medical-records/" + medicalRecordId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.diagnosis", is("Cảm cúm"));
    }

    @Test
    @Order(11)
    void test_TC04_11_SaveDiagnosis_MissingRequired() {
        MedicalRecordRequest req = new MedicalRecordRequest();
        // Missing patientId and mainDoctorId which are @NotNull in MedicalRecordRequest
        req.setDiagnosis("Đau đầu");

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .put("/api/v1/medical-records/" + medicalRecordId)
        .then()
            .statusCode(400)
            .body("success", is(false));
    }

    @Test
    @Order(12)
    void test_TC04_12_OrderService_Success() {
        ServiceOrderRequest req = new ServiceOrderRequest();
        req.setRecordId(medicalRecordId);
        req.setServiceId(serviceId);
        req.setOrderedById(doctorId);
        req.setDoctorNote("Cần xét nghiệm máu gấp");

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/v1/service-orders")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", is("ORDERED"));
    }
}
