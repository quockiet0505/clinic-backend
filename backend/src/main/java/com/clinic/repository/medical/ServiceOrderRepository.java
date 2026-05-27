package com.clinic.repository.medical;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.medical.ServiceOrder;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {
    List<ServiceOrder> findByMedicalRecord_RecordId(Integer recordId);

    @Query("SELECT s FROM ServiceOrder s WHERE s.medicalRecord.recordId = :recordId")
    List<ServiceOrder> findByMedicalRecordId(@Param("recordId") Integer recordId);
}