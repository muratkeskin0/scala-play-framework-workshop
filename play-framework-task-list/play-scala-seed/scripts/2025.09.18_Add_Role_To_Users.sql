-- 1. batch: kolonu ekle
ALTER TABLE USERS
    ADD [ROLE] VARCHAR(10) NOT NULL DEFAULT 'basic';
GO

-- 2. batch: kolonla işlem yap
UPDATE USERS
SET [ROLE] = 'basic'
WHERE [ROLE] IS NULL;

ALTER TABLE USERS
    ADD CONSTRAINT CHK_USERS_ROLE
        CHECK ([ROLE] IN ('admin', 'basic'));

CREATE INDEX IX_USERS_ROLE ON USERS ([ROLE]);
