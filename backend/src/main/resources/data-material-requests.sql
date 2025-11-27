-- Demandes de materiel avec workflow de validation
INSERT INTO material_requests (id, request_number, requester_name, requester_email, requester_department, context, urgency, description, justification, delivery_address, delivery_date, status, approved_by, approved_at, submitted_at, created_at, updated_at)
VALUES
(1, 'REQ-2024-001', 'Jean Dupont', 'jean.dupont@magscene.fr', 'Son', 'EVENT', 'NORMAL', 
 'Micros HF supplementaires pour conferences',
 'Besoin de micros supplementaires pour les conferences du Festival International.', 
 'Mag Scene - 123 rue Principale 75000 Paris', '2024-12-15', 'PENDING_APPROVAL', null, null,
 '2024-11-20 14:30:00', '2024-11-20 14:30:00', '2024-11-20 14:30:00'),
(2, 'REQ-2024-002', 'Marie Lambert', 'marie.lambert@magscene.fr', 'Structure', 'MAINTENANCE', 'HIGH',
 'Moteurs defectueux a remplacer',
 'Remplacement moteurs defectueux pour tournee theatre.', 
 'Mag Scene - 123 rue Principale 75000 Paris', '2024-12-01', 'APPROVED', 'Sophie Directrice', '2024-11-19 16:45:00',
 '2024-11-18 09:15:00', '2024-11-18 09:15:00', '2024-11-19 16:45:00'),
(3, 'REQ-2024-003', 'Pierre Martin', 'pierre.martin@magscene.fr', 'Lumiere', 'SALES', 'LOW',
 'Projecteurs LED pour nouveau projet',
 'Extension parc pour nouveau projet eclairage architectural.',
 'Mag Scene - 123 rue Principale 75000 Paris', '2025-01-15', 'DRAFT', null, null,
 '2024-11-22 11:00:00', '2024-11-22 11:00:00', '2024-11-22 11:00:00'),
(4, 'REQ-2024-004', 'Sophie Durand', 'sophie.durand@magscene.fr', 'Video', 'STOCK', 'NORMAL',
 'Cables SDI et connecteurs',
 'Renouvellement stock cables pour regie video.',
 'Mag Scene - 123 rue Principale 75000 Paris', '2024-12-20', 'PENDING_APPROVAL', null, null,
 '2024-11-21 13:45:00', '2024-11-21 13:45:00', '2024-11-21 13:45:00'),
(5, 'REQ-2024-005', 'Luc Fernandez', 'luc.fernandez@magscene.fr', 'Son', 'MAINTENANCE', 'URGENT',
 'Kits maintenance micros instrumentaux',
 'Kits maintenance pour micros instrumentaux utilises en tournee.',
 'Mag Scene - 123 rue Principale 75000 Paris', '2024-12-10', 'APPROVED', 'Marc Chef SAV', '2024-11-20 08:20:00',
 '2024-11-19 10:30:00', '2024-11-19 10:30:00', '2024-11-20 08:20:00');

-- Items associes aux demandes
INSERT INTO material_request_items (id, material_request_id, free_reference, free_name, free_description, free_brand, free_model, requested_quantity, unit, estimated_price, currency, quantity_allocated, quantity_delivered, specifications, alternatives_accepted, priority, notes, created_at, updated_at)
VALUES
-- REQ-2024-001: Micros HF
(1, 1, 'ULXD24/58', 'Microphone HF Shure ULXD24/58', 'Emetteur main numerique avec capsule SM58', 'Shure', 'ULXD24/58', 4, 'unite', 850.00, 'EUR', 0, 0, 
 'Frequence UHF, compatible avec recepteurs ULXD4', true, 1, 'Capsules SM58 ou Beta 87A acceptees', '2024-11-20 14:30:00', '2024-11-20 14:30:00'),

-- REQ-2024-002: Moteurs
(2, 2, 'CM-500', 'Moteur Chain Master 500kg', 'Palan a chaine electrique 500kg', 'Chain Master', 'CM-500', 2, 'unite', 1200.00, 'EUR', 2, 0,
 'Vitesse 4m/min, chaine 6mm', false, 1, 'Doit etre compatible structures Prolyte', '2024-11-18 09:15:00', '2024-11-18 09:15:00'),

-- REQ-2024-003: Projecteurs LED
(3, 3, 'ZENIT-W600', 'Cameo Zenit W600', 'Projecteur LED blanc froid 600W', 'Cameo', 'Zenit W600', 8, 'unite', 450.00, 'EUR', 0, 0,
 '6000K, zoom motorise 10-60°', true, 2, 'Alternatives Robe ou Martin acceptees', '2024-11-22 11:00:00', '2024-11-22 11:00:00'),

-- REQ-2024-004: Cables SDI
(4, 4, 'L-4.5CHWS-15', 'Cable SDI Canare 15m', 'Cable video SDI 75 Ohms', 'Canare', 'L-4.5CHWS', 20, 'unite', 25.00, 'EUR', 0, 0,
 'BNC 75 Ohms, blindage double', true, 3, 'Commander aussi 20 connecteurs BNC', '2024-11-21 13:45:00', '2024-11-21 13:45:00'),

-- REQ-2024-005: Kits maintenance
(5, 5, 'DPA-4098-MK', 'Kit entretien DPA 4098', 'Kit maintenance micros instrumentaux', 'DPA', '4098-MAINT', 3, 'kit', 80.00, 'EUR', 3, 0,
 'Bonnettes, pinces, adaptateurs', false, 2, 'Inclure aussi 6 pinces de rechange', '2024-11-19 10:30:00', '2024-11-19 10:30:00');