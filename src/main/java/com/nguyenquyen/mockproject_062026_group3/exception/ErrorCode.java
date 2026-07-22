package com.nguyenquyen.mockproject_062026_group3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Standard system codes (1000 - 1999)
    SUCCESS(200, "Success", HttpStatus.OK),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1002, "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS(1003, "Resource already exists", HttpStatus.CONFLICT),
    UNAUTHORIZED(1004, "You need to log in to access this resource", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1005, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INVALID_PARAMETER(1006, "Invalid request parameter", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(1007, "Validation error", HttpStatus.BAD_REQUEST),
    BUSINESS_EXCEPTION(1008, "Business rule violation", HttpStatus.BAD_REQUEST),

    // IAM & User Domain (2000 - 2999)
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "User already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(2003, "Role not found", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(2004, "Permission not found", HttpStatus.NOT_FOUND),

    // Resident & Care Level Domain (3000 - 3999)
    RESIDENT_NOT_FOUND(3001, "Resident not found", HttpStatus.NOT_FOUND),
    RESIDENT_ALREADY_EXISTS(3002, "Resident already exists", HttpStatus.CONFLICT),
    CARE_LEVEL_NOT_FOUND(3003, "Care level not found", HttpStatus.NOT_FOUND),
    ADMISSION_NOT_FOUND(3004, "Admission record not found", HttpStatus.NOT_FOUND),
    CONTACT_NOT_FOUND(3005, "Contact not found", HttpStatus.NOT_FOUND),
    RESIDENT_CONTACT_NOT_FOUND(3006, "Resident-contact link not found", HttpStatus.NOT_FOUND),
    CARE_LEVEL_HISTORY_NOT_FOUND(3007, "Care level history record not found", HttpStatus.NOT_FOUND),

    // Facility & Infrastructure Domain (4000 - 4999)
    FACILITY_NOT_FOUND(4001, "Facility not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND(4002, "Room not found", HttpStatus.NOT_FOUND),
    BED_NOT_FOUND(4003, "Bed not found", HttpStatus.NOT_FOUND),
    BED_NOT_AVAILABLE(4004, "Bed is not available", HttpStatus.BAD_REQUEST),

    // Clinical & Medication Domain (5000 - 5999)
    CLINICAL_RECORD_NOT_FOUND(5001, "Clinical record not found", HttpStatus.NOT_FOUND),
    CARE_PLAN_NOT_FOUND(5002, "Care plan not found", HttpStatus.NOT_FOUND),
    MEDICATION_ORDER_NOT_FOUND(5003, "Medication order not found", HttpStatus.NOT_FOUND),
    VITAL_SIGN_NOT_FOUND(5004, "Vital sign record not found", HttpStatus.NOT_FOUND),

    // M3 - eMAR Domain (5200 - 5299)
    MAR_RESIDENT_NOT_FOUND(5201, "Resident not found", HttpStatus.NOT_FOUND),
    MAR_NO_ACTIVE_ORDERS(5202, "No active medication orders found for this resident", HttpStatus.NOT_FOUND),
    MAR_NO_SCHEDULES(5203, "No medication schedules found", HttpStatus.NOT_FOUND),
    MAR_INVALID_SHIFT(5204, "Invalid shift parameter. Must be DAY, EVENING, or NIGHT", HttpStatus.BAD_REQUEST),
    MAR_INVALID_DATE_RANGE(5205, "Invalid date range. End date must be after start date", HttpStatus.BAD_REQUEST),
    MAR_FACILITY_REQUIRED(5206, "Facility ID is required", HttpStatus.BAD_REQUEST),
    MAR_PDF_GENERATION_FAILED(5207, "Failed to generate PDF report", HttpStatus.INTERNAL_SERVER_ERROR),

    // Billing & Insurance Domain (6000 - 6999)
    INVOICE_NOT_FOUND(6001, "Invoice not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(6002, "Payment not found", HttpStatus.NOT_FOUND),
    INSURANCE_POLICY_NOT_FOUND(6003, "Insurance policy not found", HttpStatus.NOT_FOUND),

    // Shift & Scheduling Domain (7000 - 7999)
    SHIFT_NOT_FOUND(7001, "Shift not found", HttpStatus.NOT_FOUND),
    SHIFT_ASSIGNMENT_NOT_FOUND(7002, "Shift assignment not found", HttpStatus.NOT_FOUND),
    HOLIDAY_NOT_FOUND(7003, "Public holiday not found", HttpStatus.NOT_FOUND),

    // M3 - Medication Order Domain (5400 - 5499)
    MAR_ORDER_NOT_FOUND(5401, "Medication order not found", HttpStatus.NOT_FOUND),
    MAR_ORDER_ALREADY_DISCONTINUED(5402, "Medication order is already discontinued", HttpStatus.BAD_REQUEST),
    MAR_ORDER_HAS_PENDING_DOSES(5403, "Cannot discontinue order with pending doses. Please administer or reschedule first", HttpStatus.BAD_REQUEST),
    MAR_ORDER_DUPLICATE(5404, "Duplicate medication order detected", HttpStatus.CONFLICT),
    MAR_INVALID_SCHEDULE_TIME(5405, "Invalid schedule time format. Use HH:mm:ss", HttpStatus.BAD_REQUEST),
    MAR_PRESCRIBER_NOT_FOUND(5406, "Prescriber not found", HttpStatus.NOT_FOUND),
    MAR_ORDER_UPDATE_NOT_ALLOWED(5407, "Cannot update discontinued order", HttpStatus.BAD_REQUEST),
    MAR_RESIDENT_NO_ACTIVE_ORDERS(5408, "No active medication orders found for this resident", HttpStatus.NOT_FOUND),
    // M3 - Med-Pass Domain (5300 - 5399)
    MAR_SESSION_EXPIRED(5301, "Med-Pass session has expired. Please start a new session", HttpStatus.BAD_REQUEST),
    MAR_SESSION_NOT_FOUND(5302, "Med-Pass session not found", HttpStatus.NOT_FOUND),
    MAR_SCHEDULE_NOT_FOUND(5304, "Medication schedule not found", HttpStatus.NOT_FOUND),
    MAR_BARCODE_MISMATCH(5305, "Barcode verification failed", HttpStatus.BAD_REQUEST),
    MAR_TIME_WINDOW_EXCEPTION(5306, "Outside medication time window", HttpStatus.BAD_REQUEST),
    MAR_OVERRIDE_REASON_REQUIRED(5307, "Override reason is required", HttpStatus.BAD_REQUEST),
    MAR_OTHER_REASON_REQUIRED(5308, "Other reason text is required when override reason is OTHER", HttpStatus.BAD_REQUEST),
    MAR_CLINICAL_JUSTIFICATION_REQUIRED(5309, "Clinical justification must be confirmed", HttpStatus.BAD_REQUEST),
    MAR_WITNESS_REQUIRED(5310, "Witness is required for controlled substances", HttpStatus.BAD_REQUEST),
    MAR_ALREADY_ADMINISTERED(5311, "This medication has already been administered", HttpStatus.BAD_REQUEST),
    MAR_OVERRIDE_REQUIRED(5312, "Override is required for this verification failure", HttpStatus.BAD_REQUEST),
    MAR_SCAN_FAILED(5313, "Barcode scan failed. Please try again", HttpStatus.BAD_REQUEST),
    MAR_INVALID_SESSION(5314, "Invalid session. Please start a new Med-Pass session", HttpStatus.BAD_REQUEST),// M3 - Batch & Regenerate Domain (5500 - 5599)
    MAR_BATCH_EMPTY(5501, "Batch request cannot be empty. Please select at least one medication", HttpStatus.BAD_REQUEST),
    MAR_BATCH_SIZE_MISMATCH(5502, "Order IDs and Schedule IDs count must match", HttpStatus.BAD_REQUEST),
    MAR_BATCH_ORDER_NOT_FOUND(5503, "One or more medication orders not found", HttpStatus.NOT_FOUND),
    MAR_BATCH_SCHEDULE_NOT_FOUND(5504, "One or more medication schedules not found", HttpStatus.NOT_FOUND),
    MAR_BATCH_ALREADY_ADMINISTERED(5505, "One or more medications have already been administered", HttpStatus.BAD_REQUEST),
    MAR_BATCH_OVERRIDE_REQUIRED(5506, "One or more medications require override. Please handle individually", HttpStatus.BAD_REQUEST),
    MAR_BATCH_WITNESS_REQUIRED(5507, "Witness is required for controlled substances in batch", HttpStatus.BAD_REQUEST),
    MAR_REGENERATE_ORDER_NOT_FOUND(5508, "Medication order not found for regeneration", HttpStatus.NOT_FOUND),
    MAR_REGENERATE_ORDER_NOT_ACTIVE(5509, "Cannot regenerate schedules for discontinued or on-hold orders", HttpStatus.BAD_REQUEST),
    MAR_REGENERATE_NO_TIMES(5510, "At least one scheduled time is required", HttpStatus.BAD_REQUEST),
    MAR_REGENERATE_INVALID_TIME(5511, "Invalid scheduled time format. Use HH:mm:ss", HttpStatus.BAD_REQUEST),
    // M3 - Audit Domain (5600 - 5699)
    MAR_AUDIT_NO_DATA(5601, "No audit data found for the specified criteria", HttpStatus.NOT_FOUND),
    MAR_AUDIT_INVALID_DATE_RANGE(5602, "Invalid date range for audit query", HttpStatus.BAD_REQUEST),
    MAR_AUDIT_EXPORT_FAILED(5603, "Failed to export audit report", HttpStatus.INTERNAL_SERVER_ERROR),
    MAR_PHI_ACCESS_NOT_FOUND(5604, "No PHI access logs found", HttpStatus.NOT_FOUND),
    MAR_PHI_ACCESS_REQUIRED(5605, "Resident ID is required for PHI access log", HttpStatus.BAD_REQUEST),
    MAR_AUDIT_ACTION_INVALID(5606, "Invalid audit action. Must be INSERT, UPDATE, or DELETE", HttpStatus.BAD_REQUEST),
    MAR_AUDIT_ACCESS_TYPE_INVALID(5607, "Invalid access type. Must be VIEW, PRINT, EXPORT, or DOWNLOAD", HttpStatus.BAD_REQUEST),

    // M7 - Incident Management Domain (8000 - 8999)
    INCIDENT_NOT_FOUND(8001, "Incident not found",HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}