package com.clinic.repository.staff;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.common.enums.StaffType;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.Expertise; 
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    List<Staff> findByIsDeleted(Integer isDeleted);
    List<Staff> findByStaffTypeAndIsDeleted(StaffType staffType, Integer isDeleted);
    List<Staff> findByStaffTypeAndIsDeletedAndIsFeaturedOrderByFeaturedPriorityAsc(StaffType staffType, Integer isDeleted, Boolean isFeatured);
    long countByExpertiseAndStaffType(Expertise expertise, StaffType staffType);

    Optional<Staff> findByAccount_AccountId(Integer accountId);
}