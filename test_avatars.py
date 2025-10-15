#!/usr/bin/env python3
"""
Script pour créer des avatars de test simples pour MAGSAV
"""
from PIL import Image, ImageDraw, ImageFont
import os

# Créer le dossier s'il n'existe pas
os.makedirs('medias/avatars/users', exist_ok=True)

def create_avatar(initials, color, filename):
    """Crée un avatar simple avec des initiales"""
    size = 128
    img = Image.new('RGB', (size, size), color)
    draw = ImageDraw.Draw(img)
    
    # Utiliser une police par défaut
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", 48)
    except:
        font = ImageFont.load_default()
    
    # Centrer le texte
    bbox = draw.textbbox((0, 0), initials, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    x = (size - text_width) // 2
    y = (size - text_height) // 2
    
    draw.text((x, y), initials, fill='white', font=font)
    img.save(f'medias/avatars/users/{filename}')
    print(f"Avatar créé : {filename}")

# Créer des avatars pour les utilisateurs test
users = [
    ("R", "#3498db", "Richard.png"),
    ("JD", "#e74c3c", "Jean_Dupont.png"),
    ("JT", "#2ecc71", "Jean_Technicien.png"),
    ("TU", "#f39c12", "Test_User.png"),
    ("AD", "#9b59b6", "Admin.png"),
]

for initials, color, filename in users:
    create_avatar(initials, color, filename)

print("Génération des avatars terminée !")