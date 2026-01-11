package org.emsi.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entité Annotation - Catégorie 8 du LOM
 * Commentaires sur l'utilisation pédagogique de l'objet d'apprentissage
 */
public class Annotation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String entity; // Auteur de l'annotation (CharacterString) - format vCard
    private Date date; // Date de l'annotation (DateTime)
    private String description; // Contenu de l'annotation (LangString)

    // Relation vers LomSchema
    private LomSchema lomSchema;

    public Annotation() {
        this.date = new Date();
    }

    public Annotation(String entity, String description) {
        this();
        this.entity = entity;
        this.description = description;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "id=" + id +
                ", entity='" + entity + '\'' +
                ", date=" + date +
                '}';
    }
}
