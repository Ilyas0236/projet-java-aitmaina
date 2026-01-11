package org.emsi.entities;

import java.util.Date;

/**
 * Entité ViewHistory - Historique de Consultation
 * 
 * Cette entité enregistre chaque consultation de ressource par un utilisateur,
 * permettant de suivre les ressources récemment consultées et de proposer
 * des recommandations.
 * 
 * Concepts démontrés:
 * - POO: encapsulation, associations
 * - Hibernate: mapping many-to-one
 * 
 * @author Projet LOM - EMSI
 */
public class ViewHistory {

    private Long id;
    private User user;
    private LomSchema resource;
    private Date viewedAt;
    private int viewCount; // Nombre de fois que l'utilisateur a consulté cette ressource
    private long duration; // Durée de consultation en secondes (optionnel)

    // Constructeurs
    public ViewHistory() {
        this.viewedAt = new Date();
        this.viewCount = 1;
    }

    public ViewHistory(User user, LomSchema resource) {
        this();
        this.user = user;
        this.resource = resource;
    }

    /**
     * Incrémenter le compteur de vues et mettre à jour la date
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.viewedAt = new Date();
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

    public Date getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Date viewedAt) {
        this.viewedAt = viewedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ViewHistory that = (ViewHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ViewHistory[user=" + (user != null ? user.getUsername() : "null") +
                ", resource=" + (resource != null ? resource.getResourceTitle() : "null") +
                ", views=" + viewCount + "]";
    }
}
