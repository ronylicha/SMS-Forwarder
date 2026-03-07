# Guide de capture des screenshots Play Store

## Specifications Google Play

| Type | Dimensions | Format | Min | Max |
|------|-----------|--------|-----|-----|
| Telephone | 1080x1920 (ou 1920x1080) | PNG/JPEG | 2 | 8 |
| Tablette 7" | 1200x1920 | PNG/JPEG | 0 | 8 |
| Tablette 10" | 1600x2560 | PNG/JPEG | 0 | 8 |

## Screenshots recommandes (8 ecrans)

Capturer les ecrans suivants sur un emulateur ou appareil en mode portrait (1080x1920) :

### 1. dashboard-active.png
**Ecran principal avec le transfert actif**
- Toggle ON (vert)
- Compteur affichant quelques SMS transferes
- Numero de destination visible

### 2. dashboard-inactive.png
**Ecran principal avec le transfert inactif**
- Toggle OFF
- Montrer l'etat initial propre

### 3. history-list.png
**Historique des SMS**
- Liste avec plusieurs SMS transferes
- Mix de statuts (succes, echec, en attente)
- Montrer la barre de recherche

### 4. sms-detail.png
**Detail d'un SMS transfere**
- Expediteur, contenu, horodatage
- Statut du transfert
- Bouton de retransmission visible

### 5. settings.png
**Ecran de configuration**
- Numero de destination
- Selection SIM
- Options du service

### 6. filters.png
**Ecran de gestion des filtres**
- Quelques regles en liste blanche et liste noire
- Interface de creation de regle

### 7. stats.png
**Ecran de statistiques**
- Graphique journalier avec donnees
- Resume (total, succes, echecs)

### 8. widget.png
**Widget sur l'ecran d'accueil**
- Widget SMS Forwarder sur un ecran d'accueil Android
- Etat actif visible

## Conseils de capture

1. Utiliser un emulateur Pixel 7 (API 34) pour la resolution standard
2. Activer le theme Material You avec une couleur bleue
3. Peupler la base avec des donnees de demo variees avant capture
4. Capturer en mode clair ET mode sombre (optionnel, mais valorisant)
5. Utiliser `adb shell screencap -p /sdcard/screenshot.png` ou l'outil de capture d'Android Studio

## Outils d'habillage (optionnel)

Pour encadrer les screenshots dans un cadre de telephone :
- https://screenshots.pro
- https://mockuphone.com
- Figma avec un template de device frame