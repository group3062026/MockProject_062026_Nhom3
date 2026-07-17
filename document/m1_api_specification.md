# 📋 M1 API Specification — 39 Endpoints

## Tổng quan

| Module | Số API | Color code |
|---|---|---|
| **Care Levels** | 4 | — |
| **Care Level Rates** | 5 | — |
| **Residents** | 9 | 🔴 Đỏ (ưu tiên cao) |
| **Resident Sensitive Info** | 4 | 🟡 Vàng |
| **Care Level History** | 5 | 🟦 Xanh dương |
| **Contacts** | 6 | 🔵 Xanh lam |
| **Resident Contacts** | 6 | 🔵 Xanh lam |

---

## Module 1: Care Levels (API 1–4)

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 1 | getCareLevels | GET | `/api/v1/care-levels` | All authenticated | `include_deleted` (bool, optional) |
| 2 | getCareLevelById | GET | `/api/v1/care-levels/{id}` | All authenticated | — |
| 3 | updateCareLevelName | PATCH | `/api/v1/care-levels/{id}` | System_Admin, DON | Only `level_name` editable; `level_code` immutable |
| 4 | deleteCareLevel | DELETE | `/api/v1/care-levels/{id}` | System_Admin | Soft delete; blocked if ACTIVE resident references this level |

---

## Module 2: Care Level Rates (API 5–9)

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 5 | getCareLevelRates | GET | `/api/v1/care-level-rates` | Facility_Mgr, Accountant, SysAdmin | Filter: `facility_id`, `care_level_id`, `active_only` |
| 6 | createCareLevelRate | POST | `/api/v1/care-level-rates` | Accountant, SysAdmin | Auto-closes previous rate's `effective_to`; `daily_rate > 0` |
| 7 | getCurrentCareLevelRates | GET | `/api/v1/care-level-rates/current` | Facility_Mgr, Accountant, Admission | `facility_id` (required) — pricing/quote screen |
| 8 | updateCareLevelRate | PUT | `/api/v1/care-level-rates/{id}` | Accountant, SysAdmin | Only editable if `effective_from` is in future |
| 9 | deleteCareLevelRate | DELETE | `/api/v1/care-level-rates/{id}` | SysAdmin | Hard-delete only if `effective_from` is in future |

---

## Module 3: Residents (API 10–18) 🔴

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 10 | getResidents | GET | `/api/v1/residents` | Nurse, CNA, DON, Admission, Facility_Mgr, SysAdmin | Filter: `status`, `bed_id`, `search`, `page`, `page_size`; `currentCareLevel` is derived |
| 11 | getResidentById | GET | `/api/v1/residents/{id}` | Nurse, CNA, DON, Admission, Facility_Mgr, SysAdmin | Full demographics, excluding sensitive info |
| 12 | createResident | POST | `/api/v1/residents` | Admission, SysAdmin | Default: `status=PENDING`, `is_chart_locked=false`; no `bed_id` at creation |
| 13 | updateResident | PATCH | `/api/v1/residents/{id}` | Nurse, CNA (if not locked), Admission, DON, SysAdmin | Basic demographic updates |
| 14 | updateResidentStatus | PATCH | `/api/v1/residents/{id}/status` | DON, Admission, SysAdmin | **DISCHARGED/DECEASED** → auto-clear `bed_id`; **DECEASED** → auto-lock chart |
| 15 | assignResidentBed | PATCH | `/api/v1/residents/{id}/assign-bed` | Nurse, Admission, SysAdmin | Check bed availability |
| 16 | lockResidentChart | PATCH | `/api/v1/residents/{id}/lock-chart` | DON, SysAdmin | `reason` required for audit |
| 17 | unlockResidentChart | PATCH | `/api/v1/residents/{id}/unlock-chart` | DON, SysAdmin | `reason` required for audit |
| 18 | deleteResident | DELETE | `/api/v1/residents/{id}` | ❌ **Not permitted** | PHI retention — fully blocked |

---

## Module 4: Resident Sensitive Info (API 19–22) 🟡

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 19 | getResidentSensitiveInfo | GET | `/api/v1/residents/{resident_id}/sensitive-info` | SysAdmin, Accountant, DON | Data masked; `reveal=true` requires `VIEW_FULL_PHI` + audit log |
| 20 | createResidentSensitiveInfo | POST | `/api/v1/residents/{resident_id}/sensitive-info` | SysAdmin, Admission | One per resident (UNIQUE); AES-256 encryption |
| 21 | updateResidentSensitiveInfo | PUT | `/api/v1/residents/{resident_id}/sensitive-info` | SysAdmin, Accountant | Audit logs fields changed, never plaintext |
| 22 | deleteResidentSensitiveInfo | DELETE | `/api/v1/residents/{resident_id}/sensitive-info` | SysAdmin | Right-to-erasure compliance |

---

## Module 5: Care Level History (API 23–27) 🟦

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 23 | getResidentCareLevelHistory | GET | `/api/v1/residents/{resident_id}/care-level-history` | Nurse, CNA, DON, Doctor, SysAdmin | Sorted by `start_date` DESC |
| 24 | transitionResidentCareLevel | POST | `/api/v1/residents/{resident_id}/care-level-history` | Nurse, DON, Doctor, SysAdmin | Auto-closes current record; `start_date` must be after current |
| 25 | getCareLevelActiveSummary | GET | `/api/v1/care-level-history/active-summary` | Facility_Mgr, DON, SysAdmin | `facility_id` required — dashboard use |
| 26 | updateCareLevelHistory | PATCH | `/api/v1/care-level-history/{id}` | DON, SysAdmin | Only non-current records; `reason` required |
| 27 | deleteCareLevelHistory | DELETE | `/api/v1/care-level-history/{id}` | ❌ **Not permitted** | Mandatory retention data |

---

## Module 6: Contacts (API 28–33) 🔵

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 28 | getContacts | GET | `/api/v1/contacts` | Admission, Facility_Mgr, Accountant, SysAdmin | Filter: `search`, `include_deleted`, `page`, `page_size` |
| 29 | getContactById | GET | `/api/v1/contacts/{id}` | Admission, Facility_Mgr, SysAdmin | — |
| 30 | createContact | POST | `/api/v1/contacts` | Admission, SysAdmin | `phone_primary` required (format: `1-XXX-XXX-XXXX`) |
| 31 | updateContact | PATCH | `/api/v1/contacts/{id}` | Admission, SysAdmin | Auto-updates `updated_at` |
| 32 | deleteContact | DELETE | `/api/v1/contacts/{id}` | SysAdmin | Soft delete; blocked if guarantor for ACTIVE resident |
| 33 | getResidentsByContact | GET | `/api/v1/contacts/{id}/residents` | Admission, Facility_Mgr, SysAdmin | One contact may be family to multiple residents |

---

## Module 7: Resident Contacts (API 34–39) 🔵

| # | Function | Method | URL | Auth | Notes |
|---|---|---|---|---|---|
| 34 | getResidentContacts | GET | `/api/v1/residents/{resident_id}/contacts` | Nurse, CNA, DON, Admission, Accountant, SysAdmin | With role/financial split details |
| 35 | createResidentContact | POST | `/api/v1/residents/{resident_id}/contacts` | Admission, SysAdmin | Only one `is_primary=true` per resident (auto-unset previous) |
| 36 | updateResidentContact | PATCH | `/api/v1/residents/{resident_id}/contacts/{id}` | Admission, SysAdmin | Re-applies total % and single-primary validation |
| 37 | deleteResidentContact | DELETE | `/api/v1/residents/{resident_id}/contacts/{id}` | Admission, SysAdmin | Hard delete (no PHI involved) |
| 38 | getResidentGuarantorContact | GET | `/api/v1/residents/{resident_id}/contacts/guarantor` | Accountant, Admission, SysAdmin | For billing/invoice screen |
| 39 | getResidentPrimaryContact | GET | `/api/v1/residents/{resident_id}/contacts/primary` | Admission, DON, SysAdmin | Official legal point of contact |

---

## Quy ước Response chung

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": { ... }
}
```

> [!IMPORTANT]
> Response dùng `statusCode` thay vì `code` — cần cập nhật `ApiResponse` class cho khớp.

> [!WARNING]
> 2 endpoint bị **BLOCKED** hoàn toàn: `deleteResident` (#18) và `deleteCareLevelHistory` (#27) — cần trả 405 Method Not Allowed.
