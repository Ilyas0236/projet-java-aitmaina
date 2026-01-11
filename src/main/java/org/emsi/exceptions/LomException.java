package org.emsi.exceptions;

/**
 * Exception de base pour toutes les exceptions métier du projet LOM
 * 
 * Cette classe démontre:
 * - Héritage d'exception (RuntimeException vs Exception)
 * - Codes d'erreur personnalisés
 * - Messages contextualisés
 * 
 * @author Projet LOM - EMSI
 */
public class LomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final String details;

    /**
     * Constructeur avec message uniquement
     */
    public LomException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
        this.details = null;
    }

    /**
     * Constructeur avec message et code d'erreur
     */
    public LomException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Constructeur avec message, code d'erreur et détails
     */
    public LomException(String message, ErrorCode errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Constructeur avec cause (pour chaîner les exceptions)
     */
    public LomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
        this.details = cause.getMessage();
    }

    /**
     * Constructeur complet avec cause
     */
    public LomException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = cause.getMessage();
    }

    // Getters
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Obtenir le code numérique de l'erreur
     */
    public int getNumericCode() {
        return errorCode.getCode();
    }

    /**
     * Message formaté pour l'affichage utilisateur
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode.name()).append("] ");
        sb.append(getMessage());
        if (details != null && !details.isEmpty()) {
            sb.append(" - Détails: ").append(details);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    /**
     * Énumération des codes d'erreur
     * 
     * Organisation par catégorie:
     * - 1xx: Erreurs d'authentification
     * - 2xx: Erreurs de ressources
     * - 3xx: Erreurs de fichiers
     * - 4xx: Erreurs de validation
     * - 5xx: Erreurs système
     */
    public enum ErrorCode {
        // Erreurs générales (0xx)
        GENERAL_ERROR(0, "Erreur générale"),
        UNKNOWN_ERROR(1, "Erreur inconnue"),

        // Erreurs d'authentification (1xx)
        AUTH_USER_NOT_FOUND(100, "Utilisateur non trouvé"),
        AUTH_INVALID_PASSWORD(101, "Mot de passe incorrect"),
        AUTH_ACCOUNT_DISABLED(102, "Compte désactivé"),
        AUTH_SESSION_EXPIRED(103, "Session expirée"),
        AUTH_UNAUTHORIZED(104, "Non autorisé"),
        AUTH_USER_ALREADY_EXISTS(105, "Utilisateur déjà existant"),

        // Erreurs de ressources (2xx)
        RESOURCE_NOT_FOUND(200, "Ressource non trouvée"),
        RESOURCE_ALREADY_EXISTS(201, "Ressource déjà existante"),
        RESOURCE_UPDATE_FAILED(202, "Échec de mise à jour"),
        RESOURCE_DELETE_FAILED(203, "Échec de suppression"),
        RESOURCE_CREATION_FAILED(204, "Échec de création"),
        RESOURCE_INVALID_ID(205, "ID de ressource invalide"),

        // Erreurs de fichiers (3xx)
        FILE_NOT_FOUND(300, "Fichier non trouvé"),
        FILE_UPLOAD_FAILED(301, "Échec d'upload"),
        FILE_DOWNLOAD_FAILED(302, "Échec de téléchargement"),
        FILE_INVALID_EXTENSION(303, "Extension de fichier non autorisée"),
        FILE_TOO_LARGE(304, "Fichier trop volumineux"),
        FILE_STORAGE_ERROR(305, "Erreur de stockage"),

        // Erreurs de validation (4xx)
        VALIDATION_REQUIRED_FIELD(400, "Champ obligatoire manquant"),
        VALIDATION_INVALID_FORMAT(401, "Format invalide"),
        VALIDATION_OUT_OF_RANGE(402, "Valeur hors limites"),
        VALIDATION_DUPLICATE_VALUE(403, "Valeur déjà existante"),
        VALIDATION_CONSTRAINT_VIOLATION(404, "Violation de contrainte"),

        // Erreurs système (5xx)
        DATABASE_ERROR(500, "Erreur de base de données"),
        DATABASE_CONNECTION_FAILED(501, "Connexion échouée"),
        SYSTEM_ERROR(502, "Erreur système"),
        CONFIGURATION_ERROR(503, "Erreur de configuration"),
        THREAD_INTERRUPTED(504, "Thread interrompu"),
        TIMEOUT_ERROR(505, "Délai d'attente dépassé");

        private final int code;
        private final String description;

        ErrorCode(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
