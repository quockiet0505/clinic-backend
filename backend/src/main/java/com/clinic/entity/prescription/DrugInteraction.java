package com.clinic.entity.prescription;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drug_interaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Integer interactionId;

    @Column(name = "active_element_1", nullable = false)
    private String activeElement1;

    @Column(name = "active_element_2", nullable = false)
    private String activeElement2;

    @Column(columnDefinition = "TEXT")
    private String mechanism;

    @Column(columnDefinition = "TEXT")
    private String consequence;

    @Column(columnDefinition = "TEXT")
    private String management;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
