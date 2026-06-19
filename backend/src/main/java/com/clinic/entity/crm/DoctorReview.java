package com.clinic.entity.crm;

import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_review")
@Data
public class DoctorReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Staff doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private String reply;
    private LocalDateTime repliedAt;

    @ManyToOne
    @JoinColumn(name = "replied_by")
    private Staff repliedBy;
}