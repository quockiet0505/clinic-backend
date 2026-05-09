package com.clinic.repository.prescription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.prescription.Medicine;

public interface MedicineRepository extends JpaRepository<Medicine, Integer> {
    List<Medicine> findByIsDeleted(Integer isDeleted);
}