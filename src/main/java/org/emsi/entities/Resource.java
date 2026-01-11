package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Resource - Ressource liée dans une Relation
 */
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String description; // Description de la ressource (LangString)

    // Relation vers Relation
    private Relation relation;

    // Identifiants de la ressource
    private Set<Identifier> identifiers = new HashSet<>();

    public Resource() {
    }

    public Resource(String description) {
        this.description = description;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
