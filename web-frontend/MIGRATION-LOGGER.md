# Guide de Migration vers Logger Service

## 📋 Modifications à Apporter

### 1. api.service.ts

**AVANT** (lignes avec console.log/warn/error):
```typescript
console.log('🌐 [API REQUEST]', { method, url, data });
console.log('🔐 [AUTH] Token ajouté à la requête');
console.warn('⚠️ [AUTH] Aucun token trouvé dans localStorage');
console.error('❌ [API REQUEST ERROR]', error);
console.log('✅ [API RESPONSE]', { method, url, status, data });
console.error('❌ [API ERROR]', { method, url, status, error });
console.warn('🔒 [AUTH] Non authentifié - Redirection vers login');
console.log(`📥 [API.GET] Appel: ${url}`);
console.log(`📦 [API.GET] Réponse reçue:`, response.data);
console.log(`📤 [API.POST] Appel: ${url}`, { data });
console.log(`📦 [API.POST] Réponse reçue:`, response.data);
console.log(`🔄 [API.PUT] Appel: ${url}`, { data });
console.log(`📦 [API.PUT] Réponse reçue:`, response.data);
```

**APRÈS** (avec logger service):
```typescript
import logger from './logger.service';

// Remplacer les console.log par logger
logger.apiRequest(method, url, data);
logger.auth('Token ajouté à la requête');
logger.auth('Aucun token trouvé dans localStorage');
logger.error('API REQUEST ERROR', error);
logger.apiResponse(method, url, response.status, response.data);
logger.apiError(method, url, error);
logger.auth('Non authentifié - Redirection vers login');
```

---

### 2. config.service.ts

**AVANT**:
```typescript
console.log('Configuration sauvegardée:', config);
```

**APRÈS**:
```typescript
import logger from './logger.service';
logger.info('Configuration sauvegardée:', config);
```

---

### 3. EquipmentContext.tsx

**AVANT**:
```typescript
console.error('Erreur chargement équipements:', err);
```

**APRÈS**:
```typescript
import logger from '../services/logger.service';
logger.error('Erreur chargement équipements:', err);
```

---

### 4. Vehicles.tsx

**AVANT**:
```typescript
console.log('🚗 [VEHICLES] Composant monté');
console.log('🚗 [VEHICLES] État actuel:', { ... });
console.error('Erreur lors de la mise à jour:', error);
```

**APRÈS**:
```typescript
import logger from '../services/logger.service';
logger.info('VEHICLES - Composant monté');
logger.debug('VEHICLES - État actuel:', { ... });
logger.error('Erreur lors de la mise à jour:', error);
```

---

## 🎯 Avantages du Logger Service

### ✅ Automatiquement désactivé en production
```typescript
// NODE_ENV=production → Aucun log dans la console
// NODE_ENV=development → Tous les logs actifs
```

### ✅ Meilleure lisibilité
```
[14:30:45] ℹ️ [INFO] Configuration sauvegardée: {...}
[14:30:46] 🌐 [DEBUG] API REQUEST GET /api/equipment
[14:30:46] ✅ [DEBUG] API RESPONSE 200 {...}
```

### ✅ Filtrage par niveau
```typescript
// En production, seuls WARN et ERROR sont actifs
// En développement, tous les niveaux sont actifs
```

### ✅ Performance tracking
```typescript
logger.performance('Chargement équipements', 234); // ⚡ 234ms
logger.performance('Requête lente', 1500);        // 🐌 1500ms
```

### ✅ Groupes logiques
```typescript
logger.group('Traitement commande #1234');
logger.info('Validation des données');
logger.info('Calcul du total');
logger.info('Enregistrement en base');
logger.groupEnd();
```

---

## 🚀 Commandes de Remplacement

### Rechercher tous les console.log
```bash
cd web-frontend
grep -r "console\." src/ --include="*.ts" --include="*.tsx"
```

### Script de remplacement automatique (PowerShell)
```powershell
$files = Get-ChildItem -Path "web-frontend\src" -Recurse -Include *.ts,*.tsx

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    
    # Remplacements basiques
    $content = $content -replace "console\.log\(", "logger.debug("
    $content = $content -replace "console\.info\(", "logger.info("
    $content = $content -replace "console\.warn\(", "logger.warn("
    $content = $content -replace "console\.error\(", "logger.error("
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
}
```

---

## ⚠️ Notes Importantes

1. **Ne pas tout migrer d'un coup** - Migrer fichier par fichier
2. **Tester après chaque migration** - Vérifier que tout fonctionne
3. **Garder console.error pour les vraies erreurs** - Les erreurs critiques
4. **Utiliser logger.debug pour les détails** - Remplacer console.log
5. **Ajouter l'import au début** - `import logger from './logger.service'`

---

## 📊 Priorité de Migration

### 🔴 Priorité HAUTE (Faire en premier)
- ✅ api.service.ts (22 occurrences)
- ✅ EquipmentContext.tsx (1 occurrence)

### 🟡 Priorité MOYENNE
- ✅ config.service.ts (1 occurrence)
- ✅ Vehicles.tsx (3 occurrences)

### 🟢 Priorité BASSE (Optionnel)
- Autres composants avec logs occasionnels

---

## 🧪 Test du Logger

Créer un fichier de test:

```typescript
// logger.test.ts
import logger from './logger.service';

// Test de tous les niveaux
logger.debug('Test debug', { data: 'valeur' });
logger.info('Test info');
logger.warn('Test warning');
logger.error('Test error');

// Test API logs
logger.apiRequest('GET', '/api/test', null);
logger.apiResponse('GET', '/api/test', 200, { result: 'ok' });
logger.apiError('POST', '/api/fail', new Error('Échec'));

// Test performance
logger.performance('Opération rapide', 50);
logger.performance('Opération lente', 2000);

// Test groupes
logger.group('Test Groupe');
logger.info('Log 1');
logger.info('Log 2');
logger.groupEnd();
```

---

**Fin du guide de migration**
