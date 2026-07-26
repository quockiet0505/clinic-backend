package com.clinic.entity.prescription;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Integer medicineId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "active_element")
    private String activeElement;

    @Column(name = "packing_standard", length = 100)
    private String packingStandard;

    @Column(name = "base_unit", length = 50)
    private String baseUnit;

    @Column(name = "usage_note")
    private String usageNote;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = 0;
        }
    }

    public Integer getIsDeleted() {
        return this.isDeleted == null ? 0 : this.isDeleted;
    }
}
