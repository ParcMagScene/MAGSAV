#!/bin/bash

# Script de vérification qualité MAGSAV
# Usage: ./check-quality.sh [--fix]

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "🔍 Vérification de la qualité du code MAGSAV..."
echo "======================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ISSUES=0
WARNINGS=0

# Function to report issues
report_issue() {
    echo -e "${RED}❌ ISSUE: $1${NC}"
    ((ISSUES++))
}

report_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((WARNINGS++))
}

report_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

report_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check 1: Debug prints detection
echo
echo "1. Vérification des System.out.println..."
DEBUG_PRINTS=$(find src/main -name "*.java" -type f -exec grep -l "System\.out\.println" {} \; 2>/dev/null || true)
if [ -n "$DEBUG_PRINTS" ]; then
    report_issue "Debug prints trouvés dans le code de production:"
    echo "$DEBUG_PRINTS" | sed 's/^/   /'
else
    report_success "Aucun debug print trouvé"
fi

# Check 2: RuntimeException usage
echo
echo "2. Vérification des RuntimeException génériques..."
RUNTIME_EXCEPTIONS=$(find src/main -name "*.java" -type f -exec grep -l "throw new RuntimeException" {} \; 2>/dev/null || true)
if [ -n "$RUNTIME_EXCEPTIONS" ]; then
    report_warning "RuntimeException génériques trouvées:"
    echo "$RUNTIME_EXCEPTIONS" | sed 's/^/   /'
else
    report_success "Pas de RuntimeException générique"
fi

# Check 3: TODO comments
echo
echo "3. Vérification des TODOs..."
TODO_COUNT=$(find src -name "*.java" -type f -exec grep -c "TODO" {} \; 2>/dev/null | awk '{sum += $1} END {print sum+0}')
if [ "$TODO_COUNT" -gt 0 ]; then
    report_info "$TODO_COUNT TODOs trouvés"
    find src -name "*.java" -type f -exec grep -H "TODO" {} \; 2>/dev/null | head -5
    if [ "$TODO_COUNT" -gt 5 ]; then
        echo "   ... et $(($TODO_COUNT - 5)) autres"
    fi
else
    report_success "Aucun TODO trouvé"
fi

# Check 4: Test coverage
echo
echo "4. Vérification de la couverture de tests..."
if ./gradlew test jacocoTestReport >/dev/null 2>&1; then
    if [ -f "build/reports/jacoco/test/html/index.html" ]; then
        # Extract coverage percentage (basic parsing)
        COVERAGE=$(grep -o "Total[^%]*[0-9]\+%" build/reports/jacoco/test/html/index.html | grep -o "[0-9]\+%" | head -1 || echo "0%")
        COVERAGE_NUM=$(echo "$COVERAGE" | grep -o "[0-9]\+")
        
        if [ "$COVERAGE_NUM" -ge 80 ]; then
            report_success "Couverture de tests: $COVERAGE"
        elif [ "$COVERAGE_NUM" -ge 60 ]; then
            report_warning "Couverture de tests faible: $COVERAGE (objectif: 80%)"
        else
            report_issue "Couverture de tests insuffisante: $COVERAGE (objectif: 80%)"
        fi
    else
        report_warning "Rapport de couverture non trouvé"
    fi
else
    report_issue "Échec des tests"
fi

# Check 5: Code compilation
echo
echo "5. Vérification de la compilation..."
if ./gradlew compileJava >/dev/null 2>&1; then
    report_success "Compilation réussie"
else
    report_issue "Échec de compilation"
fi

# Check 6: File organization
echo
echo "6. Vérification de l'organisation des fichiers..."

# Check for proper package structure
MISPLACED_FILES=$(find src/main/java -name "*.java" -type f | while read file; do
    PACKAGE_LINE=$(grep "^package " "$file" | head -1)
    if [ -n "$PACKAGE_LINE" ]; then
        DECLARED_PACKAGE=$(echo "$PACKAGE_LINE" | sed 's/package //; s/;//' | tr '.' '/')
        EXPECTED_PATH="src/main/java/$DECLARED_PACKAGE"
        ACTUAL_DIR=$(dirname "$file")
        if [ "$ACTUAL_DIR" != "$EXPECTED_PATH" ]; then
            echo "$file: package doesn't match directory structure"
        fi
    fi
done)

if [ -n "$MISPLACED_FILES" ]; then
    report_warning "Fichiers mal placés:"
    echo "$MISPLACED_FILES" | sed 's/^/   /'
else
    report_success "Organisation des fichiers correcte"
fi

# Check 7: Dependencies security
echo
echo "7. Vérification des dépendances..."
if ./gradlew dependencyCheckAnalyze >/dev/null 2>&1; then
    report_success "Vérification de sécurité des dépendances complète"
else
    report_info "Plugin dependency-check non configuré (optionnel)"
fi

# Check 8: Media directory structure
echo
echo "8. Vérification de la structure des médias..."
MEDIA_DIR="$HOME/MAGSAV/medias"
if [ -d "$MEDIA_DIR" ]; then
    REQUIRED_DIRS=("photos" "logos" "qrcodes")
    MISSING_DIRS=""
    
    for dir in "${REQUIRED_DIRS[@]}"; do
        if [ ! -d "$MEDIA_DIR/$dir" ]; then
            MISSING_DIRS="$MISSING_DIRS $dir"
        fi
    done
    
    if [ -n "$MISSING_DIRS" ]; then
        report_warning "Dossiers médias manquants:$MISSING_DIRS"
    else
        report_success "Structure des médias correcte"
    fi
else
    report_warning "Dossier médias principal non trouvé: $MEDIA_DIR"
fi

# Summary
echo
echo "======================================================"
echo "📊 RÉSUMÉ DE LA VÉRIFICATION"
echo "======================================================"

if [ $ISSUES -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}🎉 Parfait! Aucun problème détecté.${NC}"
    exit 0
elif [ $ISSUES -eq 0 ]; then
    echo -e "${YELLOW}⚠️  $WARNINGS avertissement(s) trouvé(s).${NC}"
    echo "Le code est globalement bon, mais peut être amélioré."
    exit 0
else
    echo -e "${RED}❌ $ISSUES problème(s) critique(s) et $WARNINGS avertissement(s) trouvé(s).${NC}"
    echo "Des corrections sont nécessaires avant de continuer."
    exit 1
fi