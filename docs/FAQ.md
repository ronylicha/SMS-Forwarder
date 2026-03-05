# FAQ — SMS Forwarder

Questions frequentes sur l'utilisation de SMS Forwarder.

---

## L'application consomme-t-elle beaucoup de batterie ?

SMS Forwarder fonctionne via un service Android de premier plan (visible par sa notification permanente). Ce type de service est concu pour avoir une empreinte reduite sur la batterie : il ne fait rien tant qu'aucun SMS n'arrive.

Dans la pratique, la consommation est negligeable sur la plupart des telephones. Si votre telephone est tres restrictif sur la batterie (marques comme Xiaomi, Huawei, Samsung avec leur mode d'economie agressive), vous devrez peut-etre exclure SMS Forwarder de l'optimisation de batterie pour garantir son bon fonctionnement en arriere-plan.

Pour ce faire : **Parametres > Applications > SMS Forwarder > Batterie > Non optimisee**.

---

## Les SMS transferes me coutent-ils de l'argent ?

Oui. Chaque transfert envoie un vrai SMS via le reseau de votre operateur. Si votre forfait inclut des SMS illimites, il n'y aura aucun cout supplementaire. Dans le cas contraire, chaque SMS transfère sera decompte comme un SMS envoye ordinaire.

Les SMS de plus de 160 caracteres sont decoupes en plusieurs parties par le reseau GSM et peuvent compter pour plusieurs SMS selon votre operateur.

---

## Mes donnees sont-elles envoyees a un serveur ?

Non. SMS Forwarder ne communique avec aucun serveur externe. Toutes vos donnees (historique, reglages, filtres) sont stockees exclusivement sur votre appareil dans une base de donnees locale. Le seul flux reseau que l'application genere est l'envoi de SMS via votre operateur, ce qui est le comportement attendu.

---

## L'application fonctionne-t-elle avec les messages RCS ?

Oui, avec une configuration supplementaire. Les messages RCS (le protocole de messagerie avance de Google et des operateurs) ne passent pas par le canal SMS traditionnel. SMS Forwarder les capture via deux mecanismes complementaires :

- **ContentObserver** : surveille la boite de reception SMS/RCS du systeme.
- **NotificationListener** : intercepte les notifications de Google Messages, Samsung Messages et AOSP Messages.

Pour activer la capture RCS, vous devez autoriser l'acces aux notifications dans **Reglages > Acces aux notifications > Configurer l'acces**, puis activer SMS Forwarder dans la liste systeme.

---

## Que se passe-t-il si je n'ai pas de reseau au moment de la reception d'un SMS ?

L'application enregistre le SMS en statut **En attente** et tente l'envoi des que le reseau est disponible. Si la tentative echoue, elle reessaie automatiquement jusqu'a 3 fois, avec un delai croissant entre chaque essai (2 secondes, puis 4 secondes, puis 8 secondes).

Au-dela de 3 tentatives, le SMS passe en statut **Echoue** et vous pouvez le renvoyer manuellement depuis l'ecran de detail de l'historique.

---

## Comment eviter une boucle de transfert ?

Une boucle se produit quand le numero de destination est le meme que le numero de votre telephone. Le SMS transfère arriverait sur votre telephone, serait a nouveau transfere, et ainsi de suite indefiniment.

SMS Forwarder integre une protection anti-boucle automatique. L'application compare le numero de destination avec le numero de votre carte SIM et bloque le transfert si une correspondance est detectee.

Si la protection automatique ne suffisait pas (cas rares, numeros avec prefixes differents), vous pouvez ajouter le numero de votre propre telephone en **liste noire** via l'ecran **Filtres**.

---

## L'application fonctionne-t-elle apres un redemarrage du telephone ?

Oui, si le transfert etait actif avant l'extinction du telephone. Au redemarrage, l'application detecte automatiquement que le service devait etre en marche et le relance. Vous n'avez rien a faire.

Si vous aviez desactive le transfert avant d'eteindre le telephone, il restera desactive au redemarrage.

---

## Puis-je filtrer les SMS par expediteur ?

Oui. Rendez-vous dans **Reglages > Gerer les regles de filtrage** ou dans l'ecran **Filtres**. Activez le mode **Liste blanche** ou **Liste noire**, puis ajoutez le numero de telephone de l'expediteur que vous souhaitez inclure ou exclure.

Vous pouvez aussi filtrer par mot-cle si vous souhaitez, par exemple, bloquer tous les SMS contenant le mot `promo` ou ne transfèrer que ceux contenant `code`.

---

## Comment exporter mon historique ?

1. Ouvrez l'ecran **Statistiques** ou **Historique**.
2. Appuyez sur le bouton **Exporter CSV**.
3. Choisissez l'emplacement de sauvegarde sur votre telephone.
4. Le fichier est enregistre au format `.csv` (lisible dans Excel, Google Sheets ou LibreOffice Calc).

Le fichier contient pour chaque SMS : l'expediteur, le contenu, la date de reception, la date de transfert, le statut et le nombre de tentatives.

---

## L'application fonctionne-t-elle en mode avion ?

Non. Le mode avion coupe le reseau GSM, ce qui empeche a la fois la reception des SMS entrants et l'envoi des SMS de transfert. Si un SMS arrive juste avant l'activation du mode avion, il sera mis en file d'attente et tente des que le reseau est retabli.

---

## Quelle version d'Android est necessaire ?

SMS Forwarder necessite **Android 8.0 (Oreo)** minimum. Les fonctionnalites de premier plan (notification permanente, redemarrage au boot) et la gestion du multi-SIM sont disponibles a partir de cette version. Le design Material You (adaptation automatique aux couleurs de votre fond d'ecran) est disponible sur Android 12 et superieur.

---

## Comment desinstaller proprement l'application ?

1. Appuyez longuement sur l'icone SMS Forwarder sur votre ecran d'accueil.
2. Appuyez sur **Desinstaller** (ou faites glisser l'icone vers la corbeille selon votre telephone).
3. Confirmez la desinstallation.

Toutes les donnees de l'application (historique, reglages, filtres) sont supprimees automatiquement lors de la desinstallation. Aucune trace ne subsiste sur votre appareil.

Si vous souhaitez conserver votre historique avant de desinstaller, pensez a l'exporter en CSV au prealable (voir la question precedente).
