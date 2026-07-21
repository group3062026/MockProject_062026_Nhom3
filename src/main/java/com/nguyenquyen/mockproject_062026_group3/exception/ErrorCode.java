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

    // Billing & Insurance Domain (6000 - 6999)
    INVOICE_NOT_FOUND(6001, "Invoice not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(6002, "Payment not found", HttpStatus.NOT_FOUND),
    INSURANCE_POLICY_NOT_FOUND(6003, "Insurance policy not found", HttpStatus.NOT_FOUND),

    // Shift & Scheduling Domain (7000 - 7999)
    SHIFT_NOT_FOUND(7001, "Shift not found", HttpStatus.NOT_FOUND),
    SHIFT_ASSIGNMENT_NOT_FOUND(7002, "Shift assignment not found", HttpStatus.NOT_FOUND),
    HOLIDAY_NOT_FOUND(7003, "Public holiday not found", HttpStatus.NOT_FOUND),

    // Inventory & Incident Domain (8000 - 8999)
    INCIDENT_NOT_FOUND(8001, "Incident not found", HttpStatus.NOT_FOUND),
    INVENTORY_ITEM_NOT_FOUND(8002, "Inventory item not found", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(8003, "Item is out of stock", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
