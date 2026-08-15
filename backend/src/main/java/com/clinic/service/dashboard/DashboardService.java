package com.clinic.service.dashboard;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.dashboard.*;
import com.clinic.dto.medical.ServiceFilterRequest;
import com.clinic.dto.patient.PatientFilterRequest;
import com.clinic.dto.staff.StaffFilterRequest;
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
import com.clinic.specification.appointment.AppointmentSpecification;
import com.clinic.specification.medical.ServiceSpecification;
import com.clinic.specification.patient.PatientSpecification;
import com.clinic.specification.staff.StaffSpecification;
import com.clinic.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public DoctorStatsPageResponse getDoctorStats(DashboardPeriodFilterRequest request) {
        int month = resolveMonth(request.getMonth());
        int year = resolveYear(request.getYear());
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        StaffFilterRequest staffFilter = new StaffFilterRequest();
        staffFilter.setSearch(request.getSearch());
        staffFilter.setStaffType(StaffType.DOCTOR);

        Specification<Staff> spec = StaffSpecification.filterBy(staffFilter);
        List<Staff> doctors = staffRepository.findAll(spec);

        List<Appointment> monthAppointments = appointmentRepository
                .findByAppointmentDateBetweenAndIsDeleted(startDate, endDate, 0);
        Map<Integer, List<Appointment>> apptsByDoctor = monthAppointments.stream()
                .filter(a -> a.getMainDoctor() != null)
                .collect(Collectors.groupingBy(a -> a.getMainDoctor().getStaffId()));

        List<DoctorStatResponse> ranked = doctors.stream()
                .map(doctor -> buildDoctorStat(doctor,
                        apptsByDoctor.getOrDefault(doctor.getStaffId(), List.of())))
                .sorted((a, b) -> Double.compare(b.getCompletionRate(), a.getCompletionRate()))
                .collect(Collectors.toList());

        PageResponse<DoctorStatResponse> page = paginateList(ranked, request.getPage(), request.getSize());

        double totalRevenue = 0;
        double completionSum = 0;
        for (DoctorStatResponse stat : ranked) {
            totalRevenue += stat.getRevenue();
            completionSum += stat.getCompletionRate();
        }

        return DoctorStatsPageResponse.builder()
                .totalDoctors(ranked.size())
                .totalRevenue(Math.round(totalRevenue * 100) / 100.0)
                .avgCompletionRate(ranked.isEmpty() ? 0
                        : Math.round((completionSum / ranked.size()) * 10) / 10.0)
                .page(page)
                .build();
    }

    private DoctorStatResponse buildDoctorStat(Staff doctor, List<Appointment> appointments) {
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
    }

    public ServiceStatsPageResponse getServiceStats(DashboardPeriodFilterRequest request) {
        int month = resolveMonth(request.getMonth());
        int year = resolveYear(request.getYear());
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startDateTime = ym.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = ym.atEndOfMonth().atTime(23, 59, 59);

        ServiceFilterRequest serviceFilter = new ServiceFilterRequest();
        serviceFilter.setSearch(request.getSearch());

        Specification<com.clinic.entity.medical.Service> spec = ServiceSpecification.filterBy(serviceFilter);
        List<com.clinic.entity.medical.Service> services = serviceRepository.findAll(spec);

        List<ServiceOrder> monthOrders = serviceOrderRepository
                .findByCreatedAtBetween(startDateTime, endDateTime);
        Map<Integer, List<ServiceOrder>> ordersByService = monthOrders.stream()
                .filter(o -> o.getService() != null)
                .collect(Collectors.groupingBy(o -> o.getService().getServiceId()));

        List<ServiceStatResponse> ranked = services.stream()
                .map(service -> buildServiceStat(service,
                        ordersByService.getOrDefault(service.getServiceId(), List.of())))
                .sorted((a, b) -> Double.compare(b.getCompletionRate(), a.getCompletionRate()))
                .collect(Collectors.toList());

        PageResponse<ServiceStatResponse> page = paginateList(ranked, request.getPage(), request.getSize());

        long totalOrders = 0;
        double totalRevenue = 0;
        for (ServiceStatResponse stat : ranked) {
            totalOrders += stat.getTotalOrders();
            totalRevenue += stat.getRevenue();
        }

        return ServiceStatsPageResponse.builder()
                .totalServices(ranked.size())
                .totalOrders(totalOrders)
                .totalRevenue(Math.round(totalRevenue * 100) / 100.0)
                .page(page)
                .build();
    }

    private ServiceStatResponse buildServiceStat(
            com.clinic.entity.medical.Service service,
            List<ServiceOrder> orders) {

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
    }

    public PatientStatsResponse getPatientStats(DashboardPeriodFilterRequest request) {
        int month = resolveMonth(request.getMonth());
        int year = resolveYear(request.getYear());
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
                .filter(p -> appointmentRepository.countByPatientIdAndAppointmentDateBetween(
                        p.getPatientId(), startDate, endDate) >= 2)
                .count();

        PatientFilterRequest patientFilter = new PatientFilterRequest();
        patientFilter.setSearch(request.getSearch());
        Specification<Patient> spec = PatientSpecification.filterBy(patientFilter);
        List<Patient> filteredPatients = patientRepository.findAll(spec);

        List<PatientStatsResponse.TopPatient> ranked = filteredPatients.stream()
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
                .collect(Collectors.toList());

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        int from = page * size;
        int to = Math.min(from + size, ranked.size());
        List<PatientStatsResponse.TopPatient> pageContent =
                from >= ranked.size() ? List.of() : ranked.subList(from, to);
        int totalPages = size > 0 ? (int) Math.ceil((double) ranked.size() / size) : 0;

        return PatientStatsResponse.builder()
                .newPatients(newPatients)
                .returningPatients(returningPatients)
                .topPatients(PageResponse.<PatientStatsResponse.TopPatient>builder()
                        .content(pageContent)
                        .totalElements(ranked.size())
                        .page(page)
                        .size(size)
                        .totalPages(totalPages)
                        .build())
                .build();
    }

    public RevenueStatsResponse getRevenueStats(DashboardPeriodFilterRequest request) {
        int month = resolveMonth(request.getMonth());
        int year = resolveYear(request.getYear());
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
                .filter(a -> "EXAM".equals(a.getService().getServiceType().name()))
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

        List<RevenueStatsResponse.ServiceRevenue> byServiceAll = serviceRevenueMap.entrySet().stream()
                .map(e -> RevenueStatsResponse.ServiceRevenue.builder()
                        .serviceName(e.getKey())
                        .revenue(Math.round(e.getValue() * 100) / 100.0)
                        .percentage(totalRevenue > 0 ? Math.round((e.getValue() / totalRevenue) * 1000) / 10.0 : 0)
                        .build())
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .collect(Collectors.toList());

        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            String q = request.getSearch().toLowerCase();
            byServiceAll = byServiceAll.stream()
                    .filter(s -> s.getServiceName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        int from = page * size;
        int to = Math.min(from + size, byServiceAll.size());
        List<RevenueStatsResponse.ServiceRevenue> pageContent =
                from >= byServiceAll.size() ? List.of() : byServiceAll.subList(from, to);
        int totalPages = size > 0 ? (int) Math.ceil((double) byServiceAll.size() / size) : 0;

        return RevenueStatsResponse.builder()
                .totalRevenue(Math.round(totalRevenue * 100) / 100.0)
                .consultationRevenue(Math.round(consultationRevenue * 100) / 100.0)
                .serviceRevenue(Math.round(serviceRevenue * 100) / 100.0)
                .monthlyTrend(monthlyTrend)
                .byService(PageResponse.<RevenueStatsResponse.ServiceRevenue>builder()
                        .content(pageContent)
                        .totalElements(byServiceAll.size())
                        .page(page)
                        .size(size)
                        .totalPages(totalPages)
                        .build())
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
        AppointmentFilterRequest filter = new AppointmentFilterRequest();
        filter.setPage(0);
        filter.setSize(limit);
        filter.setSortBy("appointmentDate");
        filter.setSortDir("DESC");

        Specification<Appointment> spec = AppointmentSpecification.filterBy(filter);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "appointmentDate"));
        Page<Appointment> page = appointmentRepository.findAll(spec, pageable);

        return page.getContent().stream()
                .map(a -> RecentAppointmentResponse.builder()
                        .appointmentId(a.getAppointmentId())
                        .patientName(a.getPatient() != null ? a.getPatient().getFullName() : "Unknown")
                        .appointmentDate(a.getAppointmentDate().toString())
                        .status(a.getStatus().name())
                        .patientAvatarUrl(a.getPatient() != null ? a.getPatient().getAvatarUrl() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private int resolveMonth(Integer month) {
        return month != null ? month : LocalDate.now().getMonthValue();
    }

    private int resolveYear(Integer year) {
        return year != null ? year : LocalDate.now().getYear();
    }

    private <T> PageResponse<T> paginateList(List<T> list, Integer page, Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        int from = p * s;
        int total = list.size();
        int totalPages = s > 0 ? (int) Math.ceil((double) total / s) : 0;
        List<T> content = from >= total ? List.of() : list.subList(from, Math.min(from + s, total));
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(total)
                .page(p)
                .size(s)
                .totalPages(totalPages)
                .build();
    }

    // ====== GENERATE REPORT ======
    public byte[] generateReport(ReportFilterRequest filter) {
        // TODO: Mốt sẽ chuyển logic tạo báo cáo lên Frontend xử lý
        String content = "Báo cáo sẽ được tạo ở frontend...";
        return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}