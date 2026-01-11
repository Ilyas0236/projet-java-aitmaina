package org.emsi.exceptions;

/**
 * Exception pour les erreurs liées aux ressources LOM
 * 
 * Exemples d'utilisation:
 * - Ressource non trouvée lors d'une recherche par ID
 * - Échec de création/mise à jour/suppression
 * - Violation de contraintes métier
 * 
 * @author Projet LOM - EMSI
 */
public class ResourceException extends LomException {

    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private String resourceTitle;

    public ResourceException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public ResourceException(String message, Long resourceId) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, "Resource ID: " + resourceId);
        this.resourceId = resourceId;
    }

    public ResourceException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // =====================================================================
    // MÉTHODES FACTORY - Création d'exceptions spécifiques
    // =====================================================================

    /**
     * Créer une exception "Ressource non trouvée"
     */
    public static ResourceException notFound(Long id) {
        ResourceException ex = new ResourceException(
                "Ressource avec l'ID " + id + " non trouvée",
                ErrorCode.RESOURCE_NOT_FOUND);
        ex.resourceId = id;
        return ex;
    }

    /**
     * Créer une exception "Ressource déjà existante"
     */
    public static ResourceException alreadyExists(String title) {
        ResourceException ex = new ResourceException(
                "Une ressource avec le titre '" + title + "' existe déjà",
                ErrorCode.RESOURCE_ALREADY_EXISTS);
        ex.resourceTitle = title;
        return ex;
    }

    /**
     * Créer une exception "Échec de création"
     */
    public static ResourceException creationFailed(String title, Throwable cause) {
        ResourceException ex = new ResourceException(
                "Échec de création de la ressource '" + title + "'",
                ErrorCode.RESOURCE_CREATION_FAILED,
                cause);
        ex.resourceTitle = title;
        return ex;
    }

    /**
     * Créer une exception "Échec de mise à jour"
     */
    public static ResourceException updateFailed(Long id, Throwable cause) {
        ResourceException ex = new ResourceException(
                "Échec de mise à jour de la ressource ID " + id,
                ErrorCode.RESOURCE_UPDATE_FAILED,
                cause);
        ex.resourceId = id;
        return ex;
    }

    /**
     * Créer une exception "Échec de suppression"
     */
    public static ResourceException deleteFailed(Long id, Throwable cause) {
        ResourceException ex = new ResourceException(
                "Échec de suppression de la ressource ID " + id,
                ErrorCode.RESOURCE_DELETE_FAILED,
                cause);
        ex.resourceId = id;
        return ex;
    }

    // Getters
    public Long getResourceId() {
        return resourceId;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }
}
