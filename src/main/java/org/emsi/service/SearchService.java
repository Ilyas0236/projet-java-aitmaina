package org.emsi.service;

import org.emsi.dao.LomSchemaDao;
import org.emsi.entities.LomSchema;

import java.util.List;

/**
 * Service de recherche des ressources pédagogiques
 * Recherche multicritères et filtrage
 */
public class SearchService {

    private static SearchService instance;
    private final LomSchemaDao lomSchemaDao;

    private SearchService() {
        this.lomSchemaDao = new LomSchemaDao();
    }

    /**
     * Obtenir l'instance unique (Singleton)
     */
    public static SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }

    /**
     * Recherche par titre
     */
    public List<LomSchema> searchByTitle(String title) {
        return lomSchemaDao.findByTitle(title);
    }

    /**
     * Recherche par mot-clé
     */
    public List<LomSchema> searchByKeyword(String keyword) {
        return lomSchemaDao.findByKeyword(keyword);
    }

    /**
     * Recherche par langue
     */
    public List<LomSchema> searchByLanguage(String language) {
        return lomSchemaDao.findByLanguage(language);
    }

    /**
     * Recherche par difficulté
     */
    public List<LomSchema> searchByDifficulty(Integer difficulty) {
        return lomSchemaDao.findByDifficulty(difficulty);
    }

    /**
     * Recherche multicritères
     * Tous les paramètres sont optionnels
     */
    /**
     * Recherche multicritères (Titre OU Mot-clé) ET filtres
     */
    public List<LomSchema> search(String query, String filterTitle, String filterLanguage, Integer filterDifficulty) {
        // Note: filterTitle parameter allows backward compatibility but we prioritize
        // query
        // Actually, let's redefine this method to match what UserDashboard needs, or
        // clean it up.
        // UserDashboard calls: search(query, query, language, difficulty)

        // Let's use the new DAO method
        return lomSchemaDao.searchByCriteria(query, filterLanguage, filterDifficulty);
    }

    /**
     * Recherche rapide (titre ou mot-clé)
     */
    public List<LomSchema> quickSearch(String query) {
        if (isEmpty(query)) {
            return lomSchemaDao.findAll();
        }

        // Rechercher dans le titre et les mots-clés
        List<LomSchema> byTitle = lomSchemaDao.findByTitle(query);
        List<LomSchema> byKeyword = lomSchemaDao.findByKeyword(query);

        // Fusionner les résultats sans doublons
        for (LomSchema lom : byKeyword) {
            if (!byTitle.contains(lom)) {
                byTitle.add(lom);
            }
        }

        return byTitle;
    }

    /**
     * Vérifier si une chaîne est vide ou null
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Obtenir les langues disponibles
     */
    public List<String> getAvailableLanguages() {
        // Liste prédéfinie des langues courantes
        return List.of("fr", "en", "ar", "es", "de", "it", "pt", "zh", "ja");
    }

    /**
     * Obtenir les niveaux de difficulté
     */
    public List<String> getDifficultyLevels() {
        return List.of(
                "1 - Très facile",
                "2 - Facile",
                "3 - Moyen",
                "4 - Difficile",
                "5 - Très difficile");
    }
}
