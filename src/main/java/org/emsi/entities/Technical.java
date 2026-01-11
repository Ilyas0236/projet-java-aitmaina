package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité Technical - Catégorie 4 du LOM
 * Exigences et caractéristiques techniques de l'objet d'apprentissage
 */
public class Technical implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String format; // Format (CharacterString) - MIME type
    private String size; // Taille (CharacterString) - en octets
    private String location; // Localisation (CharacterString) - URL
    private String installationRemarks; // Remarques d'installation (LangString)
    private String otherPlatformRequirements; // Autres exigences (LangString)
    private String duration; // Durée (Duration) - format ISO 8601

    // Relation vers LomSchema
    private LomSchema lomSchema;

    // Exigences techniques
    private Set<Requirement> requirements = new HashSet<>();

    public Technical() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInstallationRemarks() {
        return installationRemarks;
    }

    public void setInstallationRemarks(String installationRemarks) {
        this.installationRemarks = installationRemarks;
    }

    public String getOtherPlatformRequirements() {
        return otherPlatformRequirements;
    }

    public void setOtherPlatformRequirements(String otherPlatformRequirements) {
        this.otherPlatformRequirements = otherPlatformRequirements;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(Set<Requirement> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        requirement.setTechnical(this);
    }

    @Override
    public String toString() {
        return "Technical{" +
                "id=" + id +
                ", format='" + format + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
