# Frontend — Angular 21 + PrimeNG

Interface utilisateur du projet RAG-Muffin.

## Prérequis

- Node.js 22+
- npm 11+
- Backend Spring Boot lancé sur `http://localhost:8080` (pour la génération API)

## Installation

```bash
npm install
```

## Développement

```bash
ng serve
```

L'application est accessible sur `http://localhost:4200/` avec hot reload.

## Génération du client API (OpenAPI)

Les services et modèles Angular sont générés automatiquement depuis la spec OpenAPI du backend. Les fichiers générés sont dans `src/app/api/` (ignorés par git).

**Prérequis** : le backend doit tourner (`http://localhost:8080`).

```bash
npm run generate:api
```

Cette commande :

1. Récupère la spec OpenAPI depuis `http://localhost:8080/v3/api-docs`
2. Génère les services, modèles et interfaces TypeScript dans `src/app/api/`

**Quand régénérer ?**

- Après chaque modification d'un contrôleur, DTO ou endpoint côté backend
- Après un `git pull` si le backend a changé

**Configuration** : voir `ng-openapi-gen.json` à la racine du projet.

## Vérification des traductions (i18n)

Vérifie la cohérence entre les fichiers de traduction (`public/assets/i18n/*.json`) et le code source.

```bash
npm run check:i18n
```

Le script détecte :

- **Clés incohérentes** entre les fichiers de langue (ex: clé présente en `fr.json` mais absente de `en.json`)
- **Clés orphelines** dans les JSON mais jamais utilisées dans le code
- **Clés manquantes** utilisées dans le code (via `transloco`, `translate()`, `t()`) mais absentes des JSON

Retourne un exit code 1 en cas d'erreur (utilisable en CI).

## Build production

```bash
ng build
```

Les artefacts sont dans `dist/frontend/browser/`.

## Upgrade Frontend

Mise à jour des version mineurs :

```bash
npm update
```

Mise à jour Angular :

```bash
# Montre ce qu'il faut faire pour migrer (sans rien toucher)
ng update

# Met à jour Angular core + CLI (applique les migrations automatiques)
ng update @angular/core @angular/cli

# Met à jour les libs tierces compatibles (CDK, PrimeNG...)
ng update @angular/cdk
```

