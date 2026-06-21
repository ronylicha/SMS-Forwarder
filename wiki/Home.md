# SMS Forwarder Wiki

![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![License](https://img.shields.io/badge/License-AGPL%20v3-blue)
![Version](https://img.shields.io/badge/Version-1.4.0-blue)

**SMS Forwarder** est une application Android qui transfère automatiquement vos SMS et messages RCS entrants vers un numéro de téléphone de votre choix ou un webhook HTTP. Sans compte, sans serveur, sans collecte de données — tout reste sur votre appareil.

---

## Pourquoi SMS Forwarder ?

Certains scénarios nécessitent de recevoir les SMS d'un téléphone sur un autre : téléphone professionnel non porté en permanence, double SIM, numéro dédié à la réception de codes 2FA, surveillance d'un appareil familial. SMS Forwarder répond à ce besoin de façon transparente et fiable, sans dépendre d'un service cloud tiers.

---

## Pages du wiki

### Démarrage

- [Installation](Installation) — Télécharger, installer et configurer l'application en 5 minutes
- [Configuration](Configuration) — Régler le numéro de destination, les règles de transfert, les filtres, le webhook et la SIM
- [Guide utilisateur complet](../docs/USER_GUIDE.md) — Documentation détaillée pour utilisateurs

### Comprendre l'application

- [Comment ça fonctionne](How-It-Works) — Le mécanisme de capture, de déduplication, de routage et d'envoi expliqué simplement

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
| Règles de transfert | CRUD complet, critère expéditeur (regex) + mot-clé, destination par règle |
| Webhook HTTP | Envoi POST JSON vers une URL configurable, sans dépendance externe |
| Retry automatique | Politique configurable (tentatives max, délai initial, multiplicateur backoff) |
| Filtrage | Liste blanche ou liste noire (numéro ou mot-clé) |
| Multi-SIM | Sélection de la carte SIM d'envoi |
| Anti-boucle | Détection automatique du numéro SIM local |
| Surveillance d'apps tierces | Whitelist d'apps (WhatsApp, Telegram, etc.) transférées comme SMS |
| Dashboard temps réel | Statistiques sur 24h, taux de succès, règles actives, badge notifications |
| Centre de notifications | Alertes persistées en base (erreur de règle, destination injoignable, quota, batterie) |
| Diagnostics | Audit des permissions, batterie, notifications et connectivité réseau |
| Internationalisation | Interface disponible en français et anglais, sélecteur de langue |
| Persistance | Foreground Service + redémarrage après reboot |

---

## Nouveautés v1.4.0

- **i18n FR/EN** — l'interface est désormais disponible en français et anglais
- **Sélecteur de langue** — bascule instantanée entre FR/EN depuis les réglages
- **Toasts de confirmation** — retour visuel immédiat sur chaque action importante
- **Dashboard temps réel** et **règles de transfert** (depuis v1.3.0)

Voir le [Changelog](Changelog) pour l'historique complet.

---

## Confidentialité

Aucune donnée ne quitte votre appareil via internet. Les messages transitent uniquement par le réseau GSM de votre opérateur (SMS) ou vers le webhook que vous configurez (HTTPS). L'historique est stocké localement dans une base SQLite chiffrée par Android.

---

Développé par **QrCommunication** — Licence AGPL v3
