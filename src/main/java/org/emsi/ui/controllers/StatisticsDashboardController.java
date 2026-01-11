package org.emsi.ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.emsi.service.StatisticsService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le Dashboard de Statistiques
 * 
 * Ce contrôleur démontre l'utilisation intensive des Streams pour:
 * - Transformer les données pour les graphiques
 * - Calculer les statistiques en temps réel
 * - Préparer les données d'affichage
 * 
 * @author Projet LOM - EMSI
 */
public class StatisticsDashboardController {

    @FXML
    private Label totalResourcesLabel;
    @FXML
    private Label totalLanguagesLabel;
    @FXML
    private Label avgDifficultyLabel;
    @FXML
    private Label todayResourcesLabel;
    @FXML
    private Label incompleteLabel;
    @FXML
    private Label keywordsLabel;

    @FXML
    private BarChart<String, Number> languageChart;
    @FXML
    private PieChart difficultyChart;
    @FXML
    private ComboBox<String> periodCombo;

    private final StatisticsService statisticsService = StatisticsService.getInstance();
    private Stage dialogStage;

    /**
     * Définir le stage parent (pour fermeture du dialogue)
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Initialisation du contrôleur FXML
     */
    @FXML
    public void initialize() {
        // Initialiser le ComboBox de période
        periodCombo.getItems().addAll("Tout", "Cette semaine", "Ce mois", "Cette année");
        periodCombo.setValue("Tout");

        // Charger les statistiques
        loadStatistics();
    }

    /**
     * STREAM API: Charger et afficher toutes les statistiques
     * 
     * Cette méthode utilise les Streams pour:
     * - Transformer les Map en données de graphique
     * - Calculer les agrégations
     * - Préparer l'affichage
     */
    private void loadStatistics() {
        // ========================================================
        // PARTIE 1: KPIs (Indicateurs Clés de Performance)
        // ========================================================

        // Total des ressources
        long totalResources = statisticsService.getMostRecentResources(Integer.MAX_VALUE).size();
        totalResourcesLabel.setText(String.valueOf(totalResources));

        // STREAM: Compter les langues uniques
        // Utilisation de getAvailableLanguages() qui utilise distinct()
        List<String> languages = statisticsService.getAvailableLanguages();
        totalLanguagesLabel.setText(String.valueOf(languages.size()));

        // Moyenne de difficulté
        double avgDifficulty = statisticsService.getAverageDifficulty();
        avgDifficultyLabel.setText(String.format("%.1f", avgDifficulty));

        // Ressources créées aujourd'hui
        long todayCount = statisticsService.countResourcesCreatedToday();
        todayResourcesLabel.setText(String.valueOf(todayCount));

        // ========================================================
        // PARTIE 2: Graphique à barres - Distribution par langue
        // ========================================================

        Map<String, Long> languageStats = statisticsService.countResourcesByLanguage();

        // STREAM API: Transformation Map -> XYChart.Series
        // Utilisation de entrySet().stream() pour itérer sur les entrées
        XYChart.Series<String, Number> languageSeries = new XYChart.Series<>();
        languageSeries.setName("Ressources par langue");

        languageStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Tri décroissant
                .limit(10) // Top 10 langues
                .forEach(entry -> {
                    // STREAM: Ajout des données au graphique
                    languageSeries.getData().add(
                            new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        languageChart.getData().clear();
        languageChart.getData().add(languageSeries);

        // ========================================================
        // PARTIE 3: Graphique circulaire - Distribution par difficulté
        // ========================================================

        Map<Integer, Long> difficultyStats = statisticsService.countResourcesByDifficulty();

        // STREAM API: Transformation Map<Integer, Long> -> List<PieChart.Data>
        // Utilisation de map() pour transformer chaque entrée en PieChart.Data
        var pieData = difficultyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Tri par niveau de difficulté
                .map(entry -> {
                    // Transformation en label lisible
                    String label = getDifficultyLabel(entry.getKey()) + " (" + entry.getValue() + ")";
                    return new PieChart.Data(label, entry.getValue());
                })
                .collect(Collectors.toList()); // Collecter en List

        difficultyChart.setData(FXCollections.observableArrayList(pieData));

        // ========================================================
        // PARTIE 4: Statistiques additionnelles
        // ========================================================

        // Ressources incomplètes - utilise filter() avec Predicate
        var incomplete = statisticsService.findIncompleteResources();
        incompleteLabel.setText(incomplete.size() + " ressources nécessitent des métadonnées");

        // STREAM API: Top 5 mots-clés
        // Utilisation de getAllUniqueKeywords() qui utilise flatMap() et distinct()
        Set<String> keywords = statisticsService.getAllUniqueKeywords();
        String topKeywords = keywords.stream()
                .limit(5) // Limiter à 5 mots-clés
                .collect(Collectors.joining(", ")); // Joindre avec virgule

        keywordsLabel.setText(topKeywords.isEmpty() ? "Aucun mot-clé" : topKeywords);
    }

    /**
     * Convertir le niveau de difficulté en label lisible
     */
    private String getDifficultyLabel(Integer level) {
        if (level == null)
            return "Non défini";

        // COLLECTIONS: Utilisation de switch expression (Java 14+)
        return switch (level) {
            case 1 -> "Très facile";
            case 2 -> "Facile";
            case 3 -> "Moyen";
            case 4 -> "Difficile";
            case 5 -> "Très difficile";
            default -> "Niveau " + level;
        };
    }

    /**
     * Handler: Rafraîchir les statistiques
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
        System.out.println("✅ Statistiques actualisées");
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
