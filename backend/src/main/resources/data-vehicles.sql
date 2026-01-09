-- Données de test pour les véhicules DÉSACTIVÉES
-- Les véhicules réels sont importés depuis le CSV LOCMAT via l'API
-- Voir scripts/import-vehicles-csv.ps1

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
