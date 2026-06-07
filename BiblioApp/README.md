# 📚 Gestion Bibliothèque - JavaFX + SQLite

## Description
Application de gestion de bibliothèque développée en Java avec JavaFX et SQLite (JDBC).

## Fonctionnalités
- **Connexion** avec 2 rôles : Admin et Membre
- **Admin** : gestion complète des livres, membres et emprunts (CRUD)
- **Membre** : catalogue des livres, emprunt en ligne, historique personnel

## Technologies
- Java 17+
- JavaFX 21
- SQLite (via JDBC - sqlite-jdbc)
- Maven

## Structure de la base de données
| Table  | Description |
|--------|-------------|
| users  | Utilisateurs (admin + membres) |
| books  | Catalogue des livres |
| loans  | Emprunts (relation users ↔ books) |

## Prérequis
- JDK 17 ou supérieur
- Maven 3.8+

## Lancement
```bash
# Compiler et lancer
mvn javafx:run

# Ou créer un JAR exécutable
mvn package
java -jar target/BiblioApp-1.0-SNAPSHOT.jar
```

## Comptes par défaut
| Rôle  | Identifiant | Mot de passe |
|-------|-------------|--------------|
| Admin | admin       | admin123     |

> Vous pouvez créer des membres via le tableau de bord admin.

## Interfaces
1. **Login** - Identification avec routage selon le rôle
2. **Dashboard Admin** :
   - Statistiques (livres, membres, emprunts actifs)
   - Onglet Livres : CRUD + recherche
   - Onglet Membres : CRUD
   - Onglet Emprunts : création, retour, historique
3. **Dashboard Membre** :
   - Onglet Catalogue : parcourir, rechercher, emprunter
   - Onglet Mes Emprunts : historique personnel
