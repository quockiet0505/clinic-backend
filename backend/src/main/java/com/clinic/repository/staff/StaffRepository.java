package com.clinic.repository.staff;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.common.enums.StaffType;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.Expertise;

public interface StaffRepository extends JpaRepository<Staff, Integer>, JpaSpecificationExecutor<Staff> {
    List<Staff> findByIsDeleted(Integer isDeleted);
    List<Staff> findByStaffTypeAndIsDeleted(StaffType staffType, Integer isDeleted);
    List<Staff> findByStaffTypeAndIsDeletedAndIsFeaturedOrderByFeaturedPriorityAsc(StaffType staffType, Integer isDeleted, Boolean isFeatured);
    long countByExpertiseAndStaffType(Expertise expertise, StaffType staffType);
    Optional<Staff> findByAccount_AccountId(Integer accountId);
}