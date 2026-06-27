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

    /** BN đặt lịch trực tiếp (SERVICE mode): chỉ xét nghiệm và X-Quang. */
    public boolean isPatientBookable() {
        return this != EXAM && this != OTHER;
    }

    /** EXAM tạm ẩn toàn hệ thống (catalog, booking). */
    public boolean isHiddenEverywhere() {
        return this == EXAM;
    }
}