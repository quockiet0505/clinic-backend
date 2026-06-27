package com.clinic.common.enums;

public enum ServiceType {
    EXAM,       // Khám tổng quát (Tạm ẩn — không đặt lịch / không hiển thị)
    LAB_TEST,   // Xét nghiệm máu, nước tiểu...
    X_RAY,      // Chụp X-Quang
    ULTRASOUND, // Siêu âm
    CT_SCAN,    // Chụp CT
    MRI,        // Chụp MRI
    ENDOSCOPY,  // Nội soi
    OTHER;      // Chỉ định trong khám — không đặt lịch trực tiếp

    /** BN được đặt lịch trực tiếp (SERVICE mode): xét nghiệm + chẩn đoán hình ảnh. */
    public boolean isPatientBookable() {
        return this == LAB_TEST || this == X_RAY || this == ULTRASOUND
                || this == CT_SCAN || this == MRI || this == ENDOSCOPY;
    }

    /** EXAM tạm ẩn toàn hệ thống (catalog, booking). */
    public boolean isHiddenEverywhere() {
        return this == EXAM;
    }
}