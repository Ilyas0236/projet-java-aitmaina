package org.emsi.service;

import org.emsi.dao.TagDao;
import org.emsi.entities.Tag;
import org.emsi.entities.LomSchema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des tags
 * 
 * Utilise les Streams pour:
 * - Trier et filtrer les tags
 * - Calculer les statistiques d'utilisation
 * - Regrouper par populité
 * 
 * @author Projet LOM - EMSI
 */
public class TagService {

    private static TagService instance;
    private final TagDao tagDao;

    private TagService() {
        this.tagDao = new TagDao();
    }

    public static TagService getInstance() {
        if (instance == null) {
            instance = new TagService();
        }
        return instance;
    }

    /**
     * Créer un nouveau tag
     */
    public Tag createTag(String name, String color, String description) {
        // Vérifier si le tag existe déjà
        if (tagDao.exists(name)) {
            System.out.println("⚠️ Tag déjà existant: " + name);
            return tagDao.findByName(name);
        }

        Tag tag = new Tag(name, color);
        tag.setDescription(description);
        tagDao.save(tag);
        System.out.println("✅ Tag créé: " + name);
        return tag;
    }

    /**
     * Mettre à jour un tag
     */
    public void updateTag(Tag tag) {
        tagDao.update(tag);
        System.out.println("✅ Tag mis à jour: " + tag.getName());
    }

    /**
     * Supprimer un tag
     */
    public void deleteTag(Long tagId) {
        Tag tag = tagDao.findById(tagId);
        if (tag != null) {
            tagDao.delete(tag);
            System.out.println("🗑️ Tag supprimé: " + tag.getName());
        }
    }

    /**
     * Obtenir tous les tags
     */
    public List<Tag> getAllTags() {
        return tagDao.findAll();
    }

    /**
     * STREAM: Rechercher des tags
     */
    public List<Tag> searchTags(String keyword) {
        return tagDao.searchByName(keyword);
    }

    /**
     * STREAM: Obtenir les tags les plus utilisés
     * 
     * Utilise:
     * - stream().sorted() pour trier
     * - Comparator.comparing() pour définir le critère
     */
    public List<Tag> getMostUsedTags(int limit) {
        List<Tag> tags = getAllTags();

        // STREAM API: Tri par nombre de ressources (décroissant)
        return tags.stream()
                .sorted(Comparator.comparing(
                        tag -> tag.getResources().size(),
                        Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir le nuage de tags (avec comptages)
     * 
     * Utilise:
     * - stream().collect() avec toMap()
     */
    public Map<String, Integer> getTagCloud() {
        List<Tag> tags = getAllTags();

        // STREAM API: Créer une Map nom -> nombre d'utilisations
        return tags.stream()
                .collect(Collectors.toMap(
                        Tag::getName,
                        tag -> tag.getResources().size(),
                        (a, b) -> a, // En cas de collision (ne devrait pas arriver)
                        LinkedHashMap::new // Préserver l'ordre
                ));
    }

    /**
     * STREAM: Filtrer les tags par couleur
     */
    public List<Tag> getTagsByColor(String color) {
        List<Tag> tags = getAllTags();

        // STREAM API: Filtrage par couleur
        return tags.stream()
                .filter(tag -> color.equals(tag.getColor()))
                .collect(Collectors.toList());
    }

    /**
     * STREAM: Obtenir les tags non utilisés
     */
    public List<Tag> getUnusedTags() {
        List<Tag> tags = getAllTags();

        // STREAM API: Filtrer les tags sans ressources
        return tags.stream()
                .filter(tag -> tag.getResources().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Associer un tag à une ressource
     */
    public void addTagToResource(Tag tag, LomSchema resource) {
        resource.addTag(tag);
        tag.addResource(resource);
        tagDao.update(tag);
        System.out.println("🏷️ Tag ajouté à la ressource: " + tag.getName());
    }

    /**
     * Retirer un tag d'une ressource
     */
    public void removeTagFromResource(Tag tag, LomSchema resource) {
        resource.removeTag(tag);
        tag.removeResource(resource);
        tagDao.update(tag);
        System.out.println("🗑️ Tag retiré de la ressource: " + tag.getName());
    }

    /**
     * Obtenir le nombre total de tags
     */
    public long countTags() {
        return tagDao.count();
    }

    /**
     * STREAM: Obtenir les couleurs disponibles
     */
    public List<String> getAvailableColors() {
        List<Tag> tags = getAllTags();

        // STREAM API: Extraire les couleurs uniques
        return tags.stream()
                .map(Tag::getColor)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
