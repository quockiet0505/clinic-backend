package com.clinic.service.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.service.crm.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentQueueService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;

    @Transactional
    public AppointmentResponse callPatient(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN &&
            appointment.getStatus() != AppointmentStatus.SKIPPED) {
            throw new RuntimeException("Chỉ có thể gọi khám bệnh nhân đang ở trạng thái CHECKED_IN hoặc SKIPPED.");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        syncMedicalRecordStatus(appointmentId, MedicalRecordStatus.IN_PROGRESS);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Đến lượt khám của bạn. Vui lòng vào phòng khám gặp Bác sĩ " +
                    (appointment.getMainDoctor() != null ? appointment.getMainDoctor().getFullName() : ""),
                    "SYSTEM");
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse skipPatient(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN &&
            appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Chỉ có thể bỏ qua bệnh nhân đang ở trạng thái CHECKED_IN hoặc IN_PROGRESS.");
        }

        appointment.setStatus(AppointmentStatus.SKIPPED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Bạn đã bị qua lượt do không có mặt khi bác sĩ gọi tên. Vui lòng liên hệ quầy lễ tân để được xếp lại vào cuối hàng đợi.",
                    "SYSTEM");
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse returnToQueue(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.SKIPPED) {
            throw new RuntimeException("Chỉ có thể xếp lại hàng đợi cho bệnh nhân bị qua lượt (SKIPPED).");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Bạn đã được xếp lại vào hàng đợi. Vui lòng chú ý theo dõi thông báo.",
                    "SYSTEM");
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse sendToLab(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Bệnh nhân phải đang khám (IN_PROGRESS) mới có thể chuyển sang chờ kết quả.");
        }

        MedicalRecord record = medicalRecordRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ khám cho lịch hẹn này."));

        List<ServiceOrder> orders = serviceOrderRepository.findByMedicalRecordId(record.getRecordId());
        boolean hasOrdered = orders.stream()
                .anyMatch(o -> o.getStatus() == ServiceOrderStatus.ORDERED);
        if (!hasOrdered) {
            throw new RuntimeException("Phải có ít nhất một chỉ định cận lâm sàng (ORDERED) trước khi chuyển chờ kết quả.");
        }

        appointment.setStatus(AppointmentStatus.WAITING_RESULT);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        record.setStatus(MedicalRecordStatus.WAITING_RESULT);
        medicalRecordRepository.save(record);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Bác sĩ đã chỉ định cận lâm sàng. Vui lòng di chuyển đến khu vực xét nghiệm/chụp chiếu.",
                    "SYSTEM");
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse returnFromLab(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.WAITING_RESULT) {
            throw new RuntimeException("Bệnh nhân phải đang ở trạng thái chờ kết quả (WAITING_RESULT).");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setQueueNumber(0);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (appointment.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Đã có kết quả xét nghiệm. Bạn đã được xếp ưu tiên vào gặp bác sĩ để đọc kết quả.",
                    "SYSTEM");
        }

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Tự động đưa BN về hàng đợi ưu tiên khi Lab nhập xong tất cả kết quả.
     */
    @Transactional
    public void tryAutoReturnFromLab(Integer recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId).orElse(null);
        if (record == null || record.getAppointment() == null) {
            return;
        }

        Integer appointmentId = record.getAppointment().getAppointmentId();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null || appointment.getStatus() != AppointmentStatus.WAITING_RESULT) {
            return;
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByMedicalRecordId(recordId);
        List<ServiceOrder> activeOrders = orders.stream()
                .filter(o -> o.getStatus() != ServiceOrderStatus.CANCELLED
                        && o.getStatus() != ServiceOrderStatus.REJECTED)
                .toList();

        if (activeOrders.isEmpty()) {
            return;
        }

        boolean allDone = activeOrders.stream()
                .allMatch(o -> o.getStatus() == ServiceOrderStatus.DONE);
        if (!allDone) {
            return;
        }

        returnFromLab(appointmentId);
    }

    private void syncMedicalRecordStatus(Integer appointmentId, MedicalRecordStatus status) {
        medicalRecordRepository.findByAppointment_AppointmentId(appointmentId).ifPresent(record -> {
            record.setStatus(status);
            medicalRecordRepository.save(record);
        });
    }
}
