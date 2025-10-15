#!/usr/bin/env python3
"""
Script pour générer des QRCodes de test pour MAGSAV
"""
import qrcode
import os

# Créer le dossier s'il n'existe pas
os.makedirs('medias/qrcodes', exist_ok=True)

# QRCodes pour produits
products = [
    ("PROD001", "Amplificateur Wireless 240"),
    ("PROD002", "Filtre hydraulique haute pression"),
    ("PROD003", "Joint torique étanchéité"),
]

for prod_id, prod_name in products:
    qr = qrcode.QRCode(version=1, box_size=10, border=5)
    qr.add_data(f"MAGSAV-PRODUCT:{prod_id}:{prod_name}")
    qr.make(fit=True)
    
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(f'medias/qrcodes/product_{prod_id}.png')
    print(f"QRCode généré : product_{prod_id}.png")

# QRCodes pour véhicules
vehicles = [
    ("VEH001", "Camion Volvo FH16"),
    ("VEH002", "Tracteur John Deere 6120M"),
]

for veh_id, veh_name in vehicles:
    qr = qrcode.QRCode(version=1, box_size=10, border=5)
    qr.add_data(f"MAGSAV-VEHICLE:{veh_id}:{veh_name}")
    qr.make(fit=True)
    
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(f'medias/qrcodes/vehicle_{veh_id}.png')
    print(f"QRCode généré : vehicle_{veh_id}.png")

print("Génération des QRCodes terminée !")