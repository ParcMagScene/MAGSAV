#!/usr/bin/env python3
"""
Script pour ajouter automatiquement les fallbacks CSS standards 
aux propriétés JavaFX -fx-* dans les fichiers CSS.
"""

import os
import re
from pathlib import Path

# Mapping des propriétés JavaFX vers CSS standard
CSS_MAPPINGS = {
    r'-fx-background-color:\s*([^;]+);': lambda m: f'background-color: {m.group(1)};\n    -fx-background-color: {m.group(1)};',
    r'-fx-border-color:\s*([^;]+);': lambda m: f'border-color: {m.group(1)};\n    -fx-border-color: {m.group(1)};',
    r'-fx-border-width:\s*([^;]+);': lambda m: f'border-width: {m.group(1)};\n    -fx-border-width: {m.group(1)};',
    r'-fx-border-radius:\s*([^;]+);': lambda m: f'border-radius: {m.group(1)};\n    -fx-border-radius: {m.group(1)};',
    r'-fx-font-size:\s*([^;]+);': lambda m: f'font-size: {m.group(1)};\n    -fx-font-size: {m.group(1)};',
    r'-fx-font-weight:\s*([^;]+);': lambda m: f'font-weight: {m.group(1)};\n    -fx-font-weight: {m.group(1)};',
    r'-fx-font-family:\s*([^;]+);': lambda m: f'font-family: {m.group(1)};\n    -fx-font-family: {m.group(1)};',
    r'-fx-padding:\s*([^;]+);': lambda m: f'padding: {convert_padding(m.group(1))};\n    -fx-padding: {m.group(1)};',
    r'-fx-cursor:\s*hand;': 'cursor: pointer;\n    -fx-cursor: hand;',
    r'-fx-opacity:\s*([^;]+);': lambda m: f'opacity: {m.group(1)};\n    -fx-opacity: {m.group(1)};',
    r'-fx-background:\s*([^;]+);': lambda m: f'background: {m.group(1)};\n    -fx-background: {m.group(1)};',
}

def convert_padding(fx_padding):
    """Convertit le format de padding JavaFX vers CSS standard"""
    # JavaFX: "8 16 8 16" -> CSS: "8px 16px 8px 16px" 
    if ' ' in fx_padding and not 'px' in fx_padding:
        parts = fx_padding.split()
        return ' '.join(f'{p}px' if p.isdigit() else p for p in parts)
    return fx_padding

def fix_css_file(file_path):
    """Corrige un fichier CSS en ajoutant les fallbacks"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Appliquer chaque mapping
        for pattern, replacement in CSS_MAPPINGS.items():
            if callable(replacement):
                content = re.sub(pattern, replacement, content)
            else:
                content = re.sub(pattern, replacement, content)
        
        # Sauvegarder seulement si des changements ont été faits
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ Corrigé: {file_path}")
            return True
        else:
            print(f"⚪ Aucun changement: {file_path}")
            return False
            
    except Exception as e:
        print(f"❌ Erreur dans {file_path}: {e}")
        return False

def main():
    """Corrige tous les fichiers CSS du projet"""
    css_dir = Path("/Users/reunion/MAGSAV-1.2/src/main/resources/css")
    
    if not css_dir.exists():
        print(f"❌ Répertoire CSS introuvable: {css_dir}")
        return
    
    # Trouver tous les fichiers CSS
    css_files = list(css_dir.rglob("*.css"))
    
    if not css_files:
        print("❌ Aucun fichier CSS trouvé")
        return
        
    print(f"🔍 Trouvé {len(css_files)} fichiers CSS")
    
    fixed_count = 0
    for css_file in css_files:
        if fix_css_file(css_file):
            fixed_count += 1
    
    print(f"\n🎉 Terminé! {fixed_count}/{len(css_files)} fichiers corrigés")

if __name__ == "__main__":
    main()