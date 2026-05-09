package com.clinic.service.medical;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.MedicalRecordResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.MedicalRecordMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordMapper recordMapper;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Staff doctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        MedicalRecord record = recordMapper.toEntity(request);
        record.setPatient(patient);
        record.setMainDoctor(doctor);
        record.setStatus(MedicalRecordStatus.IN_PROGRESS);

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            record.setAppointment(appointment);
        }

        return recordMapper.toResponse(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getByPatientId(Integer patientId) {
        return recordRepository.findByPatient_PatientId(patientId).stream()
                .map(recordMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordResponse update(Integer id, MedicalRecordRequest request) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        
        // Cannot update if cancelled or done (Strict Business Logic)
        if (record.getStatus() == MedicalRecordStatus.DONE || record.getStatus() == MedicalRecordStatus.CANCELLED) {
            throw new RuntimeException("Cannot update a completed or cancelled medical record");
        }

        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setNote(request.getNote());

        return recordMapper.toResponse(recordRepository.save(record));
    }
    
    @Transactional
    public void updateStatus(Integer id, MedicalRecordStatus status) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        record.setStatus(status);
        recordRepository.save(record);
    }
}