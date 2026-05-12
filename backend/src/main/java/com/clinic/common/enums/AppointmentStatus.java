package com.clinic.common.enums;

public enum AppointmentStatus {
    PENDING,        // Waiting for examination (Online booking)
    CONFIRMED,      // Appointment confirmed by staff
    CHECKED_IN,     // Patient arrived at the reception (In queue)
    IN_PROGRESS,    // Currently in the examination room with the doctor
    WAITING_RESULT, // Preliminary exam completed, waiting for lab/test results
    SKIPPED,        // Patient called but not present (Skipped in queue)
    COMPLETED,      // Examination fully completed
    CANCELLED,      // Appointment cancelled (by patient, staff, or system)
    NO_SHOW         // Patient did not show up without cancelling (Auto-marked by end-of-day job)
}