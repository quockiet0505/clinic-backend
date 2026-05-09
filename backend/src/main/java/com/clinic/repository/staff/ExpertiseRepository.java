package com.clinic.repository.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.staff.Expertise;

public interface ExpertiseRepository extends JpaRepository<Expertise, Integer> {
}