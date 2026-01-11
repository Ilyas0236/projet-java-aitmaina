package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Lifecycle - Catégorie 2 du LOM
 * Information sur l'historique et l'état actuel de l'objet d'apprentissage
 */
public class Lifecycle implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String version; // Version (LangString)
    private String status; // Statut (State: draft, final, revised, unavailable)

    // Relation vers LomSchema
    private LomSchema lomSchema;

    // Contributeurs
    private Set<Contribute> contributes = new HashSet<>();

    public Lifecycle() {
    }

    public Lifecycle(String version, String status) {
        this.version = version;
        this.status = status;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Set<Contribute> getContributes() {
        return contributes;
    }

    public void setContributes(Set<Contribute> contributes) {
        this.contributes = contributes;
    }

    public void addContribute(Contribute contribute) {
        this.contributes.add(contribute);
        contribute.setLifecycle(this);
    }

    @Override
    public String toString() {
        return "Lifecycle{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
