package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité Rights - Catégorie 6 du LOM
 * Droits de propriété intellectuelle et conditions d'utilisation
 */
public class Rights implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String cost; // Coût (State: yes, no)
    private String copyrightAndOtherRestrictions; // Droits d'auteur et restrictions (State: yes, no)
    private String description; // Description des conditions d'utilisation (LangString)

    // Relation vers LomSchema
    private LomSchema lomSchema;

    public Rights() {
    }

    public Rights(String cost, String copyrightAndOtherRestrictions) {
        this.cost = cost;
        this.copyrightAndOtherRestrictions = copyrightAndOtherRestrictions;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCopyrightAndOtherRestrictions() {
        return copyrightAndOtherRestrictions;
    }

    public void setCopyrightAndOtherRestrictions(String copyrightAndOtherRestrictions) {
        this.copyrightAndOtherRestrictions = copyrightAndOtherRestrictions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public boolean isFree() {
        return "no".equalsIgnoreCase(cost);
    }

    public boolean hasCopyright() {
        return "yes".equalsIgnoreCase(copyrightAndOtherRestrictions);
    }

    @Override
    public String toString() {
        return "Rights{" +
                "id=" + id +
                ", cost='" + cost + '\'' +
                ", copyrightAndOtherRestrictions='" + copyrightAndOtherRestrictions + '\'' +
                '}';
    }
}
