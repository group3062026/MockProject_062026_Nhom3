# M3 - Revised API Specification

## 1. Căn cứ chỉnh sửa

Bản API M3 này được chỉnh lại từ 4 nguồn:

- `document/excel_M3.csv`: API M3 gốc của project, gồm medication orders, schedules, logs, verify.
- `document/MockProject_API_Document.xlsx`: bản API tổng có sheet M3.
- `document/chi tiết database/sql_final_v2.sql`: schema final cho `medication_orders`, `medication_schedules`, `medication_logs`.
- Wireframe M3:
  - M3-US-01 Medication Admin Dashboard
  - M3-US-02 Med-pass Barcode Scan
  - M3-US-03 Verification Override
  - M3-US-04 MAR
  - M3-US-06 Medication Order List

## 2. Quy ước chung

### 2.1 Base URL

```http
http://localhost:8080/api/v1
```

### 2.2 Authentication

Tất cả API cần header:

```http
Authorization: Bearer {access_token}
Content-Type: application/json
```

Nếu hệ thống chạy nhiều facility, backend nên lấy facility từ token/user context. Nếu project muốn truyền facility rõ ràng thì dùng thêm:

```http
X-Facility-ID: 1
```

### 2.3 ID type

Theo `sql_final_v2.sql`, các bảng M3 đang dùng `BIGINT`, vì vậy API này dùng `Long`/`number` cho:

- `residentId`
- `orderId`
- `scheduleId`
- `logId`
- `userId`

Không dùng `uuid` trừ khi team quyết định đổi database sang `UNIQUEIDENTIFIER`.

### 2.4 Status chuẩn

Medication order status lấy từ DB:

```text
ACTIVE | DISCONTINUED | ON_HOLD
```

Medication log status lấy từ DB:

```text
ADMINISTERED | REFUSED | HELD | NOT_AVAILABLE
```

UI task status là trạng thái tính toán để hiển thị dashboard/MAR:

```text
SCHEDULED | DUE_SOON | OVERDUE | COMPLETED | REFUSED | HELD | NOT_AVAILABLE
```

Lý do tách status:

- DB lưu kết quả thực tế sau khi nurse thao tác.
- UI cần thêm trạng thái thời gian như due soon hoặc overdue, nhưng những trạng thái này có thể tính từ `scheduled_time` và `logged_at`, không nhất thiết lưu vào `medication_logs`.

## 3. Medication Orders APIs

Medication Orders là nhóm API quản lý y lệnh thuốc. Nhóm này đến từ:

- `excel_M3.csv`: `getMedicationOrders`, `getMedicationOrderById`, `createMedicationOrder`, `updateMedicationOrder`, `deleteMedicationOrder`.
- Wireframe M3-US-06: màn hình Active Medication Orders.
- DB table: `medication_orders`.

### API 1 - Get Medication Orders

```http
GET /api/v1/medication-orders
```

#### Dùng để làm gì?

Lấy danh sách đơn thuốc, có thể lọc theo resident, status, tên thuốc. API này dùng chính cho màn hình M3-US-06 Medication Order List.

#### Từ đâu tạo ra?

Trong `excel_M3.csv`, API gốc là `getMedicationOrders` với route `/api/v1/medication-orders`. Wireframe US-06 có search drug name, tab Active/Discontinued và danh sách thuốc, nên API cần hỗ trợ filter/search/pagination.

#### Authorization

```text
Nurse, DON, Doctor/Clinical Specialist, System Administrator
```

CNA chỉ nên được xem nếu project cho phép CNA xem MAR/medication readonly. Billing chỉ nên xem phần cần cho billing, không nên thấy toàn bộ note lâm sàng nếu không cần.

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `residentId` | number | optional | Lọc thuốc của một resident |
| `status` | string | optional | `ACTIVE`, `DISCONTINUED`, `ON_HOLD`, `ALL` |
| `search` | string | optional | Search theo drug name |
| `page` | number | optional | Default 1 |
| `limit` | number | optional | Default 20 |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "metadata": {
    "currentPage": 1,
    "totalPage": 3,
    "currentLimit": 20,
    "hasNext": true,
    "hasPrevious": false
  },
  "data": {
    "medicationOrders": [
      {
        "id": 1,
        "resident": {
          "id": 1,
          "displayName": "John Smith",
          "roomNumber": "208",
          "bedNumber": "B"
        },
        "drugName": "Aspirin",
        "dosage": "81 mg",
        "route": "ORAL",
        "frequency": "Once Daily",
        "isControlledSubstance": false,
        "status": "ACTIVE",
        "prescribedBy": {
          "id": 4,
          "displayName": "Dr. Brown"
        },
        "lastAdministeredAt": "2026-07-08T08:05:00-04:00",
        "allergyConflict": false,
        "createdAt": "2026-06-01T09:00:00-04:00",
        "updatedAt": "2026-06-01T09:00:00-04:00"
      }
    ]
  }
}
```

#### Vì sao cần các field này?

- `resident`: wireframe US-06 hiển thị resident information.
- `drugName`, `dosage`, `route`, `frequency`: lấy trực tiếp từ `medication_orders`.
- `status`: dùng cho tab Active/Discontinued.
- `lastAdministeredAt`: wireframe US-06 có "Last administered timestamp".
- `allergyConflict`: nghiệp vụ eMAR yêu cầu cảnh báo dị ứng.
- `isControlledSubstance`: DB có field này, dùng để bắt buộc witness khi administer.

### API 2 - Get Medication Order Detail

```http
GET /api/v1/medication-orders/{orderId}
```

#### Dùng để làm gì?

Lấy chi tiết một đơn thuốc, dùng khi user bấm xem chi tiết hoặc mở form edit từ US-06/US-04.

#### Từ đâu tạo ra?

Có trong `excel_M3.csv` là `getMedicationOrderById`. Wireframe US-06 có phần full prescription notes và các thông tin chi tiết.

#### Authorization

```text
Nurse, DON, Doctor/Clinical Specialist, System Administrator
```

#### Path params

| Param | Type | Required |
| --- | --- | --- |
| `orderId` | number | yes |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "resident": {
      "id": 1,
      "displayName": "John Smith",
      "roomNumber": "208",
      "bedNumber": "B"
    },
    "drugName": "Aspirin",
    "dosage": "81 mg",
    "route": "ORAL",
    "frequency": "Once Daily",
    "isControlledSubstance": false,
    "status": "ACTIVE",
    "prescribedBy": {
      "id": 4,
      "displayName": "Dr. Brown"
    },
    "schedules": [
      {
        "id": 1,
        "scheduledTime": "08:00:00",
        "isActive": true
      }
    ],
    "recentLogs": [
      {
        "id": 101,
        "status": "ADMINISTERED",
        "administeredBy": {
          "id": 7,
          "displayName": "Nurse Jane"
        },
        "witnessedBy": null,
        "loggedAt": "2026-07-08T08:05:00-04:00"
      }
    ],
    "createdAt": "2026-06-01T09:00:00-04:00",
    "updatedAt": "2026-06-01T09:00:00-04:00"
  }
}
```

#### Vì sao cần API riêng?

Danh sách order không nên trả quá nhiều dữ liệu. Detail cần thêm schedules và recent logs, nên tách API giúp list nhanh hơn.

### API 3 - Create Medication Order

```http
POST /api/v1/medication-orders
```

#### Dùng để làm gì?

Tạo y lệnh thuốc mới cho resident.

#### Từ đâu tạo ra?

Có trong `excel_M3.csv` là `createMedicationOrder`. DB có bảng `medication_orders`. Nghiệp vụ nursing home yêu cầu bác sĩ hoặc DON tạo đơn thuốc.

#### Authorization

```text
DON, Doctor/Clinical Specialist
```

Không nên cho Nurse thường tạo prescription nếu nghiệp vụ yêu cầu chỉ bác sĩ/DON kê y lệnh.

#### Request body

```json
{
  "residentId": 1,
  "drugName": "Lisinopril",
  "dosage": "10 mg",
  "route": "ORAL",
  "frequency": "Once Daily",
  "isControlledSubstance": false,
  "prescribedBy": 4,
  "status": "ACTIVE",
  "scheduledTimes": ["08:00:00"]
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication order created successfully.",
  "data": {
    "id": 25,
    "residentId": 1,
    "drugName": "Lisinopril",
    "dosage": "10 mg",
    "route": "ORAL",
    "frequency": "Once Daily",
    "isControlledSubstance": false,
    "status": "ACTIVE",
    "prescribedBy": 4,
    "schedules": [
      {
        "id": 33,
        "scheduledTime": "08:00:00",
        "isActive": true
      }
    ],
    "createdAt": "2026-07-08T09:30:00-04:00"
  }
}
```

#### Vì sao request có `scheduledTimes`?

DB tách `medication_orders` và `medication_schedules`. Khi tạo đơn thuốc, frontend thường nhập luôn giờ dùng thuốc. Backend có thể tạo order và tạo schedule trong cùng transaction.

### API 4 - Update Medication Order

```http
PATCH /api/v1/medication-orders/{orderId}
```

#### Dùng để làm gì?

Cập nhật thông tin đơn thuốc: dosage, route, frequency, status.

#### Từ đâu tạo ra?

Có trong `excel_M3.csv` là `updateMedicationOrder`. Wireframe US-04 có nút Edit, vì vậy phải có API update.

#### Authorization

```text
DON, Doctor/Clinical Specialist
```

Nurse thường không nên sửa prescription, trừ khi project định nghĩa nurse có quyền chỉnh administrative fields.

#### Request body

```json
{
  "dosage": "100 mg",
  "frequency": "Twice Daily",
  "status": "ACTIVE"
}
```

#### Response

```json
{
  "statusCode": 200,
  "message": "Medication order updated successfully.",
  "data": {
    "id": 1,
    "drugName": "Aspirin",
    "dosage": "100 mg",
    "route": "ORAL",
    "frequency": "Twice Daily",
    "status": "ACTIVE",
    "updatedAt": "2026-07-08T10:30:00-04:00"
  }
}
```

#### Lưu ý nghiệp vụ

Nếu đổi `frequency`, backend nên yêu cầu cập nhật lại schedules bằng API schedule hoặc tự regenerate schedules.

### API 5 - Discontinue Medication Order

```http
PATCH /api/v1/medication-orders/{orderId}/discontinue
```

#### Dùng để làm gì?

Ngừng một đơn thuốc nhưng vẫn giữ lịch sử.

#### Từ đâu tạo ra?

API bạn thiết kế ban đầu đã có discontinue. DB có status `DISCONTINUED`, nên giữ API này hợp lý hơn xóa cứng.

#### Authorization

```text
DON, Doctor/Clinical Specialist
```

#### Request body

```json
{
  "reason": "Switched to different medication"
}
```

#### Response

```json
{
  "statusCode": 200,
  "message": "Medication order discontinued successfully.",
  "data": {
    "id": 1,
    "status": "DISCONTINUED",
    "discontinuedAt": "2026-07-08T14:00:00-04:00",
    "reason": "Switched to different medication"
  }
}
```

#### Vì sao không dùng DELETE?

Với thuốc, cần giữ audit và lịch sử MAR. `DISCONTINUED` phản ánh đúng nghiệp vụ hơn delete. Nếu vẫn cần soft delete cho admin cleanup, dùng API 6.

### API 6 - Delete Medication Order

```http
DELETE /api/v1/medication-orders/{orderId}
```

#### Dùng để làm gì?

Soft delete một order bị nhập nhầm.

#### Từ đâu tạo ra?

Có trong `excel_M3.csv` là `deleteMedicationOrder`. DB có `is_deleted`, nên API này dùng để set `is_deleted = true`.

#### Authorization

```text
DON, System Administrator
```

#### Response

```json
{
  "statusCode": 200,
  "message": "Medication order deleted successfully.",
  "data": {
    "id": 1,
    "isDeleted": true,
    "deletedAt": "2026-07-08T14:30:00-04:00"
  }
}
```

#### Lưu ý

Không nên cho delete nếu order đã có medication logs, trừ khi chỉ là soft delete và vẫn giữ audit.

## 4. Medication Schedules APIs

Medication Schedules là lịch giờ uống thuốc. Nhóm này đến từ:

- `excel_M3.csv`: `getMedicationSchedules`, `createMedicationSchedule`, `updateMedicationSchedule`, `getTodayMedicationTasks`.
- DB table: `medication_schedules`.
- Wireframe dashboard/med-pass cần danh sách thuốc theo giờ.

### API 7 - Get Medication Schedules

```http
GET /api/v1/medication-schedules
```

#### Dùng để làm gì?

Lấy danh sách lịch uống thuốc theo order, resident, ngày, active status.

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `orderId` | number | optional | Lọc theo order |
| `residentId` | number | optional | Lọc theo resident |
| `date` | date | optional | Ngày cần xem |
| `isActive` | boolean | optional | Lọc lịch còn hiệu lực |
| `page` | number | optional | Default 1 |
| `limit` | number | optional | Default 20 |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "medicationSchedules": [
      {
        "id": 1,
        "orderId": 1,
        "resident": {
          "id": 1,
          "displayName": "John Smith",
          "roomNumber": "208"
        },
        "drugName": "Aspirin",
        "dosage": "81 mg",
        "route": "ORAL",
        "scheduledTime": "08:00:00",
        "isActive": true,
        "taskStatus": "DUE_SOON"
      }
    ]
  }
}
```

#### Vì sao có `taskStatus`?

DB chỉ lưu giờ cố định. Dashboard cần biết thuốc đang upcoming/overdue/completed, nên backend tính `taskStatus` từ schedule, current time và medication logs.

### API 8 - Create Medication Schedule

```http
POST /api/v1/medication-schedules
```

#### Dùng để làm gì?

Thêm một giờ uống thuốc cho order.

#### Authorization

```text
DON, Doctor/Clinical Specialist
```

#### Request body

```json
{
  "orderId": 1,
  "scheduledTime": "08:00:00",
  "isActive": true
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication schedule created successfully.",
  "data": {
    "id": 1,
    "orderId": 1,
    "scheduledTime": "08:00:00",
    "isActive": true
  }
}
```

### API 9 - Update Medication Schedule

```http
PATCH /api/v1/medication-schedules/{scheduleId}
```

#### Dùng để làm gì?

Sửa giờ uống thuốc hoặc bật/tắt schedule.

#### Request body

```json
{
  "scheduledTime": "09:00:00",
  "isActive": true
}
```

#### Response

```json
{
  "statusCode": 200,
  "message": "Medication schedule updated successfully.",
  "data": {
    "id": 1,
    "orderId": 1,
    "scheduledTime": "09:00:00",
    "isActive": true
  }
}
```

### API 10 - Get Today Medication Tasks

```http
GET /api/v1/medication-schedules/today
```

#### Dùng để làm gì?

Lấy task phát thuốc hôm nay cho nurse. Đây là API nền cho dashboard và med-pass.

#### Từ đâu tạo ra?

Có sẵn trong `excel_M3.csv` là `getTodayMedicationTasks`. Nghiệp vụ eMAR trong tài liệu yêu cầu "Smart Med-Pass Scheduling" với list resident cần uống thuốc trong time window.

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `residentId` | number | optional | Lọc theo resident |
| `date` | date | optional | Default today |
| `shift` | string | optional | `DAY`, `EVENING`, `NIGHT` |
| `status` | string | optional | `SCHEDULED`, `DUE_SOON`, `OVERDUE`, `COMPLETED` |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "todayMedicationTasks": [
      {
        "scheduleId": 1,
        "orderId": 1,
        "resident": {
          "id": 1,
          "displayName": "John Smith",
          "roomNumber": "208",
          "bedNumber": "B"
        },
        "drugName": "Aspirin",
        "dosage": "81 mg",
        "route": "ORAL",
        "scheduledTime": "08:00:00",
        "timeWindowStart": "07:30:00",
        "timeWindowEnd": "08:30:00",
        "taskStatus": "DUE_SOON",
        "requiresWitness": false
      }
    ]
  }
}
```

## 5. MAR and Med-Pass APIs

Nhóm này là phần mới hơn so với `excel_M3.csv`, nhưng cần cho wireframe US-01, US-02, US-03, US-04.

### API 11 - Get eMAR Dashboard

```http
GET /api/v1/mar/dashboard
```

#### Dùng để làm gì?

Hiển thị dashboard phát thuốc theo ca: số pending/completed/overdue, danh sách resident cần phát thuốc, cảnh báo allergy.

#### Từ đâu tạo ra?

Tạo từ wireframe M3-US-01 Medication Admin Dashboard và nghiệp vụ "Smart Med-Pass Scheduling" trong tài liệu `Quản lý viện dưỡng lão tại Mỹ_02_EN.txt`.

#### Authorization

```text
Nurse, DON
```

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `date` | date | optional | Default today |
| `shift` | string | optional | `DAY`, `EVENING`, `NIGHT` |
| `status` | string | optional | `ALL`, `DUE_SOON`, `OVERDUE`, `COMPLETED` |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "date": "2026-07-08",
    "shift": "DAY",
    "summary": {
      "scheduled": 75,
      "completed": 48,
      "dueSoon": 24,
      "overdue": 3
    },
    "allergyAlerts": [
      {
        "residentId": 1,
        "residentName": "John Smith",
        "allergy": "Penicillin",
        "unconfirmed": true
      }
    ],
    "medPassList": [
      {
        "residentId": 2,
        "residentName": "Mary Brown",
        "roomNumber": "118",
        "bedNumber": "A",
        "taskStatus": "OVERDUE",
        "nextMedication": {
          "orderId": 8,
          "scheduleId": 10,
          "drugName": "Insulin",
          "dosage": "6 Units",
          "scheduledTime": "08:00:00",
          "timeWindowStart": "07:30:00",
          "timeWindowEnd": "08:30:00"
        },
        "hasUnconfirmedAllergy": false
      }
    ]
  }
}
```

#### Vì sao không dùng trực tiếp `/medication-schedules/today`?

`today` trả task chi tiết. Dashboard cần thêm summary, allergy alerts và grouping cho màn hình. Vì vậy dashboard là API tổng hợp từ schedules, orders, residents, allergies và logs.

### API 12 - Get MAR Resident Overview

```http
GET /api/v1/mar/residents
```

#### Dùng để làm gì?

Lấy danh sách resident và medication status để hiển thị màn hình M3-US-04 MAR overview.

#### Từ đâu tạo ra?

Wireframe M3-US-04 hiển thị nhiều resident card:

- Name
- Room
- Medication status
- Medication name
- Edit button

API cũ của bạn chỉ có MAR detail cho một resident, chưa đủ cho màn overview này.

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `date` | date | optional | Default today |
| `status` | string | optional | `ALL`, `DUE_SOON`, `OVERDUE`, `COMPLETED` |
| `search` | string | optional | Search resident |
| `page` | number | optional | Default 1 |
| `limit` | number | optional | Default 20 |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "residents": [
      {
        "residentId": 1,
        "residentName": "John Smith",
        "roomNumber": "208",
        "bedNumber": "B",
        "currentMedication": {
          "orderId": 1,
          "drugName": "Aspirin",
          "dosage": "100 mg",
          "taskStatus": "COMPLETED",
          "lastLoggedAt": "2026-07-08T08:05:00-04:00"
        }
      }
    ]
  }
}
```

### API 13 - Get Resident MAR Detail

```http
GET /api/v1/mar/residents/{residentId}
```

#### Dùng để làm gì?

Lấy Medication Administration Record chi tiết cho một resident trong một khoảng ngày.

#### Từ đâu tạo ra?

API này đến từ bản bạn thiết kế ban đầu và bổ sung cho US-04 khi user bấm vào resident hoặc Edit/View.

#### Query params

| Param | Type | Required | Note |
| --- | --- | --- | --- |
| `startDate` | date | optional | Nếu không truyền, default đầu tuần |
| `endDate` | date | optional | Nếu không truyền, default hôm nay |
| `status` | string | optional | `ALL`, `ADMINISTERED`, `REFUSED`, `HELD`, `NOT_AVAILABLE` |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "resident": {
      "id": 1,
      "displayName": "John Smith",
      "roomNumber": "208",
      "bedNumber": "B"
    },
    "dateRange": {
      "start": "2026-07-01",
      "end": "2026-07-08"
    },
    "medicationGrid": [
      {
        "orderId": 1,
        "drugName": "Aspirin",
        "dosage": "100 mg",
        "route": "ORAL",
        "frequency": "Every 8 hours",
        "days": [
          {
            "date": "2026-07-08",
            "scheduledTime": "08:00:00",
            "taskStatus": "COMPLETED",
            "logStatus": "ADMINISTERED",
            "administeredBy": "Nurse Jane",
            "witnessedBy": null,
            "loggedAt": "2026-07-08T08:05:00-04:00",
            "overrideReason": null
          }
        ]
      }
    ],
    "summaryStats": {
      "totalScheduled": 21,
      "administered": 18,
      "refused": 1,
      "held": 1,
      "notAvailable": 1
    }
  }
}
```

### API 14 - Start Med-Pass Session

```http
POST /api/v1/mar/med-pass/start
```

#### Dùng để làm gì?

Bắt đầu phiên phát thuốc cho một resident. Trả về thông tin resident, allergy và danh sách thuốc cần scan/administer.

#### Từ đâu tạo ra?

Wireframe M3-US-02 có Patient image, Patient information và barcode scanning. Trước khi scan thuốc, frontend cần biết đang phát thuốc cho resident nào.

#### Request body

```json
{
  "residentId": 1
}
```

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "sessionId": "MP-20260708-0001",
    "expiresAt": "2026-07-08T08:35:00-04:00",
    "resident": {
      "id": 1,
      "displayName": "John Smith",
      "roomNumber": "208",
      "bedNumber": "B",
      "dateOfBirth": "1945-03-12",
      "allergies": ["Penicillin", "Sulfa"],
      "allergyConfirmed": false
    },
    "pendingMedications": [
      {
        "orderId": 1,
        "scheduleId": 1,
        "drugName": "Aspirin",
        "dosage": "100 mg",
        "route": "ORAL",
        "scheduledTime": "08:00:00",
        "requiresWitness": false
      }
    ]
  }
}
```

#### Lưu ý

`sessionId` không nhất thiết phải lưu DB nếu backend không cần session thật. Nó có thể là token workflow ngắn hạn. Nếu muốn đơn giản, có thể bỏ `sessionId` và dùng `residentId + orderId + scheduleId`.

### API 15 - Scan Barcode and Verify 5 Rights

```http
POST /api/v1/mar/med-pass/scan
```

#### Dùng để làm gì?

Scan barcode thuốc hoặc nhập barcode thủ công, sau đó kiểm tra 5 Rights:

- Right resident
- Right medication
- Right dose
- Right route
- Right time

#### Từ đâu tạo ra?

Wireframe M3-US-02 có khu vực barcode scanning và checklist 5 Rights. Tài liệu nghiệp vụ eMAR cũng yêu cầu barcode scanning của wristband và medication packaging.

#### Request body

```json
{
  "sessionId": "MP-20260708-0001",
  "residentId": 1,
  "orderId": 1,
  "scheduleId": 1,
  "barcodeData": "1234567890",
  "scanMethod": "CAMERA"
}
```

#### Response success

```json
{
  "statusCode": 200,
  "message": "Verification completed.",
  "data": {
    "verificationStatus": "MATCHED",
    "fiveRights": {
      "rightResident": {
        "passed": true,
        "detail": "Barcode matches selected resident."
      },
      "rightMedication": {
        "passed": true,
        "detail": "Medication matches order."
      },
      "rightDose": {
        "passed": true,
        "detail": "Dose matches order."
      },
      "rightRoute": {
        "passed": true,
        "detail": "Route matches order."
      },
      "rightTime": {
        "passed": true,
        "detail": "Within +/- 30 minute window."
      }
    },
    "canAdminister": true,
    "requiresOverride": false,
    "requiresWitness": false
  }
}
```

#### Response failed

```json
{
  "statusCode": 200,
  "message": "Verification completed.",
  "data": {
    "verificationStatus": "FAILED",
    "fiveRights": {
      "rightResident": {
        "passed": true,
        "detail": "Resident matched."
      },
      "rightMedication": {
        "passed": false,
        "detail": "Scanned medication does not match selected order."
      },
      "rightDose": {
        "passed": true,
        "detail": "Dose matched."
      },
      "rightRoute": {
        "passed": true,
        "detail": "Route matched."
      },
      "rightTime": {
        "passed": false,
        "detail": "Outside +/- 30 minute window."
      }
    },
    "canAdminister": false,
    "requiresOverride": true,
    "overrideReasons": [
      "BARCODE_UNREADABLE",
      "EMERGENCY_ADMINISTRATION",
      "TIME_WINDOW_EXCEPTION",
      "PATIENT_UNAVAILABLE",
      "OTHER"
    ]
  }
}
```

#### Vì sao failed vẫn trả 200?

Vì scan mismatch là kết quả nghiệp vụ, không phải lỗi kỹ thuật. `400` chỉ nên dùng khi request thiếu field hoặc barcode format invalid.

### API 16 - Administer Medication

```http
POST /api/v1/mar/med-pass/administer
```

#### Dùng để làm gì?

Ghi nhận đã phát thuốc thành công sau khi verification pass.

#### Từ đâu tạo ra?

DB có `medication_logs`. Wireframe US-02 là luồng scan rồi administer. `excel_M3.csv` có `createMedicationLog`.

#### Request body

```json
{
  "sessionId": "MP-20260708-0001",
  "orderId": 1,
  "scheduleId": 1,
  "witnessedBy": null,
  "notes": "Patient took with water."
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication administered successfully.",
  "data": {
    "logId": 101,
    "orderId": 1,
    "scheduleId": 1,
    "status": "ADMINISTERED",
    "isClinicallyJustified": true,
    "administeredBy": {
      "id": 7,
      "displayName": "Nurse Jane"
    },
    "witnessedBy": null,
    "loggedAt": "2026-07-08T08:05:00-04:00"
  }
}
```

#### Vì sao không gửi `administeredBy` trong body?

User thao tác phải lấy từ token đăng nhập. Nếu cho client gửi `administeredBy`, người dùng có thể giả mạo nurse khác.

### API 17 - Override Verification

```http
POST /api/v1/mar/med-pass/override
```

#### Dùng để làm gì?

Cho phép nurse/DON override khi verification fail nhưng có lý do lâm sàng hợp lệ.

#### Từ đâu tạo ra?

Wireframe M3-US-03 có màn Override Required, Alert Summary, override reason, checkbox clinical justification và nút Confirm Override.

#### Request body

```json
{
  "sessionId": "MP-20260708-0001",
  "orderId": 1,
  "scheduleId": 1,
  "overrideReason": "TIME_WINDOW_EXCEPTION",
  "otherReasonText": null,
  "confirmClinicallyJustified": true,
  "witnessedBy": null,
  "notes": "Physician approved late administration."
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Override confirmed and medication logged.",
  "data": {
    "logId": 102,
    "orderId": 1,
    "scheduleId": 1,
    "status": "ADMINISTERED",
    "isClinicallyJustified": true,
    "overrideReason": "TIME_WINDOW_EXCEPTION",
    "administeredBy": {
      "id": 7,
      "displayName": "Nurse Jane"
    },
    "witnessedBy": null,
    "loggedAt": "2026-07-08T08:45:00-04:00",
    "auditLogged": true
  }
}
```

#### Validation

- `confirmClinicallyJustified` phải là `true`.
- `otherReasonText` required nếu `overrideReason = OTHER`.
- Nếu order là controlled substance thì `witnessedBy` required.

### API 18 - Mark Medication Refused

```http
POST /api/v1/mar/med-pass/refuse
```

#### Dùng để làm gì?

Ghi nhận resident từ chối uống thuốc.

#### Từ đâu tạo ra?

Có trong bản bạn thiết kế và tương ứng với `recordRefusedMedication` trong `excel_M3.csv`. DB có log status `REFUSED`.

#### Request body

```json
{
  "orderId": 1,
  "scheduleId": 1,
  "reason": "Resident refused due to nausea."
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication marked as refused.",
  "data": {
    "logId": 103,
    "orderId": 1,
    "scheduleId": 1,
    "status": "REFUSED",
    "overrideReason": "Resident refused due to nausea.",
    "loggedAt": "2026-07-08T08:10:00-04:00"
  }
}
```

### API 19 - Mark Medication Held

```http
POST /api/v1/mar/med-pass/hold
```

#### Dùng để làm gì?

Ghi nhận thuốc bị tạm hoãn, ví dụ resident đang ngủ, đang đi therapy, đang đi khám ngoài.

#### Từ đâu tạo ra?

Bản API bạn ban đầu có `miss`. Nhưng DB không có `MISSED`; DB có `HELD`. Vì vậy đổi API thành `hold` để khớp schema.

#### Request body

```json
{
  "orderId": 1,
  "scheduleId": 1,
  "reason": "Resident was in therapy session."
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication marked as held.",
  "data": {
    "logId": 104,
    "orderId": 1,
    "scheduleId": 1,
    "status": "HELD",
    "overrideReason": "Resident was in therapy session.",
    "loggedAt": "2026-07-08T08:15:00-04:00"
  }
}
```

### API 20 - Mark Medication Not Available

```http
POST /api/v1/mar/med-pass/not-available
```

#### Dùng để làm gì?

Ghi nhận thuốc không có sẵn trong kho/facility.

#### Từ đâu tạo ra?

DB có log status `NOT_AVAILABLE`, nhưng bản API cũ chưa tách riêng. API này giúp frontend chọn đúng lý do và tạo log chuẩn.

#### Request body

```json
{
  "orderId": 1,
  "scheduleId": 1,
  "reason": "Medication not available in cart."
}
```

#### Response

```json
{
  "statusCode": 201,
  "message": "Medication marked as not available.",
  "data": {
    "logId": 105,
    "orderId": 1,
    "scheduleId": 1,
    "status": "NOT_AVAILABLE",
    "overrideReason": "Medication not available in cart.",
    "loggedAt": "2026-07-08T08:20:00-04:00"
  }
}
```

## 6. Medication Logs APIs

Nhóm này là API lịch sử phát thuốc, lấy từ `excel_M3.csv` và DB `medication_logs`.

### API 21 - Get Medication Logs

```http
GET /api/v1/medication-logs
```

#### Dùng để làm gì?

Lấy lịch sử phát thuốc theo order/resident/status/date range.

#### Query params

| Param | Type | Required |
| --- | --- | --- |
| `orderId` | number | optional |
| `residentId` | number | optional |
| `status` | string | optional |
| `fromDate` | date | optional |
| `toDate` | date | optional |
| `page` | number | optional |
| `limit` | number | optional |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "medicationLogs": [
      {
        "id": 101,
        "orderId": 1,
        "resident": {
          "id": 1,
          "displayName": "John Smith"
        },
        "drugName": "Aspirin",
        "status": "ADMINISTERED",
        "isClinicallyJustified": true,
        "overrideReason": null,
        "administeredBy": {
          "id": 7,
          "displayName": "Nurse Jane"
        },
        "witnessedBy": null,
        "loggedAt": "2026-07-08T08:05:00-04:00"
      }
    ]
  }
}
```

### API 22 - Get Medication Log Detail

```http
GET /api/v1/medication-logs/{logId}
```

#### Dùng để làm gì?

Xem chi tiết một log phát thuốc.

#### Từ đâu tạo ra?

Có trong `excel_M3.csv` là `getMedicationLogById`.

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "id": 101,
    "orderId": 1,
    "status": "ADMINISTERED",
    "isClinicallyJustified": true,
    "overrideReason": null,
    "administeredBy": {
      "id": 7,
      "displayName": "Nurse Jane"
    },
    "witnessedBy": null,
    "loggedAt": "2026-07-08T08:05:00-04:00"
  }
}
```

## 7. Print and Export APIs

### API 23 - Print Resident MAR

```http
GET /api/v1/mar/residents/{residentId}/print
```

#### Dùng để làm gì?

Xuất MAR của resident thành PDF.

#### Từ đâu tạo ra?

Bản API bạn thiết kế ban đầu có print MAR. Đây là nghiệp vụ thường dùng khi audit, survey, hoặc in hồ sơ.

#### Query params

| Param | Type | Required |
| --- | --- | --- |
| `startDate` | date | optional |
| `endDate` | date | optional |

#### Response

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="MAR_JohnSmith_2026-07-08.pdf"
```

### API 24 - Export MAR Audit Report

```http
GET /api/v1/audit/mar-export
```

#### Dùng để làm gì?

Export báo cáo MAR dạng CSV cho compliance/regulatory review.

#### Query params

| Param | Type | Required |
| --- | --- | --- |
| `residentId` | number | optional |
| `startDate` | date | yes |
| `endDate` | date | yes |

#### Response

```http
Content-Type: text/csv
Content-Disposition: attachment; filename="MAR_Audit_2026-07-08.csv"
```

## 8. Audit APIs

### API 25 - Get Medication Audit Log

```http
GET /api/v1/audit/medication
```

#### Dùng để làm gì?

Xem audit trail cho các hành động liên quan medication: create order, update order, administer, override, view MAR.

#### Từ đâu tạo ra?

Bản API bạn thiết kế ban đầu có audit. Tài liệu nghiệp vụ yêu cầu compliance và incident/medication error tracking.

#### Authorization

```text
DON, System Administrator
```

#### Query params

| Param | Type | Required |
| --- | --- | --- |
| `residentId` | number | optional |
| `orderId` | number | optional |
| `action` | string | optional |
| `startDate` | date | yes |
| `endDate` | date | yes |
| `page` | number | optional |
| `limit` | number | optional |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "logs": [
      {
        "id": 1,
        "tableName": "medication_logs",
        "recordId": "101",
        "action": "INSERT",
        "performedBy": {
          "id": 7,
          "displayName": "Nurse Jane"
        },
        "performedAt": "2026-07-08T08:05:00-04:00",
        "ipAddress": "192.168.1.100"
      }
    ]
  }
}
```

### API 26 - Get PHI Access Log

```http
GET /api/v1/audit/phi-access
```

#### Dùng để làm gì?

Xem ai đã truy cập dữ liệu PHI liên quan medication/MAR.

#### Từ đâu tạo ra?

Bản API bạn thiết kế ban đầu có PHI access log. M1 cũng có yêu cầu audit khi xem sensitive info. MAR/medication là dữ liệu sức khỏe, nên phải log truy cập.

#### Authorization

```text
DON, System Administrator
```

#### Query params

| Param | Type | Required |
| --- | --- | --- |
| `residentId` | number | yes |
| `accessType` | string | optional |
| `startDate` | date | yes |
| `endDate` | date | yes |

#### Response

```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "logs": [
      {
        "id": 123,
        "tableName": "medication_orders",
        "recordId": "1",
        "accessedBy": {
          "id": 7,
          "displayName": "Nurse Jane"
        },
        "accessType": "VIEW",
        "accessReason": "Shift handoff review",
        "ipAddress": "192.168.1.101",
        "accessedAt": "2026-07-08T07:30:00-04:00"
      }
    ]
  }
}
```

## 9. Pharmacy Integration APIs

Tài liệu nghiệp vụ `Quản lý viện dưỡng lão tại Mỹ_02_EN.txt` có yêu cầu Pharmacy Integration: tự động gửi refill request khi inventory thấp. DB hiện tại chưa có bảng inventory/refill rõ trong M3, nên nhóm API này là đề xuất mở rộng. Nếu sprint M3 chưa làm pharmacy thì có thể để phase sau.

### API 27 - Get Low Stock Medication Inventory

```http
GET /api/v1/medication-inventory/low-stock
```

#### Dùng để làm gì?

Xem thuốc nào dưới ngưỡng reorder.

#### Từ đâu tạo ra?

Từ requirement Pharmacy Integration trong tài liệu nghiệp vụ eMAR.

### API 28 - Create Pharmacy Refill Request

```http
POST /api/v1/pharmacy/refill-requests
```

#### Dùng để làm gì?

Tạo yêu cầu refill gửi tới LTC pharmacy.

#### Từ đâu tạo ra?

Từ requirement "Automatically transmits refill requests to partner long-term care pharmacies".

## 10. Mapping nhanh API với wireframe

| Wireframe | API chính |
| --- | --- |
| M3-US-01 Dashboard | API 10, API 11 |
| M3-US-02 Barcode Scan | API 14, API 15, API 16 |
| M3-US-03 Override | API 15, API 17 |
| M3-US-04 MAR | API 12, API 13, API 18, API 19, API 20 |
| M3-US-06 Order List | API 1, API 2, API 3, API 4, API 5 |

## 11. Những thay đổi chính so với bản API bạn gửi

1. Đổi `uuid` sang `number/Long` để khớp `sql_final_v2.sql`.
2. Giữ route gốc `/api/v1/medication-orders`, `/api/v1/medication-schedules`, `/api/v1/medication-logs` để khớp `excel_M3.csv`.
3. Thêm nhóm `/api/v1/mar/...` cho dashboard, MAR overview, med-pass barcode và override vì wireframe yêu cầu.
4. Đổi `miss` thành `hold` để khớp DB enum `HELD`.
5. Thêm `not-available` vì DB có enum `NOT_AVAILABLE`.
6. Không nhận `administeredBy` từ request body; backend lấy từ token.
7. Tách `logStatus` và `taskStatus` để tránh nhầm status lưu DB với status hiển thị UI.
8. Đưa Pharmacy Integration vào phase đề xuất vì có trong tài liệu nghiệp vụ nhưng chưa có schema rõ.
