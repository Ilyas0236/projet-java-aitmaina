package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité Relation - Catégorie 7 du LOM
 * Relation avec d'autres objets d'apprentissage
 */
public class Relation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String kind; // Type de relation (State: ispartof, haspart, isversionof, hasversion,
                         // isformatof, hasformat, references, isreferencedby, isbasedon, isbasisfor,
                         // requires, isrequiredby)

    // Relation vers LomSchema
    private LomSchema lomSchema;

    // Ressource liée
    private Resource resource;

    public Relation() {
    }

    public Relation(String kind) {
        this.kind = kind;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "id=" + id +
                ", kind='" + kind + '\'' +
                '}';
    }
}
