-- Create social media info table
-- Migration: 2025.09.23_Create_Social_Media_Info_Table.sql

-- Create social_media_info table
CREATE TABLE social_media_info (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    country VARCHAR(10) NOT NULL,
    title NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Add constraint to ensure country values are valid
ALTER TABLE social_media_info ADD CONSTRAINT check_social_media_country_values 
CHECK (country IN ('tr', 'us', 'uk', 'de', 'fr', 'it', 'es', 'ca', 'au', 'jp', 'cn', 'in', 'br', 'ru', 'mx', 'nl', 'se', 'no', 'dk', 'fi', 'other'));

-- Add index for better query performance
CREATE INDEX idx_social_media_info_country ON social_media_info(country);
CREATE INDEX idx_social_media_info_active ON social_media_info(is_active);

-- Insert sample data for different countries
INSERT INTO social_media_info (country, title, content, is_active) VALUES
-- Turkey
('tr', 'Türkiye Sosyal Medya Kuralları', 
'<h3>Kişisel Verilerin Korunması Kanunu (KVKK)</h3>
<p>• Kişisel verilerinizi korumak için güçlü şifreler kullanın</p>
<p>• Sosyal medya hesaplarınızda gizlilik ayarlarınızı kontrol edin</p>
<p>• Üçüncü taraf uygulamaların erişim izinlerini düzenli olarak gözden geçirin</p>
<p>• Kişisel bilgilerinizi paylaşırken dikkatli olun</p>', 1),

-- United States
('us', 'US Social Media Guidelines', 
'<h3>Privacy and Data Protection</h3>
<p>• Use strong, unique passwords for all accounts</p>
<p>• Enable two-factor authentication where available</p>
<p>• Be cautious about sharing personal information</p>
<p>• Regularly review and update privacy settings</p>
<p>• Understand platform terms of service</p>', 1),

-- Germany
('de', 'Deutsche Social Media Richtlinien', 
'<h3>Datenschutz-Grundverordnung (DSGVO)</h3>
<p>• Ihre persönlichen Daten sind geschützt</p>
<p>• Verwenden Sie starke Passwörter</p>
<p>• Überprüfen Sie regelmäßig Ihre Datenschutzeinstellungen</p>
<p>• Seien Sie vorsichtig beim Teilen persönlicher Informationen</p>', 1),

-- United Kingdom
('uk', 'UK Social Media Guidelines', 
'<h3>Data Protection Act 2018</h3>
<p>• Keep your personal information secure</p>
<p>• Use strong passwords and enable 2FA</p>
<p>• Be mindful of what you share online</p>
<p>• Regularly review privacy settings</p>
<p>• Report suspicious activity immediately</p>', 1),

-- France
('fr', 'Règles des Médias Sociaux Français', 
'<h3>Règlement Général sur la Protection des Données (RGPD)</h3>
<p>• Protégez vos données personnelles</p>
<p>• Utilisez des mots de passe forts</p>
<p>• Vérifiez régulièrement vos paramètres de confidentialité</p>
<p>• Soyez prudent lors du partage d''informations personnelles</p>', 1),

-- General/Other
('other', 'General Social Media Guidelines', 
'<h3>Universal Best Practices</h3>
<p>• Use strong, unique passwords</p>
<p>• Enable two-factor authentication</p>
<p>• Be cautious about sharing personal information</p>
<p>• Regularly review privacy settings</p>
<p>• Keep your apps and devices updated</p>
<p>• Report suspicious activity</p>', 1);
