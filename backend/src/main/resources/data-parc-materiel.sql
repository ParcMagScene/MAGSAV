-- Insertion de catégories hiérarchiques de test pour MAGSAV 3.0
-- Parc matériel avec système de catégories avancé

INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
-- Catégories racines
('Éclairage', 'Matériel d''éclairage scénique et architectural', '#FF6B35', 'lightbulb-outline', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Son', 'Équipement audio et sonorisation', '#4ECDC4', 'volume-high', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Vidéo', 'Matériel vidéo et projection', '#45B7D1', 'video', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Structure', 'Structures scéniques et supports', '#96CEB4', 'hammer-wrench', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Transport', 'Matériel de transport et logistique', '#FECA57', 'truck', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- Sous-catégories Éclairage
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Projecteurs', 'Projecteurs d''éclairage scénique', '#FF8C42', 'spotlight-beam', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('Consoles', 'Consoles d''éclairage et contrôleurs', '#FF6B35', 'mixer', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('Accessoires éclairage', 'Câbles, filtres, accessoires divers', '#D63031', 'cable-data', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- Sous-catégories Son
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Micros', 'Microphones et accessoires', '#00B894', 'microphone', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
('Enceintes', 'Haut-parleurs et systèmes diffusion', '#00CEC9', 'speaker', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
('Consoles audio', 'Tables de mixage et interfaces', '#0984E3', 'tune-vertical', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2);

-- Sous-catégories Vidéo  
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Projecteurs vidéo', 'Vidéoprojecteurs et écrans', '#6C5CE7', 'projector', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
('Caméras', 'Caméras et équipement de prise de vue', '#A29BFE', 'camera', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
('Régie vidéo', 'Mélangeurs et équipement de régie', '#74B9FF', 'television', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3);

-- Sous-sous-catégories pour plus de détail (LED dans Projecteurs)
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('LED', 'Projecteurs LED', '#E17055', 'led-strip-variant', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
('Traditionnels', 'Projecteurs halogène et tungstène', '#FDCB6E', 'lightbulb', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
('Motorisés', 'Projecteurs automatiques et lyre', '#E84393', 'rotate-3d-variant', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6);

-- Mise à jour des équipements existants avec les nouvelles catégories
UPDATE equipment SET category_id = 14 WHERE name LIKE '%LED%' OR category = 'Projecteur LED';
UPDATE equipment SET category_id = 15 WHERE category = 'Projecteur traditionnel' OR brand LIKE '%halogène%';
UPDATE equipment SET category_id = 16 WHERE name LIKE '%lyre%' OR name LIKE '%motorisé%';
UPDATE equipment SET category_id = 9 WHERE category LIKE '%micro%' OR name LIKE '%micro%';
UPDATE equipment SET category_id = 10 WHERE category LIKE '%enceinte%' OR name LIKE '%speaker%';
UPDATE equipment SET category_id = 12 WHERE category LIKE '%projecteur%' AND category NOT LIKE '%LED%';
UPDATE equipment SET category_id = 1 WHERE category_id IS NULL AND (category LIKE '%éclairage%' OR category LIKE '%projecteur%');
UPDATE equipment SET category_id = 2 WHERE category_id IS NULL AND category LIKE '%son%';

-- Ajout d'équipements de test avec les nouvelles fonctionnalités
INSERT INTO equipment (name, description, category, status, qr_code, brand, model, serial_number, purchase_price, purchase_date, created_at, updated_at, category_id, location, notes, internal_reference, weight, dimensions, warranty_expiration, supplier, insurance_value, sub_category, specific_category, quantity_in_stock) VALUES

-- Éclairage LED
('Projecteur LED RGBW 200W', 'Projecteur LED haute puissance avec contrôle RGBW', 'Projecteur LED', 'AVAILABLE', 'MAG-LED-001', 'Chauvet', 'COLORado 2-Quad Zoom', 'CZ2023001', 1250.00, '2024-01-15 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 'Hangar A - Rack 3', 'Excellent état, révision 2024', 'LED-001', 8.5, '25 x 25 x 35 cm', '2027-01-15 00:00:00', 'Algam Entreprises', 1500.00, 'Éclairage', 'Projecteur LED', 2),

('Barre LED 12x12W', 'Barre de projecteurs LED avec contrôle pixel', 'Projecteur LED', 'AVAILABLE', 'MAG-LED-002', 'ADJ', 'Ultra Bar 12', 'UB240002', 890.00, '2024-02-20 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 'Hangar A - Rack 3', 'Neuf, jamais utilisé', 'LED-002', 4.2, '100 x 8 x 12 cm', '2027-02-20 00:00:00', 'Algam Entreprises', 1000.00, 'Éclairage', 'Barre LED', 4),

-- Lyres motorisées  
('Lyre LED Beam 230W', 'Lyre à faisceau LED haute puissance', 'Projecteur motorisé', 'AVAILABLE', 'MAG-LYR-001', 'Martin', 'MAC Viper AirFX', 'MV2024001', 4500.00, '2024-03-10 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 16, 'Hangar B - Zone lyres', 'Révision complète effectuée', 'LYR-001', 28.0, '42 x 52 x 75 cm', '2027-03-10 00:00:00', 'Martin Professional', 5500.00, 'Éclairage', 'Lyre Beam', 1),

('Lyre Wash LED 19x40W', 'Lyre wash LED zoom avec contrôle individuel', 'Projecteur motorisé', 'MAINTENANCE', 'MAG-LYR-002', 'Clay Paky', 'Mythos2', 'CP2024005', 3200.00, '2024-01-25 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 16, 'Atelier réparation', 'Moteur PAN en révision', 'LYR-002', 22.5, '40 x 48 x 70 cm', '2027-01-25 00:00:00', 'DTS Lighting', 4000.00, 'Éclairage', 'Lyre Wash', 3),

-- Micros et son
('Micro-cravate sans fil', 'Système HF cravate numérique', 'Microphone', 'AVAILABLE', 'MAG-MIC-001', 'Shure', 'GLXD14/85', 'SH2024010', 480.00, '2024-04-05 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9, 'Régie son - Tiroir 2', 'Fréquence : 2.4GHz, portée 30m', 'MIC-001', 0.8, '15 x 8 x 3 cm', '2026-04-05 00:00:00', 'Sonovente', 600.00, 'Audio', 'Microphone HF', 6),

('Enceinte line array', 'Module line array 3 voies amplifiée', 'Enceinte', 'AVAILABLE', 'MAG-SPK-001', 'L-Acoustics', 'A15 Focus', 'LA2024008', 8900.00, '2024-02-12 00:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 'Hangar C - Fly case', 'Configuration cluster disponible', 'SPK-001', 45.0, '65 x 43 x 39 cm', '2029-02-12 00:00:00', 'L-Acoustics', 12000.00, 'Audio', 'Enceinte Line Array', 8);

-- Insertion de quelques photos de test (sans fichiers physiques pour le moment)
INSERT INTO equipment_photos (equipment_id, file_name, file_path, file_size, mime_type, description, is_primary, created_at, updated_at) VALUES
(1, 'projecteur-led-face.jpg', '/uploads/equipment/1/', 256000, 'image/jpeg', 'Vue de face du projecteur LED', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'projecteur-led-side.jpg', '/uploads/equipment/1/', 189000, 'image/jpeg', 'Vue de profil', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'barre-led-setup.jpg', '/uploads/equipment/2/', 312000, 'image/jpeg', 'Barre LED en configuration', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'lyre-beam-action.jpg', '/uploads/equipment/3/', 445000, 'image/jpeg', 'Lyre en action lors d''un spectacle', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'manuel-lyre.pdf', '/uploads/equipment/3/', 2100000, 'application/pdf', 'Manuel d''utilisation complet', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

