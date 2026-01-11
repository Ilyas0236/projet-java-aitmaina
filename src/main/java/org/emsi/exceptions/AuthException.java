package org.emsi.exceptions;

/**
 * Exception pour les erreurs d'authentification et d'autorisation
 * 
 * Exemples d'utilisation:
 * - Utilisateur non trouvé
 * - Mot de passe incorrect
 * - Session expirée
 * - Accès non autorisé
 * 
 * @author Projet LOM - EMSI
 */
public class AuthException extends LomException {

    private static final long serialVersionUID = 1L;

    private String username;
    private String requiredRole;

    public AuthException(String message) {
        super(message, ErrorCode.AUTH_UNAUTHORIZED);
    }

    public AuthException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public AuthException(String message, ErrorCode errorCode, String username) {
        super(message, errorCode, "Utilisateur: " + username);
        this.username = username;
    }

    // =====================================================================
    // MÉTHODES FACTORY - Création d'exceptions spécifiques
    // =====================================================================

    /**
     * Utilisateur non trouvé
     */
    public static AuthException userNotFound(String username) {
        AuthException ex = new AuthException(
                "Aucun utilisateur avec le nom '" + username + "'",
                ErrorCode.AUTH_USER_NOT_FOUND,
                username);
        return ex;
    }

    /**
     * Mot de passe invalide
     */
    public static AuthException invalidPassword(String username) {
        AuthException ex = new AuthException(
                "Mot de passe incorrect pour '" + username + "'",
                ErrorCode.AUTH_INVALID_PASSWORD,
                username);
        return ex;
    }

    /**
     * Utilisateur déjà existant (inscription)
     */
    public static AuthException userAlreadyExists(String username) {
        AuthException ex = new AuthException(
                "Le nom d'utilisateur '" + username + "' est déjà pris",
                ErrorCode.AUTH_USER_ALREADY_EXISTS,
                username);
        return ex;
    }

    /**
     * Accès non autorisé (rôle insuffisant)
     */
    public static AuthException unauthorized(String username, String requiredRole) {
        AuthException ex = new AuthException(
                "Accès refusé. Rôle requis: " + requiredRole,
                ErrorCode.AUTH_UNAUTHORIZED);
        ex.username = username;
        ex.requiredRole = requiredRole;
        return ex;
    }

    /**
     * Session expirée
     */
    public static AuthException sessionExpired() {
        return new AuthException(
                "Votre session a expiré. Veuillez vous reconnecter.",
                ErrorCode.AUTH_SESSION_EXPIRED);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getRequiredRole() {
        return requiredRole;
    }
}
