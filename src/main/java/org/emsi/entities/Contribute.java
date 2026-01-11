package org.emsi.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entité Contribute - Contributeur d'un objet d'apprentissage
 * Associé à Lifecycle
 */
public class Contribute implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String role; // Rôle (State: author, publisher, unknown, initiator, terminator, validator,
                         // editor, graphical designer, technical implementer, content provider,
                         // technical validator, educational validator, script writer, instructional
                         // designer, subject matter expert)
    private String entity; // Entité/Personne (CharacterString) - format vCard
    private Date date; // Date de contribution (DateTime)

    // Relation vers Lifecycle
    private Lifecycle lifecycle;

    public Contribute() {
    }

    public Contribute(String role, String entity) {
        this.role = role;
        this.entity = entity;
        this.date = new Date();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public String toString() {
        return "Contribute{" +
                "role='" + role + '\'' +
                ", entity='" + entity + '\'' +
                ", date=" + date +
                '}';
    }
}
