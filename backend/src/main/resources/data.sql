-- Test data for MAGSAV-3.0 - Categories system
-- Simple categories without encoding issues

-- Root categories
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Eclairage', 'Materiel eclairage scenique et architectural', '#FF6B35', 'lightbulb-outline', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Son', 'Equipement audio et sonorisation', '#4ECDC4', 'volume-high', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Video', 'Materiel video et projection', '#45B7D1', 'video', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Structure', 'Structures sceniques et supports', '#96CEB4', 'hammer-wrench', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
('Transport', 'Materiel de transport et logistique', '#FECA57', 'truck', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- Sub-categories Eclairage
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Projecteurs', 'Projecteurs eclairage scenique', '#FF8C42', 'spotlight-beam', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('Consoles', 'Consoles eclairage et controleurs', '#FF6B35', 'mixer', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('Accessoires eclairage', 'Cables, filtres, accessoires divers', '#D63031', 'cable-data', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- Sub-categories Son
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Micros', 'Microphones et accessoires', '#00B894', 'microphone', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
('Enceintes', 'Haut-parleurs et systemes diffusion', '#00CEC9', 'speaker', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
('Consoles audio', 'Tables de mixage et interfaces', '#0984E3', 'tune-vertical', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2);

-- Sub-categories Video  
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('Projecteurs video', 'Videoprojecteurs et ecrans', '#6C5CE7', 'projector', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
('Cameras', 'Cameras et equipement de prise de vue', '#A29BFE', 'camera', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
('Regie video', 'Melangeurs et equipement de regie', '#74B9FF', 'television', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3);

-- Sub-sub-categories for LED in Projecteurs
INSERT INTO categories (name, description, color, icon, display_order, active, created_at, updated_at, parent_id) VALUES
('LED', 'Projecteurs LED', '#E17055', 'led-strip-variant', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
('Traditionnels', 'Projecteurs halogene et tungstene', '#FDCB6E', 'lightbulb', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
('Motorises', 'Projecteurs automatiques et lyre', '#E84393', 'rotate-3d-variant', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6);

-- ============================================================================
-- DONNÉES DÉMONSTRATION SAV - Service Requests
-- ============================================================================

INSERT INTO service_request (
    title, description, priority, status, type, 
    requester_name, requester_email, assigned_technician,
    estimated_cost, actual_cost, resolution_notes,
    created_at, updated_at, resolved_at
) VALUES 
-- Demande réparation urgente
(
    'Console Yamaha M32 - Écran défaillant', 
    'L''écran principal de la console M32 ne s''allume plus après l''événement de samedi. Voyant d''alimentation OK, pas de réaction tactile.',
    'URGENT', 'IN_PROGRESS', 'REPAIR',
    'Alexandre Durand', 'a.durand@magscene.fr', 'Jean Dupont',
    350.00, null, null,
    DATEADD(DAY, -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, null
),

-- Maintenance préventive planifiée  
(
    'Maintenance Lyre Robe MegaPointe #6',
    'Nettoyage optique, mise à jour firmware v2.1.3, vérification des moteurs et calibrage couleurs selon planning trimestriel.',
    'MEDIUM', 'OPEN', 'MAINTENANCE',
    'Chef Éclairage', 'eclairage@magscene.fr', null,
    150.00, null, null,
    DATEADD(HOUR, -6, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, null
),

-- Installation terminée
(
    'Installation Système Vidéo Client ABC',
    'Installation complète du système vidéo pour l''événement corporate d''ABC Industries : 3 caméras, mélangeur ATEM, écrans LED.',
    'HIGH', 'RESOLVED', 'INSTALLATION', 
    'Marie Martin', 'm.martin@magscene.fr', 'Pierre Leroy',
    2800.00, 2650.00, 'Installation réalisée avec succès. Client très satisfait. Formation équipe technique effectuée.',
    DATEADD(DAY, -5, CURRENT_TIMESTAMP), DATEADD(DAY, -1, CURRENT_TIMESTAMP), DATEADD(DAY, -1, CURRENT_TIMESTAMP)
),

-- Retour marchandise en cours
(
    'RMA Micro Shure ULXD - Défaut fabrication',
    'Micro sans fil ULXD24/SM58 présentant des coupures audio intermittentes. Sous garantie, retour constructeur pour échange.',
    'MEDIUM', 'WAITING_PARTS', 'RMA',
    'Technicien Son B', 'son@magscene.fr', 'Sophie Moreau', 
    0.00, null, null,
    DATEADD(DAY, -7, CURRENT_TIMESTAMP), DATEADD(DAY, -3, CURRENT_TIMESTAMP), null
),

-- Formation personnel
(
    'Formation Grand MA3 Light - Niveau Avancé',
    'Session de formation avancée sur console d''éclairage Grand MA3 Light pour l''équipe technique. Programmation complexe et effets.',
    'LOW', 'OPEN', 'TRAINING',
    'Responsable Formation', 'formation@magscene.fr', null,
    800.00, null, null,
    DATEADD(HOUR, -12, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, null
),

-- Réparation sous garantie fermée
(
    'Projecteur Ayrton Khamsin-S - LED défaillante',
    'Remplacement module LED défaillant (secteur rouge). Intervention garantie constructeur.',
    'HIGH', 'CLOSED', 'WARRANTY',
    'Équipe Maintenance', 'maintenance@magscene.fr', 'Jean Dupont',
    0.00, 0.00, 'Module LED remplacé sous garantie. Projecteur opérationnel. Garantie étendue 6 mois.',
    DATEADD(DAY, -10, CURRENT_TIMESTAMP), DATEADD(DAY, -8, CURRENT_TIMESTAMP), DATEADD(DAY, -8, CURRENT_TIMESTAMP)
),

-- Demande annulée
(
    'Modification Structure H40V - Projet reporté',
    'Demande de modification structure pour événement outdoor. Projet client reporté à 2026.',
    'LOW', 'CANCELLED', 'REPAIR',
    'Chef de Projet', 'projets@magscene.fr', null,
    null, null, 'Demande annulée suite au report du projet client.',
    DATEADD(DAY, -15, CURRENT_TIMESTAMP), DATEADD(DAY, -14, CURRENT_TIMESTAMP), null
),

-- Maintenance en cours
(
    'Vérification Multipaire 32 Voies Sommercable',
    'Contrôle continuité, test isolation et nettoyage connecteurs XLR. Maintenance préventive annuelle.',
    'MEDIUM', 'IN_PROGRESS', 'MAINTENANCE',
    'Technicien Câblage', 'cablage@magscene.fr', 'Sophie Moreau',
    120.00, null, null,
    DATEADD(DAY, -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, null
);

-- Personnel de démonstration
INSERT INTO personnel (
    first_name, last_name, email, phone, type, status, 
    job_title, department, hire_date, notes, created_at, updated_at
) VALUES
-- Employés permanents
('Jean', 'Dupont', 'jean.dupont@magscene.fr', '01 23 45 67 89', 'EMPLOYEE', 'ACTIVE', 
 'Technicien Son Senior', 'Audio', '2020-01-15', 'Spécialiste consoles numériques et systèmes Line Array', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Sophie', 'Moreau', 'sophie.moreau@magscene.fr', '01 23 45 67 90', 'EMPLOYEE', 'ACTIVE', 
 'Chef Éclairagiste', 'Éclairage', '2018-06-01', 'Experte en LED et automatismes d''éclairage', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Marc', 'Leroy', 'marc.leroy@magscene.fr', '01 23 45 67 91', 'EMPLOYEE', 'ACTIVE', 
 'Responsable Technique', 'Technique', '2019-03-10', 'Coordination technique générale', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Amélie', 'Bernard', 'amelie.bernard@magscene.fr', '01 23 45 67 92', 'EMPLOYEE', 'ACTIVE', 
 'Technicienne Vidéo', 'Vidéo', '2021-09-15', 'Spécialiste projection et captation', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Freelances
('Thomas', 'Martin', 'thomas.martin@freelance.fr', '06 12 34 56 78', 'FREELANCE', 'ACTIVE', 
 'Ingénieur Son', 'Audio', '2022-01-01', 'Freelance spécialisé en sonorisation grands événements', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Julie', 'Rousseau', 'julie.rousseau@freelance.fr', '06 12 34 56 79', 'FREELANCE', 'ACTIVE', 
 'Éclairagiste', 'Éclairage', '2022-03-01', 'Créatrice lumière concerts et théâtre', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Intérimaires  
('Pierre', 'Dubois', 'pierre.dubois@interim.fr', '06 98 76 54 32', 'TEMPORARY', 'ACTIVE', 
 'Assistant Technique', 'Technique', '2024-10-01', 'Renfort technique temporaire', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Personnel en congé
('Caroline', 'Petit', 'caroline.petit@magscene.fr', '01 23 45 67 93', 'EMPLOYEE', 'ON_LEAVE', 
 'Assistante de Production', 'Production', '2020-08-01', 'En congé maternité jusqu''en décembre', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========================================
-- Sales & Installations Demo Data
-- ========================================

-- Vérifier si les tables existent et nettoyer
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
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);