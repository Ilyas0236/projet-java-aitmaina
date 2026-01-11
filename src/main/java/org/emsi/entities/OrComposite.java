package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité OrComposite - Composite d'exigences alternatives
 * Associé à Requirement
 */
public class OrComposite implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String type; // Type (State: operating system, browser)
    private String name; // Nom (State: pc-dos, ms-windows, macos, unix, multi-os, none, any, netscape
                         // communicator, ms-internet explorer, opera, amaya)
    private String minimumVersion; // Version minimum (CharacterString)
    private String maximumVersion; // Version maximum (CharacterString)

    // Relation vers Requirement
    private Requirement requirement;

    public OrComposite() {
    }

    public OrComposite(String type, String name) {
        this.type = type;
        this.name = name;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    public String getMaximumVersion() {
        return maximumVersion;
    }

    public void setMaximumVersion(String maximumVersion) {
        this.maximumVersion = maximumVersion;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    @Override
    public String toString() {
        return "OrComposite{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", minimumVersion='" + minimumVersion + '\'' +
                ", maximumVersion='" + maximumVersion + '\'' +
                '}';
    }
}
