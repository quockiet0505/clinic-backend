package com.clinic.entity.crm;

import com.clinic.entity.medical.MedicalRecord; 
import com.clinic.entity.staff.Staff;
import jakarta.persistence.*; 
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedbackId;

    @ManyToOne
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;

    private Integer rating;
    private String comment;
    
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    private LocalDateTime createdAt;

    private String reply;
    private LocalDateTime repliedAt;

    @ManyToOne
    @JoinColumn(name = "replied_by")
    private Staff repliedBy;
}