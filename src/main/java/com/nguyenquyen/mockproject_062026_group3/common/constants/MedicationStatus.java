package com.nguyenquyen.mockproject_062026_group3.common.constants;

public final class MedicationStatus {
    private MedicationStatus() {}

    // Task status (UI)
    public static final String SCHEDULED = "SCHEDULED";
    public static final String DUE_SOON = "DUE_SOON";
    public static final String OVERDUE = "OVERDUE";
    public static final String COMPLETED = "COMPLETED";

    // Log status (DB)
    public static final String ADMINISTERED = "ADMINISTERED";
    public static final String REFUSED = "REFUSED";
    public static final String HELD = "HELD";
    public static final String NOT_AVAILABLE = "NOT_AVAILABLE";

    // Order status (DB)
    public static final String ACTIVE = "ACTIVE";
    public static final String DISCONTINUED = "DISCONTINUED";
    public static final String ON_HOLD = "ON_HOLD";

    // Shift
    public static final String DAY = "DAY";
    public static final String EVENING = "EVENING";
    public static final String NIGHT = "NIGHT";

    // Batch status
    public static final String FAILED = "FAILED";

    // Override reasons
    public static final String BARCODE_UNREADABLE = "BARCODE_UNREADABLE";
    public static final String EMERGENCY_ADMINISTRATION = "EMERGENCY_ADMINISTRATION";
    public static final String TIME_WINDOW_EXCEPTION = "TIME_WINDOW_EXCEPTION";
    public static final String PATIENT_UNAVAILABLE = "PATIENT_UNAVAILABLE";
    public static final String UNCONFIRMED_ALLERGY = "UNCONFIRMED_ALLERGY";
    public static final String OTHER = "OTHER";
    public static final String BATCH_ADMINISTRATION_TIME_WINDOW = "BATCH_ADMINISTRATION_TIME_WINDOW";
}