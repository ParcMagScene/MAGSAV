# Service d'Autocomplétion d'Adresse - Guide d'Utilisation

## Vue d'ensemble

Le service d'autocomplétion d'adresse MAGSAV utilise l'API gouvernementale française gratuite `api-adresse.data.gouv.fr` pour fournir des suggestions d'adresses en temps réel.

## Fonctionnalités

✨ **Autocomplétion progressive**: Suggestions à partir de 3 caractères  
🎯 **Validation d'adresse**: Vérification du format français  
🚀 **Recherche asynchrone**: Pas de blocage de l'interface  
🇫🇷 **Base de données officielle**: Données gouvernementales à jour  
⚡ **Performance optimisée**: Limite de 8 résultats maximum  

## Utilisation rapide

### Option 1: Utilitaire simple
```java
import com.magsav.util.AddressAutocompleteUtil;

// Pour un TextField
AddressAutocompleteUtil.setupFor(monChampAdresse);

// Pour un TextArea
AddressAutocompleteUtil.setupFor(monTextAreaAdresse);
```

### Option 2: Service direct
```java
import com.magsav.service.AddressService;

AddressService addressService = new AddressService();
addressService.setupAddressAutocomplete(monTextField);
```

## Intégration dans les contrôleurs

### Dans la méthode initialize()
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    // Autres initialisations...
    
    // Ajouter autocomplétion aux champs d'adresse
    if (txtAdresse != null) {
        AddressAutocompleteUtil.setupFor(txtAdresse);
    }
}
```

### Pour les formulaires dynamiques
```java
private void createAddressField() {
    TextField adresseField = new TextField();
    adresseField.setPromptText("Adresse complète");
    
    // Ajouter l'autocomplétion immédiatement
    AddressAutocompleteUtil.setupFor(adresseField);
    
    // Ajouter au formulaire...
}
```

## Comportement utilisateur

1. **Saisie**: L'utilisateur tape au moins 3 caractères
2. **Recherche**: Requête automatique vers l'API française
3. **Suggestions**: Menu déroulant avec maximum 8 résultats
4. **Sélection**: Clic sur une suggestion pour auto-compléter
5. **Navigation**: Échap pour fermer, flèches pour naviguer

## Validation d'adresse

```java
String adresse = "123 rue de la Paix, 75001 Paris";
boolean estValide = AddressAutocompleteUtil.isValidFrenchAddress(adresse);
```

### Critères de validation
- Présence d'un numéro
- Mots-clés de voirie (rue, avenue, place, etc.)
- Code postal français (5 chiffres)

## Champs actuellement configurés

✅ **ManufacturerFormController**: `taAdresse` (TextArea)  
✅ **SuppliersController**: `adresseField` (TextField dynamique)  
✅ **ExternalSavController**: `adresseField` (TextField - création et modification)  
✅ **PreferencesController**: `txtCompanyAddress` (TextField société)  

## API et performance

- **Endpoint**: `https://api-adresse.data.gouv.fr/search/`
- **Timeout**: 5 secondes (connexion et lecture)
- **Limite**: 8 résultats maximum par requête
- **Seuil**: 3 caractères minimum
- **Threading**: Recherche asynchrone non-bloquante

## Format des données retournées

```java
AddressSuggestion suggestion = ...;
String adresseComplete = suggestion.getFullAddress();  // "123 Rue de la Paix, 75001 Paris"
String rue = suggestion.getStreet();                   // "Rue de la Paix" 
String ville = suggestion.getCity();                   // "Paris"
String codePostal = suggestion.getPostalCode();        // "75001"
double latitude = suggestion.getLatitude();            // 48.8566
double longitude = suggestion.getLongitude();          // 2.3522
```

## Test et débogage

### Application de test
```bash
cd /Users/reunion/MAGSAV-1.2
./gradlew test --console=plain -Dtest.single=AddressServiceTestApp
```

### Logs de débogage
Les erreurs de réseau sont loggées dans la console :
```
Erreur lors de la recherche d'adresse: Connection timeout
```

## Limitations et considérations

⚠️ **Connexion Internet**: Nécessaire pour l'autocomplétion  
⚠️ **API Rate Limiting**: Pas de limite officielle mais usage raisonnable  
⚠️ **Données françaises uniquement**: Optimisé pour les adresses françaises  
⚠️ **Timeout**: 5 secondes maximum par requête  

## Dépendances

- **Aucune dépendance externe**: Utilise uniquement l'API Java standard
- **JavaFX**: Pour les composants d'interface
- **JSON parsing**: Parser manuel sans dépendance Gson

## Maintenance

- **API Stable**: Service gouvernemental officiel
- **Pas de clé API**: Service public gratuit
- **Mise à jour**: Données mises à jour régulièrement par l'État

---

*Service développé pour MAGSAV 1.2 - Octobre 2025*