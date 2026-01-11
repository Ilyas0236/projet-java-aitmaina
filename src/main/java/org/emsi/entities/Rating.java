package org.emsi.entities;

import java.util.Date;

/**
 * Entité Rating - Système de Notation
 * 
 * Cette entité permet aux utilisateurs de noter les ressources pédagogiques
 * sur une échelle de 1 à 5 étoiles, avec possibilité d'ajouter un commentaire.
 * 
 * Concepts démontrés:
 * - POO: encapsulation, validation des données
 * - Hibernate: mapping many-to-one, contraintes
 * 
 * @author Projet LOM - EMSI
 */
public class Rating {

    private Long id;
    private User user;
    private LomSchema resource;
    private int stars; // Notation de 1 à 5 étoiles
    private String comment; // Commentaire optionnel
    private Date createdAt;
    private Date updatedAt;

    // Constantes pour la validation
    public static final int MIN_STARS = 1;
    public static final int MAX_STARS = 5;

    // Constructeurs
    public Rating() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Rating(User user, LomSchema resource, int stars) {
        this();
        this.user = user;
        this.resource = resource;
        setStars(stars); // Utilise le setter pour validation
    }

    public Rating(User user, LomSchema resource, int stars, String comment) {
        this(user, resource, stars);
        this.comment = comment;
    }

    /**
     * Mettre à jour la notation
     * 
     * @param newStars nouvelle valeur (1-5)
     */
    public void updateRating(int newStars) {
        setStars(newStars);
        this.updatedAt = new Date();
    }

    /**
     * Mettre à jour la notation avec commentaire
     */
    public void updateRating(int newStars, String newComment) {
        updateRating(newStars);
        this.comment = newComment;
    }

    /**
     * Obtenir l'affichage en étoiles
     * 
     * @return String représentant les étoiles (ex: "★★★☆☆")
     */
    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= MAX_STARS; i++) {
            sb.append(i <= stars ? "★" : "☆");
        }
        return sb.toString();
    }

    /**
     * Vérifier si c'est une bonne note (4 ou 5 étoiles)
     */
    public boolean isPositive() {
        return stars >= 4;
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

    public int getStars() {
        return stars;
    }

    /**
     * Définir la notation avec validation
     * 
     * @param stars valeur entre 1 et 5
     * @throws IllegalArgumentException si la valeur est hors limites
     */
    public void setStars(int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            throw new IllegalArgumentException(
                    "La notation doit être entre " + MIN_STARS + " et " + MAX_STARS + " étoiles");
        }
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rating rating = (Rating) o;
        return id != null && id.equals(rating.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Rating[user=" + (user != null ? user.getUsername() : "null") +
                ", resource=" + (resource != null ? resource.getResourceTitle() : "null") +
                ", stars=" + getStarsDisplay() + "]";
    }
}
