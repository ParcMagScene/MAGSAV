#!/bin/bash

# Script de vérification de la base de données MAGSAV
DB_PATH="$HOME/MAGSAV/MAGSAV.db"

echo "=== Vérification de la base de données MAGSAV ==="
echo "Base de données: $DB_PATH"
echo

if [ ! -f "$DB_PATH" ]; then
    echo "❌ Base de données non trouvée!"
    exit 1
fi

echo "✅ Base de données trouvée"
echo

echo "=== Tables existantes ==="
sqlite3 "$DB_PATH" ".tables"
echo

echo "=== Schéma des tables ==="
echo
echo "Table: produits"
sqlite3 "$DB_PATH" ".schema produits"
echo
echo "Table: societes" 
sqlite3 "$DB_PATH" ".schema societes"
echo
echo "Table: interventions"
sqlite3 "$DB_PATH" ".schema interventions"
echo
echo "Table: categories"
sqlite3 "$DB_PATH" ".schema categories"
echo
echo "Table: requests"
sqlite3 "$DB_PATH" ".schema requests"
echo

echo "=== Statistiques des données ==="
echo "Produits: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM produits")"
echo "Sociétés: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM societes")"
echo "Interventions: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM interventions")"
echo "Catégories: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM categories")"
echo "Demandes: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM requests")"
echo

echo "=== Test de connexion ==="
if sqlite3 "$DB_PATH" "SELECT 1" > /dev/null 2>&1; then
    echo "✅ Connexion à la base de données réussie"
else
    echo "❌ Erreur de connexion à la base de données"
fi

echo
echo "=== Vérification terminée ==="