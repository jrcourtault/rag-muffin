---
name: review
description: Revue de code des modifications en cours ou d'une branche avant merge dans main
user-invocable: true
allowed-tools: Bash(git:*), Read, Grep, Glob
argument-hint: "[branch-name]"
---

Effectue une revue de code ciblée sur les modifications en cours.

## Étape 1 : Identifier les modifications

Analyser les modifications de la branche courante vs `main` :

```bash
git diff main...HEAD --name-only
git diff main...HEAD -U5
git log main..HEAD --oneline
```

Afficher un résumé :

- Fichiers modifiés (nombre et liste)
- Lignes ajoutées / supprimées
- Commits inclus

## Étape 2 : Lire et analyser les fichiers modifiés

Lire chaque fichier modifié pour comprendre le contexte complet des changements.

## Étape 3 : Revue par catégorie

Produire un rapport structuré en français. **N'afficher que les problèmes détectés** — ne pas lister les points OK.

### 🔴 Sécurité (critique)

- Secrets ou clés API en dur dans le code
- Requêtes Qdrant sans filtre `workspace_id` — **invariant de sécurité absolu**
- Endpoints API sans vérification du workspace ou du rôle
- Failles d'injection SQL, XSS, command injection
- Données sensibles (clés API, tokens) exposées dans les logs

### 🟠 Conventions backend (CLAUDE.md)

- Records Java pour les DTOs (pas de classes mutables)
- `@RequiredArgsConstructor` pour l'injection (pas de `@Autowired`, pas de constructeur explicite)
- Entités JPA : `@Getter` + `@NoArgsConstructor(access = PROTECTED)` + `@Builder` sur constructeur privé — pas de
  `@Setter`
- Mapper MapStruct avec `INSTANCE` statique et `unmappedTargetPolicy = ReportingPolicy.ERROR`
- `@Transactional(rollbackFor = Exception.class)` sur les contrôleurs, `@Transactional(readOnly = true)` sur les GET
- Pas de `ResponseEntity` — retourner directement le DTO
- Pas d'exceptions checked custom — utiliser `ResponseStatusException`
- `Optional` plutôt que `null`
- `TIMESTAMP WITH TIME ZONE` en SQL (jamais `TIMESTAMP` sans timezone)
- Clés étrangères avec `ON DELETE CASCADE`
- Migrations Liquibase database-first, `ddl-auto: validate` uniquement
- Fichier `.http` créé pour chaque nouveau contrôleur
- Tests MockMvc avec text blocks JSON et données SQL dans `src/test/resources/sql/`

### 🟠 Conventions frontend (CLAUDE.md)

- Standalone components (pas de NgModules)
- Signals pour le state management (RxJS uniquement pour les appels HTTP)
- Imports avec alias `@/` (sauf `./` pour le même dossier)
- Pas de lifecycle hooks (`ngOnInit`, `ngOnDestroy`…) — utiliser `resource()`, `effect()`, `DestroyRef`
- Toutes les clés i18n présentes dans `fr.json` ET `en.json`
- Appels API via le service généré dans `src/api/backend/`

### 🟡 Qualité

- Code mort ou inutilisé
- TODO / FIXME oubliés
- Duplication de code
- Logique métier dans un contrôleur (doit être dans le service)
- `console.log` oubliés côté frontend

### 🔵 Suggestions

- Améliorations de lisibilité ou de maintenabilité
- Optimisations possibles (requêtes N+1, appels bloquants…)

## Étape 4 : Verdict

Conclure avec :

**Verdict :** `APPROUVÉ` / `MODIFICATIONS REQUISES` / `COMMENTAIRES`

**Problèmes par sévérité :**
| Sévérité | Nombre |
|----------|--------|
| 🔴 Critique | X |
| 🟠 Majeur | Y |
| 🟡 Mineur | Z |
| 🔵 Suggestion | W |

**Corrections prioritaires** (si des problèmes existent) :

1. [Le plus critique avec chemin de fichier et numéro de ligne]
2. ...
