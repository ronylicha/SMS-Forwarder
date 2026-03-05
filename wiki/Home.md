# SMS Forwarder Wiki

![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![License](https://img.shields.io/badge/License-AGPL%20v3-blue)

**SMS Forwarder** est une application Android qui transfère automatiquement vos SMS et messages RCS entrants vers un numéro de téléphone de votre choix. Sans compte, sans serveur, sans collecte de données — tout reste sur votre appareil.

---

## Pourquoi SMS Forwarder ?

Certains scénarios nécessitent de recevoir les SMS d'un téléphone sur un autre : téléphone professionnel non porté en permanence, double SIM, numéro dédié à la réception de codes 2FA, surveillance d'un appareil familial. SMS Forwarder répond à ce besoin de façon transparente et fiable, sans dépendre d'un service cloud tiers.

---

## Pages du wiki

### Démarrage

- [Installation](Installation) — Télécharger, installer et configurer l'application en 5 minutes
- [Configuration](Configuration) — Régler le numéro de destination, les filtres et la SIM

### Comprendre l'application

- [Comment ça fonctionne](How-It-Works) — Le mécanisme de capture, de déduplication et d'envoi expliqué simplement

### Développement

- [Compiler depuis les sources](Building-From-Source) — Prérequis, build debug et release, lancement des tests

### Référence

- [Changelog](Changelog) — Historique des versions et des fonctionnalités

---

## Fonctionnalités en un coup d'oeil

| Fonctionnalité | Détail |
|---|---|
| Capture SMS | BroadcastReceiver natif Android |
| Capture RCS | ContentObserver + NotificationListener |
| Déduplication | Fenêtre de 5 secondes, cache mémoire |
| Retry automatique | 3 tentatives, délais 2s / 4s / 8s |
| Filtrage | Liste blanche ou liste noire (numéro ou mot-clé) |
| Multi-SIM | Sélection de la carte SIM d'envoi |
| Anti-boucle | Détection automatique du numéro SIM local |
| Persistance | Foreground Service + redémarrage après reboot |

---

## Confidentialité

Aucune donnée ne quitte votre appareil via internet. Les messages transitent uniquement par le réseau GSM de votre opérateur, comme un SMS ordinaire. L'historique est stocké localement dans une base SQLite chiffrée par Android.

---

Développé par **QrCommunication** — Licence AGPL v3
