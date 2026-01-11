package org.emsi.ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.emsi.entities.LomSchema;
import org.emsi.service.AuthService;
import org.emsi.service.LomService;
import org.emsi.ui.LomEditorDialog;
import org.emsi.ui.LomViewDialog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML
    private Label userLabel;
    @FXML
    private Label countLabel;
    @FXML
    private TableView<LomSchema> resourceTable;
    @FXML
    private TableColumn<LomSchema, String> idCol;
    @FXML
    private TableColumn<LomSchema, String> titleCol;
    @FXML
    private TableColumn<LomSchema, String> dateCol;
    @FXML
    private TableColumn<LomSchema, Void> actionsCol;

    private final LomService lomService = LomService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final ObservableList<LomSchema> resourceList = FXCollections.observableArrayList();

    private Stage stage;
    private Runnable onLogout;

    public void setContext(Stage stage, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        initializeData();
    }

    @FXML
    public void initialize() {
        userLabel.setText("👤 " + authService.getCurrentUser().getFullName());

        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceTitle()));
        dateCol.setCellValueFactory(data -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(sdf.format(data.getValue().getCreatedAt()));
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button viewBtn = new Button("👁️ Voir");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                viewBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> editResource(getTableRow().getItem()));
                viewBtn.setOnAction(e -> viewResource(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> deleteResource(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        resourceTable.setItems(resourceList);
    }

    private void initializeData() {
        loadResources();
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        if (onLogout != null) {
            onLogout.run();
        }
    }

    @FXML
    private void handleNewResource() {
        Dialog<LomSchema> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Ressource");
        dialog.setHeaderText("Créer une nouvelle ressource pédagogique");

        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Titre de la ressource");
        titleField.setPrefWidth(300);

        TextField urlField = new TextField();
        urlField.setPromptText("URL ou chemin du fichier");

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("URL/Fichier:"), 0, 1);
        grid.add(urlField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    return lomService.createResource(title, urlField.getText().trim());
                }
            }
            return null;
        });

        Optional<LomSchema> result = dialog.showAndWait();
        result.ifPresent(lom -> {
            loadResources();
            editResource(lom);
        });
    }

    @FXML
    private void handleRefresh() {
        loadResources();
    }

    @FXML
    private void handleStats() {
        Alert stats = new Alert(Alert.AlertType.INFORMATION);
        stats.setTitle("Statistiques");
        stats.setHeaderText("📊 Statistiques du système LOM");
        stats.setContentText(
                "📚 Nombre de ressources: " + lomService.countResources() + "\n" +
                        "👤 Utilisateur connecté: " + authService.getCurrentUser().getFullName() + "\n" +
                        "🔐 Rôle: " + authService.getCurrentUser().getRole());
        stats.showAndWait();
    }

    /**
     * HANDLER: Ouvrir le gestionnaire de tags
     */
    @FXML
    private void handleTagManager() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/tag_manager_dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("🏷️ Gestion des Tags");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setScene(new javafx.scene.Scene(root));

            TagManagerController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (Exception e) {
            showError("Erreur ouverture Tags", e.getMessage());
        }
    }

    /**
     * HANDLER: Ouvrir la recherche avancée
     */
    @FXML
    private void handleAdvancedSearch() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/advanced_search_dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("🔍 Recherche Avancée");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setScene(new javafx.scene.Scene(root));

            AdvancedSearchController controller = loader.getController();
            controller.setContext(dialogStage, authService.getCurrentUser());

            dialogStage.showAndWait();
            loadResources(); // Rafraîchir après fermeture
        } catch (Exception e) {
            showError("Erreur ouverture Recherche", e.getMessage());
        }
    }

    /**
     * HANDLER: Import en masse
     */
    @FXML
    private void handleBatchImport() {
        // 1. Sélectionner le fichier
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV d'import");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        java.io.File file = fileChooser.showOpenDialog(stage);

        if (file == null)
            return;

        try {
            // 2. Lire et Parser le fichier CSV
            List<org.emsi.service.BatchImportService.ResourceData> dataToImport = new java.util.ArrayList<>();
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());

            // Ignorer la première ligne si c'est l'en-tête (Titre,URL,Description,Langue)
            boolean firstLine = true;
            for (String line : lines) {
                if (line.trim().isEmpty())
                    continue;
                if (firstLine && (line.toLowerCase().startsWith("titre") || line.toLowerCase().startsWith("title"))) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String title = parts[0].trim();
                    String url = parts[1].trim();
                    String desc = parts.length > 2 ? parts[2].trim() : "";
                    String lang = parts.length > 3 ? parts[3].trim() : "fr";

                    dataToImport.add(new org.emsi.service.BatchImportService.ResourceData(title, url, desc, lang));
                }
            }

            if (dataToImport.isEmpty()) {
                showError("Fichier vide", "Aucune donnée valide trouvée dans le fichier.");
                return;
            }

            // 3. Lancer l'import via le service (Multi-threading)
            org.emsi.service.BatchImportService importService = org.emsi.service.BatchImportService.getInstance();

            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Import en cours");
            progressAlert.setHeaderText("Traitement de " + dataToImport.size() + " ressources...");
            progressAlert.setContentText("Veuillez patienter pendant l'import parallèle.");
            progressAlert.show(); // Non-bloquant pour l'instant

            // Utilisation d'un thread séparé pour ne pas bloquer l'UI pendant l'attente des
            // résultats
            new Thread(() -> {
                org.emsi.service.BatchImportService.ImportResult result = importService
                        .importResourcesParallel(dataToImport);

                // Mise à jour de l'UI sur le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    progressAlert.close();

                    Alert resultAlert = new Alert(
                            result.hasErrors() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
                    resultAlert.setTitle("Résultat Import");
                    resultAlert.setHeaderText("Import terminé !");

                    StringBuilder msg = new StringBuilder();
                    msg.append("✅ Succès: ").append(result.successCount).append("\n");
                    msg.append("❌ Échecs: ").append(result.errorCount).append("\n");

                    if (result.hasErrors()) {
                        msg.append("\nErreurs:\n");
                        for (String err : result.errors) {
                            msg.append("• ").append(err).append("\n");
                        }
                    }

                    resultAlert.setContentText(msg.toString());
                    resultAlert.showAndWait();

                    // Rafraîchir la liste
                    loadResources();
                });
            }).start();

        } catch (Exception e) {
            showError("Erreur Import", "Erreur lors de la lecture du fichier:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadResources() {
        resourceList.clear();
        List<LomSchema> resources = lomService.getAllResources();
        resourceList.addAll(resources);
        countLabel.setText("📚 Ressources: " + lomService.countResources());
    }

    private void editResource(LomSchema lom) {
        if (lom == null)
            return;
        LomEditorDialog editor = new LomEditorDialog(lom);
        editor.showAndWait().ifPresent(updated -> {
            lomService.updateResource(updated);
            loadResources();
        });
    }

    private void viewResource(LomSchema lom) {
        if (lom == null)
            return;
        LomSchema fullLom = lomService.getResourceById(lom.getId());
        LomViewDialog viewer = new LomViewDialog(fullLom);
        viewer.showAndWait();
    }

    private void deleteResource(LomSchema lom) {
        if (lom == null)
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la ressource?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + lom.getResourceTitle() + "\"?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                lomService.deleteResource(lom.getId());
                loadResources();
            }
        });
    }
}
