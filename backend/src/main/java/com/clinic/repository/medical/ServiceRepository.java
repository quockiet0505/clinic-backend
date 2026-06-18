package com.clinic.repository.medical;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.clinic.entity.medical.Service;

public interface ServiceRepository extends JpaRepository<Service, Integer>, JpaSpecificationExecutor<Service> {
    List<Service> findByIsDeleted(Integer isDeleted);
    List<Service> findByIsDeletedAndIsFeaturedOrderByFeaturedPriorityAsc(Integer isDeleted, Boolean isFeatured);
}