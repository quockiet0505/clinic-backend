package com.clinic.repository.medical;

import com.clinic.entity.medical.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer>, JpaSpecificationExecutor<Invoice> {
    List<Invoice> findByPatient_PatientIdOrderByCreatedAtDesc(Integer patientId);
    Optional<Invoice> findByMedicalRecord_RecordId(Integer recordId);
    List<Invoice> findByCreatedAtBetweenAndStatus(
            java.time.LocalDateTime start, java.time.LocalDateTime end, com.clinic.common.enums.InvoiceStatus status);
}
