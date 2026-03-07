# Play Store - Assets de publication

## Structure

```
play-store/
├── README.md                          # Ce fichier
├── listing/
│   ├── fr-FR/
│   │   ├── title.txt                  # Titre (30 car. max)
│   │   ├── short_description.txt      # Description courte (80 car. max)
│   │   ├── full_description.txt       # Description complete (4000 car. max)
│   │   └── changelogs/
│   │       └── 2.txt                  # Changelog versionCode 2
│   └── en-US/
│       ├── title.txt
│       ├── short_description.txt
│       ├── full_description.txt
│       └── changelogs/
│           └── 2.txt
├── graphics/
│   ├── icon-512.svg                   # Icone haute resolution (source SVG)
│   ├── icon-512.png                   # Icone 512x512 PNG (generee)
│   ├── feature-graphic.svg            # Feature graphic (source SVG)
│   ├── feature-graphic.png            # Feature graphic 1024x500 PNG (generee)
│   └── screenshots/
│       └── GUIDE.md                   # Guide de capture des screenshots
├── data-safety.md                     # Reponses Data Safety Google Play
└── store-settings.md                  # Categorie, rating, distribution
```

## Checklist de publication

### 1. Assets graphiques
- [x] Icone 512x512 (SVG source)
- [ ] Icone 512x512 PNG (convertir avec la commande ci-dessous)
- [ ] Feature graphic 1024x500 PNG (convertir avec la commande ci-dessous)
- [ ] Screenshots telephone (min 2, max 8) - voir graphics/screenshots/GUIDE.md

### 2. Textes de la fiche
- [x] Titre FR (30 car.)
- [x] Description courte FR (80 car.)
- [x] Description complete FR
- [x] Titre EN (30 car.)
- [x] Description courte EN (80 car.)
- [x] Description complete EN
- [x] Changelog versionCode 2 (FR + EN)

### 3. Configuration Play Console
- [x] Categorie et tags (voir store-settings.md)
- [x] Content rating IARC (voir store-settings.md)
- [x] Data safety (voir data-safety.md)
- [x] Justification des permissions SMS (voir data-safety.md)
- [ ] Adresse e-mail de contact (a completer dans store-settings.md)
- [x] URL politique de confidentialite

### 4. Build
- [ ] Generer l'AAB signe : `./gradlew bundleRelease`
- [ ] Verifier le versionCode (actuel : 2)

## Commandes utiles

### Convertir les SVG en PNG (necessite Inkscape ou rsvg-convert)

```bash
# Avec rsvg-convert (apt install librsvg2-bin)
rsvg-convert -w 512 -h 512 play-store/graphics/icon-512.svg -o play-store/graphics/icon-512.png
rsvg-convert -w 1024 -h 500 play-store/graphics/feature-graphic.svg -o play-store/graphics/feature-graphic.png

# Avec Inkscape
inkscape play-store/graphics/icon-512.svg -w 512 -h 512 -o play-store/graphics/icon-512.png
inkscape play-store/graphics/feature-graphic.svg -w 1024 -h 500 -o play-store/graphics/feature-graphic.png
```

### Generer l'AAB de release

```bash
export KEYSTORE_FILE=path/to/release-keystore.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew bundleRelease
```

Le fichier AAB sera dans : `app/build/outputs/bundle/release/app-release.aab`

## Declaration SMS/MMS Google Play

Google requiert une declaration pour les apps utilisant les permissions SMS.
L'application doit etre soumise via le formulaire de declaration d'utilisation SMS :
https://support.google.com/googleplay/android-developer/answer/9047303

Cas d'utilisation a declarer : **SMS/MMS forwarding**