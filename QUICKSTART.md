# 🚀 MAGSAV-3.0 - Démarrage Rapide

## ✅ Tout fonctionne !

**Date validation:** 6 janvier 2026  
**Backend:** ✅ OPÉRATIONNEL  
**Frontend:** ✅ OPÉRATIONNEL

---

## 🎯 Démarrage en 1 Commande

```powershell
.\start-dev-full.ps1
```

**Résultat:**
- Backend Spring Boot sur http://localhost:8080
- Frontend React sur http://localhost:3000
- Navigateur s'ouvre automatiquement

---

## 📍 URLs

| Service | URL | Description |
|---------|-----|-------------|
| 🌐 Frontend | http://localhost:3000 | Interface React |
| 🔧 Backend | http://localhost:8080 | API REST |
| 📊 Console H2 | http://localhost:8080/h2-console | Base de données |
| 📖 Swagger | http://localhost:8080/swagger-ui.html | Documentation API |

---

## 💾 Base H2

```
JDBC URL: jdbc:h2:file:~/magsav/data/magsav
Username: sa
Password: password
```

---

## 📚 Documentation Complète

- 📋 [README.md](README.md) - Documentation complète
- 📊 [AUDIT-RAPPORT.md](AUDIT-RAPPORT.md) - Rapport d'audit technique
- 🎨 [.github/copilot-instructions.md](.github/copilot-instructions.md) - Instructions Copilot

---

## 🛑 Arrêt

Fermez les fenêtres PowerShell ou:
```powershell
.\stop-dev.ps1
```

---

## ⚡ Performances

- ✅ Backend démarre en ~9 secondes
- ✅ Frontend compile en ~15 secondes
- ✅ Java 21 avec Virtual Threads activés
- ✅ Hot reload activé sur les 2 services

---

## 🐛 Problèmes ?

1. **Port 8080 déjà utilisé**
   ```powershell
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   ```

2. **Port 3000 déjà utilisé**
   ```powershell
   netstat -ano | findstr :3000
   taskkill /PID <PID> /F
   ```

3. **npm install échoue**
   ```bash
   cd web-frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

4. **Gradle build échoue**
   ```bash
   ./gradlew.bat clean
   ./gradlew.bat build -x test
   ```

---

**Bon développement ! 🎉**
