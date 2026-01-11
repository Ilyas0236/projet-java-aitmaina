package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Requirement - Exigence technique
 * Associé à Technical
 */
public class Requirement implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // Relation vers Technical
    private Technical technical;

    // Composites OR (alternatives)
    private Set<OrComposite> orComposites = new HashSet<>();

    public Requirement() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Technical getTechnical() {
        return technical;
    }

    public void setTechnical(Technical technical) {
        this.technical = technical;
    }

    public Set<OrComposite> getOrComposites() {
        return orComposites;
    }

    public void setOrComposites(Set<OrComposite> orComposites) {
        this.orComposites = orComposites;
    }

    public void addOrComposite(OrComposite orComposite) {
        this.orComposites.add(orComposite);
        orComposite.setRequirement(this);
    }

    @Override
    public String toString() {
        return "Requirement{" +
                "id=" + id +
                ", orComposites=" + orComposites.size() +
                '}';
    }
}
