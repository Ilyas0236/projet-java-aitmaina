package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité TaxonPath - Chemin taxonomique
 * Associé à Classification
 */
public class TaxonPath implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String source; // Source de la taxonomie (LangString)

    // Relation vers Classification
    private Classification classification;

    // Taxons du chemin
    private Set<Taxon> taxons = new HashSet<>();

    public TaxonPath() {
    }

    public TaxonPath(String source) {
        this.source = source;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public Set<Taxon> getTaxons() {
        return taxons;
    }

    public void setTaxons(Set<Taxon> taxons) {
        this.taxons = taxons;
    }

    public void addTaxon(Taxon taxon) {
        this.taxons.add(taxon);
        taxon.setTaxonPath(this);
    }

    @Override
    public String toString() {
        return "TaxonPath{" +
                "id=" + id +
                ", source='" + source + '\'' +
                '}';
    }
}
