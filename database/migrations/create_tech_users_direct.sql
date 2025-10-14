-- Script SQL pour créer directement les 5 techniciens Mag Scene comme utilisateurs authentifiés
-- Utilise des hashs bcrypt réels pour le mot de passe 'tech123'

-- 1. Cyril Dubois - Technicien Distribution
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone, 
    position, is_active, created_at
) VALUES (
    'cyril.dubois',
    'cyril.dubois@magscene.fr',
    '$2a$10$wvfKNlGSg5RYL0HQ9.8iKOYwJYdcJN7CQ.CzMz8lHx8eNX8sNlraq', -- bcrypt pour 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Cyril Dubois',
    '06 12 34 56 78',
    'Technicien Distribution',
    1,
    CURRENT_TIMESTAMP
);

-- 2. Célian Martin - Technicien Lumière  
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    position, is_active, created_at
) VALUES (
    'celian.martin',
    'celian.martin@magscene.fr', 
    '$2a$10$wvfKNlGSg5RYL0HQ9.8iKOYwJYdcJN7CQ.CzMz8lHx8eNX8sNlraq', -- bcrypt pour 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Célian Martin',
    '06 23 45 67 89',
    'Technicien Lumière',
    1,
    CURRENT_TIMESTAMP
);

-- 3. Ben Lefebvre - Technicien Structure
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    position, is_active, created_at
) VALUES (
    'ben.lefebvre',
    'ben.lefebvre@magscene.fr',
    '$2a$10$wvfKNlGSg5RYL0HQ9.8iKOYwJYdcJN7CQ.CzMz8lHx8eNX8sNlraq', -- bcrypt pour 'tech123'
    'TECHNICIEN_MAG_SCENE', 
    'Ben Lefebvre',
    '06 34 56 78 90',
    'Technicien Structure',
    1,
    CURRENT_TIMESTAMP
);

-- 4. Thomas Rousseau - Technicien Son
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    position, is_active, created_at
) VALUES (
    'thomas.rousseau',
    'thomas.rousseau@magscene.fr',
    '$2a$10$wvfKNlGSg5RYL0HQ9.8iKOYwJYdcJN7CQ.CzMz8lHx8eNX8sNlraq', -- bcrypt pour 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Thomas Rousseau', 
    '06 45 67 89 01',
    'Technicien Son',
    1,
    CURRENT_TIMESTAMP
);

-- 5. Flo Moreau - Stagiaire
INSERT OR REPLACE INTO users (
    username, email, password_hash, role, full_name, phone,
    position, is_active, created_at
) VALUES (
    'flo.moreau',
    'flo.moreau@magscene.fr',
    '$2a$10$wvfKNlGSg5RYL0HQ9.8iKOYwJYdcJN7CQ.CzMz8lHx8eNX8sNlraq', -- bcrypt pour 'tech123'
    'TECHNICIEN_MAG_SCENE',
    'Flo Moreau',
    '06 56 78 90 12', 
    'Stagiaire',
    1,
    CURRENT_TIMESTAMP
);

-- Vérification des utilisateurs créés
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
ORDER BY u.full_name;