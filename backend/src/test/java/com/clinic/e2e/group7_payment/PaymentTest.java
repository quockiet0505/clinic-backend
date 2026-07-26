package com.clinic.e2e.group7_payment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.InvoiceStatus;
import com.clinic.dto.finance.SepayWebhookPayload;
import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.e2e.BaseIntegrationTest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Invoice;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.InvoiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
public class PaymentTest extends BaseIntegrationTest {

    @Autowired private PatientRepository patientRepository;
    @Autowired private StaffRepository staffRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private com.clinic.repository.auth.AccountRepository accountRepository;
    @Autowired private com.clinic.repository.auth.RoleRepository roleRepository;
    @Autowired private com.clinic.security.JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    @Value("${sepay.webhook-secret}")
    private String sepayWebhookSecret;

    private static String receptionistToken;
    private static String patientToken;
    private static String doctorToken;
    
    private static Integer medicalRecordIdToDone;
    private static Integer newInvoiceId;
    private static Integer existingUnpaidInvoiceId;
    private static Integer existingPaidInvoiceId;
    private static Integer testPatientId;
    private static Integer testDoctorId;

    @BeforeAll
    public void setupData() {
        io.restassured.RestAssured.port = port;

        // Doctor Account
        com.clinic.entity.auth.Role docRole = roleRepository.findByRoleCode("DOCTOR").orElseGet(() -> {
            com.clinic.entity.auth.Role r = new com.clinic.entity.auth.Role();
            r.setRoleCode("DOCTOR");
            r.setRoleName("Doctor");
            return roleRepository.save(r);
        });

        com.clinic.entity.auth.Account docAcc = accountRepository.findByEmail("doc_pay@clinic.com").orElseGet(() -> {
            com.clinic.entity.auth.Account acc = new com.clinic.entity.auth.Account();
            acc.setEmail("doc_pay@clinic.com");
            acc.setPassword("Password123!");
            acc.setIsActive(1);
            acc = accountRepository.save(acc);
            acc.getRoles().add(docRole);
            return accountRepository.save(acc);
        });
        doctorToken = jwtService.generateToken(new com.clinic.security.CustomUserDetails(docAcc));

        // Receptionist Account
        com.clinic.entity.auth.Role repRole = roleRepository.findByRoleCode("RECEPTIONIST").orElseGet(() -> {
            com.clinic.entity.auth.Role r = new com.clinic.entity.auth.Role();
            r.setRoleCode("RECEPTIONIST");
            r.setRoleName("Receptionist");
            return roleRepository.save(r);
        });

        com.clinic.entity.auth.Account repAcc = accountRepository.findByEmail("rep1@clinic.com").orElseGet(() -> {
            com.clinic.entity.auth.Account acc = new com.clinic.entity.auth.Account();
            acc.setEmail("rep1@clinic.com");
            acc.setPassword("Password123!");
            acc.setIsActive(1);
            acc = accountRepository.save(acc);
            acc.getRoles().add(repRole);
            return accountRepository.save(acc);
        });
        receptionistToken = jwtService.generateToken(new com.clinic.security.CustomUserDetails(repAcc));

        // Patient Account
        com.clinic.entity.auth.Role patRole = roleRepository.findByRoleCode("PATIENT").orElseGet(() -> {
            com.clinic.entity.auth.Role r = new com.clinic.entity.auth.Role();
            r.setRoleCode("PATIENT");
            r.setRoleName("Patient");
            return roleRepository.save(r);
        });

        com.clinic.entity.auth.Account patAcc = accountRepository.findByEmail("pat1@clinic.com").orElseGet(() -> {
            com.clinic.entity.auth.Account acc = new com.clinic.entity.auth.Account();
            acc.setEmail("pat1@clinic.com");
            acc.setPassword("Password123!");
            acc.setIsActive(1);
            acc = accountRepository.save(acc);
            acc.getRoles().add(patRole);
            return accountRepository.save(acc);
        });
        patientToken = jwtService.generateToken(new com.clinic.security.CustomUserDetails(patAcc));

        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Payment Doctor")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Payment Doctor");
                    s.setStaffType(com.clinic.common.enums.StaffType.DOCTOR);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });

        Patient patient = patientRepository.findAll().stream()
                .filter(p -> p.getFullName().equals("Payment Patient")).findFirst()
                .orElseGet(() -> {
                    Patient p = new Patient();
                    p.setAccount(accountRepository.findById(patAcc.getAccountId()).orElse(null));
                    p.setFullName("Payment Patient");
                    p.setPhone("0999999999");
                    p.setGender("FEMALE");
                    p.setDateOfBirth(LocalDate.of(1998, 1, 1));
                    p.setIsDeleted(0);
                    return patientRepository.save(p);
                });
        testDoctorId = doctor.getStaffId();
        testPatientId = patient.getPatientId();

        // Setup Record To Done
        Appointment appointment1 = new Appointment();
        appointment1.setPatient(patient);
        appointment1.setMainDoctor(doctor);
        appointment1.setAppointmentDate(LocalDate.now());
        appointment1.setTimeStart(LocalTime.of(8, 0));
        appointment1.setTimeEnd(LocalTime.of(8, 30));
        appointment1.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment1.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment1.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment1 = appointmentRepository.save(appointment1);

        MedicalRecord record1 = new MedicalRecord();
        record1.setPatient(patient);
        record1.setMainDoctor(doctor);
        record1.setAppointment(appointment1);
        record1.setStatus(MedicalRecordStatus.IN_PROGRESS);
        record1 = medicalRecordRepository.save(record1);
        medicalRecordIdToDone = record1.getRecordId();

        // Setup Unpaid Invoice
        Appointment appointment2 = new Appointment();
        appointment2.setPatient(patient);
        appointment2.setMainDoctor(doctor);
        appointment2.setAppointmentDate(LocalDate.now());
        appointment2.setTimeStart(LocalTime.of(9, 0));
        appointment2.setTimeEnd(LocalTime.of(9, 30));
        appointment2.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment2.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment2.setStatus(AppointmentStatus.COMPLETED);
        appointment2 = appointmentRepository.save(appointment2);

        MedicalRecord record2 = new MedicalRecord();
        record2.setPatient(patient);
        record2.setMainDoctor(doctor);
        record2.setAppointment(appointment2);
        record2.setStatus(MedicalRecordStatus.DONE);
        record2.setConsultationFinalFee(new BigDecimal("200000"));
        record2 = medicalRecordRepository.save(record2);

        Invoice unpaidInvoice = new Invoice();
        unpaidInvoice.setMedicalRecord(record2);
        unpaidInvoice.setPatient(patient);
        unpaidInvoice.setStatus(InvoiceStatus.UNPAID);
        unpaidInvoice.setTotalPrice(new BigDecimal("200000"));
        unpaidInvoice = invoiceRepository.save(unpaidInvoice);
        existingUnpaidInvoiceId = unpaidInvoice.getInvoiceId();

        // Setup Paid Invoice
        Appointment appointment3 = new Appointment();
        appointment3.setPatient(patient);
        appointment3.setMainDoctor(doctor);
        appointment3.setAppointmentDate(LocalDate.now());
        appointment3.setTimeStart(LocalTime.of(10, 0));
        appointment3.setTimeEnd(LocalTime.of(10, 30));
        appointment3.setAppointmentType(com.clinic.common.enums.AppointmentType.WALK_IN);
        appointment3.setCreatedBy(com.clinic.common.enums.CreatedByType.RECEPTIONIST);
        appointment3.setStatus(AppointmentStatus.COMPLETED);
        appointment3 = appointmentRepository.save(appointment3);

        MedicalRecord record3 = new MedicalRecord();
        record3.setPatient(patient);
        record3.setMainDoctor(doctor);
        record3.setAppointment(appointment3);
        record3.setStatus(MedicalRecordStatus.DONE);
        record3.setConsultationFinalFee(new BigDecimal("150000"));
        record3 = medicalRecordRepository.save(record3);

        Invoice paidInvoice = new Invoice();
        paidInvoice.setMedicalRecord(record3);
        paidInvoice.setPatient(patient);
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setTotalPrice(new BigDecimal("150000"));
        paidInvoice = invoiceRepository.save(paidInvoice);
        existingPaidInvoiceId = paidInvoice.getInvoiceId();
    }

    private String generateSepaySignature(String timestamp, String rawBody) throws Exception {
        String dataToSign = timestamp + "." + rawBody;
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(sepayWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        String hash = HexFormat.of().formatHex(sha256Hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8)));
        return "sha256=" + hash;
    }

    @Test
    @Order(1)
    public void test_TC07_01_GenerateInvoice_WhenRecordDone() {
        MedicalRecordRequest req = new MedicalRecordRequest();
        req.setStatus(MedicalRecordStatus.DONE);
        req.setDiagnosis("Đã khám xong");
        req.setPatientId(testPatientId);
        req.setMainDoctorId(testDoctorId);

        given()
            .header("Authorization", "Bearer " + doctorToken)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .put("/api/v1/medical-records/" + medicalRecordIdToDone)
        .then()
            .statusCode(200)
            .body("success", is(true));

        // Get the generated invoice
        Invoice invoice = invoiceRepository.findByMedicalRecord_RecordId(medicalRecordIdToDone).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(invoice);
        org.junit.jupiter.api.Assertions.assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        newInvoiceId = invoice.getInvoiceId();
    }

    @Test
    @Order(2)
    public void test_TC07_02_GetAllInvoices_Receptionist() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/invoices")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.content", not(empty()));
    }

    @Test
    @Order(3)
    public void test_TC07_03_GetMyInvoices_Patient() {
        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/invoices/my")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data", not(empty()));
    }

    @Test
    @Order(4)
    public void test_TC07_04_GetInvoiceDetails() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/invoices/" + existingUnpaidInvoiceId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.invoiceId", equalTo(existingUnpaidInvoiceId));
    }

    @Test
    @Order(5)
    public void test_TC07_05_PayInvoiceByCash_Receptionist() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .queryParam("paymentMethod", "CASH")
        .when()
            .put("/api/v1/invoices/" + existingUnpaidInvoiceId + "/pay")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", equalTo("PAID"));
    }

    @Test
    @Order(6)
    public void test_TC07_06_PayInvoice_AlreadyPaid() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .queryParam("paymentMethod", "CASH")
        .when()
            .put("/api/v1/invoices/" + existingPaidInvoiceId + "/pay")
        .then()
            .statusCode(400)
            .body("message", containsString("already paid"));
    }

    @Test
    @Order(7)
    public void test_TC07_07_RequestTransferPayment_Patient() {
        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/v1/invoices/" + newInvoiceId + "/request-transfer")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", equalTo("PENDING_VERIFY"));
    }

    @Test
    @Order(8)
    public void test_TC07_08_ConfirmVerifyPayment_Reject() {
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .queryParam("approve", false)
        .when()
            .put("/api/v1/invoices/" + newInvoiceId + "/verify")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", equalTo("UNPAID"));
    }

    @Test
    @Order(9)
    public void test_TC07_09_ConfirmVerifyPayment_Approve() {
        // First request transfer again
        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/v1/invoices/" + newInvoiceId + "/request-transfer")
        .then().statusCode(200);

        // Approve it
        given()
            .header("Authorization", "Bearer " + receptionistToken)
            .contentType(ContentType.JSON)
            .queryParam("approve", true)
        .when()
            .put("/api/v1/invoices/" + newInvoiceId + "/verify")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.status", equalTo("PAID"));
    }

    @Test
    @Order(10)
    public void test_TC07_10_SepayWebhook_IgnoreNonIn() throws Exception {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setTransferType("out");
        String rawBody = objectMapper.writeValueAsString(payload);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSepaySignature(timestamp, rawBody);

        given()
            .header("X-SePay-Signature", signature)
            .header("X-SePay-Timestamp", timestamp)
            .contentType(ContentType.JSON)
            .body(rawBody)
        .when()
            .post("/api/v1/webhooks/sepay")
        .then()
            .statusCode(200)
            .body("success", is(true));
        
        // Nothing should change, it just returns 200 OK.
    }

    @Test
    @Order(11)
    public void test_TC07_11_SepayWebhook_InvalidSignature() throws Exception {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setTransferType("in");
        payload.setContent("Thanh toan BILL" + existingPaidInvoiceId);
        String rawBody = objectMapper.writeValueAsString(payload);
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        given()
            .header("X-SePay-Signature", "invalid-signature")
            .header("X-SePay-Timestamp", timestamp)
            .contentType(ContentType.JSON)
            .body(rawBody)
        .when()
            .post("/api/v1/webhooks/sepay")
        .then()
            .statusCode(401)
            .body("success", is(false))
            .body("message", equalTo("Invalid signature"));
    }

    @Test
    @Order(12)
    public void test_TC07_12_SepayWebhook_Success() throws Exception {
        // Create an UNPAID invoice for this test
        Invoice webhookInvoice = new Invoice();
        webhookInvoice.setMedicalRecord(medicalRecordRepository.findById(medicalRecordIdToDone).get());
        webhookInvoice.setPatient(patientRepository.findAll().get(0));
        webhookInvoice.setStatus(InvoiceStatus.UNPAID);
        webhookInvoice.setTotalPrice(new BigDecimal("500000"));
        webhookInvoice = invoiceRepository.save(webhookInvoice);

        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setTransferType("in");
        payload.setContent("CK tien thuoc BILL" + webhookInvoice.getInvoiceId());
        payload.setTransferAmount(500000.0);
        
        String rawBody = objectMapper.writeValueAsString(payload);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSepaySignature(timestamp, rawBody);

        given()
            .header("X-SePay-Signature", signature)
            .header("X-SePay-Timestamp", timestamp)
            .contentType(ContentType.JSON)
            .body(rawBody)
        .when()
            .post("/api/v1/webhooks/sepay")
        .then()
            .statusCode(200)
            .body("success", is(true));

        // Verify status changed to PAID
        Invoice updatedInvoice = invoiceRepository.findById(webhookInvoice.getInvoiceId()).get();
        org.junit.jupiter.api.Assertions.assertEquals(InvoiceStatus.PAID, updatedInvoice.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(com.clinic.common.enums.PaymentMethod.TRANSFER, updatedInvoice.getPaymentMethod());
    }
}
