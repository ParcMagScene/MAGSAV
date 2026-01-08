# 🎯 MAGSAV 3.0

> **Système de Gestion SAV et Parc Matériel pour Mag Scène**

[![Statut](https://img.shields.io/badge/Statut-STABLE-green)](https://github.com/ParcMagScene/MAGSAV)
[![Backend](https://img.shields.io/badge/Backend-OPÉRATIONNEL-green)](http://localhost:8080)
[![Frontend](https://img.shields.io/badge/Frontend-OPÉRATIONNEL-green)](http://localhost:3000)
[![Java](https://img.shields.io/badge/Java-21.0.8-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-blue)](https://reactjs.org/)

Application multi-plateforme pour la gestion complète du SAV, du parc matériel, des véhicules, du personnel et du planning de Mag Scène.

## 🚀 Démarrage Rapide

```powershell
# Lancer l'application complète
.\start-magsav.ps1
```

**Accès:**
- 🌐 Frontend: http://localhost:3000
- 🔧 Backend API: http://localhost:8080
- 📊 Console H2: http://localhost:8080/h2-console

## 📚 Documentation

📖 **[Documentation Complète](DOCUMENTATION.md)** - Guide complet d'installation, développement et configuration

## 🎨 Fonctionnalités

- **SAV**: Demandes intervention, réparations, RMA, historique
- **Parc Matériel**: Inventaire 2547 équipements, QR codes, catégories
- **Ventes & Installations**: Projets, contrats, suivi clients
- **Véhicules**: Planning, maintenance, réservations
- **Personnel**: Qualifications, planning unifié
- **Planning Global**: Vue unifiée avec détection conflits

## 🛠️ Stack Technique

- **Backend**: Spring Boot 3.4.13 + H2 + JWT Security
- **Frontend**: React 18 TypeScript
- **Build**: Gradle 8.4 multi-module
- **Java**: 21.0.8 (OpenJDK)

## 💻 Développement

```powershell
# Backend seul
.\gradlew.bat :backend:bootRun

# Frontend seul
cd web-frontend
npm start

# Build complet
.\gradlew.bat build -x test
```

## 📊 Statistiques

- **Backend**: 24 controllers, 23 repositories, 23 entities
- **Frontend**: 11 pages, 15+ composants
- **Base H2**: 2547 équipements importés
- **API REST**: ~80 endpoints (Swagger)

## 📞 Support

- **Repository**: [ParcMagScene/MAGSAV](https://github.com/ParcMagScene/MAGSAV)
- **Documentation**: [DOCUMENTATION.md](DOCUMENTATION.md)

---

**Dernière mise à jour:** 8 janvier 2026
