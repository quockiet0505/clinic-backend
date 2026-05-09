package com.clinic.repository.medicine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.medicine.Medicine;

public interface MedicineRepository extends JpaRepository<Medicine, Integer> {
    List<Medicine> findByIsDeleted(Integer isDeleted);
}