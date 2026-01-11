package org.emsi.entities;

import java.io.Serializable;

/**
 * Entité Educational - Catégorie 5 du LOM
 * Caractéristiques éducatives ou pédagogiques de l'objet d'apprentissage
 */
public class Educational implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String interactivityType; // Type d'interactivité (State: active, expositive, mixed)
    private String learningResourceType; // Type de ressource (State: exercise, simulation, questionnaire, diagram,
                                         // figure, graph, index, slide, table, narrative text, exam, experiment,
                                         // problem statement, self assessment, lecture)
    private Integer interactivityLevel; // Niveau d'interactivité (Enumerated: very low, low, medium, high, very high)
    private Integer semanticDensity; // Densité sémantique (Enumerated: very low, low, medium, high, very high)
    private String intendedEndUserRole; // Public cible (State: teacher, author, learner, manager)
    private String context; // Contexte (State: school, higher education, training, other)
    private String typicalAgeRange; // Tranche d'âge typique (LangString)
    private Integer difficulty; // Difficulté (Enumerated: very easy, easy, medium, difficult, very difficult)
    private String typicalLearningTime; // Temps d'apprentissage typique (Duration)
    private String description; // Description (LangString)
    private String language; // Langue de l'utilisateur (CharacterString)

    // Relation vers LomSchema
    private LomSchema lomSchema;

    public Educational() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInteractivityType() {
        return interactivityType;
    }

    public void setInteractivityType(String interactivityType) {
        this.interactivityType = interactivityType;
    }

    public String getLearningResourceType() {
        return learningResourceType;
    }

    public void setLearningResourceType(String learningResourceType) {
        this.learningResourceType = learningResourceType;
    }

    public Integer getInteractivityLevel() {
        return interactivityLevel;
    }

    public void setInteractivityLevel(Integer interactivityLevel) {
        this.interactivityLevel = interactivityLevel;
    }

    public Integer getSemanticDensity() {
        return semanticDensity;
    }

    public void setSemanticDensity(Integer semanticDensity) {
        this.semanticDensity = semanticDensity;
    }

    public String getIntendedEndUserRole() {
        return intendedEndUserRole;
    }

    public void setIntendedEndUserRole(String intendedEndUserRole) {
        this.intendedEndUserRole = intendedEndUserRole;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTypicalAgeRange() {
        return typicalAgeRange;
    }

    public void setTypicalAgeRange(String typicalAgeRange) {
        this.typicalAgeRange = typicalAgeRange;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getTypicalLearningTime() {
        return typicalLearningTime;
    }

    public void setTypicalLearningTime(String typicalLearningTime) {
        this.typicalLearningTime = typicalLearningTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    // Méthodes utilitaires pour affichage
    public String getDifficultyLabel() {
        if (difficulty == null)
            return "Non défini";
        switch (difficulty) {
            case 1:
                return "Très facile";
            case 2:
                return "Facile";
            case 3:
                return "Moyen";
            case 4:
                return "Difficile";
            case 5:
                return "Très difficile";
            default:
                return "Non défini";
        }
    }

    public String getInteractivityLevelLabel() {
        if (interactivityLevel == null)
            return "Non défini";
        switch (interactivityLevel) {
            case 1:
                return "Très bas";
            case 2:
                return "Bas";
            case 3:
                return "Moyen";
            case 4:
                return "Élevé";
            case 5:
                return "Très élevé";
            default:
                return "Non défini";
        }
    }

    @Override
    public String toString() {
        return "Educational{" +
                "id=" + id +
                ", interactivityType='" + interactivityType + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}
