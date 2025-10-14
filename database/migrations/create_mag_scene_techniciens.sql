-- Script de génération des techniciens Mag Scene avec données complètes

-- D'abord, s'assurer que la société Mag Scene existe
INSERT OR IGNORE INTO societes (type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe, date_creation) 
VALUES ('SAV', 'Mag Scene', 'contact@magscene.fr', '01 42 50 75 00', '123 Avenue du Spectacle, 75015 Paris', 'Société de services audiovisuels et spectacle', datetime('now'));

-- Récupérer l'ID de Mag Scene pour les FK
-- On utilise une technique avec une variable temporaire via WITH

WITH mag_scene_info AS (
    SELECT id FROM societes WHERE nom_societe = 'Mag Scene' LIMIT 1
)

-- Insérer les 5 techniciens Mag Scene
INSERT INTO techniciens (
    nom, prenom, fonction, email, telephone, telephone_urgence,
    adresse, code_postal, ville, permis_conduire, habilitations, 
    specialites, statut, societe_id, societe_nom, 
    date_obtention_permis, date_validite_habilitations,
    date_creation, date_modification, notes, sync_google_enabled
) VALUES

-- 1. Cyril - Technicien Distribution
('Dubois', 'Cyril', 'Technicien Distribution', 'cyril.dubois@magscene.fr', '06 12 34 56 78', '06 87 65 43 21',
 '15 Rue de la République', '92130', 'Issy-les-Moulineaux', 'VL, PL',
 '[{"nom": "Distribution", "dateObtention": "2018-03-15", "dateValidite": "2026-03-15", "organisme": "APAVE"}, {"nom": "Manutention", "dateObtention": "2019-01-20", "dateValidite": "2027-01-20", "organisme": "APAVE"}, {"nom": "Transport", "dateObtention": "2017-11-10", "dateValidite": "2025-11-10", "organisme": "APAVE"}]',
 '["Distribution", "Manutention", "Transport"]', 'ACTIF', (SELECT id FROM mag_scene_info), 'Mag Scene',
 '2015-06-12', '2026-03-15', datetime('now'), datetime('now'), 
 'Cyril - Technicien Distribution. Expérience solide dans le domaine du spectacle.', 0),

-- 2. Célian - Technicien Lumière  
('Martin', 'Célian', 'Technicien Lumière', 'celian.martin@magscene.fr', '06 23 45 67 89', '06 78 56 34 12',
 '28 Boulevard des Arts', '75011', 'Paris', 'VL',
 '[{"nom": "Éclairage scénique", "dateObtention": "2020-02-10", "dateValidite": "2028-02-10", "organisme": "APAVE"}, {"nom": "DMX", "dateObtention": "2020-05-15", "dateValidite": "2028-05-15", "organisme": "APAVE"}, {"nom": "Consoles lumière", "dateObtention": "2019-09-20", "dateValidite": "2027-09-20", "organisme": "APAVE"}]',
 '["Éclairage scénique", "DMX", "Consoles lumière"]', 'ACTIF', (SELECT id FROM mag_scene_info), 'Mag Scene',
 '2018-04-20', '2028-02-10', datetime('now'), datetime('now'),
 'Célian - Technicien Lumière. Très fiable et ponctuel.', 0),

-- 3. Ben - Technicien Structure
('Lefebvre', 'Ben', 'Technicien Structure', 'ben.lefebvre@magscene.fr', '06 34 56 78 90', '06 69 47 25 83',
 '42 Rue du Théâtre', '94200', 'Ivry-sur-Seine', 'VL, PL, CACES',
 '[{"nom": "Structures", "dateObtention": "2017-01-15", "dateValidite": "2025-01-15", "organisme": "APAVE"}, {"nom": "Levage", "dateObtention": "2018-06-10", "dateValidite": "2026-06-10", "organisme": "APAVE"}, {"nom": "Sécurité", "dateObtention": "2019-03-25", "dateValidite": "2027-03-25", "organisme": "APAVE"}]',
 '["Structures", "Levage", "Sécurité"]', 'ACTIF', (SELECT id FROM mag_scene_info), 'Mag Scene',
 '2014-09-30', '2025-01-15', datetime('now'), datetime('now'),
 'Ben - Technicien Structure. Bonne relation avec les clients.', 0),

-- 4. Thomas - Technicien Son
('Rousseau', 'Thomas', 'Technicien Son', 'thomas.rousseau@magscene.fr', '06 45 67 89 01', '06 58 36 14 92',
 '7 Place de la Musique', '93100', 'Montreuil', 'VL',
 '[{"nom": "Audio", "dateObtention": "2019-07-12", "dateValidite": "2027-07-12", "organisme": "APAVE"}, {"nom": "Mixage", "dateObtention": "2020-01-18", "dateValidite": "2028-01-18", "organisme": "APAVE"}, {"nom": "Sonorisation", "dateObtention": "2018-11-05", "dateValidite": "2026-11-05", "organisme": "APAVE"}]',
 '["Audio", "Mixage", "Sonorisation"]', 'ACTIF', (SELECT id FROM mag_scene_info), 'Mag Scene',
 '2016-03-15', '2027-07-12', datetime('now'), datetime('now'),
 'Thomas - Technicien Son. Formation continue régulière.', 0),

-- 5. Flo - Stagiaire
('Moreau', 'Flo', 'Stagiaire', 'flo.moreau@magscene.fr', '06 56 78 90 12', '06 47 25 83 61',
 '18 Rue des Étudiants', '75020', 'Paris', 'VL',
 '[{"nom": "Formation générale", "dateObtention": "2024-01-10", "dateValidite": "2026-01-10", "organisme": "APAVE"}, {"nom": "Support technique", "dateObtention": "2024-03-20", "dateValidite": "2026-03-20", "organisme": "APAVE"}]',
 '["Formation générale", "Support technique"]', 'ACTIF', (SELECT id FROM mag_scene_info), 'Mag Scene',
 '2023-08-15', '2026-01-10', datetime('now'), datetime('now'),
 'Flo - Stagiaire. Disponible pour les missions urgentes.', 0);

-- Maintenant créer des demandes d'intervention de test et les assigner aux techniciens
-- D'abord, récupérer les IDs des techniciens créés
INSERT INTO demande_intervention (
    type_demande, description, statut_demande, date_demande, date_souhaite, priorite, 
    technicien_assigne, client_nom, lieu_intervention, materiel_requis, notes
) VALUES

-- Demandes pour Cyril (Distribution)
('TECHNIQUE', 'Installation complète du système d''éclairage pour concert en plein air', 'EN_ATTENTE', 
 datetime('now'), date('now', '+5 days'), 'NORMALE', 
 (SELECT id FROM techniciens WHERE email = 'cyril.dubois@magscene.fr'), 
 'Festival Rock Paris', 'Parc de la Villette, Paris', 'Projecteurs LED, consoles DMX, câblage', 
 'Demande générée automatiquement pour test'),

('TECHNIQUE', 'Transport et installation matériel pour événement corporatif', 'EN_ATTENTE',
 datetime('now'), date('now', '+7 days'), 'HAUTE',
 (SELECT id FROM techniciens WHERE email = 'cyril.dubois@magscene.fr'),
 'Entreprise TechCorp', 'La Défense, Paris', 'Camion, matériel de manutention',
 'Demande générée automatiquement pour test'),

-- Demandes pour Célian (Lumière)
('TECHNIQUE', 'Maintenance préventive du système de sonorisation principal', 'EN_ATTENTE',
 datetime('now'), date('now', '+3 days'), 'NORMALE',
 (SELECT id FROM techniciens WHERE email = 'celian.martin@magscene.fr'),
 'Théâtre Municipal', 'Théâtre de Châtelet, Paris', 'Outils de diagnostic, pièces de rechange',
 'Demande générée automatiquement pour test'),

('TECHNIQUE', 'Programmation éclairage pour spectacle de danse', 'EN_ATTENTE',
 datetime('now'), date('now', '+10 days'), 'NORMALE',
 (SELECT id FROM techniciens WHERE email = 'celian.martin@magscene.fr'),
 'Compagnie Danse Moderne', 'Studio Regard du Cygne, Paris', 'Console lumière, projecteurs',
 'Demande générée automatiquement pour test'),

-- Demandes pour Ben (Structure)
('TECHNIQUE', 'Montage et sécurisation de la structure de scène modulaire', 'EN_ATTENTE',
 datetime('now'), date('now', '+4 days'), 'HAUTE',
 (SELECT id FROM techniciens WHERE email = 'ben.lefebvre@magscene.fr'),
 'Production Live Events', 'Zénith de Paris', 'Éléments modulaires, outils de fixation, équipements de sécurité',
 'Demande générée automatiquement pour test'),

('TECHNIQUE', 'Contrôle sécurité structure existante', 'EN_ATTENTE',
 datetime('now'), date('now', '+6 days'), 'URGENTE',
 (SELECT id FROM techniciens WHERE email = 'ben.lefebvre@magscene.fr'),
 'Salle Olympia', 'L''Olympia, Paris', 'Outils de contrôle, documentation sécurité',
 'Demande générée automatiquement pour test'),

-- Demandes pour Thomas (Son)
('TECHNIQUE', 'Diagnostic et réparation équipement audiovisuel défaillant', 'EN_ATTENTE',
 datetime('now'), date('now', '+2 days'), 'HAUTE',
 (SELECT id FROM techniciens WHERE email = 'thomas.rousseau@magscene.fr'),
 'Studio d''enregistrement Pro', 'Studios de Boulogne', 'Kit de réparation, outils spécialisés',
 'Demande générée automatiquement pour test'),

('TECHNIQUE', 'Réglage système son pour concert symphonique', 'EN_ATTENTE',
 datetime('now'), date('now', '+12 days'), 'NORMALE',
 (SELECT id FROM techniciens WHERE email = 'thomas.rousseau@magscene.fr'),
 'Orchestre de Paris', 'Philharmonie de Paris', 'Console de mixage, micros, retours',
 'Demande générée automatiquement pour test'),

-- Demandes pour Flo (Stagiaire)
('TECHNIQUE', 'Support technique général pour événement corporatif', 'EN_ATTENTE',
 datetime('now'), date('now', '+8 days'), 'FAIBLE',
 (SELECT id FROM techniciens WHERE email = 'flo.moreau@magscene.fr'),
 'Conférence Tech 2025', 'Palais des Congrès, Paris', 'Matériel standard, outils de base',
 'Demande générée automatiquement pour test'),

('TECHNIQUE', 'Assistance montage décor pour théâtre amateur', 'EN_ATTENTE',
 datetime('now'), date('now', '+15 days'), 'FAIBLE',
 (SELECT id FROM techniciens WHERE email = 'flo.moreau@magscene.fr'),
 'Troupe Théâtrale du Marais', 'Théâtre du Marais, Paris', 'Outils de base, matériel léger',
 'Demande générée automatiquement pour test');

-- Afficher un résumé de ce qui a été créé
SELECT '=== RÉSUMÉ DE LA GÉNÉRATION ===' as section;

SELECT '✅ Société Mag Scene:' as info, nom_societe, adresse_societe FROM societes WHERE nom_societe = 'Mag Scene';

SELECT '✅ Techniciens créés:' as info, COUNT(*) as nombre FROM techniciens WHERE societe_nom = 'Mag Scene';

SELECT 
    '✓ ' || prenom || ' ' || nom as technicien, 
    fonction, 
    email,
    ville
FROM techniciens 
WHERE societe_nom = 'Mag Scene' 
ORDER BY nom;

SELECT '✅ Demandes d''intervention créées:' as info, COUNT(*) as nombre FROM demande_intervention;

SELECT 
    '📋 ' || description as demande,
    (SELECT prenom || ' ' || nom FROM techniciens WHERE id = technicien_assigne) as assigne_a,
    date_souhaite,
    priorite
FROM demande_intervention 
ORDER BY date_souhaite;