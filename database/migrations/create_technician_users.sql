-- Script SQL pour créer les 5 techniciens Mag Scene comme utilisateurs authentifiés
-- Chaque technicien aura le rôle TECHNICIEN_MAG_SCENE avec permissions appropriées

-- D'abord, vérifier/créer la table users si nécessaire
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'INTERMITTENT',
    full_name TEXT,
    phone TEXT,
    company_id INTEGER,
    position TEXT,
    avatar_path TEXT,
    is_active INTEGER DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME,
    reset_token TEXT,
    reset_token_expires DATETIME
);

-- Fonction de hashage de mot de passe simple (à des fins de test)
-- Dans un environnement de production, utilisez un vrai système de hash sécurisé
-- Ici on utilise un hash simple pour 'tech123' : password123tech

-- 1. Cyril Dubois - Technicien Distribution
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone, 
    company_id, position, is_active, created_at
) VALUES (
    'cyril.dubois',
    'cyril.dubois@magscene.fr',
    '$2a$10$n9CM2lHWOWsMbC8kYaP1eOvRl3G8rX4vG.8r1A8mYzGkxgNfSFvFi', -- hash de 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Cyril Dubois',
    '06 12 34 56 78',
    (SELECT id FROM societes WHERE nom LIKE '%Mag%Scène%' LIMIT 1),
    'Technicien Distribution',
    1,
    CURRENT_TIMESTAMP
);

-- 2. Célian Martin - Technicien Lumière  
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    company_id, position, is_active, created_at
) VALUES (
    'celian.martin',
    'celian.martin@magscene.fr', 
    '$2a$10$n9CM2lHWOWsMbC8kYaP1eOvRl3G8rX4vG.8r1A8mYzGkxgNfSFvFi', -- hash de 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Célian Martin',
    '06 23 45 67 89',
    (SELECT id FROM societes WHERE nom LIKE '%Mag%Scène%' LIMIT 1),
    'Technicien Lumière',
    1,
    CURRENT_TIMESTAMP
);

-- 3. Ben Lefebvre - Technicien Structure
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    company_id, position, is_active, created_at
) VALUES (
    'ben.lefebvre',
    'ben.lefebvre@magscene.fr',
    '$2a$10$n9CM2lHWOWsMbC8kYaP1eOvRl3G8rX4vG.8r1A8mYzGkxgNfSFvFi', -- hash de 'tech123'
    'TECHNICIEN_MAG_SCENE', 
    'Ben Lefebvre',
    '06 34 56 78 90',
    (SELECT id FROM societes WHERE nom LIKE '%Mag%Scène%' LIMIT 1),
    'Technicien Structure',
    1,
    CURRENT_TIMESTAMP
);

-- 4. Thomas Rousseau - Technicien Son
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    company_id, position, is_active, created_at
) VALUES (
    'thomas.rousseau',
    'thomas.rousseau@magscene.fr',
    '$2a$10$n9CM2lHWOWsMbC8kYaP1eOvRl3G8rX4vG.8r1A8mYzGkxgNfSFvFi', -- hash de 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Thomas Rousseau', 
    '06 45 67 89 01',
    (SELECT id FROM societes WHERE nom LIKE '%Mag%Scène%' LIMIT 1),
    'Technicien Son',
    1,
    CURRENT_TIMESTAMP
);

-- 5. Flo Moreau - Stagiaire
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    company_id, position, is_active, created_at
) VALUES (
    'flo.moreau',
    'flo.moreau@magscene.fr',
    '$2a$10$n9CM2lHWOWsMbC8kYaP1eOvRl3G8rX4vG.8r1A8mYzGkxgNfSFvFi', -- hash de 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Flo Moreau',
    '06 56 78 90 12', 
    (SELECT id FROM societes WHERE nom LIKE '%Mag%Scène%' LIMIT 1),
    'Stagiaire',
    1,
    CURRENT_TIMESTAMP
);

-- Lier les utilisateurs créés aux données techniciens existantes
-- Mise à jour de la table techniciens pour référencer les users
UPDATE techniciens SET 
    user_id = (SELECT id FROM users WHERE username = 'cyril.dubois')
WHERE nom = 'Dubois' AND prenom = 'Cyril';

UPDATE techniciens SET 
    user_id = (SELECT id FROM users WHERE username = 'celian.martin')
WHERE nom = 'Martin' AND prenom = 'Célian';

UPDATE techniciens SET 
    user_id = (SELECT id FROM users WHERE username = 'ben.lefebvre') 
WHERE nom = 'Lefebvre' AND prenom = 'Ben';

UPDATE techniciens SET 
    user_id = (SELECT id FROM users WHERE username = 'thomas.rousseau')
WHERE nom = 'Rousseau' AND prenom = 'Thomas';

UPDATE techniciens SET 
    user_id = (SELECT id FROM users WHERE username = 'flo.moreau')
WHERE nom = 'Moreau' AND prenom = 'Flo';

-- Afficher le résumé des utilisateurs créés
SELECT 
    '🔐 UTILISATEURS TECHNICIENS CRÉÉS 🔐' as titre;

SELECT 
    u.id,
    u.username as 'Login',
    u.full_name as 'Nom complet',
    u.email as 'Email', 
    u.role as 'Rôle',
    u.position as 'Fonction',
    u.phone as 'Téléphone',
    CASE WHEN u.is_active = 1 THEN '✅ Actif' ELSE '❌ Inactif' END as 'Statut'
FROM users u 
WHERE u.role = 'TECHNICIEN_MAG_SCENE' 
AND u.username IN ('cyril.dubois', 'celian.martin', 'ben.lefebvre', 'thomas.rousseau', 'flo.moreau')
ORDER BY u.full_name;

SELECT 
    '📋 INFORMATIONS DE CONNEXION' as titre;

SELECT 
    '• ' || u.full_name || ' : Login=' || u.username || ' / Password=tech123' as 'Identifiants'
FROM users u 
WHERE u.role = 'TECHNICIEN_MAG_SCENE' 
AND u.username IN ('cyril.dubois', 'celian.martin', 'ben.lefebvre', 'thomas.rousseau', 'flo.moreau')
ORDER BY u.full_name;