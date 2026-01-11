package org.emsi.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité General - Catégorie 1 du LOM
 * Informations générales décrivant l'objet d'apprentissage
 */
public class General implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;           // Titre (LangString)
    private String language;        // Langue (CharacterString)
    private String description;     // Description (LangString)
    private String keyword;         // Mots-clés (LangString) - stockés séparés par virgule
    private String coverage;        // Couverture (LangString)
    private String structure;       // Structure (State: collection, mixed, linear, hierarchical, networked, branched, parceled, atomic)
    private Integer aggregationLevel; // Niveau d'agrégation (Enumerated: 1, 2, 3, 4)

    // Relation vers LomSchema
    private LomSchema lomSchema;
    
    // Identifiants multiples
    private Set<Identifier> identifiers = new HashSet<>();

    public General() {
    }

    public General(String title) {
        this.title = title;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public Integer getAggregationLevel() {
        return aggregationLevel;
    }

    public void setAggregationLevel(Integer aggregationLevel) {
        this.aggregationLevel = aggregationLevel;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public void addIdentifier(Identifier identifier) {
        this.identifiers.add(identifier);
        identifier.setGeneral(this);
    }

    @Override
    public String toString() {
        return "General{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
