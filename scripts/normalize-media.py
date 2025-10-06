#!/usr/bin/env python3
import sys, unicodedata, re
from pathlib import Path
from shutil import copy2

def normalize(s: str) -> str:
    s = unicodedata.normalize('NFD', s)
    s = ''.join(c for c in s if unicodedata.category(c) != 'Mn')
    s = re.sub(r'[^a-zA-Z0-9]+', '_', s.strip().lower())
    return re.sub(r'^_+|_+$', '', s)

def main():
    if len(sys.argv) < 4:
        print("Usage: normalize-media.py <photos|logos> <db-dir> <fichier|dossier> [--key NOM]")
        sys.exit(1)
    kind = sys.argv[1]
    base = Path(sys.argv[2])
    src = Path(sys.argv[3])
    key = None
    if len(sys.argv) > 4 and sys.argv[4] == "--key":
        key = sys.argv[5] if len(sys.argv) > 5 else ""

    if kind not in ("photos", "logos"):
        print("Type invalide, utilisez 'photos' ou 'logos'")
        sys.exit(1)

    dest = base / kind
    dest.mkdir(parents=True, exist_ok=True)

    files = [p for p in (src.rglob('*') if src.is_dir() else [src]) if p.is_file()]
    count = 0
    for f in files:
        name = normalize(key if key else f.stem)
        ext = f.suffix.lower() or ".png"
        out = dest / f"{name}{ext}"
        copy2(f, out)
        count += 1
    print(f"OK: {count} fichier(s) copiés vers {dest}")

if __name__ == "__main__":
    main()