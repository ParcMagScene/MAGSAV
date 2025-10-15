#!/bin/bash
# Script pour créer un logo temporaire simple
convert -size 120x40 xc:transparent \
    -font Arial-Bold -pointsize 14 -fill "#2E86AB" \
    -gravity center -annotate +0-5 "Mag Scène" \
    -font Arial -pointsize 10 -fill "#A23B72" \
    -gravity center -annotate +0+8 "SAV" \
    -bordercolor "#2E86AB" -border 2 \
    temp_logo.gif

# Si ImageMagick n'est pas disponible, créons un fichier plus volumineux
if [ ! -f temp_logo.gif ]; then
    echo "ImageMagick non disponible, création d'un logo de base"
fi