-- Donn�es de d�monstration pour les v�hicules
-- Script d'insertion pour la table vehicles

INSERT INTO vehicles (
    name, brand, model, license_plate, vin, type, status, fuel_type, 
    year_manufactured, mileage, max_payload, dimensions, 
    insurance_number, insurance_expiration, technical_control_expiration,
    last_maintenance_date, next_maintenance_date, maintenance_interval_km,
    purchase_date, purchase_price, daily_rental_rate,
    current_location, assigned_driver, notes, photo_path,
    created_at, updated_at
) VALUES 

-- Fourgons pour transport mat�riel
('Fourgon Principal', 'Mercedes', 'Sprinter 314', 'AB-123-CD', 'WDB9066331N123456', 'VAN', 'AVAILABLE', 'DIESEL',
 2020, 85000, 1500.00, '6.00 x 2.05 x 2.60',
 'ASS-2024-001', '2024-12-31', '2024-08-15',
 '2024-01-15', '2024-07-15', 15000,
 '2020-03-15', 45000.00, 150.00,
 'Entrep�t Principal', 'Pierre Martin', 'V�hicule principal pour transports mat�riel sc�ne', 'MOV160.jpg',
 '2024-01-10 08:00:00', '2024-01-10 08:00:00'),

('Fourgon Secours', 'Peugeot', 'Boxer L3H2', 'EF-456-GH', 'VF3ZDZYFZKS123789', 'VAN', 'AVAILABLE', 'DIESEL',
 2019, 92000, 1200.00, '5.41 x 2.05 x 2.52',
 'ASS-2024-002', '2024-11-30', '2024-06-20',
 '2023-12-10', '2024-06-10', 12000,
 '2019-06-20', 38000.00, 120.00,
 'Entrep�t Principal', '', 'V�hicule de secours et transports l�gers', 'MOV80.jpg',
 '2024-01-10 08:15:00', '2024-01-10 08:15:00'),

-- Camions pour gros mat�riel
('Camion Plateau', 'Iveco', 'Daily 70C18', 'IJ-789-KL', 'ZCFC470A005123456', 'TRUCK', 'AVAILABLE', 'DIESEL',
 2018, 145000, 3500.00, '7.20 x 2.55 x 2.90',
 'ASS-2024-003', '2025-01-31', '2024-09-10',
 '2024-02-01', '2024-08-01', 20000,
 '2018-04-10', 65000.00, 250.00,
 'Entrep�t Principal', 'Marc Dubois', 'Camion plateau pour gros mat�riel et structures', 'MOV60.jpg',
 '2024-01-10 08:30:00', '2024-01-10 08:30:00'),

('Camion Benne', 'Renault', 'Master Benne', 'MN-012-OP', 'VF1MA000156789123', 'TRUCK', 'MAINTENANCE', 'DIESEL',
 2017, 168000, 2800.00, '6.20 x 2.30 x 2.80',
 'ASS-2024-004', '2024-10-31', '2024-05-25',
 '2024-01-20', '2024-07-20', 15000,
 '2017-09-15', 52000.00, 200.00,
 'Garage Partenaire', 'Sophie Leroy', 'En r�vision compl�te - retour pr�vu fin semaine', 'BM-038-NY.jpg',
 '2024-01-10 08:45:00', '2024-01-10 08:45:00'),

-- Remorques
('Remorque Plateau 1', 'PTAC', 'RPL-500', 'QR-345-ST', 'FR1PTAC00500123456', 'TRAILER', 'AVAILABLE', 'OTHER',
 2021, 0, 5000.00, '8.00 x 2.55 x 0.40',
 'ASS-2024-005', '2025-02-28', '2024-11-15',
 '2024-01-05', '2024-07-05', 0,
 '2021-02-28', 15000.00, 80.00,
 'Entrep�t Principal', '', 'Remorque plateau pour structures lourdes', 'DL-622-TF.jpg',
 '2024-01-10 09:00:00', '2024-01-10 09:00:00'),

('Remorque Ferm�e', 'Humbaur', 'HK253118', 'UV-678-WX', 'EUHB25311812345678', 'TRAILER', 'AVAILABLE', 'OTHER',
 2020, 0, 2500.00, '5.00 x 2.05 x 2.10',
 'ASS-2024-006', '2024-12-15', '2024-10-20',
 '2023-11-20', '2024-05-20', 0,
 '2020-05-12', 18500.00, 90.00,
 'Entrep�t Principal', '', 'Remorque ferm�e pour mat�riel sensible', 'DQ-055-LG.jpg',
 '2024-01-10 09:15:00', '2024-01-10 09:15:00'),

-- V�hicules l�gers
('Voiture Service', 'Citro�n', 'Berlingo', 'YZ-901-AB', 'VF7KURHZGKJ123456', 'CAR', 'AVAILABLE', 'DIESEL',
 2022, 35000, 800.00, '4.40 x 1.85 x 1.88',
 'ASS-2024-007', '2025-03-31', '2025-01-10',
 '2023-10-15', '2024-04-15', 10000,
 '2022-01-20', 22000.00, 60.00,
 'Bureau', 'Julie Moreau', 'V�hicule pour d�placements commerciaux et petites livraisons', 'DS-377-RL.jpg',
 '2024-01-10 09:30:00', '2024-01-10 09:30:00'),

('Utilitaire Compact', 'Volkswagen', 'Caddy', 'CD-234-EF', 'WVWZZZ2KZD123456', 'CAR', 'RENTED_OUT', 'GASOLINE',
 2021, 48000, 600.00, '4.85 x 1.79 x 1.84',
 'ASS-2024-008', '2024-09-30', '2024-07-05',
 '2024-01-08', '2024-07-08', 8000,
 '2021-03-10', 25500.00, 70.00,
 'Location Externe', '', 'Lou� jusqu''au 15 f�vrier - retour pr�vu', 'DT-406-TJ.jpg',
 '2024-01-10 09:45:00', '2024-01-10 09:45:00'),

-- V�hicule �lectrique
('Fourgon �lectrique', 'Nissan', 'e-NV200', 'GH-567-IJ', 'SJNFBAAE0U0123456', 'VAN', 'AVAILABLE', 'ELECTRIC',
 2023, 12000, 700.00, '4.56 x 1.76 x 1.86',
 'ASS-2024-009', '2025-06-30', '2025-04-20',
 '2023-12-01', '2024-06-01', 5000,
 '2023-04-15', 35000.00, 110.00,
 'Entrep�t Principal', '', 'V�hicule �cologique pour livraisons urbaines', 'DT-692-RE.jpg',
 '2024-01-10 10:00:00', '2024-01-10 10:00:00'),

-- Moto pour urgences
('Moto Urgence', 'BMW', 'R1250GS', 'KL-890-MN', 'WB10AA110L123456', 'MOTORCYCLE', 'AVAILABLE', 'GASOLINE',
 2022, 15000, 50.00, '2.21 x 0.96 x 1.43',
 'ASS-2024-010', '2024-08-31', '2024-12-30',
 '2024-01-03', '2024-07-03', 3000,
 '2022-07-08', 18500.00, 40.00,
 'Bureau', 'Alex Rousseau', 'Moto pour interventions rapides et rep�rages', 'EB-855-VR.jpg',
 '2024-01-10 10:15:00', '2024-01-10 10:15:00');
