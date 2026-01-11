package org.emsi.ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.emsi.entities.*;
import org.emsi.service.LomService;

public class LomEditorController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField languageField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField keywordField;
    @FXML
    private ComboBox<String> structureCombo;
    @FXML
    private ComboBox<Integer> aggregationLevelCombo;

    @FXML
    private TextField versionField;
    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private TextField formatField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField durationField;

    @FXML
    private ComboBox<String> interactivityTypeCombo;
    @FXML
    private ComboBox<String> learningResourceTypeCombo;
    @FXML
    private ComboBox<Integer> interactivityLevelCombo;
    @FXML
    private ComboBox<String> intendedEndUserRoleCombo;
    @FXML
    private ComboBox<String> contextCombo;
    @FXML
    private ComboBox<Integer> difficultyCombo;
    @FXML
    private TextField typicalLearningTimeField;
    @FXML
    private TextField typicalAgeRangeField;

    @FXML
    private ComboBox<String> costCombo;
    @FXML
    private ComboBox<String> copyrightCombo;
    @FXML
    private TextArea rightsDescriptionArea;

    private LomService lomService;

    @FXML
    public void initialize() {
        lomService = LomService.getInstance();
        initializeCombos();
    }

    private void initializeCombos() {
        structureCombo.setItems(FXCollections.observableArrayList("atomic", "collection", "networked", "hierarchical",
                "linear", "branched", "mixed", "parceled"));
        aggregationLevelCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        statusCombo.setItems(FXCollections.observableArrayList("draft", "final", "revised", "unavailable"));
        interactivityTypeCombo.setItems(FXCollections.observableArrayList("active", "expositive", "mixed"));
        learningResourceTypeCombo.setItems(FXCollections.observableArrayList("exercise", "simulation", "questionnaire",
                "diagram", "figure", "graph", "index", "slide", "table", "narrative text", "exam", "experiment",
                "problem statement", "self assessment", "lecture"));
        interactivityLevelCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        intendedEndUserRoleCombo.setItems(FXCollections.observableArrayList("teacher", "author", "learner", "manager"));
        contextCombo.setItems(FXCollections.observableArrayList("school", "higher education", "training", "other"));
        difficultyCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        costCombo.setItems(FXCollections.observableArrayList("yes", "no"));
        copyrightCombo.setItems(FXCollections.observableArrayList("yes", "no"));
    }

    public void loadValues(LomSchema lom) {
        // General
        General general = lom.getGeneral();
        if (general != null) {
            titleField.setText(general.getTitle() != null ? general.getTitle() : "");
            languageField.setText(general.getLanguage() != null ? general.getLanguage() : "");
            descriptionArea.setText(general.getDescription() != null ? general.getDescription() : "");
            keywordField.setText(general.getKeyword() != null ? general.getKeyword() : "");
            structureCombo.setValue(general.getStructure());
            aggregationLevelCombo.setValue(general.getAggregationLevel());
        }

        // Lifecycle
        Lifecycle lifecycle = lom.getLifecycle();
        if (lifecycle != null) {
            versionField.setText(lifecycle.getVersion() != null ? lifecycle.getVersion() : "");
            statusCombo.setValue(lifecycle.getStatus());
        }

        // Technical
        Technical technical = lom.getTechnical();
        if (technical != null) {
            formatField.setText(technical.getFormat() != null ? technical.getFormat() : "");
            sizeField.setText(technical.getSize() != null ? technical.getSize() : "");
            locationField.setText(technical.getLocation() != null ? technical.getLocation() : "");
            durationField.setText(technical.getDuration() != null ? technical.getDuration() : "");
        }

        // Educational
        Educational educational = lom.getEducational();
        if (educational != null) {
            interactivityTypeCombo.setValue(educational.getInteractivityType());
            learningResourceTypeCombo.setValue(educational.getLearningResourceType());
            interactivityLevelCombo.setValue(educational.getInteractivityLevel());
            intendedEndUserRoleCombo.setValue(educational.getIntendedEndUserRole());
            contextCombo.setValue(educational.getContext());
            difficultyCombo.setValue(educational.getDifficulty());
            typicalLearningTimeField
                    .setText(educational.getTypicalLearningTime() != null ? educational.getTypicalLearningTime() : "");
            typicalAgeRangeField
                    .setText(educational.getTypicalAgeRange() != null ? educational.getTypicalAgeRange() : "");
        }

        // Rights
        Rights rights = lom.getRights();
        if (rights != null) {
            costCombo.setValue(rights.getCost());
            copyrightCombo.setValue(rights.getCopyrightAndOtherRestrictions());
            rightsDescriptionArea.setText(rights.getDescription() != null ? rights.getDescription() : "");
        }
    }

    public void saveValues(LomSchema lom) {
        // General
        lomService.updateGeneral(lom,
                titleField.getText(),
                languageField.getText(),
                descriptionArea.getText(),
                keywordField.getText(),
                structureCombo.getValue(),
                aggregationLevelCombo.getValue());

        // Lifecycle
        lomService.updateLifecycle(lom,
                versionField.getText(),
                statusCombo.getValue());

        // Technical
        lomService.updateTechnical(lom,
                formatField.getText(),
                sizeField.getText(),
                locationField.getText(),
                durationField.getText());

        // Educational
        lomService.updateEducational(lom,
                interactivityTypeCombo.getValue(),
                learningResourceTypeCombo.getValue(),
                interactivityLevelCombo.getValue(),
                intendedEndUserRoleCombo.getValue(),
                contextCombo.getValue(),
                difficultyCombo.getValue(),
                typicalLearningTimeField.getText(),
                typicalAgeRangeField.getText());

        // Rights
        lomService.updateRights(lom,
                costCombo.getValue(),
                copyrightCombo.getValue(),
                rightsDescriptionArea.getText());
    }
}
