package org.emsi.service;

import org.emsi.dao.GenericDao;
import org.emsi.dao.HibernateUtil;
import org.emsi.entities.LomSchema;
import org.emsi.entities.User;
import org.emsi.entities.ViewHistory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion de l'historique de consultation
 * 
 * Ce service démontre l'utilisation des Streams pour:
 * - Filtrer et trier l'historique
 * - Calculer les statistiques de consultation
 * - Recommander des ressources basées sur l'historique
 * 
 * Concepts démontrés:
 * - STREAMS: filter, map, sorted, limit, collect
 * - COLLECTIONS: groupingBy, counting
 * - Hibernate HQL avec requêtes paramétrées
 * 
 * @author Projet LOM - EMSI
 */
public class ViewHistoryService {

    private static ViewHistoryService instance;
    private final GenericDao<ViewHistory, Long> dao;

    private ViewHistoryService() {
        this.dao = new GenericDao<>(ViewHistory.class);
    }

    public static ViewHistoryService getInstance() {
        if (instance == null) {
            instance = new ViewHistoryService();
        }
        return instance;
    }

    /**
     * Enregistrer une consultation de ressource
     * 
     * Si l'utilisateur a déjà consulté cette ressource, incrémente le compteur
     */
    public void recordView(User user, LomSchema resource) {
        ViewHistory existing = findExisting(user, resource);

        if (existing != null) {
            existing.incrementViewCount();
            dao.update(existing);
        } else {
            ViewHistory history = new ViewHistory(user, resource);
            dao.save(history);
        }
    }

    /**
     * Chercher un historique existant pour user + resource
     */
    private ViewHistory findExisting(User user, LomSchema resource) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ViewHistory> query = session.createQuery(
                    "FROM ViewHistory WHERE user.id = :userId AND resource.id = :resourceId",
                    ViewHistory.class);
            query.setParameter("userId", user.getId());
            query.setParameter("resourceId", resource.getId());
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * STREAM: Obtenir l'historique récent d'un utilisateur
     * 
     * Utilise:
     * - sorted() avec Comparator pour trier par date
     * - limit() pour les N plus récents
     */
    public List<ViewHistory> getRecentHistory(User user, int limit) {
        List<ViewHistory> all = getUserHistory(user);

        // STREAM: Trier par date décroissante et limiter
        return all.stream()
                .sorted(Comparator.comparing(ViewHistory::getViewedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir les ressources les plus consultées par l'utilisateur
     * 
     * Utilise:
     * - sorted() par viewCount décroissant
     * - map() pour extraire les ressources
     */
    public List<LomSchema> getMostViewedResources(User user, int limit) {
        List<ViewHistory> all = getUserHistory(user);

        // STREAM: Trier par nombre de vues et extraire les ressources
        return all.stream()
                .sorted(Comparator.comparingInt(ViewHistory::getViewCount).reversed())
                .limit(limit)
                .map(ViewHistory::getResource)
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Statistiques de consultation
     * 
     * Utilise:
     * - mapToInt() pour extraire les compteurs
     * - summaryStatistics() pour obtenir min, max, avg, sum
     */
    public IntSummaryStatistics getViewStatistics(User user) {
        List<ViewHistory> all = getUserHistory(user);

        return all.stream()
                .mapToInt(ViewHistory::getViewCount)
                .summaryStatistics();
    }

    /**
     * STREAM: Compter les consultations par langue
     * 
     * Utilise:
     * - filter() pour les ressources avec langue
     * - groupingBy() + summingInt() pour agréger
     */
    public Map<String, Integer> getViewsByLanguage(User user) {
        List<ViewHistory> all = getUserHistory(user);

        return all.stream()
                .filter(h -> h.getResource().getGeneral() != null)
                .filter(h -> h.getResource().getGeneral().getLanguage() != null)
                .collect(Collectors.groupingBy(
                        h -> h.getResource().getGeneral().getLanguage(),
                        Collectors.summingInt(ViewHistory::getViewCount)));
    }

    /**
     * Obtenir tout l'historique d'un utilisateur
     */
    private List<ViewHistory> getUserHistory(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ViewHistory> query = session.createQuery(
                    "FROM ViewHistory WHERE user.id = :userId",
                    ViewHistory.class);
            query.setParameter("userId", user.getId());
            return query.list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Obtenir le nombre total de consultations
     */
    public long getTotalViewCount(User user) {
        return getUserHistory(user).stream()
                .mapToInt(ViewHistory::getViewCount)
                .sum();
    }

    /**
     * Vérifier si une ressource a été consultée
     */
    public boolean hasViewed(User user, LomSchema resource) {
        return findExisting(user, resource) != null;
    }

    /**
     * Effacer l'historique d'un utilisateur
     */
    public void clearHistory(User user) {
        List<ViewHistory> all = getUserHistory(user);
        all.forEach(dao::delete);
    }
}
