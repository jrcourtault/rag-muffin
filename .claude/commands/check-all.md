---
name: check-all
description: Vérifie tous les points d'attention sur l'ensemble du code existant
---

Analyse l'ensemble du code source du projet et vérifie systématiquement les points d'attention suivants :

1. Liste tous les fichiers de code du projet (Java, Vue/TypeScript, YAML, SQL, Dockerfile, docker-compose) en excluant :
   - les fichiers générés
   - les fichiers exclus par .gitignore
   - les fichiers dans les répertoires cachés comme : .mvn
   - les fichiers docker-compose-dev.yml
2. Prend note que le fichier docker-compose-dev.yml dans backend ne sera utilisé qu'en dev, donc ce n'est pas grave s'il y a des problèmes
3. Lis et analyse chaque fichier de code
4. Produis un rapport structuré en français
5. N'affiche pas les points qui sont OK

## Sécurité
- Secrets en dur (mots de passe, clés API, tokens) dans le code ou les fichiers de config versionnés
- Failles d'injection (SQL, commandes, XSS)
- Données sensibles exposées dans les logs
- Dépendances avec des vulnérabilités connues
- Ports exposés inutilement

## Isolation multi-workspace
- Requêtes SQL ou Qdrant sans filtre workspace_id
- Endpoints API sans vérification du workspace
- Risques de fuite de données entre workspaces

## Conventions du projet (CLAUDE.md)
- Respect du style de code (Google Java Format, Composition API, TypeScript)
- Optional plutôt que null
- Cohérence des noms de fichiers et packages

## Qualité du code
- Code mort ou inutilisé
- TODO / FIXME / HACK oubliés
- Duplication de code
- Fonctions trop longues ou trop complexes

## Configuration et infra
- Cohérence entre docker-compose.yml, application.yml et pom.xml
- Variables d'environnement manquantes ou en dur
- Healthchecks manquants
- Volumes persistants manquants

## Tests
- Fichiers de code sans tests correspondants
- Couverture estimée par module

## Performances
- Requêtes N+1 potentielles
- Appels bloquants dans du code async
- Ressources non fermées (streams, connexions)

## Rapport final
- Nombre total de problèmes par catégorie (critique / attention / suggestion)
- Liste priorisée des actions à mener
