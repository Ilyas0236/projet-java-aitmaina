package org.emsi.exceptions;

/**
 * Exception pour les erreurs de validation des données
 * 
 * Exemples d'utilisation:
 * - Champ obligatoire manquant
 * - Format de données invalide
 * - Valeur hors des limites autorisées
 * - Violation de contraintes
 * 
 * @author Projet LOM - EMSI
 */
public class ValidationException extends LomException {

    private static final long serialVersionUID = 1L;

    private String fieldName;
    private Object invalidValue;
    private String constraint;

    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_CONSTRAINT_VIOLATION);
    }

    public ValidationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public ValidationException(String message, String fieldName, Object invalidValue) {
        super(message, ErrorCode.VALIDATION_CONSTRAINT_VIOLATION,
                "Champ: " + fieldName + ", Valeur: " + invalidValue);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    // =====================================================================
    // MÉTHODES FACTORY - Création d'exceptions spécifiques
    // =====================================================================

    /**
     * Champ obligatoire manquant
     */
    public static ValidationException requiredField(String fieldName) {
        ValidationException ex = new ValidationException(
                "Le champ '" + fieldName + "' est obligatoire",
                ErrorCode.VALIDATION_REQUIRED_FIELD);
        ex.fieldName = fieldName;
        return ex;
    }

    /**
     * Format invalide (email, date, etc.)
     */
    public static ValidationException invalidFormat(String fieldName, String expectedFormat, Object value) {
        ValidationException ex = new ValidationException(
                "Format invalide pour '" + fieldName + "'. " +
                        "Format attendu: " + expectedFormat + ", reçu: " + value,
                ErrorCode.VALIDATION_INVALID_FORMAT);
        ex.fieldName = fieldName;
        ex.invalidValue = value;
        ex.constraint = expectedFormat;
        return ex;
    }

    /**
     * Valeur hors des limites
     */
    public static ValidationException outOfRange(String fieldName, Object value, Object min, Object max) {
        ValidationException ex = new ValidationException(
                "La valeur de '" + fieldName + "' doit être entre " + min + " et " + max +
                        ". Valeur reçue: " + value,
                ErrorCode.VALIDATION_OUT_OF_RANGE);
        ex.fieldName = fieldName;
        ex.invalidValue = value;
        ex.constraint = "Min: " + min + ", Max: " + max;
        return ex;
    }

    /**
     * Valeur déjà existante (unicité)
     */
    public static ValidationException duplicate(String fieldName, Object value) {
        ValidationException ex = new ValidationException(
                "La valeur '" + value + "' existe déjà pour le champ '" + fieldName + "'",
                ErrorCode.VALIDATION_DUPLICATE_VALUE);
        ex.fieldName = fieldName;
        ex.invalidValue = value;
        return ex;
    }

    /**
     * Longueur de chaîne invalide
     */
    public static ValidationException invalidLength(String fieldName, int actualLength, int minLength, int maxLength) {
        ValidationException ex = new ValidationException(
                "La longueur de '" + fieldName + "' doit être entre " + minLength + " et " + maxLength +
                        " caractères. Longueur actuelle: " + actualLength,
                ErrorCode.VALIDATION_OUT_OF_RANGE);
        ex.fieldName = fieldName;
        ex.invalidValue = actualLength;
        ex.constraint = "Min: " + minLength + ", Max: " + maxLength;
        return ex;
    }

    // Getters
    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public String getConstraint() {
        return constraint;
    }
}
