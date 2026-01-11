package org.emsi.ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.emsi.entities.LomSchema;
import org.emsi.service.AuthService;
import org.emsi.service.LomService;
import org.emsi.service.SearchService;
import org.emsi.service.XmlExportService;
import org.emsi.service.FavoriteService;
import org.emsi.ui.LomViewDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class UserDashboardController {

    @FXML
    private Label userLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> languageCombo;
    @FXML
    private ComboBox<Integer> difficultyCombo;
    @FXML
    private TableView<LomSchema> resourceTable;
    @FXML
    private TableColumn<LomSchema, String> titleCol;
    @FXML
    private TableColumn<LomSchema, String> langCol;
    @FXML
    private TableColumn<LomSchema, String> diffCol;
    @FXML
    private TableColumn<LomSchema, String> dateCol;
    @FXML
    private TableColumn<LomSchema, Void> actionsCol;

    private final LomService lomService = LomService.getInstance();
    private final SearchService searchService = SearchService.getInstance();
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

        // Initialiser ComboBox
        languageCombo.getItems().add("Toutes");
        languageCombo.getItems().addAll(searchService.getAvailableLanguages());
        languageCombo.setValue("Toutes");

        // Columns
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceTitle()));

        langCol.setCellValueFactory(data -> {
            if (data.getValue().getGeneral() != null) {
                return new SimpleStringProperty(data.getValue().getGeneral().getLanguage());
            }
            return new SimpleStringProperty("-");
        });

        diffCol.setCellValueFactory(data -> {
            if (data.getValue().getEducational() != null && data.getValue().getEducational().getDifficulty() != null) {
                return new SimpleStringProperty(data.getValue().getEducational().getDifficultyLabel());
            }
            return new SimpleStringProperty("-");
        });

        dateCol.setCellValueFactory(data -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(sdf.format(data.getValue().getCreatedAt()));
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button downloadBtn = new Button("📥 PDF");
            private final Button exportBtn = new Button("📄 XML");
            private final Button viewBtn = new Button("👁️ Voir");
            private final Button favoriteBtn = new Button("⭐");
            private final HBox buttons = new HBox(5, viewBtn, downloadBtn, exportBtn, favoriteBtn);

            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                downloadBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand;");
                exportBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                favoriteBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> viewResource(getTableRow().getItem()));
                downloadBtn.setOnAction(e -> downloadResource(getTableRow().getItem()));
                exportBtn.setOnAction(e -> exportResource(getTableRow().getItem()));
                favoriteBtn.setOnAction(e -> toggleFavorite(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    // Mettre à jour le bouton favori selon l'état
                    LomSchema resource = getTableRow().getItem();
                    boolean isFav = FavoriteService.getInstance().isFavorite(authService.getCurrentUser(), resource);
                    favoriteBtn.setText(isFav ? "★" : "☆");
                    favoriteBtn
                            .setStyle(isFav ? "-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-cursor: hand;"
                                    : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand;");
                    setGraphic(buttons);
                }
            }
        });

        resourceTable.setItems(resourceList);

        // Double-click
        resourceTable.setRowFactory(tv -> {
            TableRow<LomSchema> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewResource(row.getItem());
                }
            });
            return row;
        });
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
    private void performSearch() {
        String query = searchField.getText().trim();
        String language = "Toutes".equals(languageCombo.getValue()) ? null : languageCombo.getValue();
        Integer difficulty = difficultyCombo.getValue();

        resourceList.clear();
        List<LomSchema> results = searchService.search(query, query, language, difficulty);
        resourceList.addAll(results);
    }

    @FXML
    private void resetSearch() {
        searchField.clear();
        languageCombo.setValue("Toutes");
        difficultyCombo.setValue(null);
        loadResources();
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
     * HANDLER: Afficher les favoris
     */
    @FXML
    private void handleFavorites() {
        org.emsi.service.FavoriteService favoriteService = org.emsi.service.FavoriteService.getInstance();
        java.util.List<org.emsi.entities.LomSchema> favorites = favoriteService
                .getUserFavoriteResources(authService.getCurrentUser());

        if (favorites.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Favoris");
            alert.setHeaderText("⭐ Vos Favoris");
            alert.setContentText("Vous n'avez pas encore de ressources favorites.\n\n" +
                    "Pour ajouter une ressource aux favoris, utilisez la recherche avancée " +
                    "puis cliquez sur le bouton ⭐ Favoris.");
            alert.showAndWait();
        } else {
            // Afficher les favoris dans la liste principale
            resourceList.clear();
            resourceList.addAll(favorites);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Favoris");
            alert.setHeaderText("⭐ " + favorites.size() + " ressource(s) favorite(s)");
            alert.setContentText("La liste affiche maintenant uniquement vos favoris.\n" +
                    "Cliquez sur 'Réinitialiser' pour voir toutes les ressources.");
            alert.showAndWait();
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
    }

    private void viewResource(LomSchema lom) {
        if (lom == null)
            return;
        LomSchema fullLom = lomService.getResourceById(lom.getId());
        LomViewDialog viewer = new LomViewDialog(fullLom);
        viewer.showAndWait();
    }

    private void exportResource(LomSchema lom) {
        if (lom == null)
            return;

        LomSchema fullLom = lomService.getResourceById(lom.getId());
        XmlExportService exportService = new XmlExportService();
        String xml = exportService.exportToXml(fullLom);

        TextArea textArea = new TextArea(xml);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export XML");
        alert.setHeaderText("Métadonnées LOM - " + fullLom.getResourceTitle());
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * HANDLER: Télécharger les fichiers d'une ressource
     * 
     * Ouvre le dialogue de téléchargement pour permettre à l'utilisateur
     * de télécharger les PDFs et autres fichiers associés
     */
    private void downloadResource(LomSchema lom) {
        if (lom == null)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/download_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("📥 Télécharger - " + lom.getResourceTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setScene(new Scene(root));

            DownloadController controller = loader.getController();
            LomSchema fullLom = lomService.getResourceById(lom.getId());
            controller.setContext(dialogStage, fullLom);

            dialogStage.showAndWait();
        } catch (Exception e) {
            showError("Erreur Téléchargement", "Impossible d'ouvrir le dialogue de téléchargement:\n" + e.getMessage());
        }
    }

    /**
     * HANDLER: Basculer l'état favori d'une ressource
     * 
     * Utilise FavoriteService pour ajouter/retirer des favoris
     * Démontre l'utilisation des services et de la logique métier
     */
    private void toggleFavorite(LomSchema lom) {
        if (lom == null)
            return;

        FavoriteService favoriteService = FavoriteService.getInstance();
        boolean isFavorite = favoriteService.isFavorite(authService.getCurrentUser(), lom);

        if (isFavorite) {
            favoriteService.removeFavorite(authService.getCurrentUser(), lom);
            showInfo("Favori retiré", "La ressource \"" + lom.getResourceTitle() + "\" a été retirée de vos favoris.");
        } else {
            favoriteService.addFavorite(authService.getCurrentUser(), lom);
            showInfo("Favori ajouté", "La ressource \"" + lom.getResourceTitle() + "\" a été ajoutée à vos favoris ⭐");
        }

        // Rafraîchir la table pour mettre à jour l'icône
        resourceTable.refresh();
    }

    /**
     * Afficher un message d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
