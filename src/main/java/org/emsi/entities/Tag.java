package org.emsi.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Tag pour le système de tags des ressources LOM
 * 
 * Relation many-to-many avec LomSchema via table de jointure
 * Mapping Hibernate XML dans Tag.hbm.xml
 * 
 * @author Projet LOM - EMSI
 */
public class Tag {

    private Long id;
    private String name;
    private String color;
    private String description;
    private Date createdAt;

    // Relation many-to-many avec LomSchema
    private Set<LomSchema> resources = new HashSet<>();

    // Constructeurs
    public Tag() {
        this.createdAt = new Date();
    }

    public Tag(String name) {
        this();
        this.name = name;
    }

    public Tag(String name, String color) {
        this(name);
        this.color = color;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Set<LomSchema> getResources() {
        return resources;
    }

    public void setResources(Set<LomSchema> resources) {
        this.resources = resources;
    }

    // Méthodes utilitaires
    public void addResource(LomSchema resource) {
        this.resources.add(resource);
    }

    public void removeResource(LomSchema resource) {
        this.resources.remove(resource);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
