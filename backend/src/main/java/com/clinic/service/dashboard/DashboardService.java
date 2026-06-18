package com.clinic.service.dashboard;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.dashboard.*;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.crm.FeedbackRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffDoctorReviewRepository;
import com.clinic.repository.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final FeedbackRepository feedbackRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final StaffDoctorReviewRepository doctorReviewRepository;

    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

        long totalPatients = patientRepository.count();
        long appointmentsToday = appointmentRepository
                .findByAppointmentDateAndStatusAndIsDeleted(today, AppointmentStatus.CONFIRMED, 0).size();
        long appointmentsThisWeek = appointmentRepository
                .findByAppointmentDateBetweenAndIsDeleted(startOfWeek, today, 0).size();
        long totalAppointments = appointmentRepository.count();

        long completedAppointments = appointmentRepository
                .findByAppointmentDateAndStatusAndIsDeleted(today, AppointmentStatus.COMPLETED, 0).size();
        long cancelledAppointments = appointmentRepository
                .findByAppointmentDateAndStatusAndIsDeleted(today, AppointmentStatus.CANCELLED, 0).size();
        long noShowAppointments = appointmentRepository
                .findByAppointmentDateAndStatusAndIsDeleted(today, AppointmentStatus.NO_SHOW, 0).size();
        long pendingAppointments = appointmentRepository
                .findByAppointmentDateAndStatusAndIsDeleted(today, AppointmentStatus.PENDING, 0).size();

        long totalStaff = staffRepository.count();
        long totalDoctors = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0).size();
        long totalFeedbacks = feedbackRepository.count();

        Double avgRating = doctorReviewRepository.getAverageRatingByDoctorId(null);
        if (avgRating == null) avgRating = 0.0;

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        double monthlyRevenue = appointmentRepository
                .findByAppointmentDateBetweenAndIsDeleted(startOfMonth, endOfMonth, 0)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .mapToDouble(a -> a.getService() != null ? a.getService().getOriginalPrice().doubleValue() : 0)
                .sum();

        return DashboardStatsResponse.builder()
                .totalPatients(totalPatients)
                .appointmentsToday(appointmentsToday)
                .appointmentsThisWeek(appointmentsThisWeek)
                .completedAppointments(completedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .noShowAppointments(noShowAppointments)
                .pendingAppointments(pendingAppointments)
                .totalAppointments(totalAppointments)
                .totalStaff(totalStaff)
                .totalDoctors(totalDoctors)
                .totalFeedbacks(totalFeedbacks)
                .avgRating(avgRating)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    public List<DoctorStatResponse> getDoctorStats(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Staff> doctors = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);

        return doctors.stream().map(doctor -> {
            List<Appointment> appointments = appointmentRepository
                    .findByMainDoctor_StaffIdAndAppointmentDateBetweenAndIsDeleted(
                            doctor.getStaffId(), startDate, endDate, 0);

            long total = appointments.size();
            long completed = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();

            double revenue = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .mapToDouble(a -> a.getService() != null ? a.getService().getOriginalPrice().doubleValue() : 0)
                    .sum();

            double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
            Double avgRating = doctorReviewRepository.getAverageRatingByDoctorId(doctor.getStaffId());
            if (avgRating == null) avgRating = 0.0;

            return DoctorStatResponse.builder()
                    .doctorId(doctor.getStaffId())
                    .doctorName(doctor.getFullName())
                    .imageUrl(doctor.getImageUrl())
                    .totalAppointments(total)
                    .completedAppointments(completed)
                    .completionRate(Math.round(completionRate * 10) / 10.0)
                    .revenue(Math.round(revenue * 100) / 100.0)
                    .avgRating(Math.round(avgRating * 10) / 10.0)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ServiceStatResponse> getServiceStats(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startDateTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = ym.atEndOfMonth().atTime(23, 59, 59);

        List<com.clinic.entity.medical.Service> services = serviceRepository.findByIsDeleted(0);

        return services.stream().map(service -> {
            List<ServiceOrder> orders = serviceOrderRepository
                    .findByServiceIdAndCreatedAtBetween(service.getServiceId(), startDateTime, endDateTime);

            long total = orders.size();
            long completed = orders.stream()
                    .filter(o -> o.getStatus() == ServiceOrderStatus.DONE)
                    .count();

            double revenue = orders.stream()
                    .filter(o -> o.getStatus() == ServiceOrderStatus.DONE)
                    .mapToDouble(o -> service.getOriginalPrice().doubleValue())
                    .sum();

            double completionRate = total > 0 ? (completed * 100.0 / total) : 0;

            return ServiceStatResponse.builder()
                    .serviceId(service.getServiceId())
                    .serviceName(service.getServiceName())
                    .imageUrl(service.getImageUrl()) 
                    .totalOrders(total)
                    .completedOrders(completed)
                    .completionRate(Math.round(completionRate * 10) / 10.0)
                    .revenue(Math.round(revenue * 100) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }

    public PatientStatsResponse getPatientStats(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        long newPatients = patientRepository.findByIsDeleted(0)
                .stream()
                .filter(p -> {
                    if (p.getCreatedAt() == null) return false;
                    LocalDate created = p.getCreatedAt().toLocalDate();
                    return !created.isBefore(startDate) && !created.isAfter(endDate);
                })
                .count();

        List<Patient> allPatients = patientRepository.findByIsDeleted(0);
        long returningPatients = allPatients.stream()
                .filter(p -> {
                    long count = appointmentRepository.countByPatientIdAndAppointmentDateBetween(
                            p.getPatientId(), startDate, endDate);
                    return count >= 2;
                })
                .count();

        List<PatientStatsResponse.TopPatient> topPatients = allPatients.stream()
                .map(p -> {
                    long count = appointmentRepository.countByPatientIdAndAppointmentDateBetween(
                            p.getPatientId(), startDate, endDate);
                    double spent = appointmentRepository.sumServicePriceByPatientIdAndAppointmentDateBetween(
                            p.getPatientId(), startDate, endDate);
                    String lastVisit = appointmentRepository
                            .findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(p.getPatientId(), 0)
                            .stream()
                            .findFirst()
                            .map(a -> a.getAppointmentDate().toString())
                            .orElse(null);
                    return PatientStatsResponse.TopPatient.builder()
                            .patientId(p.getPatientId())
                            .patientName(p.getFullName())
                            .avatarUrl(p.getAvatarUrl())
                            .visitCount(count)
                            .totalSpent(spent)
                            .lastVisit(lastVisit)
                            .build();
                })
                .filter(p -> p.getVisitCount() > 0)
                .sorted((a, b) -> Long.compare(b.getVisitCount(), a.getVisitCount()))
                .limit(5)
                .collect(Collectors.toList());

        return PatientStatsResponse.builder()
                .newPatients(newPatients)
                .returningPatients(returningPatients)
                .topPatients(topPatients)
                .build();
    }

    public RevenueStatsResponse getRevenueStats(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Appointment> appointments = appointmentRepository
                .findByAppointmentDateBetweenAndIsDeleted(startDate, endDate, 0);

        double totalRevenue = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .mapToDouble(a -> a.getService() != null ? a.getService().getOriginalPrice().doubleValue() : 0)
                .sum();

        double consultationRevenue = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED && a.getService() != null)
                .filter(a -> "EXAMINATION".equals(a.getService().getServiceType().name()))
                .mapToDouble(a -> a.getService().getOriginalPrice().doubleValue())
                .sum();

        double serviceRevenue = totalRevenue - consultationRevenue;

        List<RevenueStatsResponse.MonthlyTrend> monthlyTrend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ymTrend = YearMonth.of(year, month).minusMonths(i);
            LocalDate startTrend = ymTrend.atDay(1);
            LocalDate endTrend = ymTrend.atEndOfMonth();

            double revenue = appointmentRepository
                    .findByAppointmentDateBetweenAndIsDeleted(startTrend, endTrend, 0)
                    .stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .mapToDouble(a -> a.getService() != null ? a.getService().getOriginalPrice().doubleValue() : 0)
                    .sum();

            monthlyTrend.add(RevenueStatsResponse.MonthlyTrend.builder()
                    .name(ymTrend.getMonth().toString() + " " + ymTrend.getYear())
                    .revenue(Math.round(revenue * 100) / 100.0)
                    .build());
        }

        Map<String, Double> serviceRevenueMap = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED && a.getService() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getService().getServiceName(),
                        Collectors.summingDouble(a -> a.getService().getOriginalPrice().doubleValue())
                ));

        List<RevenueStatsResponse.ServiceRevenue> byService = serviceRevenueMap.entrySet().stream()
                .map(e -> RevenueStatsResponse.ServiceRevenue.builder()
                        .serviceName(e.getKey())
                        .revenue(Math.round(e.getValue() * 100) / 100.0)
                        .percentage(totalRevenue > 0 ? Math.round((e.getValue() / totalRevenue) * 1000) / 10.0 : 0)
                        .build())
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .collect(Collectors.toList());

        return RevenueStatsResponse.builder()
                .totalRevenue(Math.round(totalRevenue * 100) / 100.0)
                .consultationRevenue(Math.round(consultationRevenue * 100) / 100.0)
                .serviceRevenue(Math.round(serviceRevenue * 100) / 100.0)
                .monthlyTrend(monthlyTrend)
                .byService(byService)
                .build();
    }

    // ====== MONTHLY STATS ======
    public List<MonthlyStatResponse> getMonthlyStats(int year) {
        String[] monthNames = {"Thg 1", "Thg 2", "Thg 3", "Thg 4", "Thg 5", "Thg 6",
                               "Thg 7", "Thg 8", "Thg 9", "Thg 10", "Thg 11", "Thg 12"};
        List<MonthlyStatResponse> result = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            
            List<Appointment> appointments = appointmentRepository
                    .findByAppointmentDateBetweenAndIsDeleted(start, end, 0);
            
            long completed = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();
            long cancelled = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                    .count();
            long rescheduled = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED 
                            && a.getCancelReason() != null 
                            && a.getCancelReason().contains("reschedule"))
                    .count();
                    
            result.add(MonthlyStatResponse.builder()
                    .name(monthNames[month - 1])
                    .completed(completed)
                    .cancelled(cancelled)
                    .rescheduled(rescheduled)
                    .build());
        }
        return result;
    }

    // ====== RECENT APPOINTMENTS ======
    public List<RecentAppointmentResponse> getRecentAppointments(int limit) {
        return appointmentRepository.findByIsDeleted(0)
                .stream()
                .sorted((a, b) -> b.getAppointmentDate().compareTo(a.getAppointmentDate()))
                .limit(limit)
                .map(a -> RecentAppointmentResponse.builder()
                        .appointmentId(a.getAppointmentId())
                        .patientName(a.getPatient() != null ? a.getPatient().getFullName() : "Unknown")
                        .appointmentDate(a.getAppointmentDate().toString())
                        .status(a.getStatus().name())
                        .patientAvatarUrl(a.getPatient().getAvatarUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // ====== REPORT PREVIEW ======
    public String generateReportPreview(ReportFilterRequest filter) {
        StringBuilder preview = new StringBuilder();
        preview.append("=== BÁO CÁO THỐNG KÊ ===\n");
        preview.append("Thời gian: ");
        if ("month".equals(filter.getPeriod())) {
            preview.append("Tháng ").append(filter.getMonth()).append("/").append(filter.getYear());
        } else {
            preview.append("Quý ").append(filter.getQuarter()).append("/").append(filter.getYear());
        }
        preview.append("\n\n");

        String type = filter.getType();

        if ("all".equals(type) || "overview".equals(type)) {
            DashboardStatsResponse stats = getStats();
            preview.append("--- TỔNG QUAN ---\n");
            preview.append("Tổng bệnh nhân: ").append(stats.getTotalPatients()).append("\n");
            preview.append("Lịch hẹn hôm nay: ").append(stats.getAppointmentsToday()).append("\n");
            preview.append("Hoàn thành: ").append(stats.getCompletedAppointments()).append("\n");
            preview.append("Đã hủy: ").append(stats.getCancelledAppointments()).append("\n");
            preview.append("Điểm đánh giá TB: ").append(stats.getAvgRating()).append("\n\n");
        }

        if ("all".equals(type) || "doctors".equals(type)) {
            int month = filter.getMonth() != null ? filter.getMonth() : 1;
            int year = filter.getYear() != null ? filter.getYear() : LocalDate.now().getYear();
            List<DoctorStatResponse> doctors = getDoctorStats(month, year);
            preview.append("--- THỐNG KÊ BÁC SĨ ---\n");
            doctors.forEach(d -> preview.append(d.getDoctorName())
                    .append(": ").append(d.getTotalAppointments())
                    .append(" ca, hoàn thành ").append(d.getCompletionRate())
                    .append("%, doanh thu ").append(String.format("%.0f", d.getRevenue())).append("đ\n"));
            preview.append("\n");
        }

        if ("all".equals(type) || "services".equals(type)) {
            int month = filter.getMonth() != null ? filter.getMonth() : 1;
            int year = filter.getYear() != null ? filter.getYear() : LocalDate.now().getYear();
            List<ServiceStatResponse> services = getServiceStats(month, year);
            preview.append("--- THỐNG KÊ DỊCH VỤ ---\n");
            services.forEach(s -> preview.append(s.getServiceName())
                    .append(": ").append(s.getTotalOrders())
                    .append(" lượt, hoàn thành ").append(s.getCompletionRate())
                    .append("%, doanh thu ").append(String.format("%.0f", s.getRevenue())).append("đ\n"));
            preview.append("\n");
        }

        if ("all".equals(type) || "patients".equals(type)) {
            int month = filter.getMonth() != null ? filter.getMonth() : 1;
            int year = filter.getYear() != null ? filter.getYear() : LocalDate.now().getYear();
            PatientStatsResponse patients = getPatientStats(month, year);
            preview.append("--- THỐNG KÊ BỆNH NHÂN ---\n");
            preview.append("Bệnh nhân mới: ").append(patients.getNewPatients()).append("\n");
            preview.append("Bệnh nhân quay lại: ").append(patients.getReturningPatients()).append("\n");
            preview.append("Top bệnh nhân:\n");
            patients.getTopPatients().forEach(p -> preview.append("  - ").append(p.getPatientName())
                    .append(": ").append(p.getVisitCount()).append(" lần, chi ").append(String.format("%.0f", p.getTotalSpent())).append("đ\n"));
            preview.append("\n");
        }

        if ("all".equals(type) || "revenue".equals(type)) {
            int month = filter.getMonth() != null ? filter.getMonth() : 1;
            int year = filter.getYear() != null ? filter.getYear() : LocalDate.now().getYear();
            RevenueStatsResponse revenue = getRevenueStats(month, year);
            preview.append("--- THỐNG KÊ DOANH THU ---\n");
            preview.append("Tổng doanh thu: ").append(String.format("%.0f", revenue.getTotalRevenue())).append("đ\n");
            preview.append("Tiền khám: ").append(String.format("%.0f", revenue.getConsultationRevenue())).append("đ\n");
            preview.append("Tiền dịch vụ: ").append(String.format("%.0f", revenue.getServiceRevenue())).append("đ\n");
            preview.append("Doanh thu theo dịch vụ:\n");
            revenue.getByService().forEach(s -> preview.append("  - ").append(s.getServiceName())
                    .append(": ").append(String.format("%.0f", s.getRevenue())).append("đ (").append(s.getPercentage()).append("%)\n"));
        }

        return preview.toString();
    }

    // ====== GENERATE REPORT ======
    public byte[] generateReport(ReportFilterRequest filter) {
        String content = generateReportPreview(filter);
        return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}