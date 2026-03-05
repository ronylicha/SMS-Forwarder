# Configuration

Tous les réglages se trouvent dans l'écran **Paramètres**, accessible depuis le menu de navigation principal.

---

## Numéro de destination

C'est le numéro qui recevra tous les SMS transférés.

### Formats acceptés

| Format | Exemple | Accepté |
|---|---|---|
| International E.164 | `+33612345678` | Oui (recommandé) |
| National français | `0612345678` | Oui |
| Indicatif 0033 | `0033612345678` | Oui |
| Avec espaces | `06 12 34 56 78` | Oui |
| Avec tirets | `06-12-34-56-78` | Oui |

En interne, tous les formats sont normalisés en E.164. Le format `+33612345678` est recommandé pour éviter toute ambiguïté, en particulier pour les numéros étrangers.

### Enregistrer le numéro

1. Tapez le numéro dans le champ **Numéro de destination**
2. Le champ affiche une erreur si le format n'est pas reconnu
3. Appuyez sur **Enregistrer** (le bouton n'est actif que si le format est valide)

---

## SMS de test

Une fois le numéro enregistré, le bouton **Envoyer un test** déclenche l'envoi d'un SMS de validation immédiat. Ce SMS passe par le pipeline complet (filtres inclus) et apparaît dans l'historique.

Le résultat s'affiche dans un message en bas de l'écran ("Envoyé" ou message d'erreur).

---

## Filtres

Les filtres permettent de contrôler quels messages sont transférés. Trois modes sont disponibles :

### Aucun filtre (par défaut)

Tous les messages reçus sont transférés, sans exception.

### Liste blanche

Seuls les messages correspondant à au moins une règle de la liste blanche sont transférés. Les autres sont bloqués et enregistrés avec le statut "Filtré".

**Cas d'usage :** ne transférer que les SMS de certaines banques ou services spécifiques.

### Liste noire

Tous les messages sont transférés, **sauf** ceux qui correspondent à une règle de la liste noire.

**Cas d'usage :** bloquer les publicités d'un expéditeur particulier.

---

### Gérer les règles de filtrage

Appuyez sur **Gérer les règles de filtrage** pour accéder à l'écran dédié. Chaque règle est un **motif** de type :

| Type de motif | Exemple | Comportement |
|---|---|---|
| Numéro de téléphone | `+33612345678` ou `0612345678` | Comparaison exacte sur le numéro normalisé |
| Mot-clé | `BANQUE` ou `code` | Recherche insensible à la casse dans l'expéditeur ET le contenu |

Une règle peut être **activée ou désactivée** sans être supprimée, ce qui permet de la conserver pour un usage futur.

---

## Sélection de la SIM (appareils double SIM)

Sur les appareils disposant de deux cartes SIM, la section **Multi-SIM** apparaît automatiquement dans les paramètres. Trois options sont disponibles :

- **SIM par défaut** — utilise la SIM configurée par défaut dans Android pour les SMS sortants
- **SIM 1** — force l'envoi depuis le premier emplacement SIM
- **SIM 2** — force l'envoi depuis le deuxième emplacement SIM

Cette section n'apparaît pas sur les appareils mono-SIM.

---

## Accès aux notifications (pour les RCS)

Cette section indique si l'accès aux notifications est activé pour l'application. Cet accès est requis pour capturer les messages RCS envoyés par des applications comme Google Messages ou Samsung Messages.

- **Activé** (indicateur vert) — les RCS sont capturés via les notifications
- **Désactivé** (indicateur rouge) — seuls les SMS classiques et les RCS passant par la base de données sont capturés

Pour activer l'accès : appuyez sur **Ouvrir les paramètres** et activez SMS Forwarder dans la liste.

---

## Conseils pour une fiabilité optimale

### Désactiver l'optimisation de la batterie

Android peut endormir les applications en arrière-plan pour économiser la batterie, ce qui risque de retarder ou manquer des SMS.

1. Ouvrez **Paramètres système > Applications > SMS Forwarder**
2. Allez dans **Batterie**
3. Sélectionnez **Non restreint** ou **Pas d'optimisation**

La procédure varie selon le constructeur (Samsung, Xiaomi, OnePlus ont leurs propres gestionnaires d'énergie).

### Vérifier les permissions après une mise à jour Android

Une mise à jour majeure d'Android peut réinitialiser certaines permissions. Si des SMS ne sont plus transférés après une mise à jour, vérifiez que toutes les permissions restent accordées dans **Paramètres système > Applications > SMS Forwarder > Autorisations**.

---

## Prochaine étape

Pour comprendre en détail le mécanisme interne, consultez [Comment ça fonctionne](How-It-Works).
