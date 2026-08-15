package com.clinic.config;

import com.clinic.common.enums.*;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Service;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.patient.Patient;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.crm.DoctorReviewRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
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
    private final ServiceOrderRepository serviceOrderRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking dashboard mock data...");

        // 1. Check/Seed Expertise
        List<Expertise> expertises = expertiseRepository.findAll();
        if (expertises.isEmpty()) {
            expertises = new ArrayList<>();
            expertises.add(createExpertise("Nội tổng quát"));
            expertises.add(createExpertise("Tai Mũi Họng"));
            expertises.add(createExpertise("Răng Hàm Mặt"));
        }

        // 2. Check/Seed Services
        List<Service> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            services = new ArrayList<>();
            services.add(createService("Khám tổng quát", ServiceType.EXAM, new BigDecimal("150000")));
            services.add(createService("Xét nghiệm tổng công thức máu", ServiceType.LAB_TEST, new BigDecimal("250000")));
            services.add(createService("Siêu âm bụng tổng quát", ServiceType.ULTRASOUND, new BigDecimal("350000")));
            services.add(createService("Chụp X-Quang phổi", ServiceType.X_RAY, new BigDecimal("200000")));
        }

        // 3. Check/Seed Doctors
        List<Staff> doctors = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);
        if (doctors.isEmpty()) {
            doctors = new ArrayList<>();
            doctors.add(createDoctor("Nguyen Van A", "doctor1@clinic.com", expertises.get(0)));
            doctors.add(createDoctor("Tran Thi B", "doctor2@clinic.com", expertises.get(1)));
            doctors.add(createDoctor("Pham Minh C", "doctor3@clinic.com", expertises.get(2)));
        }

        // 4. Check/Seed Patients
        List<Patient> patients = patientRepository.findByIsDeleted(0);
        if (patients.isEmpty()) {
            patients = new ArrayList<>();
            patients.add(createPatient("Nguyen Van Binh", "patient1@clinic.com", "0901234567"));
            patients.add(createPatient("Le Thi Hoa", "patient2@clinic.com", "0902345678"));
            patients.add(createPatient("Tran Van Cuong", "patient3@clinic.com", "0903456789"));
            patients.add(createPatient("Pham Thi Mai", "patient4@clinic.com", "0904567890"));
            patients.add(createPatient("Hoang Xuan Phuc", "patient5@clinic.com", "0905678901"));
        }

        // 5. Check/Seed Appointments from August 1 to August 14, 2026
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 14);

        long countInPeriod = appointmentRepository.findByAppointmentDateBetweenAndIsDeleted(startDate, endDate, 0).size();
        if (countInPeriod == 0) {
            log.info("Seeding dashboard appointments from Aug 1 to Aug 14, 2026...");
            Random rand = new Random();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // Seed 3-4 appointments per day to ensure rich dashboard charts
                int numAppointments = 3 + rand.nextInt(2); 
                for (int i = 0; i < numAppointments; i++) {
                    Patient patient = patients.get(rand.nextInt(patients.size()));
                    Staff doctor = doctors.get(rand.nextInt(doctors.size()));
                    Service service = services.get(rand.nextInt(services.size()));

                    // Status distribution: 70% COMPLETED, 15% CANCELLED, 15% CONFIRMED
                    int roll = rand.nextInt(100);
                    AppointmentStatus status;
                    if (roll < 70) {
                        status = AppointmentStatus.COMPLETED;
                    } else if (roll < 85) {
                        status = AppointmentStatus.CANCELLED;
                    } else {
                        status = AppointmentStatus.CONFIRMED;
                    }

                    // Create unique slot time to satisfy (doctor, date, timeStart) unique constraint
                    LocalTime timeStart = LocalTime.of(8 + i, i % 2 == 0 ? 0 : 30);
                    LocalTime timeEnd = timeStart.plusMinutes(30);

                    Appointment appointment = new Appointment();
                    appointment.setPatient(patient);
                    appointment.setMainDoctor(doctor);
                    appointment.setService(service);
                    appointment.setExpertise(doctor.getExpertise());
                    appointment.setAppointmentDate(date);
                    appointment.setTimeStart(timeStart);
                    appointment.setTimeEnd(timeEnd);
                    appointment.setAppointmentType(AppointmentType.ONLINE);
                    appointment.setStatus(status);
                    appointment.setCreatedBy(CreatedByType.PATIENT);
                    appointment.setBookingMode(BookingMode.DOCTOR);
                    appointment.setIsDeleted(0);

                    if (status == AppointmentStatus.CANCELLED) {
                        appointment.setCancelledBy(CancelledByType.PATIENT);
                        appointment.setCancelReason("Bận việc đột xuất");
                    }

                    appointment = appointmentRepository.save(appointment);

                    // Seed related record for completed appointments
                    if (status == AppointmentStatus.COMPLETED) {
                        MedicalRecord record = new MedicalRecord();
                        record.setPatient(patient);
                        record.setAppointment(appointment);
                        record.setMainDoctor(doctor);
                        record.setDiagnosis("Kiểm tra định kỳ");
                        record.setTreatment("Sức khỏe bình thường");
                        record.setNote("Theo dõi thêm");
                        record.setStatus(MedicalRecordStatus.DONE);
                        record.setConsultationOriginalFee(service.getOriginalPrice());
                        record.setConsultationFinalFee(service.getOriginalPrice());
                        record.setVitalsTaken(true);
                        record = medicalRecordRepository.save(record);

                        // Seed doctor review
                        DoctorReview review = new DoctorReview();
                        review.setDoctor(doctor);
                        review.setPatient(patient);
                        review.setAppointment(appointment);
                        review.setRating(4 + rand.nextInt(2)); // 4 or 5 stars
                        review.setComment("Dịch vụ tốt, bác sĩ nhiệt tình!");
                        review.setCreatedAt(date.atTime(timeStart));
                        review.setAiStatus("APPROVED");
                        doctorReviewRepository.save(review);

                        // 50% chance to have an extra service order (e.g. lab test ordered during exam)
                        if (rand.nextBoolean() && service.getServiceType() == ServiceType.EXAM) {
                            Service labService = services.stream()
                                    .filter(s -> s.getServiceType() == ServiceType.LAB_TEST)
                                    .findFirst()
                                    .orElse(service);

                            ServiceOrder order = new ServiceOrder();
                            order.setMedicalRecord(record);
                            order.setService(labService);
                            order.setOrderedBy(doctor);
                            order.setServiceOriginalFee(labService.getOriginalPrice());
                            order.setServiceFinalFee(labService.getOriginalPrice());
                            order.setStatus(ServiceOrderStatus.DONE);
                            order.setSampleCollectedAt(date.atTime(timeStart.plusMinutes(20)));
                            order.setSampleCollectedBy(doctor);
                            serviceOrderRepository.save(order);
                        }
                    }
                }
            }
            log.info("Successfully seeded dashboard mock data!");
        } else {
            log.info("Dashboard statistics in Aug 2026 already populated. Skipping seeder.");
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

    private Staff createDoctor(String name, String email, Expertise expertise) {
        Role role = roleRepository.findByRoleCode("DOCTOR")
                .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));

        Account acc = new Account();
        acc.setEmail(email);
        acc.setPassword(passwordEncoder.encode("12345678"));
        acc.setIsActive(1);
        acc.getRoles().add(role);
        acc = accountRepository.save(acc);

        Staff staff = new Staff();
        staff.setAccount(acc);
        staff.setFullName(name);
        staff.setGender("Nam");
        staff.setStaffType(StaffType.DOCTOR);
        staff.setExpertise(expertise);
        staff.setIsDeleted(0);
        return staffRepository.save(staff);
    }

    private Patient createPatient(String name, String email, String phone) {
        Role role = roleRepository.findByRoleCode("PATIENT")
                .orElseThrow(() -> new RuntimeException("PATIENT role not found"));

        Account acc = new Account();
        acc.setEmail(email);
        acc.setPassword(passwordEncoder.encode("12345678"));
        acc.setIsActive(1);
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
