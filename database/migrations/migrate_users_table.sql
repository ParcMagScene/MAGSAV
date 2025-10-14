-- Script pour migrer la table users et ajouter le rôle TECHNICIEN_MAG_SCENE

-- 1. Sauvegarder les données existantes
CREATE TABLE users_backup AS SELECT * FROM users;

-- 2. Supprimer l'ancienne table
DROP TABLE users;

-- 3. Recréer la table avec les nouveaux rôles
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'USER', 'TECHNICIEN_MAG_SCENE', 'INTERMITTENT')) DEFAULT 'USER',
    full_name TEXT,
    phone TEXT,
    company_id INTEGER,
    position TEXT,
    avatar_path TEXT,
    is_active BOOLEAN DEFAULT 1,
    created_at TEXT DEFAULT (datetime('now')),
    last_login TEXT,
    reset_token TEXT,
    reset_token_expires TEXT
);

-- 4. Restaurer les données existantes
INSERT INTO users (id, username, email, password_hash, role, full_name, phone, company_id, position, is_active, created_at, last_login, reset_token, reset_token_expires)
SELECT id, username, email, password_hash, role, full_name, phone, company_id, position, is_active, created_at, last_login, reset_token, reset_token_expires
FROM users_backup;

-- 5. Recréer les index
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- 6. Supprimer la sauvegarde
DROP TABLE users_backup;

-- 7. Vérifier la nouvelle structure
.schema users