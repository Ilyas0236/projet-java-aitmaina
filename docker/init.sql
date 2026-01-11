-- Script d'initialisation de la base de données LOM
-- Ce script est exécuté automatiquement au premier démarrage du conteneur

-- Création de la base de données (si elle n'existe pas déjà)
CREATE DATABASE IF NOT EXISTS lom_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE lom_db;

-- Accorder tous les privilèges à l'utilisateur
GRANT ALL PRIVILEGES ON lom_db.* TO 'lom_user'@'%';
FLUSH PRIVILEGES;

-- Tables Creation

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100)
) ENGINE=InnoDB;

-- 2. LomSchema (Root)
CREATE TABLE IF NOT EXISTS lom_schema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_title VARCHAR(500),
    resource_url VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB;

-- 3. General
CREATE TABLE IF NOT EXISTS lom_general (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500),
    language VARCHAR(50),
    description TEXT,
    keyword TEXT,
    coverage VARCHAR(500),
    structure VARCHAR(50),
    aggregation_level INT,
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_general_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Lifecycle
CREATE TABLE IF NOT EXISTS lom_lifecycle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(100),
    status VARCHAR(50),
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_lifecycle_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. MetaMetadata
CREATE TABLE IF NOT EXISTS lom_meta_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metadata_schema VARCHAR(100),
    language VARCHAR(50),
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_meta_metadata_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Technical
CREATE TABLE IF NOT EXISTS lom_technical (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    format VARCHAR(100),
    size VARCHAR(50),
    location VARCHAR(1000),
    installation_remarks TEXT,
    other_platform_requirements TEXT,
    duration VARCHAR(100),
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_technical_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Educational
CREATE TABLE IF NOT EXISTS lom_educational (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interactivity_type VARCHAR(50),
    learning_resource_type VARCHAR(100),
    interactivity_level INT,
    semantic_density INT,
    intended_end_user_role VARCHAR(100),
    context VARCHAR(100),
    typical_age_range VARCHAR(100),
    difficulty INT,
    typical_learning_time VARCHAR(100),
    description TEXT,
    language VARCHAR(50),
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_educational_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Rights
CREATE TABLE IF NOT EXISTS lom_rights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cost VARCHAR(10),
    copyright_and_other_restrictions VARCHAR(10),
    description TEXT,
    lom_schema_id BIGINT UNIQUE,
    CONSTRAINT fk_rights_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. Relation
CREATE TABLE IF NOT EXISTS lom_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kind VARCHAR(50),
    lom_schema_id BIGINT,
    CONSTRAINT fk_relation_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Annotation
CREATE TABLE IF NOT EXISTS lom_annotation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity TEXT,
    annotation_date DATETIME,
    description TEXT,
    lom_schema_id BIGINT,
    CONSTRAINT fk_annotation_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Classification
CREATE TABLE IF NOT EXISTS lom_classification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purpose VARCHAR(100),
    description TEXT,
    keyword TEXT,
    lom_schema_id BIGINT,
    CONSTRAINT fk_classification_schema FOREIGN KEY (lom_schema_id) REFERENCES lom_schema(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. Resource (Child of Relation)
CREATE TABLE IF NOT EXISTS lom_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    relation_id BIGINT UNIQUE,
    CONSTRAINT fk_resource_relation FOREIGN KEY (relation_id) REFERENCES lom_relation(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. Identifier (Shared Child)
CREATE TABLE IF NOT EXISTS lom_identifier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    catalog VARCHAR(255),
    entry VARCHAR(500),
    general_id BIGINT,
    meta_metadata_id BIGINT,
    resource_id BIGINT,
    CONSTRAINT fk_identifier_general FOREIGN KEY (general_id) REFERENCES lom_general(id) ON DELETE CASCADE,
    CONSTRAINT fk_identifier_meta_metadata FOREIGN KEY (meta_metadata_id) REFERENCES lom_meta_metadata(id) ON DELETE CASCADE,
    CONSTRAINT fk_identifier_resource FOREIGN KEY (resource_id) REFERENCES lom_resource(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. Contribute (Shared Child)
CREATE TABLE IF NOT EXISTS lom_contribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(100),
    entity TEXT,
    contribute_date DATETIME,
    lifecycle_id BIGINT,
    meta_metadata_id BIGINT,
    CONSTRAINT fk_contribute_lifecycle FOREIGN KEY (lifecycle_id) REFERENCES lom_lifecycle(id) ON DELETE CASCADE,
    CONSTRAINT fk_contribute_meta_metadata FOREIGN KEY (meta_metadata_id) REFERENCES lom_meta_metadata(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. Requirement (Child of Technical)
CREATE TABLE IF NOT EXISTS lom_requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    technical_id BIGINT,
    CONSTRAINT fk_requirement_technical FOREIGN KEY (technical_id) REFERENCES lom_technical(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. OrComposite (Child of Requirement)
CREATE TABLE IF NOT EXISTS lom_or_composite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    name VARCHAR(100),
    minimum_version VARCHAR(50),
    maximum_version VARCHAR(50),
    requirement_id BIGINT,
    CONSTRAINT fk_or_composite_requirement FOREIGN KEY (requirement_id) REFERENCES lom_requirement(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 17. TaxonPath (Child of Classification)
CREATE TABLE IF NOT EXISTS lom_taxon_path (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(500),
    classification_id BIGINT,
    CONSTRAINT fk_taxon_path_classification FOREIGN KEY (classification_id) REFERENCES lom_classification(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. Taxon (Child of TaxonPath)
CREATE TABLE IF NOT EXISTS lom_taxon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    taxon_id VARCHAR(100),
    entry VARCHAR(500),
    taxon_path_id BIGINT,
    CONSTRAINT fk_taxon_taxon_path FOREIGN KEY (taxon_path_id) REFERENCES lom_taxon_path(id) ON DELETE CASCADE
) ENGINE=InnoDB;-- Hibernate se chargera de créer le schéma (hbm2ddl.auto = update).

