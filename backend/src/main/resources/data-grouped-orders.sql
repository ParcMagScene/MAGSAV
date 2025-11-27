-- Commandes groupees pour optimiser seuils de franco
INSERT INTO grouped_orders (id, order_number, supplier_id, status, current_amount, threshold_alert_sent, auto_validate_on_threshold, validated_by, validated_at, validation_notes, delivery_address, delivery_date, urgency, notes, created_at, updated_at)
VALUES
(1, 'GRP-2025-001', 1, 'VALIDATED', 485.50, true, false, 'Marc Chef Achat', '2025-01-16', 
 'Franco atteint 97% - Commande Algam N ALG-556677',
 'Mag Scene - 123 rue Principale 75000 Paris', '2025-01-22', 'NORMAL',
 'Regroupement 3 demandes - Optimisation reussie', '2025-01-15 09:00:00', '2025-01-16 10:30:00'),

(2, 'GRP-2025-002', 2, 'THRESHOLD_REACHED', 356.00, true, false, null, null,
 'Seuil depasse 118% - Validation en attente',
 'Mag Scene - 123 rue Principale 75000 Paris', '2025-01-27', 'NORMAL',
 'Groupage batteries - Seuil largement depasse', '2025-01-20 14:15:00', '2025-01-20 14:15:00'),

(3, 'GRP-2025-003', 3, 'ORDERED', 8750.00, true, false, 'Sophie Directrice', '2025-01-13',
 'Montant eleve - Validation directrice requise',
 'Mag Scene - 123 rue Principale 75000 Paris', '2025-02-15', 'URGENT',
 'Commande lyres - Delai 4 semaines - Tracking: TH-889944', '2025-01-12 11:00:00', '2025-01-14 16:00:00'),

(4, 'GRP-2025-004', 5, 'OPEN', 87.50, false, true, null, null,
 'Seuil 58% - Attendre autres besoins',
 'Mag Scene - 123 rue Principale 75000 Paris', null, 'LOW',
 'Cables - ATTENDRE pour optimiser franco', '2025-01-18 08:30:00', '2025-01-18 08:30:00'),

(5, 'GRP-2025-005', 3, 'VALIDATED', 1020.00, true, false, 'Marc Chef Achat', '2025-01-19',
 'Seuil atteint 102% - Consommables Thomann',
 'Mag Scene - 123 rue Principale 75000 Paris', '2025-01-25', 'NORMAL',
 'Regroupement 4 demandes consommables', '2025-01-18 13:45:00', '2025-01-19 09:20:00');