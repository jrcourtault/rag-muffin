---
name: commit
description: Analyse toutes les modifications Git en cours et propose un message de commit
---

Analyse toutes les modifications Git en cours avec `git status` et `git diff`, puis propose un message de commit adapté en français, en suivant ces règles :
- Format : type(scope): description courte
- Types : feat, fix, refactor, docs, chore, style
- Message concis en français
- Dans le message de commit, n'affiche pas les "Co-Authored-By"
- Ne propose pas de commiter : c'est important que tu ne commites pas 
