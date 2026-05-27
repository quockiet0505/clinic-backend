package com.clinic.entity.medical;

import java.math.BigDecimal;

import com.clinic.entity.staff.Staff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medical_record_vital")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordVital {
    @Id
    @Column(name = "record_id")
    private Integer recordId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    private Integer pulse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private Staff recordedBy; // Usually the Nurse/Staff taking vitals

    
    @Column(name = "status")
    private String status; // hoặc enum MedicalRecordStatus
}