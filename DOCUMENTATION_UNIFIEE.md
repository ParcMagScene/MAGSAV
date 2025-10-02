# MAGSAV 1.2 — Documentation unifiée

## 1. Objectif
- Suivi SAV: produits, interventions, RMA, documents.
- Cible: macOS (Apple Silicon), Java 21. UI: JavaFX.

## 2. Architecture
- Monolithique JavaFX + SQLite (embarqué).
- Couches: gui, service, repo, model, media, label, imports.
- Logging: SLF4J + Logback.

## 3. Build
- JDK 21, Gradle 8.10.x (migration vers 9 après audit plugins).
- Déps: JavaFX 21, SQLite JDBC, HikariCP, PDFBox, OpenCSV, ZXing, SLF4J/Logback.
- Tests: JUnit 5, AssertJ, Mockito.
- Qualité (optionnel): Spotless, SpotBugs, OpenRewrite à la demande.

## 4. Base de données
- SQLite local: ~/MAGSAV/MAGSAV.db (pool Hikari).
- Migration initiale: V1__init.sql.

## 5. Flux MVP
- Recherche globale produit/intervention.
- Création intervention, historique produit.
- RMA: création/assignation.
- Documents: import, listage, lien produit/SN.
- Impression PDF simple.

## 6. Structure projet
- src/main/java/com/magsav/... (gui, service, repo, model…)
- src/main/resources/{fxml,style,db/migration}
- App.java (bootstrap JavaFX), main.fxml (layout principal)

## 7. Commandes
- Construire: ./gradlew build
- Lancer: ./gradlew run
- Tests: ./gradlew test
- OpenRewrite: ./gradlew -Prewrite rewriteDryRun

## 8. Conventions
- Java 21. Records OK; si TableView + PropertyValueFactory, prévoir getters JavaBean ou lambdas.