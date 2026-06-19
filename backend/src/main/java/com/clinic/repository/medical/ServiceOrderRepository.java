package com.clinic.repository.medical;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.medical.ServiceOrder;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer>, JpaSpecificationExecutor<ServiceOrder> {
    List<ServiceOrder> findByMedicalRecord_RecordId(Integer recordId);

    @Query("SELECT s FROM ServiceOrder s WHERE s.medicalRecord.recordId = :recordId")
    List<ServiceOrder> findByMedicalRecordId(@Param("recordId") Integer recordId);

    @Query("SELECT so FROM ServiceOrder so WHERE so.service.serviceId = :serviceId AND so.createdAt BETWEEN :start AND :end")
    List<ServiceOrder> findByServiceIdAndCreatedAtBetween(
            @Param("serviceId") Integer serviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<ServiceOrder> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}