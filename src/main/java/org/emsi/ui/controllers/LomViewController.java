package org.emsi.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.emsi.entities.*;

import java.text.SimpleDateFormat;

public class LomViewController {

    @FXML
    private VBox infoBox;

    @FXML
    private Label generalTitle;
    @FXML
    private Label generalLanguage;
    @FXML
    private Label generalDescription;
    @FXML
    private Label generalKeywords;
    @FXML
    private Label generalCoverage;
    @FXML
    private Label generalStructure;
    @FXML
    private Label generalAggregation;

    @FXML
    private Label lifecycleVersion;
    @FXML
    private Label lifecycleStatus;
    @FXML
    private VBox contributeBox;

    @FXML
    private Label technicalFormat;
    @FXML
    private Label technicalSize;
    @FXML
    private Label technicalLocation;
    @FXML
    private Label technicalDuration;
    @FXML
    private Label technicalRemarks;
    @FXML
    private Label technicalRequirements;

    @FXML
    private Label eduInteractivityType;
    @FXML
    private Label eduResourceType;
    @FXML
    private Label eduInteractivityLevel;
    @FXML
    private Label eduEndUserRole;
    @FXML
    private Label eduContext;
    @FXML
    private Label eduDifficulty;
    @FXML
    private Label eduLearningTime;
    @FXML
    private Label eduAgeRange;
    @FXML
    private Label eduLanguage;
    @FXML
    private Label eduDescription;

    @FXML
    private Label rightsCost;
    @FXML
    private Label rightsCopyright;
    @FXML
    private Label rightsDescription;

    @FXML
    private VBox annotationsBox;

    public void setLom(LomSchema lom) {
        if (lom == null)
            return;

        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

        // Info Tab
        infoBox.getChildren().clear();
        infoBox.getChildren().addAll(
                createInfoLabel("ID:", String.valueOf(lom.getId())),
                createInfoLabel("Titre:", lom.getResourceTitle()),
                createInfoLabel("URL:", lom.getResourceUrl()),
                createInfoLabel("Créé le:", sdfFull.format(lom.getCreatedAt())),
                createInfoLabel("Modifié le:", sdfFull.format(lom.getUpdatedAt())));

        // General Tab
        General g = lom.getGeneral();
        if (g != null) {
            generalTitle.setText(g.getTitle());
            generalLanguage.setText(g.getLanguage());
            generalDescription.setText(g.getDescription());
            generalKeywords.setText(g.getKeyword());
            generalCoverage.setText(g.getCoverage());
            generalStructure.setText(g.getStructure());
            generalAggregation.setText(g.getAggregationLevel() != null ? String.valueOf(g.getAggregationLevel()) : "-");
        }

        // Lifecycle Tab
        Lifecycle l = lom.getLifecycle();
        if (l != null) {
            lifecycleVersion.setText(l.getVersion());
            lifecycleStatus.setText(l.getStatus());

            contributeBox.getChildren().clear();
            if (l.getContributes() != null) {
                for (Contribute c : l.getContributes()) {
                    contributeBox.getChildren().add(
                            createInfoLabel("• " + c.getRole() + ":", c.getEntity()));
                }
            }
        }

        // Technical Tab
        Technical t = lom.getTechnical();
        if (t != null) {
            technicalFormat.setText(t.getFormat());
            technicalSize.setText(t.getSize());
            technicalLocation.setText(t.getLocation());
            technicalDuration.setText(t.getDuration());
            technicalRemarks.setText(t.getInstallationRemarks());
            technicalRequirements.setText(t.getOtherPlatformRequirements());
        }

        // Educational Tab
        Educational e = lom.getEducational();
        if (e != null) {
            eduInteractivityType.setText(e.getInteractivityType());
            eduResourceType.setText(e.getLearningResourceType());
            eduInteractivityLevel.setText(e.getInteractivityLevelLabel());
            eduEndUserRole.setText(e.getIntendedEndUserRole());
            eduContext.setText(e.getContext());
            eduDifficulty.setText(e.getDifficultyLabel());
            eduLearningTime.setText(e.getTypicalLearningTime());
            eduAgeRange.setText(e.getTypicalAgeRange());
            eduLanguage.setText(e.getLanguage());
            eduDescription.setText(e.getDescription());
        }

        // Rights Tab
        Rights r = lom.getRights();
        if (r != null) {
            rightsCost.setText("yes".equals(r.getCost()) ? "Oui" : "Non");
            rightsCopyright.setText("yes".equals(r.getCopyrightAndOtherRestrictions()) ? "Oui" : "Non");
            rightsDescription.setText(r.getDescription());
        }

        // Annotations Tab
        annotationsBox.getChildren().clear();
        if (lom.getAnnotations() != null && !lom.getAnnotations().isEmpty()) {
            for (Annotation a : lom.getAnnotations()) {
                VBox annotationItem = new VBox(5);
                annotationItem.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");
                annotationItem.getChildren().addAll(
                        new Label("✍️ " + a.getEntity() + " - " + sdfDate.format(a.getDate())),
                        new Label(a.getDescription()));
                annotationsBox.getChildren().add(annotationItem);
            }
        } else {
            annotationsBox.getChildren().add(new Label("Aucune annotation"));
        }
    }

    private HBox createInfoLabel(String label, String value) {
        HBox box = new HBox(10);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");
        Label valueNode = new Label(value != null && !value.isEmpty() ? value : "-");
        valueNode.setWrapText(true);
        box.getChildren().addAll(labelNode, valueNode);
        return box;
    }
}
