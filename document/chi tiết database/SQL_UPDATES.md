# SQL Database Updates - 17/07/2026

## Tóm Tắt Thay Đổi

File `sql_final_v2.sql` đã được cập nhật để phản ánh các thay đổi trong Entity models.

---

## Chi Tiết Thay Đổi

### 1. Bảng `care_plans` - Thêm 5 columns

| Column | Type | Mô Tả |
|--------|------|-------|
| `version` | INT | Phiên bản của kế hoạch (1, 2, 3, ...) |
| `parent_plan_id` | BIGINT | ID của kế hoạch cha (cho versioning) |
| `review_trigger` | VARCHAR(50) | Lý do tái đánh giá (90_DAY_CYCLE, SIGNIFICANT_CHANGE) |
| `review_due_date` | DATE | Ngày đến hạn tái đánh giá |
| `rejection_reason` | NVARCHAR(1000) | Lý do từ chối (khi status = REJECTED) |

**SQL:**
```sql
ALTER TABLE [care_plans] ADD [version] INT NOT NULL DEFAULT (1);
ALTER TABLE [care_plans] ADD [parent_plan_id] BIGINT;
ALTER TABLE [care_plans] ADD [review_trigger] VARCHAR(50);
ALTER TABLE [care_plans] ADD [review_due_date] DATE;
ALTER TABLE [care_plans] ADD [rejection_reason] NVARCHAR(1000);
```

**Status Check cập nhật:**
```sql
-- Thêm 'PENDING_REVIEW' vào danh sách status
CHECK (status IN ('DRAFT','ACTIVE','RESOLVED','DISCONTINUED','PENDING_REVIEW'))
```

---

### 2. Bảng `care_goals` - Thêm 5 columns

| Column | Type | Mô Tả |
|--------|------|-------|
| `care_area_name` | NVARCHAR(200) | Tên lĩnh vực chăm sóc |
| `source_type` | VARCHAR(50) | Loại nguồn mục tiêu |
| `goal_description` | NVARCHAR(MAX) | Mô tả chi tiết mục tiêu |
| `measure` | NVARCHAR(200) | Cách đo lường tiến độ |
| `target_date` | DATE | Ngày đạt mục tiêu dự kiến |

**SQL:**
```sql
ALTER TABLE [care_goals] ADD [care_area_name] NVARCHAR(200);
ALTER TABLE [care_goals] ADD [source_type] VARCHAR(50);
ALTER TABLE [care_goals] ADD [goal_description] NVARCHAR(MAX);
ALTER TABLE [care_goals] ADD [measure] NVARCHAR(200);
ALTER TABLE [care_goals] ADD [target_date] DATE;
```

---

### 3. Bảng `care_interventions` - Thêm 2 columns

| Column | Type | Mô Tả |
|--------|------|-------|
| `description` | NVARCHAR(MAX) | Mô tả chi tiết can thiệp |
| `frequency` | NVARCHAR(100) | Tần suất thực hiện (hàng ngày, 3 lần/tuần, v.v.) |

**SQL:**
```sql
ALTER TABLE [care_interventions] ADD [description] NVARCHAR(MAX) NOT NULL;
ALTER TABLE [care_interventions] ADD [frequency] NVARCHAR(100);
```

---

## Hướng Dẫn Cập Nhật Database

### Cách 1: Chạy Script SQL trực tiếp

```sql
-- Backup database trước
BACKUP DATABASE [MockProject] TO DISK = 'C:\Backup\MockProject_backup.bak';

-- Sau đó chạy sql_final_v2.sql
-- Drop và tạo lại tất cả tables
```

### Cách 2: Chỉ thêm columns (nếu database đã tồn tại)

```sql
-- care_plans
ALTER TABLE [care_plans] ADD [version] INT NOT NULL DEFAULT (1);
ALTER TABLE [care_plans] ADD [parent_plan_id] BIGINT;
ALTER TABLE [care_plans] ADD [review_trigger] VARCHAR(50);
ALTER TABLE [care_plans] ADD [review_due_date] DATE;
ALTER TABLE [care_plans] ADD [rejection_reason] NVARCHAR(1000);

-- care_goals
ALTER TABLE [care_goals] ADD [care_area_name] NVARCHAR(200);
ALTER TABLE [care_goals] ADD [source_type] VARCHAR(50);
ALTER TABLE [care_goals] ADD [goal_description] NVARCHAR(MAX);
ALTER TABLE [care_goals] ADD [measure] NVARCHAR(200);
ALTER TABLE [care_goals] ADD [target_date] DATE;

-- care_interventions
ALTER TABLE [care_interventions] ADD [description] NVARCHAR(MAX) NOT NULL;
ALTER TABLE [care_interventions] ADD [frequency] NVARCHAR(100);
```

---

## Kiểm Tra Dữ Liệu Sau Cập Nhật

```sql
-- Kiểm tra columns mới
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'care_plans'
ORDER BY ORDINAL_POSITION;

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'care_goals'
ORDER BY ORDINAL_POSITION;

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'care_interventions'
ORDER BY ORDINAL_POSITION;
```

---

## Entity-to-SQL Mapping

### CarePlan Entity → SQL Table
```
java: Long id              → sql: BIGINT IDENTITY PRIMARY KEY ✓
java: String status        → sql: VARCHAR(20) CHECK ✓
java: Boolean flag         → sql: BIT DEFAULT(0) ✓
java: Resident resident    → sql: BIGINT (FK) ✓
java: Integer version      → sql: INT DEFAULT(1) ✓ [NEW]
java: Long parentPlanId    → sql: BIGINT ✓ [NEW]
java: String reviewTrigger → sql: VARCHAR(50) ✓ [NEW]
java: LocalDate reviewDueDate → sql: DATE ✓ [NEW]
java: String rejectionReason → sql: NVARCHAR(1000) ✓ [NEW]
java: Boolean isDeleted    → sql: BIT DEFAULT(0) ✓
java: OffsetDateTime createdAt → sql: DATETIMEOFFSET ✓
java: OffsetDateTime updatedAt → sql: DATETIMEOFFSET ✓
```

### CareGoal Entity → SQL Table
```
java: Long id                  → sql: BIGINT IDENTITY PRIMARY KEY ✓
java: String careAreaName      → sql: NVARCHAR(200) ✓ [NEW]
java: String sourceType        → sql: VARCHAR(50) ✓ [NEW]
java: String goalDescription   → sql: NVARCHAR(MAX) ✓ [NEW]
java: String measure           → sql: NVARCHAR(200) ✓ [NEW]
java: LocalDate targetDate     → sql: DATE ✓ [NEW]
java: String status            → sql: VARCHAR(20) CHECK ✓
java: CarePlan carePlan        → sql: BIGINT (FK) ✓
```

### CareIntervention Entity → SQL Table
```
java: Long id              → sql: BIGINT IDENTITY PRIMARY KEY ✓
java: String assignedRole  → sql: VARCHAR(50) NOT NULL ✓
java: String description   → sql: NVARCHAR(MAX) NOT NULL ✓ [NEW]
java: String frequency     → sql: NVARCHAR(100) ✓ [NEW]
java: CarePlan carePlan    → sql: BIGINT (FK) NOT NULL ✓
```

---

## Lưu Ý Quan Trọng

1. **Backup Database**: Luôn backup database trước khi chạy script
2. **Test trên Dev**: Test script trên environment dev trước
3. **Data Migration**: Nếu có dữ liệu cũ, cần migration script
4. **Version Management**: Dùng Flyway hoặc Liquibase cho production

---

## File Liên Quan

- 📄 `sql_final_v2.sql` - Main SQL script (cập nhật)
- 📄 `db_seed.sql` - Seed data
- 📁 `entity/CarePlan.java` - Entity model
- 📁 `entity/CareGoal.java` - Entity model
- 📁 `entity/CareIntervention.java` - Entity model

---

**Cập nhật bởi:** GitHub Copilot
**Ngày:** 17/07/2026
**Version:** 2.1

