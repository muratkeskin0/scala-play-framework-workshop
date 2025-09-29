-- Create law info table
-- Migration: 2025.09.23_Create_Law_Info_Table.sql

-- Create law_info table
CREATE TABLE law_info (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    country VARCHAR(10) NOT NULL,
    title NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Add constraint to ensure country values are valid
ALTER TABLE law_info ADD CONSTRAINT check_law_country_values 
CHECK (country IN ('tr', 'us', 'uk', 'de', 'fr', 'it', 'es', 'ca', 'au', 'jp', 'cn', 'in', 'br', 'ru', 'mx', 'nl', 'se', 'no', 'dk', 'fi', 'other'));

-- Add index for better query performance
CREATE INDEX idx_law_info_country ON law_info(country);
CREATE INDEX idx_law_info_active ON law_info(is_active);

-- Insert sample data for different countries
INSERT INTO law_info (country, title, content, is_active) VALUES
-- Turkey
('tr', 'Türkiye Hukuki Düzenlemeleri', 
'<h3>Kişisel Verilerin Korunması Kanunu (KVKK)</h3>
<p>• Kişisel verilerin toplanması, işlenmesi ve saklanması kuralları</p>
<p>• Veri sahiplerinin hakları ve yükümlülükleri</p>
<p>• Veri ihlali durumunda bildirim yükümlülükleri</p>
<p>• Kişisel veri güvenliği önlemleri</p>
<p>• Veri işleme faaliyetlerinin kayıt altına alınması</p>', 1),

-- United States
('us', 'US Legal Regulations', 
'<h3>Federal and State Laws</h3>
<p>• Privacy laws and data protection regulations</p>
<p>• Consumer protection laws</p>
<p>• Employment law requirements</p>
<p>• Intellectual property rights</p>
<p>• Regulatory compliance requirements</p>', 1),

-- Germany
('de', 'Deutsche Rechtliche Bestimmungen', 
'<h3>Bundesdatenschutzgesetz (BDSG)</h3>
<p>• Datenschutz und Informationsfreiheit</p>
<p>• Verbraucherschutzgesetze</p>
<p>• Arbeitsrechtliche Bestimmungen</p>
<p>• Urheberrecht und geistiges Eigentum</p>
<p>• Compliance und Regulierungsanforderungen</p>', 1),

-- United Kingdom
('uk', 'UK Legal Framework', 
'<h3>UK Laws and Regulations</h3>
<p>• Data Protection Act 2018</p>
<p>• Consumer Rights Act 2015</p>
<p>• Employment Rights Act 1996</p>
<p>• Copyright, Designs and Patents Act 1988</p>
<p>• Regulatory compliance requirements</p>', 1),

-- France
('fr', 'Cadre Juridique Français', 
'<h3>Règlement Général sur la Protection des Données (RGPD)</h3>
<p>• Protection des données personnelles</p>
<p>• Droits des consommateurs</p>
<p>• Code du travail français</p>
<p>• Propriété intellectuelle</p>
<p>• Conformité réglementaire</p>', 1),

-- General/Other
('other', 'General Legal Framework', 
'<h3>Universal Legal Principles</h3>
<p>• Data protection and privacy rights</p>
<p>• Consumer protection laws</p>
<p>• Employment and labor rights</p>
<p>• Intellectual property protection</p>
<p>• Regulatory compliance standards</p>
<p>• Legal liability and risk management</p>', 1);
