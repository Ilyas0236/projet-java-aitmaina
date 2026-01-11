package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité Identifier - Identifiant unique d'une ressource
 * Associé à General et MetaMetadata
 */
public class Identifier implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String catalog; // Nom du catalogue (LangString)
    private String entry; // Entrée dans le catalogue (LangString)

    // Relations
    private General general;
    private MetaMetadata metaMetadata;

    public Identifier() {
    }

    public Identifier(String catalog, String entry) {
        this.catalog = catalog;
        this.entry = entry;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public MetaMetadata getMetaMetadata() {
        return metaMetadata;
    }

    public void setMetaMetadata(MetaMetadata metaMetadata) {
        this.metaMetadata = metaMetadata;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "catalog='" + catalog + '\'' +
                ", entry='" + entry + '\'' +
                '}';
    }
}
