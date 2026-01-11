# 🎓 Projet Java Avancé - Application LOM
## Gestion des Ressources Pédagogiques (LOM 1.0)

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-5.6.15-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)

> **Étudiant :** ILYAS AIT MAINA (4IIR - EMSI)
> **Encadrant :** Pr. ABDERRAHIM LARHLIMI

---

## � GUIDE DE DÉMARRAGE RAPIDE (POUR ÉVALUATION)

Suivez ces étapes pour lancer le projet sur votre machine.

### 1. Prérequis
*   **Java 21** (Obligatoire)
*   **Maven** 3.x
*   **Docker Desktop** (Pour la base de données MySQL)

### 2. Lancement
Ouvrez un terminal (PowerShell ou CMD) à la racine du projet `projet-lom/` et exécutez :

```bash
# 1. Démarrer la Base de Données (MySQL via Docker)
docker-compose -f docker/docker-compose.yml up -d

# 2. Lancer l'application (Compilation + Exécution)
mvn clean javafx:run
```

### 3. Identifiants de Connexion
*   **Administrateur** (Accès complet) : 
    *   User: `admin`
    *   Pass: `admin`
*   **Utilisateur** (Accès consultation) : 
    *   User: `user`
    *   Pass: `user`

---

## �📋 Table des Matières

1.  [Guide de Démarrage Rapide](#-guide-de-démarrage-rapide-pour-évaluation)
2.  [Introduction Générale](#1-introduction-générale)
3.  [Analyse et Conception](#2-analyse-et-conception)
4.  [Environnement Technique](#3-environnement-technique)
5.  [Architecture et Implémentation](#4-architecture-et-implémentation)
6.  [Interface Utilisateur et Tests](#5-interface-utilisateur-et-tests)
7.  [Structure du Projet](#7-structure-du-projet)

---

## 1. Introduction Générale

### 1.1 Contexte
Dans le cadre de la formation **4IIR à l'EMSI**, ce projet met en œuvre les concepts avancés de Java pour la digitalisation des ressources pédagogiques. Il s'appuie sur la norme **LOM (Learning Object Metadata)** (IEEE 1484.12.1) pour indexer et partager efficacement les contenus éducatifs.

### 1.2 Problématique
Les établissements font face à :
*   **Dispersion des ressources** sur différents supports.
*   **Difficulté de recherche** due au manque de métadonnées.
*   **Manque de traçabilité** des consultations.
*   **Administration complexe** sans outils centralisés.

### 1.3 Objectifs
1.  **Gestion complète (CRUD)** des ressources avec métadonnées LOM 1.0.
2.  **Authentification sécurisée** (Admin/User).
3.  **Recherche avancée** multicritères (langue, difficulté, tags).
4.  **Système de favoris** et **Historique de consultation**.
5.  **Import en masse (Batch)** via CSV avec traitement parallèle.
6.  **Statistiques** (Graphiques interactifs).
7.  **Gestion des fichiers** (Upload/Download).

---

## 2. Analyse et Conception

### 2.1 Besoins Fonctionnels

| Acteur | Actions Principales |
| :--- | :--- |
| **Administrateur** | • CRUD Ressources<br>• Gestion des Tags<br>• Import en Masse (Batch)<br>• Consultation Statistiques |
| **Utilisateur** | • Recherche filtrée<br>• Favoris (⭐) & Notation (1-5)<br>• Historique de navigation<br>• Téléchargement Fichiers |

### 2.2 Besoins Non-Fonctionnels
*   **Sécurité** : Hashage des mots de passe.
*   **Performance** : Multi-threading pour l'import et la BDD.
*   **Ergonomie** : JavaFX/FXML avec design responsive.
*   **Fiabilité** : Transactions ACID avec Hibernate.

### 2.3 Modèle de Données (LOM)
L'application implémente les 9 catégories du standard :
*   `General`, `Lifecycle`, `Meta-Metadata`, `Technical`, `Educational`, `Rights`, `Relation`, `Annotation`, `Classification`.
*   Ajouts spécifiques : `Tag` (Many-to-Many), `ResourceFile` (One-to-Many), `Favorites`, `ViewHistory`.

---

## 3. Environnement Technique

| Technologie | Version | Rôle |
| :--- | :--- | :--- |
| **Java** | 21 LTS | Langage (Records, Pattern Matching) |
| **JavaFX** | 21.0.1 | Interface Homme-Machine (FXML) |
| **Hibernate** | 5.6.15 | ORM avec Mapping XML (`.hbm.xml`) |
| **MySQL** | 8.0.33 | Base de données relationnelle |
| **Docker** | 24.x | Conteneurisation de la BDD |
| **Maven** | 3.9.x | Gestion de projet |

---

## 4. Architecture et Implémentation

### 4.1 Architecture en Couches
```
org.emsi
  ├── entities/       # Modèle de données (LomSchema, etc.)
  ├── dao/            # Accès aux données (Hibernate)
  ├── service/        # Logique métier (Singleton)
  ├── ui/             # Contrôleurs JavaFX
  ├── exceptions/     # Gestion erreurs
  └── MainApp.java    # Point d'entrée
```

### 4.2 Patterns Design
*   **Singleton** : Pour les Services et `HibernateUtil`.
*   **DAO** : Isolation de l'accès aux données (`GenericDao`).
*   **Factory** : Création centralisée d'exceptions (`ResourceException`).

### 4.3 Concepts Java Avancés (Implémentés)

#### 1. POO & Collections
Utilisation intensive de `Set` (Tags uniques), `List` (Fichiers ordonnés) et Encapsulation.

#### 2. Java Streams API
Filtrage et aggrégations complexes (ex: Statistiques).
```java
// Exemple : Compter par langue
return resources.stream()
    .filter(r -> r.getGeneral() != null)
    .collect(Collectors.groupingBy(
        r -> r.getGeneral().getLanguage(), 
        Collectors.counting()
    ));
```

#### 3. Gestion des Exceptions
Hiérarchie personnalisée `LomException` avec codes d'erreur (AUTH, DATABASE, VALIDATION).

#### 4. ThreadPool / ExecutorService
Utilisé pour l'**Import en Masse** (`BatchImportService`) afin de ne pas bloquer l'UI.
```java
// Pool de thread personnalisé
private final ExecutorService executor = new ThreadPoolExecutor(
    4, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100)
);
```

#### 5. Synchronisation
Utilisation de `ReadWriteLock` et `CountDownLatch` dans `DatabaseSyncService` pour gérer l'accès concurrent à la BDD.

#### 6. Hibernate ORM (XML Mapping)
Configuration via fichiers `.hbm.xml` pour toutes les entités.
```xml
<class name="org.emsi.entities.LomSchema" table="lom_schema">
    <one-to-one name="general" class="org.emsi.entities.General" cascade="all"/>
    <set name="tags" table="lom_tags" cascade="save-update">...</set>
</class>
```

---

## 5. Interface Utilisateur et Tests

### Fonctionnalités Clés
*   **Dashboard Admin** : Vue d'ensemble, Graphiques, Actions rapides.
*   **Import CSV** : Fenêtre modale avec barre de progression.
*   **Recherche** : Filtres dynamiques sans rechargement.
*   **Détails** : Vue par onglets des métadonnées LOM.

### Scénarios de Test
*   ✅ **Nominal** : Création ressource, Ajout favori, Recherche "Java".
*   ❌ **Erreur** : Login incorrect, Fichier import invalide, BDD coupée.

---

## 7. Structure du Projet

```
projet-lom/
├── docker/
│   ├── docker-compose.yml
│   └── init.sql
├── src/main/
│   ├── java/org/emsi/
│   │   ├── entities/ (20+ classes)
│   │   ├── dao/
│   │   ├── service/ (LomService, BatchImportService, etc.)
│   │   ├── ui/ (AdminDashboard, Login, etc.)
│   │   └── exceptions/
│   └── resources/
│       ├── fxml/ (9 vues)
│       ├── *.hbm.xml (Mappings Hibernate)
│       └── hibernate.cfg.xml
└── pom.xml
```
