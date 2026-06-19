package com.clinic.repository.prescription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.entity.prescription.Medicine;

public interface MedicineRepository extends JpaRepository<Medicine, Integer>, JpaSpecificationExecutor<Medicine> {
    List<Medicine> findByIsDeleted(Integer isDeleted);
}