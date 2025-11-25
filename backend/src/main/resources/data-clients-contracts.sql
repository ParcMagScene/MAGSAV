-- Données de démonstration pour les clients et contrats
-- Module Clients & Contrats de MAGSAV-3.0

-- ============================================
-- CLIENTS
-- ============================================

INSERT INTO clients (
    company_name, siret_number, vat_number, type, status, category,
    address, postal_code, city, country, email, phone, website,
    business_sector, annual_revenue, employee_count,
    credit_limit, outstanding_amount, payment_terms_days, preferred_payment_method,
    notes, assigned_sales_rep, created_at, updated_at
) VALUES

-- Client 1 : Grande entreprise événementielle
(
    'EventCorp International', '12345678901234', 'FR12345678901', 'CORPORATE', 'ACTIVE', 'PREMIUM',
    '15 Avenue des Champs-Élysées', '75008', 'Paris', 'France',
    'contact@eventcorp.fr', '+33 1 42 56 78 90', 'www.eventcorp.fr',
    'Événementiel', 5000000.00, 150,
    100000.00, 15000.00, 30, 'BANK_TRANSFER',
    'Client premium avec de nombreux événements corporate. Excellent payeur.',
    'Sophie Moreau', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Client 2 : Théâtre municipal
(
    'Théâtre Municipal de Lyon', '98765432109876', 'FR98765432109', 'GOVERNMENT', 'ACTIVE', 'STANDARD',
    '1 Place de la Comédie', '69001', 'Lyon', 'France',
    'technique@theatre-lyon.fr', '+33 4 78 90 12 34', 'www.theatre-lyon.fr',
    'Culture', 2000000.00, 45,
    50000.00, 8500.00, 45, 'BANK_TRANSFER',
    'Théâtre municipal avec saison culturelle riche. Contrats de maintenance récurrents.',
    'Marc Leroy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Client 3 : Société de production audiovisuelle
(
    'AudioVision Productions', '11223344556677', 'FR11223344556', 'CORPORATE', 'ACTIVE', 'STANDARD',
    '28 Rue de la Production', '92100', 'Boulogne-Billancourt', 'France',
    'prod@audiovision.com', '+33 1 46 84 92 15', 'www.audiovision.com',
    'Production audiovisuelle', 1500000.00, 25,
    75000.00, 22000.00, 30, 'CREDIT_CARD',
    'Spécialisée dans les productions TV et web. Demandes techniques pointues.',
    'Jean Dupont', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Client 4 : Association culturelle
(
    'Association Les Arts en Scène', '55667788990011', NULL, 'ASSOCIATION', 'ACTIVE', 'BASIC',
    '42 Rue des Artistes', '13001', 'Marseille', 'France',
    'contact@arts-en-scene.org', '+33 4 91 23 45 67', NULL,
    'Association culturelle', 100000.00, 8,
    10000.00, 2500.00, 15, 'CHECK',
    'Association dynamique organisant festivals et spectacles locaux.',
    'Sophie Moreau', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Client 5 : Entreprise technologique
(
    'TechCorp Solutions', '99887766554433', 'FR99887766554', 'CORPORATE', 'ACTIVE', 'PREMIUM',
    '101 Boulevard de la Technologie', '31000', 'Toulouse', 'France',
    'events@techcorp.fr', '+33 5 61 78 90 12', 'www.techcorp.fr',
    'Technologies', 8000000.00, 350,
    150000.00, 45000.00, 30, 'DIRECT_DEBIT',
    'Grands événements corporate et lancements produits. Budget conséquent.',
    'Marc Leroy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Client 6 : Mairie
(
    'Mairie de Nice', '12345098765432', NULL, 'GOVERNMENT', 'ACTIVE', 'STANDARD',
    'Hôtel de Ville, 5 Rue de l''Hôtel de ville', '06000', 'Nice', 'France',
    'evenements@ville-nice.fr', '+33 4 97 13 20 00', 'www.nice.fr',
    'Administration publique', 50000000.00, 2500,
    80000.00, 0.00, 60, 'BANK_TRANSFER',
    'Événements municipaux : festivals, cérémonies officielles, concerts.',
    'Jean Dupont', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ============================================
-- CONTACTS
-- ============================================

INSERT INTO contacts (
    first_name, last_name, job_title, department, email, phone, mobile, direct_phone,
    type, status, is_primary, is_decision_maker, receive_marketing,
    notes, client_id, created_at, updated_at
) VALUES

-- Contacts pour EventCorp International (client_id = 1)
(
    'Pierre', 'Durand', 'Directeur Technique', 'Production',
    'p.durand@eventcorp.fr', '+33 1 42 56 78 91', '+33 6 12 34 56 78', '+33 1 42 56 78 91',
    'TECHNICAL', 'ACTIVE', true, true, true,
    'Contact principal pour tous les aspects techniques. Très compétent.',
    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'Marie', 'Lecomte', 'Responsable Achats', 'Achats',
    'm.lecomte@eventcorp.fr', '+33 1 42 56 78 92', '+33 6 98 76 54 32', '+33 1 42 56 78 92',
    'COMMERCIAL', 'ACTIVE', false, true, true,
    'Décisionnaire pour les achats et locations. Négociatrice expérimentée.',
    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contacts pour Théâtre Municipal de Lyon (client_id = 2)
(
    'François', 'Martin', 'Directeur Technique', 'Technique',
    'f.martin@theatre-lyon.fr', '+33 4 78 90 12 35', '+33 6 11 22 33 44', NULL,
    'TECHNICAL', 'ACTIVE', true, false, true,
    'Responsable de toute la technique du théâtre. Contact privilégié.',
    2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'Isabelle', 'Rousseau', 'Directrice Administrative', 'Administration',
    'i.rousseau@theatre-lyon.fr', '+33 4 78 90 12 36', '+33 6 55 66 77 88', NULL,
    'FINANCIAL', 'ACTIVE', false, true, false,
    'Responsable budgets et contrats. Validation financière obligatoire.',
    2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contacts pour AudioVision Productions (client_id = 3)
(
    'Thomas', 'Bernard', 'Producteur Exécutif', 'Production',
    't.bernard@audiovision.com', '+33 1 46 84 92 16', '+33 6 77 88 99 00', NULL,
    'DECISION_MAKER', 'ACTIVE', true, true, true,
    'Producteur senior, décisionnaire final sur les projets.',
    3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'Julie', 'Petit', 'Chef Opératrice Son', 'Technique',
    'j.petit@audiovision.com', '+33 1 46 84 92 17', '+33 6 12 21 34 43', NULL,
    'TECHNICAL', 'ACTIVE', false, false, true,
    'Experte technique son. Conseil sur les équipements spécialisés.',
    3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contacts pour Association Les Arts en Scène (client_id = 4)
(
    'Luc', 'Moreau', 'Président', 'Direction',
    'president@arts-en-scene.org', '+33 4 91 23 45 68', '+33 6 99 88 77 66', NULL,
    'DECISION_MAKER', 'ACTIVE', true, true, true,
    'Président bénévole de l''association. Très investi et disponible.',
    4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contacts pour TechCorp Solutions (client_id = 5)
(
    'Sylvie', 'Garnier', 'Directrice Communication', 'Communication',
    's.garnier@techcorp.fr', '+33 5 61 78 90 13', '+33 6 55 44 33 22', NULL,
    'COMMERCIAL', 'ACTIVE', true, true, true,
    'Responsable des événements corporate. Budget important.',
    5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'David', 'Roux', 'Chef de Projet Événements', 'Événementiel',
    'd.roux@techcorp.fr', '+33 5 61 78 90 14', '+33 6 11 00 99 88', NULL,
    'PROJECT_MANAGER', 'ACTIVE', false, false, true,
    'Coordination opérationnelle des événements. Interlocuteur technique.',
    5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contacts pour Mairie de Nice (client_id = 6)
(
    'Catherine', 'Blanc', 'Responsable Événements', 'Culture',
    'c.blanc@ville-nice.fr', '+33 4 97 13 20 01', '+33 6 78 65 43 21', NULL,
    'COMMERCIAL', 'ACTIVE', true, true, false,
    'Coordination des événements municipaux. Processus administratif strict.',
    6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ============================================
-- CONTRATS
-- ============================================

INSERT INTO contracts (
    contract_number, title, type, status, start_date, end_date, signature_date,
    total_amount, monthly_amount, invoiced_amount, remaining_amount,
    billing_frequency, payment_terms, is_auto_renewable, renewal_period_months, notice_period_days,
    description, terms_and_conditions, notes,
    client_signatory, magscene_signatory,
    client_id, created_at, updated_at
) VALUES

-- Contrat 1 : Maintenance EventCorp
(
    'CONT-2024-001', 'Contrat Maintenance Annuelle EventCorp', 'MAINTENANCE', 'ACTIVE',
    '2024-01-01', '2024-12-31', '2023-12-15',
    48000.00, 4000.00, 16000.00, 32000.00,
    'MONTHLY', 'NET_30', true, 12, 60,
    'Contrat de maintenance préventive et curative pour l''ensemble du parc matériel d''EventCorp International.',
    'Interventions dans les 24h en cas de panne. Pièces détachées incluses selon liste annexée.',
    'Renouvellement automatique sauf dénonciation. Client très satisfait.',
    'Pierre Durand', 'Sophie Moreau',
    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contrat 2 : Prestation Théâtre Lyon
(
    'CONT-2024-002', 'Sonorisation Saison Culturelle 2024', 'SERVICE', 'ACTIVE',
    '2024-09-01', '2025-06-30', '2024-08-20',
    75000.00, NULL, 25000.00, 50000.00,
    'QUARTERLY', 'NET_45', false, NULL, 30,
    'Prestation complète de sonorisation pour la saison culturelle 2024-2025 du Théâtre Municipal de Lyon.',
    'Forfait incluant installation, exploitation et démontage pour 20 représentations.',
    'Partenariat historique. Renouvellement probable pour la saison suivante.',
    'François Martin', 'Marc Leroy',
    2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contrat 3 : Location AudioVision
(
    'CONT-2024-003', 'Location Matériel Production TV', 'RENTAL', 'ACTIVE',
    '2024-03-01', '2025-02-28', '2024-02-10',
    120000.00, 10000.00, 80000.00, 40000.00,
    'MONTHLY', 'NET_30', true, 12, 30,
    'Location longue durée d''équipements audiovisuels pour productions télévisées.',
    'Matériel dédié : consoles, micros HF, caméras. Maintenance incluse.',
    'Contrat rentable. Client fiable avec paiements réguliers.',
    'Thomas Bernard', 'Jean Dupont',
    3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contrat 4 : Support Association
(
    'CONT-2024-004', 'Support Technique Festival 2024', 'SUPPORT', 'COMPLETED',
    '2024-06-15', '2024-07-15', '2024-05-20',
    15000.00, NULL, 15000.00, 0.00,
    'ONE_TIME', 'NET_15', false, NULL, 15,
    'Support technique complet pour le festival annuel de l''association Les Arts en Scène.',
    'Prestation ponctuelle incluant matériel et techniciens sur 3 jours.',
    'Festival réussi. Association satisfaite. Recommandation pour 2025.',
    'Luc Moreau', 'Sophie Moreau',
    4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contrat 5 : Prestations TechCorp
(
    'CONT-2024-005', 'Événements Corporate TechCorp 2024', 'SERVICE', 'ACTIVE',
    '2024-01-01', '2024-12-31', '2023-11-30',
    200000.00, NULL, 150000.00, 50000.00,
    'ON_DELIVERY', 'NET_30', false, NULL, 90,
    'Prestations audiovisuelles pour l''ensemble des événements corporate de TechCorp Solutions.',
    'Facturation à la prestation. Tarifs préférentiels négociés.',
    'Excellent client. Événements de qualité avec budgets conséquents.',
    'Sylvie Garnier', 'Marc Leroy',
    5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),

-- Contrat 6 : Fourniture Mairie Nice
(
    'CONT-2024-006', 'Équipement Événements Municipaux', 'SUPPLY', 'PENDING_SIGNATURE',
    '2025-01-01', '2027-12-31', NULL,
    350000.00, NULL, 0.00, 350000.00,
    'ANNUAL', 'NET_60', true, 36, 180,
    'Fourniture et installation d''équipements audiovisuels pour les événements municipaux de Nice.',
    'Marché public. Équipements neufs avec garantie constructeur étendue.',
    'Marché public remporté. En attente de signature officielle.',
    'Catherine Blanc', 'Jean Dupont',
    6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ============================================
-- ARTICLES DE CONTRATS (CONTRACT_ITEMS)
-- ============================================

INSERT INTO contract_items (
    item_name, description, quantity, unit, unit_price, total_price,
    item_type, status, notes, contract_id
) VALUES

-- Articles pour Contrat Maintenance EventCorp (contract_id = 1)
(
    'Maintenance Consoles Audio', 'Maintenance préventive mensuelle consoles Yamaha et Soundcraft',
    12, 'Interventions', 800.00, 9600.00,
    'MAINTENANCE', 'ACTIVE', 'Inclut nettoyage, calibrage et mise à jour firmware',
    1
),
(
    'Maintenance Éclairage', 'Maintenance projecteurs et lyres automatiques',
    12, 'Interventions', 1200.00, 14400.00,
    'MAINTENANCE', 'ACTIVE', 'Nettoyage optique et vérification moteurs',
    1
),
(
    'Support Technique 24/7', 'Hotline technique et interventions d''urgence',
    12, 'Mois', 2000.00, 24000.00,
    'SERVICE', 'ACTIVE', 'Disponibilité 24h/24 avec engagement de délai',
    1
),

-- Articles pour Contrat Théâtre Lyon (contract_id = 2)
(
    'Système de Sonorisation Principal', 'Installation système L-Acoustics K2 avec amplification',
    1, 'Installation', 45000.00, 45000.00,
    'EQUIPMENT', 'ACTIVE', 'Configuration adaptée à l''acoustique du théâtre',
    2
),
(
    'Consoles de Mixage', 'Mise à disposition consoles Yamaha CL5 et moniteurs',
    20, 'Représentations', 800.00, 16000.00,
    'RENTAL', 'ACTIVE', 'Configuration différente selon les spectacles',
    2
),
(
    'Techniciens Spécialisés', 'Techniciens son certifiés pour exploitation',
    20, 'Représentations', 700.00, 14000.00,
    'SERVICE', 'ACTIVE', 'Équipe formée à l''acoustique théâtrale',
    2
),

-- Articles pour Contrat Location AudioVision (contract_id = 3)
(
    'Parc Micros HF Shure', 'Location 16 micros HF Shure ULXD avec stations',
    12, 'Mois', 3500.00, 42000.00,
    'RENTAL', 'ACTIVE', 'Fréquences coordonnées et libres d''interférences',
    3
),
(
    'Caméras Blackmagic', 'Location 4 caméras URSA Mini Pro avec objectifs',
    12, 'Mois', 4000.00, 48000.00,
    'RENTAL', 'ACTIVE', 'Configuration multi-caméras pour production live',
    3
),
(
    'Régie Mobile', 'Régie vidéo complète avec mélangeur ATEM et monitoring',
    12, 'Mois', 2500.00, 30000.00,
    'RENTAL', 'ACTIVE', 'Solution clé en main pour productions extérieures',
    3
),

-- Articles pour Contrat Support Association (contract_id = 4)
(
    'Système Son Festival', 'Sonorisation complète scène principale 3 jours',
    1, 'Prestation', 8000.00, 8000.00,
    'SERVICE', 'DELIVERED', 'Line array + retours + console pour 15 groupes',
    4
),
(
    'Éclairage Scène', 'Éclairage LED et traditionnel avec jeux de couleurs',
    1, 'Prestation', 4500.00, 4500.00,
    'SERVICE', 'DELIVERED', 'Ambiances adaptées aux différents styles musicaux',
    4
),
(
    'Équipe Technique', 'Techniciens son et lumière pendant le festival',
    1, 'Prestation', 2500.00, 2500.00,
    'SERVICE', 'DELIVERED', '3 techniciens sur 3 jours avec coordination',
    4
),

-- Articles pour Contrat TechCorp (contract_id = 5)
(
    'Événement Lancement Produit Q1', 'Prestation complète auditorium 500 personnes',
    1, 'Événement', 35000.00, 35000.00,
    'SERVICE', 'DELIVERED', 'Sonorisation, vidéo projection, éclairage scénique',
    5
),
(
    'Convention Annuelle', 'Équipement audiovisuel convention 2 jours - 1200 personnes',
    1, 'Événement', 85000.00, 85000.00,
    'SERVICE', 'DELIVERED', 'Multi-salles avec vidéo-conférence et streaming',
    5
),
(
    'Séminaires Trimestriels', 'Prestations régulières salle de conférence',
    4, 'Séminaires', 20000.00, 80000.00,
    'SERVICE', 'ACTIVE', 'Configuration standard avec possibilité de streaming',
    5
),

-- Articles pour Contrat Mairie Nice (contract_id = 6)
(
    'Système Son Place Masséna', 'Installation permanente système de sonorisation',
    1, 'Installation', 150000.00, 150000.00,
    'EQUIPMENT', 'ACTIVE', 'Résistant aux intempéries avec contrôle à distance',
    6
),
(
    'Éclairage Architectural', 'Éclairage LED façades et monuments',
    1, 'Installation', 120000.00, 120000.00,
    'EQUIPMENT', 'ACTIVE', 'Programmable avec scénarios pour événements',
    6
),
(
    'Écrans LED Mobiles', 'Écrans haute résolution pour retransmissions',
    4, 'Écrans', 20000.00, 80000.00,
    'EQUIPMENT', 'ACTIVE', 'Modulaires et transportables selon les événements',
    6
);