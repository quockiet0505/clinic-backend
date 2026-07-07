package com.clinic.repository.prescription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinic.entity.prescription.DrugInteraction;

import java.util.List;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Integer> {

    @Query("SELECT d FROM DrugInteraction d WHERE " +
           "(LOWER(d.activeElement1) = LOWER(:elem1) AND LOWER(d.activeElement2) = LOWER(:elem2)) " +
           "OR (LOWER(d.activeElement1) = LOWER(:elem2) AND LOWER(d.activeElement2) = LOWER(:elem1))")
    List<DrugInteraction> findInteractions(@Param("elem1") String elem1, @Param("elem2") String elem2);
}
