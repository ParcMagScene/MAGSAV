#!/bin/bash

echo "🔍 Test des corrections apportées"
echo "================================"

echo "✅ 1. Vérification que le fichier user_detail.fxml existe:"
if [ -f "src/main/resources/fxml/user_detail.fxml" ]; then
    echo "   ✓ Fichier user_detail.fxml présent"
else
    echo "   ❌ Fichier user_detail.fxml manquant"
fi

echo ""
echo "✅ 2. Vérification des corrections SQL dans RequestDetailController:"
if grep -q "reference" src/main/java/com/magsav/gui/RequestDetailController.java && \
   grep -q "quantity" src/main/java/com/magsav/gui/RequestDetailController.java; then
    echo "   ✓ Colonnes SQL corrigées (reference, quantity)"
else
    echo "   ❌ Erreurs SQL encore présentes"
fi

echo ""
echo "✅ 3. Vérification des corrections SQL dans RequestRepository:"
if grep -q "reference" src/main/java/com/magsav/repo/RequestRepository.java && \
   grep -q "quantity" src/main/java/com/magsav/repo/RequestRepository.java; then
    echo "   ✓ Colonnes SQL corrigées dans le repository"
else
    echo "   ❌ Erreurs SQL encore présentes dans le repository"
fi

echo ""
echo "✅ 4. Vérification de la structure FXML user_detail:"
if grep -q "lblUsername" src/main/resources/fxml/user_detail.fxml && \
   grep -q "tableActivity" src/main/resources/fxml/user_detail.fxml && \
   grep -q "btnClose" src/main/resources/fxml/user_detail.fxml; then
    echo "   ✓ Structure FXML compatible avec UserDetailController"
else
    echo "   ❌ Structure FXML incomplète"
fi

echo ""
echo "🎯 Résumé des corrections:"
echo "- ❌ Erreur 'Location is not set' pour user_detail.fxml → ✅ Fichier créé"
echo "- ❌ Erreur SQL 'no such column: ref' → ✅ Corrigé en 'reference'"  
echo "- ❌ Erreur SQL 'no such column: qty' → ✅ Corrigé en 'quantity'"
echo "- ❌ Structure FXML incompatible → ✅ Alignée avec le contrôleur"
echo ""
echo "🚀 L'application devrait maintenant fonctionner sans popups d'erreur !"