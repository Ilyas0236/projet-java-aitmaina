package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité LomSchema - Entité racine représentant un objet d'apprentissage LOM
 * 1.0
 * Regroupe toutes les 9 catégories de métadonnées LOM
 */
public class LomSchema implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String resourceTitle; // Titre simplifié pour affichage
    private String resourceUrl; // URL ou chemin du fichier
    private java.util.Date createdAt;
    private java.util.Date updatedAt;

    // Relations vers les 9 catégories LOM
    private General general;
    private Lifecycle lifecycle;
    private MetaMetadata metaMetadata;
    private Technical technical;
    private Educational educational;
    private Rights rights;

    // Collections pour les relations multiples
    private Set<Relation> relations = new HashSet<>();
    private Set<Annotation> annotations = new HashSet<>();
    private Set<Classification> classifications = new HashSet<>();

    // NOUVELLES COLLECTIONS: Tags et Fichiers
    private Set<Tag> tags = new HashSet<>();
    private Set<ResourceFile> resourceFiles = new HashSet<>();

    public LomSchema() {
        this.createdAt = new java.util.Date();
        this.updatedAt = new java.util.Date();
    }

    public LomSchema(String resourceTitle) {
        this();
        this.resourceTitle = resourceTitle;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }

    public void setResourceTitle(String resourceTitle) {
        this.resourceTitle = resourceTitle;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public MetaMetadata getMetaMetadata() {
        return metaMetadata;
    }

    public void setMetaMetadata(MetaMetadata metaMetadata) {
        this.metaMetadata = metaMetadata;
    }

    public Technical getTechnical() {
        return technical;
    }

    public void setTechnical(Technical technical) {
        this.technical = technical;
    }

    public Educational getEducational() {
        return educational;
    }

    public void setEducational(Educational educational) {
        this.educational = educational;
    }

    public Rights getRights() {
        return rights;
    }

    public void setRights(Rights rights) {
        this.rights = rights;
    }

    public Set<Relation> getRelations() {
        return relations;
    }

    public void setRelations(Set<Relation> relations) {
        this.relations = relations;
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Set<Classification> getClassifications() {
        return classifications;
    }

    public void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }

    // Getters et Setters pour Tags
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    // Getters et Setters pour ResourceFiles
    public Set<ResourceFile> getResourceFiles() {
        return resourceFiles;
    }

    public void setResourceFiles(Set<ResourceFile> resourceFiles) {
        this.resourceFiles = resourceFiles;
    }

    // Méthodes utilitaires
    public void addRelation(Relation relation) {
        this.relations.add(relation);
        relation.setLomSchema(this);
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
        annotation.setLomSchema(this);
    }

    public void addClassification(Classification classification) {
        this.classifications.add(classification);
        classification.setLomSchema(this);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public void addResourceFile(ResourceFile file) {
        this.resourceFiles.add(file);
        file.setLomSchema(this);
    }

    @Override
    public String toString() {
        return "LomSchema{" +
                "id=" + id +
                ", resourceTitle='" + resourceTitle + '\'' +
                '}';
    }
}
