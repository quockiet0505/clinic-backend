package com.clinic.repository.medical;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.entity.medical.DoctorServicePrice;

public interface DoctorServicePriceRepository extends JpaRepository<DoctorServicePrice, Integer>, JpaSpecificationExecutor<DoctorServicePrice> {
    Optional<DoctorServicePrice> findByStaff_StaffIdAndService_ServiceId(Integer staffId, Integer serviceId);
}