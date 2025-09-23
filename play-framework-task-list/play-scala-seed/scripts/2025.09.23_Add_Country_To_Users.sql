ALTER TABLE users
    ADD country VARCHAR(10) DEFAULT 'other';
GO

UPDATE users
SET country = 'other'
WHERE country IS NULL;
GO

ALTER TABLE users
    ADD CONSTRAINT check_country_values
        CHECK (country IN ('tr','us','uk','de','fr','it','es','ca','au','jp',
                           'cn','in','br','ru','mx','nl','se','no','dk','fi','other'));
GO

CREATE INDEX idx_users_country ON users(country);
GO
