package com.clinic.repository.medical;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.entity.medical.DoctorServicePrice;

public interface DoctorServicePriceRepository extends JpaRepository<DoctorServicePrice, Integer>, JpaSpecificationExecutor<DoctorServicePrice> {
    Optional<DoctorServicePrice> findByStaff_StaffIdAndService_ServiceId(Integer staffId, Integer serviceId);

    @org.springframework.data.jpa.repository.Query("SELECT MIN(dsp.discountPrice) FROM DoctorServicePrice dsp WHERE dsp.staff.staffId = :staffId AND dsp.service.serviceType = 'EXAM'")
    java.math.BigDecimal getBaseConsultationFeeByDoctorId(@org.springframework.data.repository.query.Param("staffId") Integer staffId);
}