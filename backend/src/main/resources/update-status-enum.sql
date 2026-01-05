-- Script pour ajouter les nouveaux statuts VALIDATED et EXTERNAL à la contrainte ENUM

-- Modifier la colonne status pour accepter tous les statuts
ALTER TABLE service_request ALTER COLUMN status VARCHAR(50);

-- Recréer avec la bonne contrainte ENUM incluant VALIDATED et EXTERNAL
ALTER TABLE service_request ALTER COLUMN status ENUM('OPEN', 'VALIDATED', 'IN_PROGRESS', 'WAITING_PARTS', 'RESOLVED', 'CANCELLED', 'EXTERNAL', 'CLOSED') DEFAULT 'OPEN';
