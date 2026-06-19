package com.clinic.repository.medical;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.medical.ServiceResult;

public interface ServiceResultRepository extends JpaRepository<ServiceResult, Integer>, JpaSpecificationExecutor<ServiceResult> {
    
    @Query("SELECT r FROM ServiceResult r WHERE r.serviceOrder.orderId = :orderId")
    Optional<ServiceResult> findByOrderId(@Param("orderId") Integer orderId);

    java.util.List<ServiceResult> findByServiceOrder_MedicalRecord_Patient_PatientId(Integer patientId);
}