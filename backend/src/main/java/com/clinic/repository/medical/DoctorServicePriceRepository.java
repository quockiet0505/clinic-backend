package com.clinic.repository.medical;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinic.entity.medical.DoctorServicePrice;

public interface DoctorServicePriceRepository extends JpaRepository<DoctorServicePrice, Integer>, JpaSpecificationExecutor<DoctorServicePrice> {
    Optional<DoctorServicePrice> findByStaff_StaffId(Integer staffId);

    @Query("""
        SELECT CASE
            WHEN dsp.discountAmount IS NOT NULL AND dsp.discountAmount > 0 THEN dsp.discountAmount
            ELSE dsp.originalPrice
        END
        FROM DoctorServicePrice dsp
        WHERE dsp.staff.staffId = :staffId
        """)
    java.math.BigDecimal getBaseConsultationFeeByDoctorId(@Param("staffId") Integer staffId);

    @Query("SELECT dsp FROM DoctorServicePrice dsp WHERE dsp.staff.staffId = :staffId")
    DoctorServicePrice findActivePriceByDoctorId(@Param("staffId") Integer staffId);
}
