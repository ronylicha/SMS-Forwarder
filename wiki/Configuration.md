# Configuration

Tous les réglages se trouvent dans l'écran **Paramètres**, accessible depuis la navigation principale. Depuis v1.3.0, l'écran d'accueil est un **Dashboard** qui sert de hub vers 6 écrans : Notifications, Règles, Historique, Diagnostics, Statistiques, Réglages.

---

## Numéro de destination

C'est le numéro qui recevra tous les SMS transférés (lorsqu'aucune règle de transfert ne redirige le message vers une autre destination).

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

> Depuis v1.4.0, un **toast de confirmation** s'affiche à l'enregistrement.

---

## SMS de test

Une fois le numéro enregistré, le bouton **Envoyer un test** déclenche l'envoi d'un SMS de validation immédiat. Ce SMS passe par le pipeline complet (filtres inclus) et apparaît dans l'historique.

Le résultat s'affiche dans un message en bas de l'écran (« Envoyé » ou message d'erreur).

---

## Règles de transfert

> Disponible depuis v1.3.0. L'écran **Règles** est accessible depuis le Dashboard.

Les **règles de transfert** permettent de router différemment les messages selon l'expéditeur et/ou le contenu. Elles complètent (sans remplacer) le numéro de destination global : si aucune règle ne correspond, le message est envoyé vers la destination globale.

### CRUD complet

Depuis l'écran **Règles**, vous pouvez :

- **Créer** une règle (bouton +)
- **Modifier** une règle existante
- **Supprimer** une règle
- **Activer / désactiver** une règle sans la supprimer (interrupteur)
- **Réordonner** par priorité (les règles sont évaluées dans l'ordre)
- **Tester** une règle avec un SMS d'exemple (bouton « Tester »)

### Composition d'une règle

| Champ | Description | Exemple |
|---|---|---|
| Critère expéditeur | Expression régulière (regex) sur le numéro normalisé | `^\+33` |
| Mot-clé | Recherche insensible à la casse dans le contenu | `code` |
| Type de destination | `SMS` ou `Webhook` | `SMS` |
| Destination | Numéro de téléphone (si SMS) ou URL (si Webhook) | `+33612345678` |
| Priorité | Ordre d'évaluation (plus faible = évalué en premier) | `1` |
| Activée | Oui / Non | `Oui` |

Une règle correspond si **les deux** critères (expéditeur ET mot-clé) correspondent. Laisser un critère vide le rend toujours vérifié.

### Routage

Le pipeline consulte `MatchForwardingRuleUseCase` qui évalue les règles actives dans l'ordre de priorité. La **première** règle correspondante détermine la destination :

- **Destination SMS** → envoi d'un SMS vers le numéro configuré pour la règle
- **Destination Webhook** → envoi d'un POST HTTP JSON vers l'URL configurée

Si **aucune** règle ne correspond, le message est envoyé vers la destination globale (rétro-compatibilité v1.2.x).

---

## Webhook HTTP

> Disponible depuis v1.3.0.

Le webhook permet d'envoyer les messages vers une URL HTTP(S) au lieu d'un numéro de téléphone. Il est configuré par règle de transfert (voir ci-dessus) et utilise `HttpURLConnection` — aucune dépendance externe.

### Format du payload JSON

Le corps de la requête POST est un objet JSON :

```json
{
  "sender": "+33612345678",
  "content": "Votre code est 847291",
  "receivedAt": "2026-06-21T14:23:05Z",
  "sourceLabel": "WhatsApp",
  "originalDestination": "+33698765432"
}
```

| Champ | Type | Présence | Description |
|---|---|---|---|
| `sender` | string | Toujours | Numéro normalisé de l'expéditeur |
| `content` | string | Toujours | Contenu textuel du message |
| `receivedAt` | string (ISO 8601) | Toujours | Horodatage de réception |
| `sourceLabel` | string | Optionnel | Nom de l'app source si message d'une app tierce (WhatsApp, Telegram…) |
| `originalDestination` | string | Optionnel | Numéro de destination global configuré |

La réponse HTTP est considérée comme un succès pour tout code `2xx`.

---

## Filtres

> Les filtres existent depuis v1.0.0. Ils sont distincts des règles de transfert (v1.3.0) : les filtres décident quels messages sont traités, les règles décident où ils vont.

Les filtres permettent de contrôler quels messages sont transférés. Trois modes sont disponibles :

### Aucun filtre (par défaut)

Tous les messages reçus sont transférés, sans exception.

### Liste blanche

Seuls les messages correspondant à au moins une règle de la liste blanche sont transférés. Les autres sont bloqués et enregistrés avec le statut « Filtré ».

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

## Surveillance d'apps tierces

> Disponible depuis v1.3.0.

SMS Forwarder peut capturer les notifications d'**applications tierces** (WhatsApp, Telegram, Allo, Ringover, Onoff…) et les transférer comme des SMS ou webhooks. La **whitelist** contrôle quelles apps sont surveillées.

### Configurer la whitelist

1. Allez dans **Réglages → Surveillance d'apps**
2. Activez les applications dont vous voulez transférer les notifications
3. Chaque notification d'une app activée est traitée comme un message entrant

Le champ `sourceLabel` du payload webhook contiendra le nom de l'app source (par ex. `WhatsApp`).

> Cette fonctionnalité repose sur le `NotificationListenerService` (voir ci-dessous).

---

## Politique de retry configurable

> Disponible depuis v1.3.0.

En cas d'échec d'envoi (réseau indisponible, etc.), SMS Forwarder réessaie automatiquement. Depuis v1.3.0, la **politique de retry est configurable** dans les réglages :

| Paramètre | Plage | Valeur par défaut |
|---|---|---|
| Tentatives maximales | 1 – 10 (slider) | 3 |
| Délai initial | 30s / 1min / 5min / 15min (chips) | 30s |
| Multiplicateur de backoff | x1.5 / x2 / x3 | x2 |

Exemple avec les valeurs par défaut : tentative 1 immédiate, puis 30s, 60s, 120s…

En cas d'échec définitif, la **retransmission manuelle** reste disponible depuis l'écran de détail et depuis l'historique (bouton Renvoyer inline).

---

## Sélection de la SIM (appareils double SIM)

Sur les appareils disposant de deux cartes SIM, la section **Multi-SIM** apparaît automatiquement dans les paramètres. Trois options sont disponibles :

- **SIM par défaut** — utilise la SIM configurée par défaut dans Android pour les SMS sortants
- **SIM 1** — force l'envoi depuis le premier emplacement SIM
- **SIM 2** — force l'envoi depuis le deuxième emplacement SIM

Cette section n'apparaît pas sur les appareils mono-SIM.

---

## Accès aux notifications

> Requis pour la capture RCS (v1.0.0) et la surveillance d'apps tierces (v1.3.0).

Cette section indique si l'accès aux notifications est activé pour l'application. Cet accès est requis pour capturer les messages RCS envoyés par des applications comme Google Messages ou Samsung Messages, ainsi que les notifications d'apps tierces (WhatsApp, Telegram…).

- **Activé** (indicateur vert) — les RCS et notifications d'apps sont capturées
- **Désactivé** (indicateur rouge) — seuls les SMS classiques et les RCS passant par la base de données sont capturés

Pour activer l'accès : appuyez sur **Ouvrir les paramètres** et activez SMS Forwarder dans la liste.

---

## Sélecteur de langue

> Disponible depuis v1.4.0.

L'interface est disponible en **français** et en **anglais**. Depuis les réglages, le **sélecteur de langue** permet de basculer instantanément entre les deux, sans redémarrage.

- Au premier lancement, la langue suit celle du système
- Le changement est immédiat et persistant

---

## Diagnostics

> Disponible depuis v1.3.0.

L'écran **Diagnostics** audite l'état de l'application et propose des actions correctives :

| Contrôle | Description | Action proposée |
|---|---|---|
| Permissions | SMS, notifications, etc. | Lien vers les paramètres d'autorisations |
| Optimisation batterie | État de l'optimisation batterie | Lien vers les paramètres batterie |
| Accès aux notifications | Activé / désactivé | Lien vers l'accès aux notifications |
| Connectivité réseau | État du réseau | — |

Chaque check propose un **Intent** vers les paramètres système adapté.

---

## Conseils pour une fiabilité optimale

### Désactiver l'optimisation de la batterie

Android peut endormir les applications en arrière-plan pour économiser la batterie, ce qui risque de retarder ou manquer des SMS.

1. Ouvrez **Paramètres système > Applications > SMS Forwarder**
2. Allez dans **Batterie**
3. Sélectionnez **Non restreint** ou **Pas d'optimisation**

La procédure varie selon le constructeur (Samsung, Xiaomi, OnePlus ont leurs propres gestionnaires d'énergie).

> L'écran **Diagnostics** (v1.3.0) vérifie automatiquement ce réglage.

### Vérifier les permissions après une mise à jour Android

Une mise à jour majeure d'Android peut réinitialiser certaines permissions. Si des SMS ne sont plus transférés après une mise à jour, vérifiez que toutes les permissions restent accordées dans **Paramètres système > Applications > SMS Forwarder > Autorisations**.

---

## Prochaine étape

Pour comprendre en détail le mécanisme interne, consultez [Comment ça fonctionne](How-It-Works).
