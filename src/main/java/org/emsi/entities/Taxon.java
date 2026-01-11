package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité Taxon - Élément de taxonomie
 * Associé à TaxonPath
 */
public class Taxon implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String taxonId; // ID du taxon (CharacterString)
    private String entry; // Libellé du taxon (LangString)

    // Relation vers TaxonPath
    private TaxonPath taxonPath;

    public Taxon() {
    }

    public Taxon(String taxonId, String entry) {
        this.taxonId = taxonId;
        this.entry = entry;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public TaxonPath getTaxonPath() {
        return taxonPath;
    }

    public void setTaxonPath(TaxonPath taxonPath) {
        this.taxonPath = taxonPath;
    }

    @Override
    public String toString() {
        return "Taxon{" +
                "taxonId='" + taxonId + '\'' +
                ", entry='" + entry + '\'' +
                '}';
    }
}
