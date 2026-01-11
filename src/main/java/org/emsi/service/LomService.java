package org.emsi.service;

import org.emsi.dao.LomSchemaDao;
import org.emsi.entities.*;

import java.util.Date;
import java.util.List;

/**
 * Service de gestion des ressources pédagogiques LOM
 * Opérations CRUD et gestion des métadonnées
 */
public class LomService {

    private static LomService instance;
    private final LomSchemaDao lomSchemaDao;

    private LomService() {
        this.lomSchemaDao = new LomSchemaDao();
    }

    /**
     * Obtenir l'instance unique (Singleton)
     */
    public static LomService getInstance() {
        if (instance == null) {
            instance = new LomService();
        }
        return instance;
    }

    /**
     * Créer une nouvelle ressource LOM
     */
    public LomSchema createResource(String title, String url) {
        LomSchema lom = new LomSchema(title);
        lom.setResourceUrl(url);

        // Initialiser les catégories LOM vides
        General general = new General(title);
        general.setLomSchema(lom);
        lom.setGeneral(general);

        Lifecycle lifecycle = new Lifecycle("1.0", "draft");
        lifecycle.setLomSchema(lom);
        lom.setLifecycle(lifecycle);

        MetaMetadata metaMetadata = new MetaMetadata();
        metaMetadata.setLomSchema(lom);
        lom.setMetaMetadata(metaMetadata);

        Technical technical = new Technical();
        technical.setLomSchema(lom);
        lom.setTechnical(technical);

        Educational educational = new Educational();
        educational.setLomSchema(lom);
        lom.setEducational(educational);

        Rights rights = new Rights("no", "no");
        rights.setLomSchema(lom);
        lom.setRights(rights);

        lomSchemaDao.save(lom);
        System.out.println("✅ Ressource LOM créée: " + title);

        return lom;
    }

    /**
     * Obtenir toutes les ressources
     */
    public List<LomSchema> getAllResources() {
        return lomSchemaDao.findAll();
    }

    /**
     * Obtenir une ressource par ID
     */
    public LomSchema getResourceById(Long id) {
        return lomSchemaDao.findByIdWithRelations(id);
    }

    /**
     * Mettre à jour une ressource
     */
    public void updateResource(LomSchema lom) {
        lom.setUpdatedAt(new Date());
        lomSchemaDao.update(lom);
        System.out.println("✅ Ressource mise à jour: " + lom.getResourceTitle());
    }

    /**
     * Supprimer une ressource
     */
    public void deleteResource(Long id) {
        LomSchema lom = lomSchemaDao.findById(id);
        if (lom != null) {
            lomSchemaDao.delete(lom);
            System.out.println("✅ Ressource supprimée: " + lom.getResourceTitle());
        }
    }

    /**
     * Mettre à jour les métadonnées General
     */
    public void updateGeneral(LomSchema lom, String title, String language,
            String description, String keywords, String structure,
            Integer aggregationLevel) {
        General general = lom.getGeneral();
        if (general == null) {
            general = new General();
            general.setLomSchema(lom);
            lom.setGeneral(general);
        }

        general.setTitle(title);
        general.setLanguage(language);
        general.setDescription(description);
        general.setKeyword(keywords);
        general.setStructure(structure);
        general.setAggregationLevel(aggregationLevel);

        lom.setResourceTitle(title);
        updateResource(lom);
    }

    /**
     * Mettre à jour les métadonnées Lifecycle
     */
    public void updateLifecycle(LomSchema lom, String version, String status) {
        Lifecycle lifecycle = lom.getLifecycle();
        if (lifecycle == null) {
            lifecycle = new Lifecycle();
            lifecycle.setLomSchema(lom);
            lom.setLifecycle(lifecycle);
        }

        lifecycle.setVersion(version);
        lifecycle.setStatus(status);

        updateResource(lom);
    }

    /**
     * Ajouter un contributeur
     */
    public void addContributor(LomSchema lom, String role, String entity) {
        Lifecycle lifecycle = lom.getLifecycle();
        if (lifecycle != null) {
            Contribute contribute = new Contribute(role, entity);
            lifecycle.addContribute(contribute);
            updateResource(lom);
        }
    }

    /**
     * Mettre à jour les métadonnées Technical
     */
    public void updateTechnical(LomSchema lom, String format, String size,
            String location, String duration) {
        Technical technical = lom.getTechnical();
        if (technical == null) {
            technical = new Technical();
            technical.setLomSchema(lom);
            lom.setTechnical(technical);
        }

        technical.setFormat(format);
        technical.setSize(size);
        technical.setLocation(location);
        technical.setDuration(duration);

        updateResource(lom);
    }

    /**
     * Mettre à jour les métadonnées Educational
     */
    public void updateEducational(LomSchema lom, String interactivityType,
            String learningResourceType, Integer interactivityLevel,
            String intendedEndUserRole, String context,
            Integer difficulty, String typicalLearningTime,
            String typicalAgeRange) {
        Educational educational = lom.getEducational();
        if (educational == null) {
            educational = new Educational();
            educational.setLomSchema(lom);
            lom.setEducational(educational);
        }

        educational.setInteractivityType(interactivityType);
        educational.setLearningResourceType(learningResourceType);
        educational.setInteractivityLevel(interactivityLevel);
        educational.setIntendedEndUserRole(intendedEndUserRole);
        educational.setContext(context);
        educational.setDifficulty(difficulty);
        educational.setTypicalLearningTime(typicalLearningTime);
        educational.setTypicalAgeRange(typicalAgeRange);

        updateResource(lom);
    }

    /**
     * Mettre à jour les métadonnées Rights
     */
    public void updateRights(LomSchema lom, String cost, String copyright, String description) {
        Rights rights = lom.getRights();
        if (rights == null) {
            rights = new Rights();
            rights.setLomSchema(lom);
            lom.setRights(rights);
        }

        rights.setCost(cost);
        rights.setCopyrightAndOtherRestrictions(copyright);
        rights.setDescription(description);

        updateResource(lom);
    }

    /**
     * Ajouter une annotation
     */
    public void addAnnotation(LomSchema lom, String author, String description) {
        Annotation annotation = new Annotation(author, description);
        lom.addAnnotation(annotation);
        updateResource(lom);
    }

    /**
     * Ajouter une classification
     */
    public void addClassification(LomSchema lom, String purpose, String description, String keywords) {
        Classification classification = new Classification(purpose);
        classification.setDescription(description);
        classification.setKeyword(keywords);
        lom.addClassification(classification);
        updateResource(lom);
    }

    /**
     * Obtenir les ressources récentes
     */
    public List<LomSchema> getRecentResources(int limit) {
        return lomSchemaDao.findRecent(limit);
    }

    /**
     * Compter le nombre de ressources
     */
    public long countResources() {
        return lomSchemaDao.count();
    }
}
