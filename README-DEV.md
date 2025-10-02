# Guide de développement MAGSAV

Ce document complète le README et cible les contributeurs.

## Construire et lancer
- Build rapide (sans tests) : via la tâche VS Code « Build MAGSAV » ou `./gradlew clean build -x test`
- Lancer l'UI JavaFX en dev : `./gradlew run` (ou tâche dédiée si présente)

## Tests
- Framework: JUnit 5, AssertJ, Mockito
- Lancer les tests: `./gradlew test`

## Qualité de code
- Checkstyle + SpotBugs: `./gradlew lint`
- Rapports: `build/reports/checkstyle` et `build/reports/spotbugs`

## Répertoires utiles
- Images produits: `photos/products`
- Logos: `photos/logos/{companies,manufacturers}`
- Documents importés: `documents/` (si présent)
- Sorties (PDF/QR): `output/`

Configuration du répertoire « photos »:
- Par défaut: `photos/` à la racine du projet.
- Surcharges possibles:
	- Propriété JVM: `-Dmagsav.photos.dir=/chemin/vers/photos`
	- Variable d’environnement: `MAGSAV_PHOTOS_DIR=/chemin/vers/photos`

## Conventions
- MVC: Controllers JavaFX fins; logique métier dans Services; accès DB dans Repositories.
- Ne pas dupliquer la logique d’IO image: utiliser `ImageLibraryService`.

### Services média et impression

- `ImageLibraryService` (statique):
	- `baseDir()`, `productsDir()`, `manufacturerLogosDir()`, `companyLogosDir()`
	- `normalizeExt(filename, fallback)`, `productPhotoDest(sn, ext)`, `copyToLibrary(src, dest)`

- `AvatarService`:
	- `computeInitials(name)`: 1–2 lettres (ou « ? »).
	- `renderInitialsPng(name, width, height)`: badge PNG arrondi (bleu #4285F4, texte blanc).

- `ManufacturerLogoService`:
	- `resolveLogo(name, w, h) -> LogoResult(pathOrNull, fallbackPngOrNull)`
	- Utilise `ManufacturerRepository`; si aucun logo présent, génère un fallback via `AvatarService`.

- `ProductQrService`:
	- `buildQrContent(codeOrSn, numeroSerie)`: string QR canonique.
	- `generateQrPng(codeOrSn, numeroSerie)`: bytes PNG (via `QRCodeService`).

- `ProductPhotoService`:
	- `importAndAssignPhoto(numeroSerie, produitOrNull, sourcePath)`: copie dans la médiathèque et met à jour la DB (par produit si fourni, sinon par SN).

- `PrintService`:
	- `generateLabel(title, qrData, logoOrNull, outDir) -> PrintResult(qrPng, pdf)`
	- Utilisé pour l’écran d’impression et la fiche produit.

### Contrôleurs JavaFX

- `ProductDetailController`:
	- Délègue QR à `ProductQrService`, logos à `ManufacturerLogoService`, photos à `ProductPhotoService`, étiquette à `PrintService`.
	- Setters d’injection (tests):
		- `setAvatarService`, `setManufacturerLogoService`, `setProductQrService`, `setProductPhotoService`, `setLabelService`, `setQrService`.
	- Getters utiles tests: `getLblCode()`, `getImgQr()`, `getImgManufacturerLogo()`.

### Tests

- Exemples de tests unitaires:
	- `AvatarServiceTest`, `ManufacturerLogoServiceTest`, `ProductQrServiceTest`, `ProductPhotoServiceTest`.

- Test JavaFX headless:
	- `ProductDetailControllerFxTest` charge `product-detail.fxml`, initialise JavaFX avec `Platform.startup`, injecte des mocks via setters et vérifie QR/labels/logos.
	- Lancer: `./gradlew test --no-build-cache --tests com.magsav.gui.ProductDetailControllerFxTest`

