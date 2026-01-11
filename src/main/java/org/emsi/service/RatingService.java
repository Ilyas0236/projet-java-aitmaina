package org.emsi.service;

import org.emsi.dao.GenericDao;
import org.emsi.dao.HibernateUtil;
import org.emsi.entities.LomSchema;
import org.emsi.entities.Rating;
import org.emsi.entities.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des notations
 * 
 * Ce service démontre l'utilisation des Streams pour:
 * - Calculer les moyennes de notes
 * - Classer les ressources par popularité
 * - Analyser les tendances de notation
 * 
 * Concepts démontrés:
 * - STREAMS: average, groupingBy, averagingDouble
 * - COLLECTIONS: Map, Optional, DoubleSummaryStatistics
 * - Hibernate HQL avec agrégations
 * 
 * @author Projet LOM - EMSI
 */
public class RatingService {

    private static RatingService instance;
    private final GenericDao<Rating, Long> dao;

    private RatingService() {
        this.dao = new GenericDao<>(Rating.class);
    }

    public static RatingService getInstance() {
        if (instance == null) {
            instance = new RatingService();
        }
        return instance;
    }

    /**
     * Ajouter ou mettre à jour une notation
     */
    public Rating rateResource(User user, LomSchema resource, int stars) {
        return rateResource(user, resource, stars, null);
    }

    /**
     * Ajouter ou mettre à jour une notation avec commentaire
     */
    public Rating rateResource(User user, LomSchema resource, int stars, String comment) {
        Rating existing = findExisting(user, resource);

        if (existing != null) {
            existing.updateRating(stars, comment);
            dao.update(existing);
            return existing;
        } else {
            Rating rating = new Rating(user, resource, stars, comment);
            dao.save(rating);
            return rating;
        }
    }

    /**
     * Obtenir la notation d'un utilisateur pour une ressource
     */
    public Optional<Rating> getUserRating(User user, LomSchema resource) {
        return Optional.ofNullable(findExisting(user, resource));
    }

    /**
     * STREAM: Calculer la moyenne des notes pour une ressource
     * 
     * Utilise:
     * - stream() + mapToInt() + average()
     * - OptionalDouble pour gérer le cas sans notes
     */
    public double getAverageRating(LomSchema resource) {
        List<Rating> ratings = getResourceRatings(resource);

        return ratings.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);
    }

    /**
     * STREAM: Obtenir les statistiques complètes d'une ressource
     * 
     * Utilise:
     * - DoubleSummaryStatistics pour min, max, avg, count
     */
    public DoubleSummaryStatistics getRatingStatistics(LomSchema resource) {
        List<Rating> ratings = getResourceRatings(resource);

        return ratings.stream()
                .mapToDouble(Rating::getStars)
                .summaryStatistics();
    }

    /**
     * STREAM: Compter les notes par valeur (1-5 étoiles)
     * 
     * Utilise:
     * - groupingBy() + counting()
     */
    public Map<Integer, Long> getRatingDistribution(LomSchema resource) {
        List<Rating> ratings = getResourceRatings(resource);

        // STREAM: Regrouper par nombre d'étoiles
        return ratings.stream()
                .collect(Collectors.groupingBy(
                        Rating::getStars,
                        Collectors.counting()));
    }

    /**
     * STREAM: Obtenir les ressources les mieux notées
     * 
     * Utilise:
     * - Calcul de moyenne par ressource
     * - sorted() par moyenne décroissante
     */
    public List<LomSchema> getTopRatedResources(int limit) {
        List<Rating> allRatings = dao.findAll();

        // STREAM: Grouper par ressource et calculer la moyenne
        Map<LomSchema, Double> averageByResource = allRatings.stream()
                .collect(Collectors.groupingBy(
                        Rating::getResource,
                        Collectors.averagingDouble(Rating::getStars)));

        // STREAM: Trier par moyenne décroissante et limiter
        return averageByResource.entrySet().stream()
                .sorted(Map.Entry.<LomSchema, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir les notes récentes positives (4+ étoiles)
     */
    public List<Rating> getRecentPositiveRatings(int limit) {
        List<Rating> allRatings = dao.findAll();

        return allRatings.stream()
                .filter(Rating::isPositive)
                .sorted(Comparator.comparing(Rating::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Compter le nombre total de notes pour une ressource
     */
    public int getRatingCount(LomSchema resource) {
        return getResourceRatings(resource).size();
    }

    /**
     * Supprimer la notation d'un utilisateur
     */
    public void removeRating(User user, LomSchema resource) {
        Rating existing = findExisting(user, resource);
        if (existing != null) {
            dao.delete(existing);
        }
    }

    /**
     * Chercher une notation existante
     */
    private Rating findExisting(User user, LomSchema resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery(
                    "FROM Rating WHERE user.id = :userId AND resource.id = :resourceId",
                    Rating.class);
            query.setParameter("userId", user.getId());
            query.setParameter("resourceId", resource.getId());
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtenir toutes les notes d'une ressource
     */
    private List<Rating> getResourceRatings(LomSchema resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery(
                    "FROM Rating WHERE resource.id = :resourceId",
                    Rating.class);
            query.setParameter("resourceId", resource.getId());
            return query.list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Obtenir toutes les notes d'un utilisateur
     */
    public List<Rating> getUserRatings(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery(
                    "FROM Rating WHERE user.id = :userId ORDER BY createdAt DESC",
                    Rating.class);
            query.setParameter("userId", user.getId());
            return query.list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Afficher la note moyenne formatée (ex: "★★★★☆ (4.2)")
     */
    public String getFormattedRating(LomSchema resource) {
        double avg = getAverageRating(resource);
        int count = getRatingCount(resource);

        if (count == 0) {
            return "☆☆☆☆☆ (Pas encore noté)";
        }

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= Math.round(avg) ? "★" : "☆");
        }

        return String.format("%s (%.1f - %d avis)", stars, avg, count);
    }
}
