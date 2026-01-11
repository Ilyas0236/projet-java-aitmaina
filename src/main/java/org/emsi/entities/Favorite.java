package org.emsi.entities;

import java.util.Date;

/**
 * Entité Favorite pour gérer les ressources favorites des utilisateurs
 * 
 * Cette entité permet aux utilisateurs de marquer des ressources comme
 * favorites
 * Mapping Hibernate XML dans Favorite.hbm.xml
 * 
 * @author Projet LOM - EMSI
 */
public class Favorite {

    private Long id;
    private User user;
    private LomSchema resource;
    private Date addedAt;
    private String note; // Note personnelle optionnelle

    // Constructeurs
    public Favorite() {
        this.addedAt = new Date();
    }

    public Favorite(User user, LomSchema resource) {
        this();
        this.user = user;
        this.resource = resource;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LomSchema getResource() {
        return resource;
    }

    public void setResource(LomSchema resource) {
        this.resource = resource;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Favorite favorite = (Favorite) o;
        return id != null && id.equals(favorite.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Favorite[user=" + (user != null ? user.getUsername() : "null") +
                ", resource=" + (resource != null ? resource.getResourceTitle() : "null") + "]";
    }
}
