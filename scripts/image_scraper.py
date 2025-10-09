#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MAGSAV Image Scraper
Script de scraping d'images pour les produits MAGSAV
Recherche et télécharge les images depuis les sites des fabricants
"""

import requests
from bs4 import BeautifulSoup
import os
import sys
import json
import time
import re
import hashlib
from urllib.parse import urljoin, urlparse
from pathlib import Path
import argparse
from typing import List, Dict, Optional, Tuple

class ImageScraper:
    """Scraper d'images pour les produits MAGSAV"""
    
    def __init__(self, medias_path: str = "medias"):
        self.medias_path = Path(medias_path)
        self.photos_path = self.medias_path / "photos"
        self.scraped_path = self.medias_path / "scraped"
        
        # Créer les dossiers nécessaires
        self.photos_path.mkdir(parents=True, exist_ok=True)
        self.scraped_path.mkdir(parents=True, exist_ok=True)
        
        # Configuration des sites de fabricants/revendeurs
        self.search_engines = {
            "google_images": "https://www.google.com/search?tbm=isch&q={query}",
            "bing_images": "https://www.bing.com/images/search?q={query}",
        }
        
        # Sites de fabricants audio/vidéo
        self.manufacturer_sites = {
            "yamaha": {
                "base_url": "https://www.yamaha.com",
                "search_url": "https://www.yamaha.com/search?q={query}",
                "img_selectors": ["img.product-image", ".product-photo img", ".gallery img"]
            },
            "sony": {
                "base_url": "https://www.sony.com",
                "search_url": "https://www.sony.com/search?q={query}",
                "img_selectors": ["img.product-hero-image", ".product-image img", ".gallery-image"]
            },
            "panasonic": {
                "base_url": "https://www.panasonic.com",
                "search_url": "https://www.panasonic.com/search?q={query}",
                "img_selectors": ["img.product-image", ".hero-image img", ".product-gallery img"]
            },
            "bose": {
                "base_url": "https://www.bose.com",
                "search_url": "https://www.bose.com/search?q={query}",
                "img_selectors": ["img.product-image", ".hero-image", ".product-hero img"]
            }
        }
        
        # Headers pour éviter la détection de bot
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'fr-FR,fr;q=0.9,en;q=0.8',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1'
        }
        
        # Délai entre les requêtes (respecter les serveurs)
        self.delay_between_requests = 2
        
    def normalize_product_name(self, name: str, manufacturer: str = "") -> str:
        """Normalise le nom du produit pour la recherche"""
        # Retirer les caractères spéciaux
        normalized = re.sub(r'[^\w\s-]', '', name)
        # Ajouter le fabricant si fourni
        if manufacturer:
            normalized = f"{manufacturer} {normalized}"
        # Remplacer les espaces par des +
        return normalized.replace(' ', '+')
    
    def get_image_filename(self, product_uid: str, image_url: str, index: int = 0) -> str:
        """Génère un nom de fichier pour l'image"""
        # Extraire l'extension de l'URL
        parsed = urlparse(image_url)
        path = parsed.path.lower()
        
        # Déterminer l'extension
        if '.jpg' in path or '.jpeg' in path:
            ext = 'jpg'
        elif '.png' in path:
            ext = 'png'
        elif '.webp' in path:
            ext = 'webp'
        elif '.gif' in path:
            ext = 'gif'
        else:
            ext = 'jpg'  # Par défaut
        
        # Générer le nom avec UID + index
        if index == 0:
            return f"{product_uid}_scraped.{ext}"
        else:
            return f"{product_uid}_scraped_{index}.{ext}"
    
    def download_image(self, image_url: str, filename: str) -> bool:
        """Télécharge une image"""
        try:
            response = requests.get(image_url, headers=self.headers, timeout=30)
            response.raise_for_status()
            
            # Vérifier que c'est bien une image
            content_type = response.headers.get('content-type', '')
            if not content_type.startswith('image/'):
                print(f"⚠️  Contenu non-image: {content_type}")
                return False
            
            # Vérifier la taille minimale (éviter les pixels de tracking)
            if len(response.content) < 1024:  # Moins de 1KB
                print(f"⚠️  Image trop petite: {len(response.content)} bytes")
                return False
            
            # Sauvegarder l'image
            filepath = self.scraped_path / filename
            with open(filepath, 'wb') as f:
                f.write(response.content)
            
            print(f"✅ Image téléchargée: {filename} ({len(response.content)} bytes)")
            return True
            
        except Exception as e:
            print(f"❌ Erreur téléchargement {image_url}: {e}")
            return False
    
    def extract_images_from_page(self, url: str, selectors: List[str]) -> List[str]:
        """Extrait les URLs d'images d'une page"""
        try:
            response = requests.get(url, headers=self.headers, timeout=30)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            image_urls = []
            
            # Essayer chaque sélecteur
            for selector in selectors:
                images = soup.select(selector)
                for img in images:
                    src = img.get('src') or img.get('data-src') or img.get('data-lazy-src')
                    if src:
                        # Convertir en URL absolue
                        abs_url = urljoin(url, src)
                        if abs_url not in image_urls:
                            image_urls.append(abs_url)
            
            # Fallback: chercher toutes les images de bonne taille
            if not image_urls:
                all_images = soup.find_all('img')
                for img in all_images:
                    src = img.get('src') or img.get('data-src')
                    if src:
                        abs_url = urljoin(url, src)
                        # Filtrer par taille et nom
                        if (any(size in src for size in ['large', 'big', 'main', 'hero', 'product']) or
                            any(dim in src for dim in ['800', '600', '1000', '1200'])):
                            if abs_url not in image_urls:
                                image_urls.append(abs_url)
            
            return image_urls[:5]  # Limiter à 5 images max
            
        except Exception as e:
            print(f"❌ Erreur extraction {url}: {e}")
            return []
    
    def search_google_images(self, query: str) -> List[str]:
        """Recherche d'images via Google (méthode simple)"""
        try:
            # Note: Google Images nécessite des méthodes plus sophistiquées
            # pour un usage en production (API, Selenium, etc.)
            search_url = f"https://www.google.com/search?tbm=isch&q={query}"
            
            response = requests.get(search_url, headers=self.headers, timeout=30)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # Extraction simple des URLs d'images
            image_urls = []
            img_tags = soup.find_all('img')
            
            for img in img_tags:
                src = img.get('src')
                if src and 'gstatic.com' not in src and src.startswith('http'):
                    image_urls.append(src)
                    if len(image_urls) >= 3:
                        break
            
            return image_urls
            
        except Exception as e:
            print(f"❌ Erreur recherche Google: {e}")
            return []
    
    def scrape_product_images(self, product_name: str, manufacturer: str = "", 
                            product_uid: str = "", max_images: int = 3) -> Dict:
        """
        Scrape les images d'un produit
        
        Args:
            product_name: Nom du produit
            manufacturer: Fabricant (optionnel)
            product_uid: UID du produit pour nommer les fichiers
            max_images: Nombre maximum d'images à télécharger
        
        Returns:
            Dict avec les résultats du scraping
        """
        print(f"\n🔍 Recherche d'images pour: {product_name} ({manufacturer})")
        
        if not product_uid:
            product_uid = hashlib.md5(f"{product_name}_{manufacturer}".encode()).hexdigest()[:8]
        
        results = {
            'product_name': product_name,
            'manufacturer': manufacturer,
            'product_uid': product_uid,
            'scraped_images': [],
            'errors': [],
            'success': False
        }
        
        # Normaliser la requête de recherche
        search_query = self.normalize_product_name(product_name, manufacturer)
        print(f"🔍 Requête de recherche: {search_query}")
        
        downloaded_count = 0
        
        # 1. Essayer les sites de fabricants spécifiques
        if manufacturer.lower() in self.manufacturer_sites:
            print(f"🏭 Recherche sur le site {manufacturer}...")
            site_info = self.manufacturer_sites[manufacturer.lower()]
            
            try:
                search_url = site_info["search_url"].format(query=search_query)
                image_urls = self.extract_images_from_page(search_url, site_info["img_selectors"])
                
                for i, img_url in enumerate(image_urls):
                    if downloaded_count >= max_images:
                        break
                    
                    filename = self.get_image_filename(product_uid, img_url, i)
                    if self.download_image(img_url, filename):
                        results['scraped_images'].append({
                            'filename': filename,
                            'source_url': img_url,
                            'source': f"{manufacturer}_official"
                        })
                        downloaded_count += 1
                    
                    time.sleep(self.delay_between_requests)
                    
            except Exception as e:
                error_msg = f"Erreur site {manufacturer}: {e}"
                results['errors'].append(error_msg)
                print(f"❌ {error_msg}")
        
        # 2. Recherche Google Images si pas assez d'images
        if downloaded_count < max_images:
            print("🔍 Recherche Google Images...")
            try:
                google_images = self.search_google_images(search_query)
                
                for i, img_url in enumerate(google_images):
                    if downloaded_count >= max_images:
                        break
                    
                    filename = self.get_image_filename(product_uid, img_url, downloaded_count)
                    if self.download_image(img_url, filename):
                        results['scraped_images'].append({
                            'filename': filename,
                            'source_url': img_url,
                            'source': 'google_images'
                        })
                        downloaded_count += 1
                    
                    time.sleep(self.delay_between_requests)
                    
            except Exception as e:
                error_msg = f"Erreur Google Images: {e}"
                results['errors'].append(error_msg)
                print(f"❌ {error_msg}")
        
        results['success'] = downloaded_count > 0
        print(f"✅ Images téléchargées: {downloaded_count}/{max_images}")
        
        return results

def main():
    """Point d'entrée principal"""
    parser = argparse.ArgumentParser(description='MAGSAV Image Scraper')
    parser.add_argument('--product', required=True, help='Nom du produit')
    parser.add_argument('--manufacturer', help='Fabricant du produit')
    parser.add_argument('--uid', help='UID du produit')
    parser.add_argument('--max-images', type=int, default=3, help='Nombre max d\'images')
    parser.add_argument('--medias-path', default='medias', help='Chemin vers le dossier medias')
    parser.add_argument('--output-json', help='Fichier JSON de sortie')
    
    args = parser.parse_args()
    
    # Créer le scraper
    scraper = ImageScraper(args.medias_path)
    
    # Scraper les images
    results = scraper.scrape_product_images(
        product_name=args.product,
        manufacturer=args.manufacturer or "",
        product_uid=args.uid or "",
        max_images=args.max_images
    )
    
    # Afficher les résultats
    print(f"\n📊 RÉSULTATS:")
    print(f"   Produit: {results['product_name']}")
    print(f"   Fabricant: {results['manufacturer']}")
    print(f"   Images trouvées: {len(results['scraped_images'])}")
    print(f"   Erreurs: {len(results['errors'])}")
    print(f"   Succès: {results['success']}")
    
    if results['scraped_images']:
        print(f"\n📁 Images téléchargées:")
        for img in results['scraped_images']:
            print(f"   • {img['filename']} (source: {img['source']})")
    
    if results['errors']:
        print(f"\n⚠️  Erreurs:")
        for error in results['errors']:
            print(f"   • {error}")
    
    # Sauvegarder en JSON si demandé
    if args.output_json:
        with open(args.output_json, 'w', encoding='utf-8') as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"\n💾 Résultats sauvegardés: {args.output_json}")
    
    # Code de sortie
    sys.exit(0 if results['success'] else 1)

if __name__ == "__main__":
    main()