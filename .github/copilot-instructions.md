# MAGSAV-3.0 - Système de Gestion SAV et Parc Matériel

##  Description du Projet
Application multi-plateforme de gestion pour Mag Scène :
- **SAV** : Gestion demandes d'intervention, réparations, RMA, historique
- **Parc Matériel** : Inventaire avec QR codes, catégories hiérarchiques, photos
- **Ventes & Installations** : Import PDF affaires, gestion commandes fournisseurs
- **Véhicules** : Planning, maintenance, entretiens, locations externes
- **Personnel** : Qualifications, permis, planning, intermittents/freelances

##  Architecture Technique
- **Backend** : Spring Boot 3.1 + H2 Database + JWT Security
- **Desktop** : JavaFX 21 (interface principale)  
- **Web** : React 18 TypeScript (même interface que desktop)
- **Build** : Gradle multi-module monorepo
- **Base** : Java 17+, Node.js 18+

##  Modules
```
MAGSAV-3.0/
 backend/          # Spring Boot REST API + H2
 desktop-javafx/   # Application JavaFX desktop  
 web-frontend/     # Interface React TypeScript
 common-models/    # Entités JPA partagées
 integration-tests/ # Tests E2E
```

##  Développement
- **Prérequis** : Java 17+, Node.js 18+, VS Code
- **Build** : `./gradlew build`
- **Backend** : `./gradlew :backend:bootRun` (port 8080)
- **Desktop** : `./gradlew :desktop-javafx:run`
- **Web Dev** : `cd web-frontend && npm start` (port 3000)
