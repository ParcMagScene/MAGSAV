-- Données de démonstration pour le module SAV de MAGSAV-3.0
-- Demandes SAV avec différents statuts et priorités

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