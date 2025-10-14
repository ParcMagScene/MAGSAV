# Système d'Avatars et Images par Défaut - MAGSAV

## Vue d'ensemble

Le système d'avatars et d'images par défaut de MAGSAV fournit une expérience utilisateur améliorée en affichant automatiquement des images appropriées lorsque les images personnalisées ne sont pas disponibles.

## Composants du Système

### 1. Service Principal (`AvatarService`)

Le service singleton `AvatarService` gère tous les avatars et images par défaut :

```java
AvatarService avatarService = AvatarService.getInstance();
```

### 2. Types d'Images Disponibles

#### Avatars Utilisateurs (6 variants)
- `avatar_default_1.svg` - Avatar bleu classique
- `avatar_default_2.svg` - Avatar rouge souriant  
- `avatar_default_3.svg` - Avatar vert neutre
- `avatar_default_4.svg` - Avatar orange avec lunettes
- `avatar_default_5.svg` - Avatar violet avec cheveux
- `avatar_default_6.svg` - Avatar turquoise avec barbe

#### Images par Défaut
- `company_logo_default.svg` - Logo générique d'entreprise
- `product_image_default.svg` - Image générique de produit  
- `image_placeholder.svg` - Placeholder générique

## Utilisation

### Avatars Utilisateurs

#### Attribution Automatique par Nom d'Utilisateur
```java
// Génère un avatar cohérent basé sur le nom d'utilisateur
ImageView avatar = avatarService.createAvatarImageView("john_doe", 32);
```

#### Attribution par ID Utilisateur
```java
// Génère un avatar cohérent basé sur l'ID
ImageView avatar = avatarService.createAvatarImageView(userId, 48);
```

#### Avatar Aléatoire
```java
Image randomAvatar = avatarService.getRandomAvatar();
```

#### Avatar Spécifique
```java
Image specificAvatar = avatarService.getAvatarByIndex(2); // Avatar #3
```

### Images par Défaut

#### Image de Produit avec Fallback
```java
// Charge l'image personnalisée ou utilise l'image par défaut
ImageView productImage = avatarService.createImageViewOrDefault(
    productImagePath,
    AvatarService.DefaultImageType.PRODUCT_IMAGE,
    200, 200
);
```

#### Logo d'Entreprise avec Fallback
```java
ImageView companyLogo = avatarService.createImageViewOrDefault(
    logoPath,
    AvatarService.DefaultImageType.COMPANY_LOGO,
    100, 100
);
```

### Utilitaires UI (`UIAvatarUtils`)

#### Composant Utilisateur Complet
```java
// Crée un composant avec avatar + nom complet + username
HBox userComponent = UIAvatarUtils.createUserComponent(
    "john_doe", 
    "John Doe", 
    32
);
```

#### Composant Utilisateur Compact
```java
// Crée un composant minimal avec avatar + nom
HBox compactUser = UIAvatarUtils.createCompactUserComponent("john_doe", 24);
```

#### Composant Produit
```java
HBox productComponent = UIAvatarUtils.createProductComponent(
    "iPhone 15",
    "/path/to/product/image.jpg",
    48
);
```

#### Composant Entreprise
```java
HBox companyComponent = UIAvatarUtils.createCompanyComponent(
    "Apple Inc.",
    "/path/to/apple/logo.png",
    40
);
```

#### Sélecteur d'Avatar
```java
VBox avatarSelector = UIAvatarUtils.createAvatarSelector(
    "current_user",
    (avatarIndex) -> {
        // Callback quand l'utilisateur choisit un nouvel avatar
        updateUserAvatar(currentUser, avatarIndex);
    }
);
```

## Intégration dans les Contrôleurs

### Dans TableView (Exemple : TechnicienUsersController)

```java
// Configuration de la colonne avatar
colAvatar.setCellValueFactory(cellData -> {
    User user = cellData.getValue();
    ImageView avatar = AvatarService.getInstance().createAvatarImageView(user.username(), 32);
    return new SimpleObjectProperty<>(avatar);
});

colAvatar.setCellFactory(col -> new TableCell<User, ImageView>() {
    @Override
    protected void updateItem(ImageView item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            setGraphic(item);
            setAlignment(Pos.CENTER);
        }
    }
});
```

### Dans ProductDetailController

```java
// Chargement automatique avec fallback
private void loadProductPhoto(String photoFilename) {
    if (photoFilename == null || photoFilename.trim().isEmpty()) {
        Image defaultImage = AvatarService.getInstance().getDefaultProductImage();
        imgPhoto.setImage(defaultImage);
        return;
    }
    // ... tentative de chargement de l'image personnalisée
    // En cas d'échec :
    Image defaultImage = AvatarService.getInstance().getDefaultProductImage();
    imgPhoto.setImage(defaultImage);
}
```

## Configuration et Personnalisation

### Ajouter de Nouveaux Avatars

1. Créer un fichier SVG dans `/src/main/resources/images/avatars/`
2. Nommer le fichier `avatar_default_X.svg` (où X est le numéro suivant)
3. Mettre à jour la constante `AVAILABLE_AVATARS` dans `AvatarService`

### Personnaliser les Images par Défaut

1. Remplacer les fichiers dans `/src/main/resources/images/defaults/`
2. Maintenir les mêmes noms de fichiers pour la compatibilité
3. Utiliser le format SVG pour une meilleure scalabilité

## Style CSS

### Styles Recommandés pour les Avatars

```css
.avatar-small {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
}

.avatar-medium {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);
}

.avatar-large {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);
}

.avatar-selected {
    -fx-effect: dropshadow(gaussian, rgba(0,100,255,0.6), 8, 0, 0, 3);
}
```

## Performance

### Cache et Optimisations

- **Préchargement** : Tous les avatars et images par défaut sont préchargés au démarrage
- **Cache mémoire** : Les images sont mises en cache pour éviter les rechargements
- **Format SVG** : Utilisation du SVG pour une scalabilité parfaite
- **Chargement asynchrone** : Images personnalisées chargées de manière asynchrone

### Bonnes Pratiques

1. **Tailles cohérentes** : Utiliser des tailles standardisées (24, 32, 48, 64, 96px)
2. **Fallback systématique** : Toujours prévoir un fallback vers l'image par défaut
3. **Lazy loading** : Charger les avatars seulement quand nécessaire
4. **Réutilisation** : Réutiliser les ImageView quand possible

## Exemples d'Intégration

### Dans une ListView
```java
listView.setCellFactory(param -> new ListCell<User>() {
    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);
        if (empty || user == null) {
            setGraphic(null);
            setText(null);
        } else {
            HBox userComponent = UIAvatarUtils.createUserComponent(
                user.username(), 
                user.fullName(), 
                24
            );
            setGraphic(userComponent);
            setText(null);
        }
    }
});
```

### Dans un ComboBox
```java
comboBox.setButtonCell(new ListCell<User>() {
    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);
        if (empty || user == null) {
            setGraphic(null);
            setText("Sélectionner un utilisateur");
        } else {
            HBox userComponent = UIAvatarUtils.createCompactUserComponent(
                user.username(), 
                20
            );
            setGraphic(userComponent);
            setText(null);
        }
    }
});
```

## Dépannage

### Problèmes Courants

1. **Images ne s'affichent pas** : Vérifier que le service est initialisé
2. **Avatars identiques** : S'assurer que les noms d'utilisateur sont différents
3. **Performance dégradée** : Vérifier que les images ne sont pas rechargées inutilement

### Logs de Debug

Le service génère des logs pour faciliter le débogage :

```
AvatarService initialisé avec succès - 6 avatars et 3 images par défaut chargées
Image non trouvée: /images/avatars/avatar_default_7.svg
```

## Évolutions Futures

- Support des avatars personnalisés uploadés par l'utilisateur
- Génération d'avatars basés sur les initiales
- Support des formats WebP et AVIF
- Système de cache disque pour les images optimisées