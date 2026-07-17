-- SQL Migration Script
-- Migration from Version 2.0 to Version 2.1
-- Date: 17/07/2026
-- Purpose: Add missing columns to support CarePlan versioning and Reassessment feature

-- =====================================================
-- STEP 1: ALTER care_plans TABLE
-- =====================================================

-- Thêm columns cho versioning
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_plans' AND COLUMN_NAME='version')
BEGIN
    ALTER TABLE [care_plans] ADD [version] INT NOT NULL DEFAULT (1);
    PRINT 'Added column [version] to care_plans';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_plans' AND COLUMN_NAME='parent_plan_id')
BEGIN
    ALTER TABLE [care_plans] ADD [parent_plan_id] BIGINT;
    PRINT 'Added column [parent_plan_id] to care_plans';
END
GO

-- Thêm columns cho reassessment workflow
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_plans' AND COLUMN_NAME='review_trigger')
BEGIN
    ALTER TABLE [care_plans] ADD [review_trigger] VARCHAR(50);
    PRINT 'Added column [review_trigger] to care_plans';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_plans' AND COLUMN_NAME='review_due_date')
BEGIN
    ALTER TABLE [care_plans] ADD [review_due_date] DATE;
    PRINT 'Added column [review_due_date] to care_plans';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_plans' AND COLUMN_NAME='rejection_reason')
BEGIN
    ALTER TABLE [care_plans] ADD [rejection_reason] NVARCHAR(1000);
    PRINT 'Added column [rejection_reason] to care_plans';
END
GO

-- =====================================================
-- STEP 2: ALTER care_goals TABLE
-- =====================================================

-- Thêm columns cho chi tiết mục tiêu chăm sóc
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_goals' AND COLUMN_NAME='care_area_name')
BEGIN
    ALTER TABLE [care_goals] ADD [care_area_name] NVARCHAR(200);
    PRINT 'Added column [care_area_name] to care_goals';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_goals' AND COLUMN_NAME='source_type')
BEGIN
    ALTER TABLE [care_goals] ADD [source_type] VARCHAR(50);
    PRINT 'Added column [source_type] to care_goals';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_goals' AND COLUMN_NAME='goal_description')
BEGIN
    ALTER TABLE [care_goals] ADD [goal_description] NVARCHAR(MAX);
    PRINT 'Added column [goal_description] to care_goals';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_goals' AND COLUMN_NAME='measure')
BEGIN
    ALTER TABLE [care_goals] ADD [measure] NVARCHAR(200);
    PRINT 'Added column [measure] to care_goals';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_goals' AND COLUMN_NAME='target_date')
BEGIN
    ALTER TABLE [care_goals] ADD [target_date] DATE;
    PRINT 'Added column [target_date] to care_goals';
END
GO

-- =====================================================
-- STEP 3: ALTER care_interventions TABLE
-- =====================================================

-- Thêm columns cho chi tiết can thiệp
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_interventions' AND COLUMN_NAME='description')
BEGIN
    ALTER TABLE [care_interventions] ADD [description] NVARCHAR(MAX) NOT NULL DEFAULT '';
    PRINT 'Added column [description] to care_interventions';
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='care_interventions' AND COLUMN_NAME='frequency')
BEGIN
    ALTER TABLE [care_interventions] ADD [frequency] NVARCHAR(100);
    PRINT 'Added column [frequency] to care_interventions';
END
GO

-- =====================================================
-- STEP 4: CREATE FOREIGN KEY CONSTRAINTS
-- =====================================================

-- Foreign key cho parent_plan_id (self-referencing)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
              WHERE TABLE_NAME='care_plans' AND CONSTRAINT_NAME='FK_care_plans_parent')
BEGIN
    ALTER TABLE [care_plans]
    ADD CONSTRAINT [FK_care_plans_parent]
    FOREIGN KEY ([parent_plan_id]) REFERENCES [care_plans]([id]);
    PRINT 'Added foreign key FK_care_plans_parent';
END
GO

-- =====================================================
-- STEP 5: CREATE INDEXES
-- =====================================================

-- Index cho versioning queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_care_plans_resident_version' AND object_id=OBJECT_ID('care_plans'))
BEGIN
    CREATE INDEX [idx_care_plans_resident_version] ON [care_plans] ([resident_id], [version]);
    PRINT 'Created index idx_care_plans_resident_version';
END
GO

-- Index cho reassessment queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_care_plans_review_due_date' AND object_id=OBJECT_ID('care_plans'))
BEGIN
    CREATE INDEX [idx_care_plans_review_due_date] ON [care_plans] ([review_due_date], [status]);
    PRINT 'Created index idx_care_plans_review_due_date';
END
GO

-- =====================================================
-- STEP 6: VALIDATION
-- =====================================================

-- Verify all columns were added
PRINT '';
PRINT '===== MIGRATION VERIFICATION =====';
PRINT '';

-- Verify care_plans columns
PRINT 'Columns in care_plans:';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME='care_plans'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT 'Columns in care_goals:';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME='care_goals'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT 'Columns in care_interventions:';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME='care_interventions'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '===== MIGRATION COMPLETED SUCCESSFULLY =====';

