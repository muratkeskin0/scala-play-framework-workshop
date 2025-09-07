-- Migration: Update USERS table to use EMAIL instead of USERNAME
-- Date: 2025-01-15

-- Add EMAIL column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('USERS') AND name = 'EMAIL')
BEGIN
ALTER TABLE USERS ADD EMAIL NVARCHAR(255) NULL;
END

-- Migrate existing data from USERNAME to EMAIL (if any)
UPDATE USERS
SET EMAIL = USERNAME + '@example.com'
WHERE EMAIL IS NULL AND USERNAME IS NOT NULL;

-- Make EMAIL column NOT NULL and UNIQUE
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('USERS') AND name = 'EMAIL' AND is_nullable = 1)
BEGIN
ALTER TABLE USERS ALTER COLUMN EMAIL NVARCHAR(255) NOT NULL;
END

-- Add unique constraint on EMAIL
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('USERS') AND name = 'UQ_USERS_EMAIL')
BEGIN
ALTER TABLE USERS ADD CONSTRAINT UQ_USERS_EMAIL UNIQUE (EMAIL);
END

DECLARE @sql nvarchar(max);

-- Unique/PK constraints
SET @sql = N'';
SELECT @sql = STRING_AGG(N'ALTER TABLE dbo.USERS DROP CONSTRAINT ' + QUOTENAME(k.name) + N';', CHAR(10))
FROM sys.key_constraints k
         JOIN sys.index_columns ic ON ic.object_id = k.parent_object_id AND ic.index_id = k.unique_index_id
         JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE k.parent_object_id = OBJECT_ID('dbo.USERS') AND c.name = 'USERNAME';

IF @sql <> N'' EXEC(@sql);

-- Remove old USERNAME column (optional - comment out if you want to keep it for now)
ALTER TABLE USERS DROP COLUMN USERNAME;

-- Insert new test data with proper emails
IF NOT EXISTS (SELECT * FROM USERS WHERE EMAIL = 'admin@example.com')
INSERT INTO USERS (EMAIL, PASSWORD) VALUES ('admin@example.com', 'admin123');

IF NOT EXISTS (SELECT * FROM USERS WHERE EMAIL = 'test@example.com')
INSERT INTO USERS (EMAIL, PASSWORD) VALUES ('test@example.com', 'test123');
