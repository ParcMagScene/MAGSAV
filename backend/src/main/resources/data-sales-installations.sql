-- Données de test pour le module Ventes & Installations
-- Insertion des projets de démonstration

-- Vérifier si la table existe et est vide
DELETE FROM supplier_order_items WHERE supplier_order_id IN (SELECT id FROM supplier_orders);
DELETE FROM supplier_orders WHERE id > 0;
DELETE FROM projects WHERE id > 0;

-- Projets de démonstration
INSERT INTO projects (
    project_number, name, type, status, priority, description,
    client_name, client_contact, client_email, client_phone, client_address,
    start_date, end_date, installation_date, delivery_date,
    estimated_amount, final_amount, deposit_amount, remaining_amount,
    venue, venue_address, venue_contact,
    project_manager, technical_manager, sales_representative,
    notes, technical_notes, client_requirements,
    created_at, updated_at
) VALUES
-- Projet 1: Festival de musique électronique
('PRJ-2024-0001', 'Festival Électro Summer 2024', 'EVENT', 'IN_PROGRESS', 'HIGH',
'Sonorisation complète pour festival de musique électronique sur 3 jours, 20 000 personnes attendues.',
'Festival Productions SAS', 'Marie Dubois', 'marie.dubois@festival-prod.com', '06.12.34.56.78',
'15 rue de la Musique, 75009 Paris',
'2024-06-01', '2024-07-15', '2024-07-10', '2024-07-08',
95000.00, 98500.00, 30000.00, 68500.00,
'Parc de la Villette', '211 Avenue Jean Jaurès, 75019 Paris', 'Jean Martin - 06.98.76.54.32',
'Alexis Moreau', 'Thierry Dubois', 'Sophie Bernard',
'Client très exigeant sur la qualité sonore. Prévoir tests approfondis.',
'Configuration Line Array + Subwoofers enterrés. Attention aux contraintes météo.',
'Système de sonorisation premium, éclairage LED synchronisé, écrans géants 4K.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Projet 2: Mariage de luxe
('PRJ-2024-0002', 'Mariage Château de Versailles', 'SALE', 'QUOTED', 'MEDIUM',
'Sonorisation et éclairage pour mariage haut de gamme dans les jardins du château.',
'M. et Mme Delacroix', 'Pierre Delacroix', 'p.delacroix@luxury-events.fr', '01.45.67.89.01',
'25 Avenue Foch, 75016 Paris',
'2024-08-15', '2024-08-15', '2024-08-14', '2024-08-13',
25000.00, NULL, 8000.00, 17000.00,
'Château de Versailles', 'Place d''Armes, 78000 Versailles', 'Catherine Leroy - 06.55.44.33.22',
'Sophie Bernard', 'Marc Antoine', 'Julie Lecomte',
'Mariage dans les jardins à la française. Très beau projet.',
'Sonorisation discrète, éclairage romantique, pas d''impact sur les jardins historiques.',
'Sonorisation invisible, éclairage féerique, respect du patrimoine historique.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Projet 3: Conférence corporate
('PRJ-2024-0003', 'Congrès Médical International', 'INSTALLATION', 'CONFIRMED', 'HIGH',
'Installation complète A/V pour congrès médical de 3 jours, 1200 participants.',
'Association Médicale Française', 'Dr. Laurent Petit', 'l.petit@amf-congres.org', '01.42.33.44.55',
'12 rue des Médecins, 75005 Paris',
'2024-09-10', '2024-09-13', '2024-09-09', '2024-09-08',
45000.00, 47200.00, 15000.00, 32200.00,
'Palais des Congrès Porte Maillot', '2 Place de la Porte Maillot, 75017 Paris', 'Michel Rousseau - 06.77.88.99.00',
'Alexis Moreau', 'Laurent Technician', 'Sophie Bernard',
'Congrès prestigieux avec diffusion en direct.',
'Multi-room A/V, streaming HD, interprétariat simultané en 5 langues.',
'Qualité broadcast, enregistrement HD, transmission satellite.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Projet 4: Location courte durée
('PRJ-2024-0004', 'Séminaire Entreprise TechCorp', 'RENTAL', 'COMPLETED', 'LOW',
'Location matériel A/V pour séminaire d''entreprise de 2 jours.',
'TechCorp Solutions', 'Sandrine Moreau', 's.moreau@techcorp.com', '01.98.76.54.32',
'42 Avenue des Champs-Elysées, 75008 Paris',
'2024-05-15', '2024-05-17', '2024-05-14', '2024-05-14',
8500.00, 8200.00, 2500.00, 5700.00,
'Hôtel Marriott Champs-Elysées', '70 Avenue des Champs-Elysées, 75008 Paris', 'Hôtel Reception - 01.53.93.55.00',
'Marc Antoine', 'Thierry Dubois', 'Julie Lecomte',
'Client régulier, facturation rapide.',
'Setup standard corporate, micros HF, écrans plasma.',
'Sonorisation claire pour 80 personnes, projection HD, micros sans fil.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Projet 5: Maintenance préventive
('PRJ-2024-0005', 'Maintenance Salle Pleyel', 'MAINTENANCE', 'IN_PROGRESS', 'MEDIUM',
'Maintenance préventive annuelle du système de sonorisation de la Salle Pleyel.',
'Salle Pleyel', 'Directeur Technique', 'technique@sallepleyel.fr', '01.42.56.13.13',
'252 rue du Faubourg Saint-Honoré, 75008 Paris',
'2024-07-01', '2024-07-05', '2024-06-30', '2024-07-01',
15000.00, NULL, 5000.00, 10000.00,
'Salle Pleyel', '252 rue du Faubourg Saint-Honoré, 75008 Paris', 'Régisseur Général - 01.42.56.13.14',
'Laurent Technician', 'Thierry Dubois', 'Alexis Moreau',
'Maintenance critique pour la saison suivante.',
'Révision complète système Meyer Sound, recalibrage, mise à jour firmware.',
'Maintenance préventive complète, tests acoustiques, certification.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Projet 6: Vente d'équipement
('PRJ-2024-0006', 'Vente Studio Enregistrement Pro', 'SALE', 'DRAFT', 'MEDIUM',
'Vente et installation complète d''un studio d''enregistrement professionnel.',
'Sound Records Studio', 'Anthony Music', 'a.music@soundrecords.com', '06.11.22.33.44',
'88 rue de la Musique, 75020 Paris',
'2024-08-01', '2024-09-30', '2024-09-15', '2024-09-10',
150000.00, NULL, 45000.00, 105000.00,
'Sound Records Studio', '88 rue de la Musique, 75020 Paris', 'Anthony Music - 06.11.22.33.44',
'Sophie Bernard', 'Marc Antoine', 'Julie Lecomte',
'Gros projet de vente, marge importante.',
'Console SSL, monitoring Genelec, acoustique sur-mesure.',
'Studio professionnel complet, isolation acoustique, équipement haut de gamme.',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Commandes fournisseurs de démonstration
INSERT INTO supplier_orders (
    order_number, type, status, supplier_name, supplier_contact, supplier_email, supplier_phone, supplier_address,
    description, purchase_reason, ordered_by, approved_by,
    order_date, expected_delivery_date, actual_delivery_date, approval_date,
    amount_ht, amount_ttc, vat_rate, shipping_cost, payment_terms, payment_method,
    delivery_address, delivery_contact, delivery_phone, tracking_number, carrier_name,
    notes, internal_notes, created_at, updated_at
) VALUES
-- Commande 1: Matériel sonorisation
('CMD-2024-00001', 'EQUIPMENT', 'ORDERED', 'Meyer Sound Europe', 'Commercial Meyer', 'sales@meyersound.eu', '+33.1.41.83.11.00',
'5 rue de l''Industrie, 93200 Saint-Denis',
'Commande enceintes Line Array pour projet Festival Électro Summer 2024',
'Matériel principal pour projet festival musique électronique',
'Sophie Bernard', 'Alexis Moreau',
'2024-05-15', '2024-06-25', NULL, '2024-05-16',
65000.00, 78000.00, 20.00, 1200.00, 'Paiement à 30 jours', 'Virement bancaire',
'Entrepôt Mag Scène, 15 rue de l''Entrepôt, 93100 Montreuil', 'Thierry Dubois', '06.12.34.56.78',
'MS2024FR0156', 'DHL Express',
'Commande urgente pour festival été',
'Vérifier compatibilité avec matériel existant avant réception',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Commande 2: Éclairage LED
('CMD-2024-00002', 'EQUIPMENT', 'RECEIVED', 'Robe Lighting France', 'Service Commercial', 'france@robe.cz', '+33.1.48.87.64.44',
'12 Avenue de l''Éclairage, 77100 Meaux',
'Projecteurs LED dernière génération pour mariage château',
'Éclairage haut de gamme pour événement prestigieux',
'Julie Lecomte', 'Sophie Bernard',
'2024-06-01', '2024-07-15', '2024-07-12', '2024-06-02',
18500.00, 22200.00, 20.00, 450.00, 'Paiement comptant', 'Carte bancaire entreprise',
'Entrepôt Mag Scène, 15 rue de l''Entrepôt, 93100 Montreuil', 'Marc Antoine', '06.98.76.54.32',
'ROBE2024FR789', 'Chronopost',
'Matériel reçu en parfait état',
'Tests effectués - conforme aux spécifications',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Commande 3: Consommables
('CMD-2024-00003', 'SUPPLIES', 'PARTIALLY_RECEIVED', 'Thomann France', 'Service Clients', 'info@thomann.fr', '03.26.77.40.00',
'Zone Industrielle, 51470 Saint-Memmie',
'Câbles, fiches, consommables divers',
'Réapprovisionnement stock consommables',
'Marc Antoine', 'Thierry Dubois',
'2024-06-20', '2024-07-05', NULL, '2024-06-21',
2800.00, 3360.00, 20.00, 85.00, 'Paiement à réception', 'Prélèvement automatique',
'Entrepôt Mag Scène, 15 rue de l''Entrepôt, 93100 Montreuil', 'Thierry Dubois', '06.12.34.56.78',
'TH240621FR456', 'La Poste Colissimo',
'Livraison partielle reçue - manque câbles XLR',
'Contacter fournisseur pour complément livraison',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Commande 4: Service de maintenance
('CMD-2024-00004', 'SERVICE', 'APPROVED', 'TechniSON Services', 'Département Maintenance', 'maintenance@technison.fr', '01.49.88.77.66',
'33 rue de la Réparation, 92400 Courbevoie',
'Contrat maintenance préventive annuelle',
'Maintenance préventive matériel critique',
'Thierry Dubois', 'Alexis Moreau',
'2024-07-01', '2024-07-30', NULL, '2024-07-02',
12000.00, 14400.00, 20.00, 0.00, 'Paiement mensuel', 'Prélèvement automatique',
'Sur site clients selon planning', 'Thierry Dubois', '06.12.34.56.78',
NULL, NULL,
'Contrat annuel renouvelable',
'Inclut tous déplacements et main d''œuvre',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Articles de commandes fournisseurs
INSERT INTO supplier_order_items (
    supplier_order_id, item_reference, item_name, description, quantity, quantity_received,
    unit, unit_price, total_amount, notes
) VALUES
-- Articles commande Meyer Sound
(1, 'LEOPARD-M100', 'Meyer Sound LEOPARD M100', 'Enceinte Line Array compacte', 12, 0, 'pcs', 4200.00, 50400.00, 'Enceintes principales'),
(1, 'LYON-M', 'Meyer Sound LYON-M', 'Enceinte Line Array moyenne portée', 8, 0, 'pcs', 6800.00, 54400.00, 'Enceintes secondaires'),
(1, 'FLIGHT-CASE', 'Flight case sur mesure', 'Protection transport enceintes', 4, 0, 'pcs', 850.00, 3400.00, 'Protection matériel'),

-- Articles commande Robe Lighting  
(2, 'ROBE-T2-PROFILE', 'Robe T2 Profile', 'Projecteur LED profil haute puissance', 16, 16, 'pcs', 980.00, 15680.00, 'Éclairage principal'),
(2, 'ROBE-T1-FRESNEL', 'Robe T1 Fresnel', 'Projecteur LED Fresnel', 8, 8, 'pcs', 750.00, 6000.00, 'Éclairage d''ambiance'),
(2, 'ROBE-CASE', 'Case de transport Robe', 'Flight case projecteurs', 6, 6, 'pcs', 320.00, 1920.00, 'Transport sécurisé'),

-- Articles commande Thomann
(3, 'CABLE-XLR-5M', 'Câble XLR 3 broches 5m', 'Câble microphone professionnel', 50, 30, 'pcs', 18.50, 925.00, 'Manque 20 pièces'),
(3, 'CABLE-JACK-3M', 'Câble Jack 6.35 mono 3m', 'Câble instrument', 25, 25, 'pcs', 12.00, 300.00, 'Livraison complète'),
(3, 'ADAPTATEUR-XLR', 'Adaptateur XLR/Jack', 'Adaptateur connexion', 20, 20, 'pcs', 8.50, 170.00, 'Livraison complète'),
(3, 'GAFFER-NOIR', 'Gaffer tape noir 50mm', 'Adhésif professionnel', 10, 10, 'rouleaux', 15.00, 150.00, 'Stock reconstitué'),

-- Articles service maintenance
(4, 'MAINT-PREV-FULL', 'Maintenance préventive complète', 'Service annuel maintenance', 1, 0, 'contrat', 12000.00, 12000.00, 'Contrat 12 mois');

COMMIT;

-- Statistiques après insertion
SELECT 'PROJETS CRÉÉS' as type, COUNT(*) as count FROM projects
UNION ALL
SELECT 'COMMANDES FOURNISSEURS', COUNT(*) FROM supplier_orders  
UNION ALL
SELECT 'ARTICLES COMMANDÉS', COUNT(*) FROM supplier_order_items;