package org.emsi.ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.emsi.entities.LomSchema;
import org.emsi.service.FileStorageService;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le téléchargement de fichiers
 * 
 * Ce contrôleur démontre l'utilisation des Streams pour:
 * - Filtrer et transformer les listes de fichiers
 * - Préparer les données d'affichage
 * - Gérer les téléchargements multiples
 * 
 * @author Projet LOM - EMSI
 */
public class DownloadController {

    @FXML
    private Label resourceTitleLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label difficultyLabel;
    @FXML
    private Label keywordsLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label noFilesLabel;
    @FXML
    private Label progressLabel;

    @FXML
    private ListView<String> filesListView;
    @FXML
    private ProgressBar downloadProgressBar;
    @FXML
    private Button downloadBtn;
    @FXML
    private Button downloadAllBtn;
    @FXML
    private VBox progressBox;

    private final FileStorageService fileStorageService = FileStorageService.getInstance();

    private Stage dialogStage;
    private LomSchema currentResource;
    private List<String> availableFiles;

    /**
     * Définir le stage et la ressource à afficher
     */
    public void setContext(Stage stage, LomSchema resource) {
        this.dialogStage = stage;
        this.currentResource = resource;
        loadResourceInfo();
        loadFilesList();
    }

    /**
     * Initialisation FXML
     */
    @FXML
    public void initialize() {
        // Listener pour activer/désactiver le bouton de téléchargement
        filesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            downloadBtn.setDisable(newVal == null);
        });
    }

    /**
     * Charger les informations de la ressource
     */
    private void loadResourceInfo() {
        if (currentResource == null)
            return;

        resourceTitleLabel.setText("Ressource: " + currentResource.getResourceTitle());

        // Informations de la catégorie General
        if (currentResource.getGeneral() != null) {
            languageLabel.setText(currentResource.getGeneral().getLanguage() != null
                    ? currentResource.getGeneral().getLanguage()
                    : "-");
            keywordsLabel.setText(currentResource.getGeneral().getKeyword() != null
                    ? currentResource.getGeneral().getKeyword()
                    : "-");
        }

        // Difficulté depuis Educational
        if (currentResource.getEducational() != null && currentResource.getEducational().getDifficulty() != null) {
            difficultyLabel.setText(currentResource.getEducational().getDifficultyLabel());
        }
    }

    /**
     * STREAM API: Charger la liste des fichiers disponibles
     * 
     * Utilise:
     * - stream() pour parcourir les fichiers
     * - map() pour transformer en format d'affichage
     * - collect() pour convertir en List
     */
    private void loadFilesList() {
        if (currentResource == null)
            return;

        // STREAM API: Récupérer les fichiers depuis le service de stockage
        // Filtrage par ID de ressource
        availableFiles = fileStorageService.listFilesByResource(currentResource.getId());

        if (availableFiles.isEmpty()) {
            noFilesLabel.setVisible(true);
            noFilesLabel.setManaged(true);
            downloadAllBtn.setDisable(true);
        } else {
            noFilesLabel.setVisible(false);
            noFilesLabel.setManaged(false);
            downloadAllBtn.setDisable(false);

            // STREAM API: Transformer les noms de fichiers pour l'affichage
            // Extraction du nom original et calcul de la taille
            List<String> displayNames = availableFiles.stream()
                    .map(fileName -> {
                        var info = fileStorageService.getFileInfo(fileName);
                        String originalName = (String) info.get("originalName");
                        Long size = (Long) info.get("size");
                        return String.format("%s (%s)", originalName, formatSize(size));
                    })
                    .collect(Collectors.toList());

            filesListView.getItems().setAll(displayNames);
        }
    }

    /**
     * Handler: Télécharger le fichier sélectionné
     */
    @FXML
    private void handleDownload() {
        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= availableFiles.size())
            return;

        String fileName = availableFiles.get(selectedIndex);

        // Choisir le répertoire de destination
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir le dossier de destination");
        File destinationDir = directoryChooser.showDialog(dialogStage);

        if (destinationDir != null) {
            downloadFile(fileName, destinationDir.toPath());
        }
    }

    /**
     * Handler: Télécharger tous les fichiers
     * 
     * Utilise:
     * - forEach() pour itérer sur les fichiers
     * - Task JavaFX pour exécution asynchrone
     */
    @FXML
    private void handleDownloadAll() {
        if (availableFiles.isEmpty())
            return;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir le dossier de destination");
        File destinationDir = directoryChooser.showDialog(dialogStage);

        if (destinationDir != null) {
            // STREAM API: Téléchargement par lot avec forEach
            // Progression affichée pour chaque fichier
            progressBox.setVisible(true);
            progressBox.setManaged(true);

            Task<Void> downloadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    int total = availableFiles.size();
                    int current = 0;

                    // STREAM: Itération sur les fichiers
                    for (String fileName : availableFiles) {
                        current++;
                        updateMessage("Téléchargement: " + fileName);
                        updateProgress(current, total);

                        fileStorageService.downloadFile(fileName, destinationDir.toPath());

                        // Petite pause pour visualiser la progression
                        Thread.sleep(200);
                    }
                    return null;
                }
            };

            progressLabel.textProperty().bind(downloadTask.messageProperty());
            downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

            downloadTask.setOnSucceeded(e -> {
                progressBox.setVisible(false);
                progressBox.setManaged(false);
                statusLabel.setText("✅ " + availableFiles.size() + " fichiers téléchargés");
            });

            downloadTask.setOnFailed(e -> {
                progressBox.setVisible(false);
                progressBox.setManaged(false);
                statusLabel.setText("❌ Erreur: " + downloadTask.getException().getMessage());
            });

            new Thread(downloadTask).start();
        }
    }

    /**
     * Télécharger un fichier unique
     */
    private void downloadFile(String fileName, Path destinationDir) {
        progressBox.setVisible(true);
        progressBox.setManaged(true);
        progressLabel.setText("Téléchargement en cours...");
        downloadProgressBar.setProgress(-1); // Indéterminé

        Task<Path> downloadTask = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return fileStorageService.downloadFile(fileName, destinationDir);
            }
        };

        downloadTask.setOnSucceeded(e -> {
            progressBox.setVisible(false);
            progressBox.setManaged(false);
            statusLabel.setText("✅ Fichier téléchargé: " + downloadTask.getValue().getFileName());
        });

        downloadTask.setOnFailed(e -> {
            progressBox.setVisible(false);
            progressBox.setManaged(false);
            statusLabel.setText("❌ Erreur: " + downloadTask.getException().getMessage());
        });

        new Thread(downloadTask).start();
    }

    /**
     * Formater la taille du fichier
     */
    private String formatSize(Long bytes) {
        if (bytes == null)
            return "0 B";
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
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
