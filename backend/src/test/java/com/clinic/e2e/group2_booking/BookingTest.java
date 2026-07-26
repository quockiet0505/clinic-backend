package com.clinic.e2e.group2_booking;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CreatedByType;
import com.clinic.common.enums.ServiceType;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.e2e.BaseIntegrationTest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.medical.Service;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.LeaveRequest;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.security.CustomUserDetails;
import com.clinic.security.JwtService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Nhóm 2: Đặt lịch khám trực tuyến (12 Test Cases)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingTest extends BaseIntegrationTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private StaffRepository staffRepository;
    @Autowired private ExpertiseRepository expertiseRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private JwtService jwtService;

    private String patientToken;
    private Integer patientId;
    private Integer doctorId;
    private Integer expertiseId;
    private Integer serviceId;
    private Integer otherDoctorId; // Bác sĩ không thuộc chuyên khoa
    private String adminToken; // Admin cho TC02-10 (khóa tài khoản)

    @BeforeEach
    public void setupData() {
        // Create Expertise
        Expertise expertise = expertiseRepository.findAll().stream()
                .filter(e -> e.getExpertiseName().equals("Tim Mạch Test")).findFirst()
                .orElseGet(() -> {
                    Expertise e = new Expertise();
                    e.setExpertiseName("Tim Mạch Test");
                    return expertiseRepository.save(e);
                });
        expertiseId = expertise.getExpertiseId();

        // Create Doctor
        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Dr. Booking Test")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Dr. Booking Test");
                    s.setStaffType(StaffType.DOCTOR);
                    s.setExpertise(expertise);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });
        doctorId = doctor.getStaffId();

        // Create other Doctor (khác chuyên khoa)
        Expertise otherExpertise = expertiseRepository.findAll().stream()
                .filter(e -> e.getExpertiseName().equals("Nhi Khoa Test")).findFirst()
                .orElseGet(() -> {
                    Expertise e = new Expertise();
                    e.setExpertiseName("Nhi Khoa Test");
                    return expertiseRepository.save(e);
                });

        Staff otherDoctor = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Dr. Other Test")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Dr. Other Test");
                    s.setStaffType(StaffType.DOCTOR);
                    s.setExpertise(otherExpertise);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });
        otherDoctorId = otherDoctor.getStaffId();

        // Create Service (Cận lâm sàng)
        Service service = serviceRepository.findAll().stream()
                .filter(s -> s.getServiceName().equals("Chụp X-Quang Test")).findFirst()
                .orElseGet(() -> {
                    Service s = new Service();
                    s.setServiceName("Chụp X-Quang Test");
                    s.setServiceType(ServiceType.X_RAY);
                    s.setOriginalPrice(new BigDecimal("150000"));
                    s.setIsDeleted(0);
                    return serviceRepository.save(s);
                });
        serviceId = service.getServiceId();

        // Tạo Lab Tech để Service mode chạy được
        Staff labTech = staffRepository.findAll().stream()
                .filter(s -> s.getFullName().equals("Tech Test")).findFirst()
                .orElseGet(() -> {
                    Staff s = new Staff();
                    s.setFullName("Tech Test");
                    s.setStaffType(StaffType.LAB_TECH);
                    s.setIsDeleted(0);
                    return staffRepository.save(s);
                });

        // Create Role if not exist
        Role patientRole = roleRepository.findByRoleCode("PATIENT").orElseGet(() -> {
            Role r = new Role();
            r.setRoleCode("PATIENT");
            r.setRoleName("Patient");
            return roleRepository.save(r);
        });

        // Create Patient Account
        Account patientAcc = accountRepository.findByEmail("patient_booking@gmail.com").orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail("patient_booking@gmail.com");
            acc.setPassword("123456");
            acc.setIsActive(1);
            acc = accountRepository.save(acc); // Save first to get ID
            acc.getRoles().add(patientRole);
            return accountRepository.save(acc); // Update with merge
        });

        // Create Patient Profile
        Patient patient = patientRepository.findByAccount_AccountId(patientAcc.getAccountId()).orElseGet(() -> {
            Patient p = new Patient();
            p.setAccount(patientAcc);
            p.setFullName("Booking Patient");
            p.setPhone("0909999999");
            p.setGender("MALE");
            p.setDateOfBirth(LocalDate.of(1990, 1, 1));
            p.setAddress("HCM");
            p.setIsDeleted(0);
            return patientRepository.save(p);
        });
        patientId = patient.getPatientId();

        patientToken = jwtService.generateToken(new CustomUserDetails(patientAcc));
        
        // Admin token for TC02-10
        Optional<Account> adminOpt = accountRepository.findByEmail("kiet@gmail.com");
        if (adminOpt.isPresent()) {
            adminToken = jwtService.generateToken(new CustomUserDetails(adminOpt.get()));
        }
    }

    private LocalDate getNextWorkingDay() {
        LocalDate date = LocalDate.now().plusDays(2); // Tránh vi phạm < 24h
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    @Test
    @Order(1)
    @DisplayName("TC02-01: Đặt lịch khám bác sĩ thành công")
    public void test_TC02_01_BookDoctor_Success() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(getNextWorkingDay());
        request.setTimeStart(LocalTime.of(8, 0));
        request.setTimeEnd(LocalTime.of(8, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Đau tức ngực"); // Triệu chứng

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", equalTo("Appointment created successfully"))
            .body("data.bookingMode", equalTo("DOCTOR"));
    }

    @Test
    @Order(2)
    @DisplayName("TC02-02: Đặt dịch vụ cận lâm sàng thành công")
    public void test_TC02_02_BookService_Success() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.SERVICE);
        request.setServiceId(serviceId);
        request.setAppointmentDate(getNextWorkingDay().plusDays(2)); // Tránh trùng ngày với TC02-01
        request.setTimeStart(LocalTime.of(9, 0));
        request.setTimeEnd(LocalTime.of(9, 30));
        request.setAppointmentType(AppointmentType.ONLINE); // Hoặc WALK_IN nhưng ONLINE yêu cầu hẹn trước
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Đăng ký chụp X-Quang");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.bookingMode", equalTo("SERVICE"));
    }

    @Test
    @Order(3)
    @DisplayName("TC02-03: Không chọn chuyên khoa")
    public void test_TC02_03_MissingExpertise() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setMainDoctorId(doctorId); // Thiếu expertiseId
        request.setAppointmentDate(getNextWorkingDay());
        request.setTimeStart(LocalTime.of(8, 0));
        request.setTimeEnd(LocalTime.of(8, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Triệu chứng abc");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            // Exception trong service throw RuntimeException -> GlobalException mapping to 400
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("Vui lòng chọn chuyên khoa"));
    }

    @Test
    @Order(4)
    @DisplayName("TC02-04: Không chọn bác sĩ")
    public void test_TC02_04_MissingDoctor() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId); // Thiếu mainDoctorId
        request.setAppointmentDate(getNextWorkingDay());
        request.setTimeStart(LocalTime.of(8, 0));
        request.setTimeEnd(LocalTime.of(8, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Triệu chứng xyz");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("Vui lòng chọn bác sĩ"));
    }

    @Test
    @Order(5)
    @DisplayName("TC02-05: Không chọn ngày hoặc giờ")
    public void test_TC02_05_MissingDateTime() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Triệu chứng xyz");
        // Thiếu Date, TimeStart, TimeEnd (@NotNull sẽ báo Validation Error)

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("Validation"));
    }

    @Test
    @Order(6)
    @DisplayName("TC02-06: Bỏ trống triệu chứng")
    public void test_TC02_06_MissingSymptoms() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(getNextWorkingDay());
        request.setTimeStart(LocalTime.of(8, 0));
        request.setTimeEnd(LocalTime.of(8, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote(""); // Trống note (@NotBlank báo Validation Error)

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("data.note", notNullValue());
    }

    @Test
    @Order(7)
    @DisplayName("TC02-07: Đặt lịch dưới 24 giờ")
    public void test_TC02_07_BookUnder24Hours() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(LocalDate.now()); // Khám trong ngày
        request.setTimeStart(LocalTime.now().plusHours(1)); // Dưới 24h
        request.setTimeEnd(LocalTime.now().plusHours(1).plusMinutes(30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Cần khám gấp");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("24 hours"));
    }

    @Test
    @Order(8)
    @DisplayName("TC02-08: Đặt vào ngày bác sĩ nghỉ phép")
    public void test_TC02_08_DoctorOnLeave() {
        LocalDate leaveDate = getNextWorkingDay();
        
        // Tạo đơn xin nghỉ phép cho bác sĩ
        LeaveRequest leave = new LeaveRequest();
        leave.setStaff(staffRepository.findById(doctorId).get());
        leave.setFromDate(leaveDate);
        leave.setToDate(leaveDate);
        leave.setStatus(com.clinic.common.enums.LeaveStatus.APPROVED);
        leaveRequestRepository.save(leave);

        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(leaveDate); // Đặt trúng ngày nghỉ
        request.setTimeStart(LocalTime.of(10, 0)); // Tránh trùng 08:00
        request.setTimeEnd(LocalTime.of(10, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Khám bình thường");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("Doctor is on leave"));
    }

    @Test
    @Order(9)
    @DisplayName("TC02-09: Khung giờ vừa được người khác đặt")
    public void test_TC02_09_SlotAlreadyTaken() {
        LocalDate date = getNextWorkingDay().plusDays(1); // Tránh dính ngày nghỉ ở TC08
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 30);

        // Tạo 1 cuộc hẹn ảo chiếm chỗ trước
        Appointment taken = new Appointment();
        taken.setPatient(patientRepository.findById(patientId).get());
        taken.setMainDoctor(staffRepository.findById(doctorId).get());
        taken.setAppointmentDate(date);
        taken.setTimeStart(start);
        taken.setTimeEnd(end);
        taken.setAppointmentType(AppointmentType.ONLINE);
        taken.setCreatedBy(CreatedByType.RECEPTIONIST);
        taken.setBookingMode(BookingMode.DOCTOR);
        taken.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(taken);

        // Đặt lại đúng giờ đó
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(date);
        request.setTimeStart(start);
        request.setTimeEnd(end);
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Khám bệnh");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("already have an appointment"));
            // Lưu ý: Logic code hiện tại ném lỗi "You already have an appointment at this time." 
            // Nếu dùng account khác đặt thì sẽ nhận lỗi "Selected time slot is no longer available."
    }

    @Test
    @Order(10)
    @DisplayName("TC02-10: Tài khoản bị khóa đặt lịch")
    public void test_TC02_10_AccountLocked() {
        // Tạm khóa account bệnh nhân
        if (adminToken != null) {
            Account acc = accountRepository.findByEmail("patient_booking@gmail.com").get();
            acc.setIsActive(0); // Khóa
            accountRepository.save(acc);
            
            AppointmentRequest request = new AppointmentRequest();
            request.setBookingMode(BookingMode.DOCTOR);
            request.setExpertiseId(expertiseId);
            request.setMainDoctorId(doctorId);
            request.setAppointmentDate(getNextWorkingDay().plusDays(2));
            request.setTimeStart(LocalTime.of(14, 0));
            request.setTimeEnd(LocalTime.of(14, 30));
            request.setAppointmentType(AppointmentType.ONLINE);
            request.setCreatedBy(CreatedByType.PATIENT);
            request.setNote("Test");

            given()
                .header("Authorization", "Bearer " + patientToken) // Token vẫn còn hạn nhưng account bị vô hiệu
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/v1/appointments")
            .then()
                .statusCode(anyOf(equalTo(403), equalTo(401), equalTo(400)));
            
            // Mở khóa lại để không ảnh hưởng TC sau
            acc.setIsActive(1);
            accountRepository.save(acc);
        }
    }

    @Test
    @Order(11)
    @DisplayName("TC02-11: Đặt lịch khi chưa đăng nhập")
    public void test_TC02_11_BookWithoutLogin() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId);
        request.setMainDoctorId(doctorId);
        request.setAppointmentDate(getNextWorkingDay().plusDays(2));
        request.setTimeStart(LocalTime.of(15, 0));
        request.setTimeEnd(LocalTime.of(15, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Test no auth");

        given()
            // Không set Authorization header
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(12)
    @DisplayName("TC02-12: Bác sĩ không thuộc chuyên khoa")
    public void test_TC02_12_DoctorWrongExpertise() {
        AppointmentRequest request = new AppointmentRequest();
        request.setBookingMode(BookingMode.DOCTOR);
        request.setExpertiseId(expertiseId); // Chọn Tim mạch
        request.setMainDoctorId(otherDoctorId); // Bác sĩ không thuộc Tim mạch
        request.setAppointmentDate(getNextWorkingDay().plusDays(2));
        request.setTimeStart(LocalTime.of(16, 0));
        request.setTimeEnd(LocalTime.of(16, 30));
        request.setAppointmentType(AppointmentType.ONLINE);
        request.setCreatedBy(CreatedByType.PATIENT);
        request.setNote("Khám");

        given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", containsString("Bác sĩ không thuộc chuyên khoa"));
    }
}
