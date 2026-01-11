package org.emsi.exceptions;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire centralisé des exceptions
 * 
 * Ce service démontre:
 * - Gestion centralisée des exceptions
 * - Logging des erreurs
 * - Affichage utilisateur adapté au type d'erreur
 * - Pattern Singleton
 * 
 * @author Projet LOM - EMSI
 */
public class ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());
    private static ExceptionHandler instance;

    private ExceptionHandler() {
        // Configuration du logger
        LOGGER.setLevel(Level.ALL);
    }

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            synchronized (ExceptionHandler.class) {
                if (instance == null) {
                    instance = new ExceptionHandler();
                }
            }
        }
        return instance;
    }

    /**
     * Gérer une exception de type LomException
     * 
     * @param exception l'exception à gérer
     */
    public void handle(LomException exception) {
        // 1. Logger l'exception
        logException(exception);

        // 2. Afficher à l'utilisateur
        showUserAlert(exception);
    }

    /**
     * Gérer une exception générale
     * 
     * @param exception l'exception à gérer
     * @param context   le contexte (où s'est produite l'erreur)
     */
    public void handle(Exception exception, String context) {
        // Convertir en LomException si nécessaire
        if (exception instanceof LomException) {
            handle((LomException) exception);
        } else {
            LomException lomEx = new LomException(
                    context + ": " + exception.getMessage(),
                    exception);
            handle(lomEx);
        }
    }

    /**
     * Logger l'exception avec niveau approprié
     */
    private void logException(LomException exception) {
        String logMessage = String.format(
                "[%s] Code: %d - %s",
                exception.getErrorCode().name(),
                exception.getNumericCode(),
                exception.getFormattedMessage());

        // Déterminer le niveau de log selon le code d'erreur
        if (exception.getNumericCode() >= 500) {
            // Erreurs système - SEVERE
            LOGGER.log(Level.SEVERE, logMessage, exception);
        } else if (exception.getNumericCode() >= 400) {
            // Erreurs de validation - WARNING
            LOGGER.log(Level.WARNING, logMessage);
        } else if (exception.getNumericCode() >= 300) {
            // Erreurs de fichiers - WARNING
            LOGGER.log(Level.WARNING, logMessage, exception);
        } else {
            // Autres erreurs - INFO
            LOGGER.log(Level.INFO, logMessage);
        }
    }

    /**
     * Afficher une alerte utilisateur adaptée
     */
    private void showUserAlert(LomException exception) {
        // S'assurer d'être sur le thread JavaFX
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showUserAlert(exception));
            return;
        }

        Alert alert = createAlert(exception);

        // Ajouter les détails techniques pour les erreurs système
        if (exception.getNumericCode() >= 500) {
            addExceptionDetails(alert, exception);
        }

        alert.showAndWait();
    }

    /**
     * Créer une alerte selon le type d'exception
     */
    private Alert createAlert(LomException exception) {
        Alert.AlertType alertType = determineAlertType(exception);
        Alert alert = new Alert(alertType);

        alert.setTitle(getTitle(exception));
        alert.setHeaderText(getHeader(exception));
        alert.setContentText(exception.getMessage());

        return alert;
    }

    /**
     * Déterminer le type d'alerte selon le code d'erreur
     */
    private Alert.AlertType determineAlertType(LomException exception) {
        int code = exception.getNumericCode();

        if (code >= 500) {
            return Alert.AlertType.ERROR;
        } else if (code >= 400) {
            return Alert.AlertType.WARNING;
        } else if (code >= 100 && code < 200) {
            return Alert.AlertType.WARNING;
        } else {
            return Alert.AlertType.INFORMATION;
        }
    }

    /**
     * Obtenir le titre selon le type d'erreur
     */
    private String getTitle(LomException exception) {
        int code = exception.getNumericCode();

        if (code >= 500)
            return "Erreur Système";
        if (code >= 400)
            return "Erreur de Validation";
        if (code >= 300)
            return "Erreur de Fichier";
        if (code >= 200)
            return "Erreur de Ressource";
        if (code >= 100)
            return "Erreur d'Authentification";
        return "Erreur";
    }

    /**
     * Obtenir le header selon le type d'erreur
     */
    private String getHeader(LomException exception) {
        return "⚠️ " + exception.getErrorCode().getDescription();
    }

    /**
     * Ajouter les détails techniques (stack trace) pour les erreurs critiques
     */
    private void addExceptionDetails(Alert alert, LomException exception) {
        // Créer le contenu expandable
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
    }

    /**
     * Afficher une confirmation avant action critique
     * 
     * @param title   titre de la confirmation
     * @param message message de confirmation
     * @return true si l'utilisateur confirme
     */
    public boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Afficher un message de succès
     */
    public void showSuccess(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showSuccess(title, message));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher un avertissement
     */
    public void showWarning(String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showWarning(title, message));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("⚠️ Attention");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
