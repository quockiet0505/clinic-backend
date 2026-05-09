package com.clinic.repository.staff;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.staff.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    List<Staff> findByIsDeleted(Integer isDeleted);
}