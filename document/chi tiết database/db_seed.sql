USE [nursing_home_db];
GO

-- 1. Insert Default Roles

INSERT INTO [roles] ([role_name], [description], [is_deleted]) VALUES
('System_Administrator', 'System Administrator with full access', 0),
('DON', 'Director of Nursing - Clinical leadership role', 0),
('Facility_Manager', 'Facility Manager responsible for operations', 0),
('Accountant/Billing_Staff', 'Finance and billing staff', 0),
('Admission_Staff', 'Staff managing intakes and admissions', 0),
('Nurse', 'Registered Nurse or Licensed Practical Nurse', 0),
('CNA', 'Certified Nursing Assistant / Caregiver', 0),
('Doctor/Clinical_Specialist', 'External or internal physicians', 0);
GO

-- 2. Insert Care Levels
INSERT INTO [care_levels] ([level_code], [level_name], [is_deleted]) VALUES
('INDEPENDENT_LIVING', 'Independent Living', 0),
('ASSISTED_LIVING', 'Assisted Living', 0),
('MEMORY_CARE', 'Memory Care', 0),
('SKILLED_NURSING', 'Skilled Nursing', 0),
('HOSPICE', 'Hospice Care', 0);
GO

-- 3. Insert Sample Addresses
INSERT INTO [addresses] ([street_line1], [street_line2], [city], [state], [zip_code], [address_type], [is_deleted]) VALUES
('100 Main St', 'Suite 200', 'Boston', 'MA', '02108', 'FACILITY', 0),
('250 Care Dr', NULL, 'Los Angeles', 'CA', '90001', 'FACILITY', 0),
('12 Pine St', NULL, 'Boston', 'MA', '02109', 'HOME', 0);
GO

-- 4. Insert Sample Facilities
INSERT INTO [facilities] ([facility_code], [name], [license_number], [target_state], [address_id], [phone_number], [is_deleted]) VALUES
('FAC-BOS-01', 'Boston Senior Living', 'LIC-BOS-001', 'MA', 1, '1-617-555-0100', 0),
('FAC-LA-01', 'LA Memory Care Center', 'LIC-LA-002', 'CA', 2, '1-213-555-0200', 0);
GO

-- 5. Insert Sample Rooms
INSERT INTO [rooms] ([room_number], [room_type], [facility_id], [is_deleted]) VALUES
('101', 'Semi-Private', 1, 0),
('102', 'Private', 1, 0),
('201', 'Memory Suite', 2, 0);
GO

-- 6. Insert Sample Beds
INSERT INTO [beds] ([bed_number], [status], [room_id]) VALUES
('101-A', 'AVAILABLE', 1),
('101-B', 'AVAILABLE', 1),
('102-A', 'AVAILABLE', 2),
('201-A', 'AVAILABLE', 3);
GO

-- 7. Insert Sample Users (System_Administrator and default accounts for testing)
-- password hash is bcrypt of 'Nhms@Demo2026'
INSERT INTO [users] ([employee_code], [email], [password_hash], [first_name], [middle_name], [last_name], [license_number], [phone_number], [status], [mfa_enabled], [role_id], [is_deleted]) VALUES
('EMP-ADMIN-01', 'daniel.brooks@nhms-demo.local', '$2a$10$tZ2yD0L9lCq2Kj1q4.P5zOjUshN4XlW/PzG0UvM1Z9cZk/eG44w.2', 'Daniel', 'James', 'Brooks', 'LIC-ADM-01', '1-212-555-0143', 'ACTIVE', 0, 1, 0),
('EMP-DON-01', 'sarah.connor@nhms-demo.local', '$2a$10$tZ2yD0L9lCq2Kj1q4.P5zOjUshN4XlW/PzG0UvM1Z9cZk/eG44w.2', 'Sarah', NULL, 'Connor', 'LIC-RN-1234', '1-212-555-0155', 'ACTIVE', 0, 2, 0),
('EMP-ACCT-01', 'billing@nhms-demo.local', '$2a$10$tZ2yD0L9lCq2Kj1q4.P5zOjUshN4XlW/PzG0UvM1Z9cZk/eG44w.2', 'Jane', 'Doe', 'Billing', NULL, '1-212-555-0188', 'ACTIVE', 0, 4, 0);
GO
