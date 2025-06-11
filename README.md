# xoudouqi
# Xou Dou Qi - Jeu de la Jungle

Un jeu de stratégie traditionnel chinois implémenté en Java avec interface console.

## Description

Xou Dou Qi, également connu sous le nom de "Jeu de la Jungle" ou "Échecs des Animaux", est un jeu de stratégie pour deux joueurs. Chaque joueur contrôle huit animaux avec des capacités différentes et doit atteindre le sanctuaire adverse pour gagner.

## Fonctionnalités

- ✅ Gestion des comptes utilisateurs avec base de données SQLite
- ✅ Implémentation complète des règles du jeu
- ✅ Interface console intuitive
- ✅ Historique des parties
- ✅ Statistiques des joueurs
- ✅ Sauvegarde automatique des résultats

## Règles du jeu

### Hiérarchie des animaux (du plus fort au plus faible)
1. **Éléphant** (E) - Le plus fort
2. **Lion** (L) - Peut sauter par-dessus l'eau
3. **Tigre** (T) - Peut sauter par-dessus l'eau
4. **Panthère** (P)
5. **Chien** (C)
6. **Loup** (W)
7. **Chat** (H)
8. **Rat** (R) - Peut nager et capturer l'éléphant

### Règles spéciales
- Le **Rat** peut capturer l'**Éléphant** (exception à la hiérarchie)
- Le **Lion** et le **Tigre** peuvent sauter par-dessus l'eau
- Seul le **Rat** peut se déplacer dans l'eau
- Les **pièges** neutralisent les pièces ennemies
- Les animaux ne peuvent pas entrer dans leur propre sanctuaire
