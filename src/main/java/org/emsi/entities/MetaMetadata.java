package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité MetaMetadata - Catégorie 3 du LOM
 * Information sur les métadonnées elles-mêmes
 */
public class MetaMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String metadataSchema; // Schéma de métadonnées (CharacterString) - ex: "LOMv1.0"
    private String language; // Langue des métadonnées

    // Relation vers LomSchema
    private LomSchema lomSchema;

    // Identifiants de catalogue
    private Set<Identifier> identifiers = new HashSet<>();

    // Contributeurs des métadonnées
    private Set<Contribute> contributes = new HashSet<>();

    public MetaMetadata() {
        this.metadataSchema = "LOMv1.0";
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetadataSchema() {
        return metadataSchema;
    }

    public void setMetadataSchema(String metadataSchema) {
        this.metadataSchema = metadataSchema;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<Contribute> getContributes() {
        return contributes;
    }

    public void setContributes(Set<Contribute> contributes) {
        this.contributes = contributes;
    }

    @Override
    public String toString() {
        return "MetaMetadata{" +
                "id=" + id +
                ", metadataSchema='" + metadataSchema + '\'' +
                '}';
    }
}
