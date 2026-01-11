package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Classification - Catégorie 9 du LOM
 * Description de l'objet d'apprentissage par rapport à un système de
 * classification
 */
public class Classification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String purpose; // Objectif (State: discipline, idea, prerequisite, educational objective,
                            // accessibility restrictions, educational level, skill level, security level,
                            // competency)
    private String description; // Description (LangString)
    private String keyword; // Mots-clés (LangString) - séparés par virgule

    // Relation vers LomSchema
    private LomSchema lomSchema;

    // Chemins taxonomiques
    private Set<TaxonPath> taxonPaths = new HashSet<>();

    public Classification() {
    }

    public Classification(String purpose) {
        this.purpose = purpose;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Set<TaxonPath> getTaxonPaths() {
        return taxonPaths;
    }

    public void setTaxonPaths(Set<TaxonPath> taxonPaths) {
        this.taxonPaths = taxonPaths;
    }

    public void addTaxonPath(TaxonPath taxonPath) {
        this.taxonPaths.add(taxonPath);
        taxonPath.setClassification(this);
    }

    @Override
    public String toString() {
        return "Classification{" +
                "id=" + id +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}
