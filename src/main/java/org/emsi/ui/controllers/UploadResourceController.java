package org.emsi.ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.emsi.entities.LomSchema;
import org.emsi.entities.ResourceFile;
import org.emsi.service.FileStorageService;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour l'upload de fichiers (Admin)
 * 
 * Ce contrôleur démontre l'utilisation des Streams pour:
 * - Valider les fichiers avec filter() et anyMatch()
 * - Transformer les données avec map()
 * - Collecter les résultats avec collect()
 * 
 * @author Projet LOM - EMSI
 */
public class UploadResourceController {

    @FXML
    private Label resourceInfoLabel;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label fileSizeLabel;
    @FXML
    private Label fileTypeLabel;
    @FXML
    private Label progressLabel;

    @FXML
    private VBox fileInfoBox;
    @FXML
    private VBox progressBox;
    @FXML
    private ListView<String> uploadedFilesListView;
    @FXML
    private ProgressBar uploadProgressBar;
    @FXML
    private Button uploadBtn;
    @FXML
    private Button selectFileBtn;

    private final FileStorageService fileStorageService = FileStorageService.getInstance();

    private Stage dialogStage;
    private LomSchema currentResource;
    private File selectedFile;

    /**
     * Définir le contexte du dialogue
     */
    public void setContext(Stage stage, LomSchema resource) {
        this.dialogStage = stage;
        this.currentResource = resource;

        if (resource != null) {
            resourceInfoLabel.setText("Ressource: " + resource.getResourceTitle());
            loadUploadedFiles();
        }
    }

    /**
     * Initialisation FXML
     */
    @FXML
    public void initialize() {
        // Le bouton upload est désactivé par défaut
        uploadBtn.setDisable(true);
    }

    /**
     * STREAM API: Charger la liste des fichiers déjà uploadés
     * 
     * Utilise:
     * - listFilesByResource() du service qui utilise Files.list() + filter()
     * - stream().map() pour transformer les noms de fichiers
     */
    private void loadUploadedFiles() {
        if (currentResource == null)
            return;

        // STREAM API: Récupérer et transformer la liste des fichiers
        List<String> files = fileStorageService.listFilesByResource(currentResource.getId());

        // STREAM: Transformation pour l'affichage avec informations
        List<String> displayItems = files.stream()
                .map(fileName -> {
                    var info = fileStorageService.getFileInfo(fileName);
                    String originalName = (String) info.get("originalName");
                    Long size = (Long) info.get("size");
                    return String.format("📄 %s (%s)", originalName, formatSize(size));
                })
                .collect(Collectors.toList());

        uploadedFilesListView.getItems().setAll(displayItems);
    }

    /**
     * Handler: Sélectionner un fichier
     * 
     * Utilise FileChooser de JavaFX
     */
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier à uploader");

        // STREAM API: Créer les filtres d'extension dynamiquement
        // Ajout de filtres pour les types de fichiers courants
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mkv"),
                new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z"));

        selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null) {
            displayFileInfo();
            uploadBtn.setDisable(false);
        }
    }

    /**
     * Afficher les informations du fichier sélectionné
     */
    private void displayFileInfo() {
        if (selectedFile == null)
            return;

        fileInfoBox.setVisible(true);
        fileInfoBox.setManaged(true);

        fileNameLabel.setText(selectedFile.getName());
        fileSizeLabel.setText(formatSize(selectedFile.length()));
        fileTypeLabel.setText(getFileExtension(selectedFile.getName()).toUpperCase());
    }

    /**
     * Handler: Uploader le fichier
     * 
     * Utilise:
     * - Task JavaFX pour exécution asynchrone
     * - Files.copy() via FileStorageService
     */
    @FXML
    private void handleUpload() {
        if (selectedFile == null || currentResource == null)
            return;

        progressBox.setVisible(true);
        progressBox.setManaged(true);
        uploadBtn.setDisable(true);
        selectFileBtn.setDisable(true);

        Task<String> uploadTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Upload en cours: " + selectedFile.getName());
                updateProgress(-1, 1); // Indéterminé

                // STREAM I/O: Upload du fichier via le service
                return fileStorageService.uploadFile(selectedFile, currentResource.getId());
            }
        };

        progressLabel.textProperty().bind(uploadTask.messageProperty());
        uploadProgressBar.progressProperty().bind(uploadTask.progressProperty());

        uploadTask.setOnSucceeded(e -> {
            progressBox.setVisible(false);
            progressBox.setManaged(false);
            uploadBtn.setDisable(false);
            selectFileBtn.setDisable(false);

            // Créer l'entité ResourceFile
            ResourceFile resourceFile = new ResourceFile(
                    uploadTask.getValue(),
                    selectedFile.getName(),
                    selectedFile.length());
            resourceFile.setMimeType(getFileExtension(selectedFile.getName()));
            resourceFile.setLomSchema(currentResource);

            // Ajouter à la ressource
            currentResource.addResourceFile(resourceFile);

            // Rafraîchir la liste
            loadUploadedFiles();

            // Réinitialiser la sélection
            selectedFile = null;
            fileInfoBox.setVisible(false);
            fileInfoBox.setManaged(false);
            uploadBtn.setDisable(true);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fichier uploadé avec succès!");
        });

        uploadTask.setOnFailed(e -> {
            progressBox.setVisible(false);
            progressBox.setManaged(false);
            uploadBtn.setDisable(false);
            selectFileBtn.setDisable(false);

            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'upload: " + uploadTask.getException().getMessage());
        });

        new Thread(uploadTask).start();
    }

    /**
     * Handler: Annuler et fermer
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Formater la taille du fichier
     */
    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Extraire l'extension du fichier
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
