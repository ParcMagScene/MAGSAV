# Menu Préférences - Gestion du Scraping d'Images

## 📍 Localisation

La gestion du scraping d'images est maintenant accessible via le menu principal :

```
☰ Menus → ⚙️ Préférences → 🖼️ Gestion Scraping Images
```

## 🎯 Vue d'ensemble

Le menu **Préférences** regroupe maintenant toutes les configurations de l'application :

- **📧 Configuration Email** - Configuration SMTP pour l'envoi d'emails
- **🧹 Maintenance Médias** - Gestion et nettoyage des fichiers médias
- **🖼️ Gestion Scraping Images** - Configuration du scraping automatique d'images

## 🖼️ Interface de Gestion du Scraping

L'interface de préférences du scraping propose plusieurs sections :

### 📋 1. Sources de Scraping
- **Tableau des sources configurées** : Fabricants et revendeurs
- **Types** : Fabricant ou Revendeur
- **Configuration** : URL de base, motifs de recherche, sélecteurs CSS
- **Actions** : Ajouter, modifier, supprimer, tester les sources

### ⚙️ 2. Paramètres de Scraping
- **Délai entre requêtes** : 1-30 secondes (défaut: 3s)
- **Timeout des requêtes** : 5-60 secondes (défaut: 15s)
- **Qualité minimale** : Largeur × Hauteur en pixels (défaut: 300×300)
- **Dossier de téléchargement** : Chemin local pour sauvegarder les images

### 🚀 3. Actions en Lot
- **Scraper Produits sans Images** : Process uniquement les produits n'ayant pas d'images
- **Mettre à Jour Toutes les Images** : Recherche de nouvelles images pour tous les produits
- **Statistiques** : Affichage détaillé de l'état des images dans la base

## 📊 Sources Préconfigurées

### 🏭 Fabricants (6 sources)
- **Yamaha** : www.yamaha.com
- **Sony** : www.sony.com  
- **Panasonic** : www.panasonic.com
- **Bose** : www.bose.com
- **Martin Audio** : www.martin-audio.com
- **Robe** : www.robe.cz

### 🏪 Revendeurs (2 sources)
- **Thomann** : www.thomann.de
- **SonoVente** : www.sonovente.com

## 💡 Utilisation

1. **Accéder aux préférences** : Menu → Préférences → Gestion Scraping Images
2. **Consulter les sources** : Vérifier la configuration des fabricants/revendeurs
3. **Ajuster les paramètres** : Modifier délais, qualité, dossier de destination
4. **Lancer le scraping** : 
   - "Scraper Produits sans Images" pour un process ciblé
   - "Mettre à Jour Toutes les Images" pour un process complet
5. **Suivre le progrès** : Barre de progression et compteurs en temps réel
6. **Consulter les statistiques** : Bouton "Statistiques" pour un rapport détaillé

## 🔧 Configuration Avancée

### Ajout de nouvelles sources
- Bouton "➕ Ajouter Source" (fonctionnalité à implémenter)
- Configuration des URL, patterns de recherche, sélecteurs CSS
- Test des sources avant activation

### Optimisation des paramètres
- **Délai entre requêtes** : Augmenter pour éviter les blocages anti-robot
- **Qualité minimale** : Ajuster selon les besoins (images haute résolution)
- **Timeout** : Adapter selon la vitesse de connexion

## 📈 Statistiques et Monitoring

L'interface affiche en permanence :
- **Nombre total de produits** dans la base
- **Produits avec images** déjà scrapées
- **Produits sans images** à traiter
- **Pourcentage de couverture** des images

## 🔄 Intégration avec l'Application

La fonctionnalité s'intègre parfaitement avec :
- **Base de données** : Colonne `scraped_images` pour stocker les URLs
- **Système de médias** : Compatible avec la gestion existante des images
- **Scripts Python** : Utilise `scripts/image_scraper.py` pour le scraping effectif
- **Logging** : Traçabilité complète des opérations dans les logs de l'application

## 🎨 Design et Ergonomie

- **Interface intuitive** : Spinners, boutons avec icônes, barres de progression
- **Retours visuels** : Messages de statut, compteurs en temps réel
- **Gestion d'erreurs** : Alertes informatives en cas de problème
- **Thème cohérent** : Intégration harmonieuse avec le design MAGSAV

---

**Note** : Cette fonctionnalité nécessite l'installation des dépendances Python (voir `scripts/requirements.txt`) pour fonctionner pleinement.