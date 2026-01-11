package org.emsi.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.emsi.entities.Tag;
import org.emsi.service.TagService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des tags
 * 
 * Utilise les Streams pour:
 * - Afficher les tags de manière formatée
 * - Mettre à jour les statistiques
 * - Rechercher dans les tags
 * 
 * @author Projet LOM - EMSI
 */
public class TagManagerController {

    @FXML
    private TextField tagNameField;
    @FXML
    private TextField tagColorField;
    @FXML
    private TextField tagDescriptionField;
    @FXML
    private TextField searchTagField;
    @FXML
    private ListView<String> tagsListView;
    @FXML
    private Label tagCountLabel;
    @FXML
    private Label totalTagsLabel;
    @FXML
    private Label mostUsedTagLabel;
    @FXML
    private Label unusedTagsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button deleteBtn;

    private final TagService tagService = TagService.getInstance();
    private Stage dialogStage;
    private List<Tag> currentTags;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        // Listener pour activer le bouton supprimer
        tagsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteBtn.setDisable(newVal == null);
        });

        // Listener pour recherche en temps réel
        searchTagField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                searchTags(newVal);
            } else {
                loadTags();
            }
        });

        loadTags();
        updateStatistics();
    }

    /**
     * STREAM: Charger tous les tags
     * 
     * Utilise:
     * - stream().map() pour formater l'affichage
     */
    private void loadTags() {
        currentTags = tagService.getAllTags();

        // STREAM API: Transformation Tag -> String pour l'affichage
        List<String> displayItems = currentTags.stream()
                .map(tag -> {
                    int resourceCount = tag.getResources().size();
                    String color = tag.getColor() != null ? tag.getColor() : "#95a5a6";
                    return String.format("🏷️ %s (%d ressources) - %s",
                            tag.getName(), resourceCount, color);
                })
                .collect(Collectors.toList());

        tagsListView.getItems().setAll(displayItems);
        tagCountLabel.setText("(" + currentTags.size() + " tags)");
    }

    /**
     * STREAM: Rechercher des tags
     */
    private void searchTags(String keyword) {
        List<Tag> results = tagService.searchTags(keyword);
        currentTags = results;

        // STREAM: Affichage des résultats
        List<String> displayItems = results.stream()
                .map(tag -> String.format("🏷️ %s (%d ressources)",
                        tag.getName(), tag.getResources().size()))
                .collect(Collectors.toList());

        tagsListView.getItems().setAll(displayItems);
        tagCountLabel.setText("(" + results.size() + " résultats)");
    }

    /**
     * STREAM: Mettre à jour les statistiques
     */
    private void updateStatistics() {
        long total = tagService.countTags();
        totalTagsLabel.setText(String.valueOf(total));

        // Tag le plus utilisé
        List<Tag> mostUsed = tagService.getMostUsedTags(1);
        if (!mostUsed.isEmpty()) {
            Tag topTag = mostUsed.get(0);
            mostUsedTagLabel.setText(topTag.getName() + " (" + topTag.getResources().size() + ")");
        } else {
            mostUsedTagLabel.setText("-");
        }

        // Tags non utilisés
        List<Tag> unused = tagService.getUnusedTags();
        unusedTagsLabel.setText(String.valueOf(unused.size()));
    }

    /**
     * Handler: Créer un nouveau tag
     */
    @FXML
    private void handleCreateTag() {
        String name = tagNameField.getText().trim();
        String color = tagColorField.getText().trim();
        String description = tagDescriptionField.getText().trim();

        if (name.isEmpty()) {
            statusLabel.setText("⚠️ Le nom du tag est obligatoire");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Validation couleur hexa
        if (!color.isEmpty() && !color.matches("^#[0-9a-fA-F]{6}$")) {
            statusLabel.setText("⚠️ Format de couleur invalide (ex: #3498db)");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        Tag created = tagService.createTag(name, color.isEmpty() ? "#3498db" : color, description);

        if (created != null) {
            statusLabel.setText("✅ Tag créé: " + name);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            // Réinitialiser le formulaire
            tagNameField.clear();
            tagColorField.clear();
            tagDescriptionField.clear();

            loadTags();
            updateStatistics();
        }
    }

    /**
     * Handler: Supprimer un tag
     */
    @FXML
    private void handleDeleteTag() {
        int selectedIndex = tagsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentTags.size())
            return;

        Tag tag = currentTags.get(selectedIndex);

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le tag ?");
        confirm.setContentText("Voulez-vous vraiment supprimer le tag \"" + tag.getName() + "\" ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            tagService.deleteTag(tag.getId());
            statusLabel.setText("✅ Tag supprimé");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            loadTags();
            updateStatistics();
        }
    }

    /**
     * Handler: Rafraîchir la liste
     */
    @FXML
    private void handleRefresh() {
        searchTagField.clear();
        loadTags();
        updateStatistics();
        statusLabel.setText("✅ Liste actualisée");
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
    }

    /**
     * Handler: Fermer le dialogue
     */
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
