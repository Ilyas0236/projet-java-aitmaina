package org.emsi.service;

import org.emsi.dao.GenericDao;
import org.emsi.entities.Favorite;
import org.emsi.entities.LomSchema;
import org.emsi.entities.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.emsi.dao.HibernateUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des favoris
 * 
 * Ce service utilise intensivement les Streams pour:
 * - Filtrer et trier les favoris
 * - Regrouper par catégories
 * - Calculer des statistiques
 * 
 * @author Projet LOM - EMSI
 */
public class FavoriteService {

    private static FavoriteService instance;
    private final GenericDao<Favorite, Long> favoriteDao;

    private FavoriteService() {
        this.favoriteDao = new GenericDao<>(Favorite.class);
    }

    public static FavoriteService getInstance() {
        if (instance == null) {
            instance = new FavoriteService();
        }
        return instance;
    }

    /**
     * Ajouter une ressource aux favoris
     */
    public Favorite addFavorite(User user, LomSchema resource) {
        // Vérifier si déjà en favoris
        if (isFavorite(user, resource)) {
            System.out.println("⚠️ Ressource déjà dans les favoris");
            return null;
        }

        Favorite favorite = new Favorite(user, resource);
        favoriteDao.save(favorite);
        System.out.println("⭐ Favori ajouté: " + resource.getResourceTitle());
        return favorite;
    }

    /**
     * Retirer une ressource des favoris
     */
    public void removeFavorite(User user, LomSchema resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.resource.id = :resourceId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            query.setParameter("resourceId", resource.getId());

            Favorite favorite = query.uniqueResult();
            if (favorite != null) {
                favoriteDao.delete(favorite);
                System.out.println("🗑️ Favori supprimé: " + resource.getResourceTitle());
            }
        }
    }

    /**
     * Vérifier si une ressource est en favoris
     */
    public boolean isFavorite(User user, LomSchema resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId AND f.resource.id = :resourceId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", user.getId());
            query.setParameter("resourceId", resource.getId());
            return query.uniqueResult() > 0;
        }
    }

    /**
     * STREAM: Obtenir tous les favoris d'un utilisateur
     * 
     * Utilise:
     * - HQL pour récupérer les favoris
     * - stream() pour la transformation
     */
    public List<Favorite> getUserFavorites(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId ORDER BY f.addedAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            return query.list();
        }
    }

    /**
     * STREAM: Obtenir les ressources favorites d'un utilisateur
     * 
     * Utilise:
     * - stream().map() pour extraire les ressources
     * - collect() pour convertir en List
     */
    public List<LomSchema> getUserFavoriteResources(User user) {
        List<Favorite> favorites = getUserFavorites(user);

        // STREAM API: Extraction des ressources depuis les favoris
        // map() transforme chaque Favorite en LomSchema
        return favorites.stream()
                .map(Favorite::getResource) // Référence de méthode
                .filter(Objects::nonNull) // Exclure les nulls
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir les favoris par langue
     * 
     * Utilise:
     * - filter() pour filtrer par langue
     * - groupingBy() pour regrouper
     */
    public Map<String, List<LomSchema>> getFavoritesByLanguage(User user) {
        List<LomSchema> resources = getUserFavoriteResources(user);

        // STREAM API: Groupement par langue
        return resources.stream()
                .filter(r -> r.getGeneral() != null)
                .filter(r -> r.getGeneral().getLanguage() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getGeneral().getLanguage()));
    }

    /**
     * STREAM: Obtenir les favoris par difficulté
     * 
     * Utilise:
     * - filter() et groupingBy()
     */
    public Map<Integer, List<LomSchema>> getFavoritesByDifficulty(User user) {
        List<LomSchema> resources = getUserFavoriteResources(user);

        // STREAM API: Groupement par difficulté
        return resources.stream()
                .filter(r -> r.getEducational() != null)
                .filter(r -> r.getEducational().getDifficulty() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getEducational().getDifficulty()));
    }

    /**
     * STREAM: Rechercher dans les favoris
     * 
     * Utilise:
     * - filter() avec Predicate complexe
     */
    public List<LomSchema> searchInFavorites(User user, String query) {
        List<LomSchema> resources = getUserFavoriteResources(user);
        String lowerQuery = query.toLowerCase();

        // STREAM API: Recherche dans titre et mots-clés
        return resources.stream()
                .filter(r -> {
                    // Recherche dans le titre
                    if (r.getResourceTitle() != null &&
                            r.getResourceTitle().toLowerCase().contains(lowerQuery)) {
                        return true;
                    }
                    // Recherche dans les mots-clés
                    if (r.getGeneral() != null &&
                            r.getGeneral().getKeyword() != null &&
                            r.getGeneral().getKeyword().toLowerCase().contains(lowerQuery)) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir les favoris les plus récents
     * 
     * Utilise:
     * - sorted() avec Comparator
     * - limit() pour limiter les résultats
     */
    public List<Favorite> getRecentFavorites(User user, int limit) {
        List<Favorite> favorites = getUserFavorites(user);

        // STREAM API: Tri par date et limitation
        return favorites.stream()
                .sorted(Comparator.comparing(Favorite::getAddedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Compter les favoris par langue
     */
    public Map<String, Long> countFavoritesByLanguage(User user) {
        List<LomSchema> resources = getUserFavoriteResources(user);

        // STREAM API: Comptage par langue
        return resources.stream()
                .filter(r -> r.getGeneral() != null)
                .filter(r -> r.getGeneral().getLanguage() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getGeneral().getLanguage(),
                        Collectors.counting()));
    }

    /**
     * STREAM: Vérifier si l'utilisateur a des favoris
     */
    public boolean hasFavorites(User user) {
        return !getUserFavorites(user).isEmpty();
    }

    /**
     * Compter le nombre de favoris d'un utilisateur
     */
    public long countUserFavorites(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", user.getId());
            return query.uniqueResult();
        }
    }

    /**
     * STREAM: Obtenir les ressources les plus favorites
     * (utile pour les recommandations)
     */
    public List<LomSchema> getMostFavoritedResources(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT f.resource, COUNT(f) as favCount " +
                    "FROM Favorite f " +
                    "GROUP BY f.resource " +
                    "ORDER BY favCount DESC";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setMaxResults(limit);

            List<Object[]> results = query.list();

            // STREAM API: Extraction des ressources
            return results.stream()
                    .map(row -> (LomSchema) row[0])
                    .collect(Collectors.toList());
        }
    }

    /**
     * Mettre à jour la note personnelle d'un favori
     */
    public void updateFavoriteNote(User user, LomSchema resource, String note) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.resource.id = :resourceId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            query.setParameter("resourceId", resource.getId());

            Favorite favorite = query.uniqueResult();
            if (favorite != null) {
                favorite.setNote(note);
                favoriteDao.update(favorite);
                System.out.println("📝 Note mise à jour pour: " + resource.getResourceTitle());
            }
        }
    }
}
