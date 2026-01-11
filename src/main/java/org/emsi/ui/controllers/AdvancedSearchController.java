package org.emsi.ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.emsi.entities.LomSchema;
import org.emsi.entities.Tag;
import org.emsi.entities.User;
import org.emsi.service.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la recherche avancée
 * 
 * Ce contrôleur démontre:
 * - Utilisation avancée des Streams avec Predicate combinés
 * - CompletableFuture pour recherche asynchrone
 * - Pagination avec Stream.skip() et limit()
 * - Tri dynamique avec Comparator
 * 
 * @author Projet LOM - EMSI
 */
public class AdvancedSearchController {

    @FXML
    private TextField searchTextField;
    @FXML
    private CheckBox exactMatchCheckBox;
    @FXML
    private ComboBox<String> languageCombo;
    @FXML
    private Slider difficultyMinSlider;
    @FXML
    private Slider difficultyMaxSlider;
    @FXML
    private ListView<String> tagsFilterList;
    @FXML
    private ComboBox<String> sortByCombo;
    @FXML
    private CheckBox descendingCheckBox;

    @FXML
    private ListView<String> resultsListView;
    @FXML
    private Label resultsCountLabel;
    @FXML
    private Label searchTimeLabel;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private Button prevPageBtn;
    @FXML
    private Button nextPageBtn;
    @FXML
    private Button viewDetailBtn;
    @FXML
    private Button addFavoriteBtn;

    private final LomService lomService = LomService.getInstance();
    private final StatisticsService statisticsService = StatisticsService.getInstance();
    private final TagService tagService = TagService.getInstance();
    private final FavoriteService favoriteService = FavoriteService.getInstance();

    private Stage dialogStage;
    private User currentUser;

    // Résultats et pagination
    private List<LomSchema> allResults = new ArrayList<>();
    private List<LomSchema> currentPageResults = new ArrayList<>();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    public void setContext(Stage stage, User user) {
        this.dialogStage = stage;
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        // Initialiser les langues disponibles
        List<String> languages = statisticsService.getAvailableLanguages();
        languageCombo.getItems().add("Toutes les langues");
        languageCombo.getItems().addAll(languages);
        languageCombo.setValue("Toutes les langues");

        // Initialiser les options de tri
        sortByCombo.getItems().addAll(
                "Date de création",
                "Titre",
                "Langue",
                "Difficulté");
        sortByCombo.setValue("Date de création");

        // Charger les tags disponibles (multi-sélection)
        loadAvailableTags();

        // Listener pour activer les boutons
        resultsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            viewDetailBtn.setDisable(!selected);
            addFavoriteBtn.setDisable(!selected);
        });

        // Recherche initiale
        handleSearch();
    }

    /**
     * STREAM: Charger les tags avec checkbox
     */
    private void loadAvailableTags() {
        List<Tag> tags = tagService.getAllTags();

        // STREAM: Transformation Tag -> String pour affichage
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.toList());

        tagsFilterList.getItems().setAll(tagNames);
        tagsFilterList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * COMPLETABLEFUTURE + STREAM: Recherche asynchrone avec filtres combinés
     * 
     * Démontre:
     * - CompletableFuture.supplyAsync() pour recherche asynchrone
     * - Platform.runLater() pour mise à jour UI depuis thread secondaire
     * - Combinaison de Predicates avec and()
     */
    @FXML
    private void handleSearch() {
        statusLabel.setText("Recherche en cours...");
        long startTime = System.currentTimeMillis();

        // Récupérer les valeurs des filtres
        String searchText = searchTextField.getText().trim();
        boolean exactMatch = exactMatchCheckBox.isSelected();
        String language = languageCombo.getValue();
        int minDifficulty = (int) difficultyMinSlider.getValue();
        int maxDifficulty = (int) difficultyMaxSlider.getValue();
        List<String> selectedTags = new ArrayList<>(tagsFilterList.getSelectionModel().getSelectedItems());
        String sortBy = sortByCombo.getValue();
        boolean descending = descendingCheckBox.isSelected();

        // COMPLETABLEFUTURE: Exécution asynchrone de la recherche
        CompletableFuture.supplyAsync(() -> {
            // Récupérer toutes les ressources
            List<LomSchema> resources = lomService.getAllResources();

            // ============================================================
            // STREAM API: Construction des Predicates de filtrage
            // ============================================================

            // Predicate 1: Recherche textuelle
            Predicate<LomSchema> textPredicate;
            if (searchText.isEmpty()) {
                textPredicate = r -> true; // Pas de filtre texte
            } else if (exactMatch) {
                // Correspondance exacte
                textPredicate = r -> {
                    if (r.getResourceTitle() == null)
                        return false;
                    return r.getResourceTitle().equalsIgnoreCase(searchText);
                };
            } else {
                // Recherche partielle (contains)
                String lowerSearch = searchText.toLowerCase();
                textPredicate = r -> {
                    // Chercher dans le titre
                    if (r.getResourceTitle() != null &&
                            r.getResourceTitle().toLowerCase().contains(lowerSearch)) {
                        return true;
                    }
                    // Chercher dans les mots-clés
                    if (r.getGeneral() != null && r.getGeneral().getKeyword() != null &&
                            r.getGeneral().getKeyword().toLowerCase().contains(lowerSearch)) {
                        return true;
                    }
                    // Chercher dans la description
                    if (r.getGeneral() != null && r.getGeneral().getDescription() != null &&
                            r.getGeneral().getDescription().toLowerCase().contains(lowerSearch)) {
                        return true;
                    }
                    return false;
                };
            }

            // Predicate 2: Filtre par langue
            Predicate<LomSchema> languagePredicate;
            if (language == null || "Toutes les langues".equals(language)) {
                languagePredicate = r -> true;
            } else {
                languagePredicate = r -> r.getGeneral() != null &&
                        language.equals(r.getGeneral().getLanguage());
            }

            // Predicate 3: Filtre par difficulté (plage)
            Predicate<LomSchema> difficultyPredicate = r -> {
                if (r.getEducational() == null || r.getEducational().getDifficulty() == null) {
                    return true; // Inclure si pas de difficulté
                }
                int diff = r.getEducational().getDifficulty();
                return diff >= minDifficulty && diff <= maxDifficulty;
            };

            // Predicate 4: Filtre par tags (OR logic)
            Predicate<LomSchema> tagsPredicate;
            if (selectedTags.isEmpty()) {
                tagsPredicate = r -> true;
            } else {
                tagsPredicate = r -> {
                    Set<String> resourceTagNames = r.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toSet());
                    // STREAM: anyMatch pour vérifier si au moins un tag correspond
                    return selectedTags.stream()
                            .anyMatch(resourceTagNames::contains);
                };
            }

            // ============================================================
            // STREAM: Combiner les predicates et appliquer le tri
            // ============================================================

            // Combiner tous les predicates avec and()
            Predicate<LomSchema> combinedPredicate = textPredicate
                    .and(languagePredicate)
                    .and(difficultyPredicate)
                    .and(tagsPredicate);

            // Créer le Comparator selon le tri sélectionné
            Comparator<LomSchema> comparator = getComparator(sortBy);
            if (descending) {
                comparator = comparator.reversed();
            }

            // STREAM: Filtrer, trier et collecter
            return resources.stream()
                    .filter(combinedPredicate)
                    .sorted(comparator)
                    .collect(Collectors.toList());

        }).thenAccept(results -> {
            long endTime = System.currentTimeMillis();

            // PLATFORM.RUNLATER: Mise à jour UI depuis thread JavaFX
            Platform.runLater(() -> {
                allResults = results;
                currentPage = 0;

                updateResultsDisplay();

                searchTimeLabel.setText(String.format("(%.2f s)", (endTime - startTime) / 1000.0));
                statusLabel.setText("Recherche terminée");
            });

        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                statusLabel.setText("Erreur: " + ex.getMessage());
            });
            return null;
        });
    }

    /**
     * STREAM: Créer un Comparator selon le critère de tri
     */
    private Comparator<LomSchema> getComparator(String sortBy) {
        return switch (sortBy) {
            case "Titre" -> Comparator.comparing(
                    LomSchema::getResourceTitle,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "Langue" -> Comparator.comparing(
                    r -> r.getGeneral() != null ? r.getGeneral().getLanguage() : null,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "Difficulté" -> Comparator.comparing(
                    r -> r.getEducational() != null ? r.getEducational().getDifficulty() : null,
                    Comparator.nullsFirst(Integer::compareTo));
            default -> Comparator.comparing(
                    LomSchema::getCreatedAt,
                    Comparator.nullsLast(Date::compareTo));
        };
    }

    /**
     * STREAM: Mettre à jour l'affichage avec pagination
     * 
     * Utilise:
     * - stream().skip() pour sauter les éléments des pages précédentes
     * - stream().limit() pour limiter à PAGE_SIZE éléments
     */
    private void updateResultsDisplay() {
        int totalResults = allResults.size();
        int totalPages = (int) Math.ceil((double) totalResults / PAGE_SIZE);

        // STREAM API: Pagination avec skip() et limit()
        currentPageResults = allResults.stream()
                .skip((long) currentPage * PAGE_SIZE) // Sauter les éléments précédents
                .limit(PAGE_SIZE) // Limiter au nombre par page
                .collect(Collectors.toList());

        // STREAM: Transformer pour affichage
        List<String> displayItems = currentPageResults.stream()
                .map(r -> {
                    String lang = r.getGeneral() != null ? r.getGeneral().getLanguage() : "-";
                    Integer diff = r.getEducational() != null ? r.getEducational().getDifficulty() : null;
                    String diffStr = diff != null ? "⭐".repeat(diff) : "-";
                    return String.format("📘 %s | 🌍 %s | %s",
                            r.getResourceTitle(), lang, diffStr);
                })
                .collect(Collectors.toList());

        resultsListView.getItems().setAll(displayItems);

        // Mise à jour des labels
        resultsCountLabel.setText(totalResults + " résultats");
        pageInfoLabel.setText(String.format("Page %d / %d", currentPage + 1, Math.max(totalPages, 1)));

        // Activation des boutons de pagination
        prevPageBtn.setDisable(currentPage == 0);
        nextPageBtn.setDisable(currentPage >= totalPages - 1);
    }

    /**
     * Handler: Page précédente
     */
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateResultsDisplay();
        }
    }

    /**
     * Handler: Page suivante
     */
    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) allResults.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateResultsDisplay();
        }
    }

    /**
     * Handler: Effacer les filtres
     */
    @FXML
    private void handleClearFilters() {
        searchTextField.clear();
        exactMatchCheckBox.setSelected(false);
        languageCombo.setValue("Toutes les langues");
        difficultyMinSlider.setValue(1);
        difficultyMaxSlider.setValue(5);
        tagsFilterList.getSelectionModel().clearSelection();
        sortByCombo.setValue("Date de création");
        descendingCheckBox.setSelected(true);

        handleSearch();
    }

    /**
     * Handler: Voir les détails
     */
    @FXML
    private void handleViewDetail() {
        int selectedIndex = resultsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentPageResults.size()) {
            LomSchema resource = currentPageResults.get(selectedIndex);
            // Ouvrir le dialogue de visualisation
            statusLabel.setText("Détails: " + resource.getResourceTitle());
        }
    }

    /**
     * Handler: Ajouter aux favoris
     */
    @FXML
    private void handleAddFavorite() {
        int selectedIndex = resultsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentPageResults.size() && currentUser != null) {
            LomSchema resource = currentPageResults.get(selectedIndex);
            favoriteService.addFavorite(currentUser, resource);
            statusLabel.setText("⭐ Ajouté aux favoris: " + resource.getResourceTitle());
        }
    }

    /**
     * Handler: Fermer
     */
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
