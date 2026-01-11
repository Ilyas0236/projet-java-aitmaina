package org.emsi.exceptions;

/**
 * Exception pour les erreurs liées aux fichiers
 * 
 * Exemples d'utilisation:
 * - Fichier non trouvé
 * - Échec d'upload/download
 * - Extension non autorisée
 * - Fichier trop volumineux
 * 
 * @author Projet LOM - EMSI
 */
public class FileException extends LomException {

    private static final long serialVersionUID = 1L;

    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private Long maxSize;

    public FileException(String message) {
        super(message, ErrorCode.FILE_STORAGE_ERROR);
    }

    public FileException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public FileException(String message, ErrorCode errorCode, String fileName) {
        super(message, errorCode, "Fichier: " + fileName);
        this.fileName = fileName;
    }

    public FileException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // =====================================================================
    // MÉTHODES FACTORY - Création d'exceptions spécifiques
    // =====================================================================

    /**
     * Fichier non trouvé
     */
    public static FileException notFound(String fileName) {
        FileException ex = new FileException(
                "Le fichier '" + fileName + "' n'existe pas",
                ErrorCode.FILE_NOT_FOUND,
                fileName);
        return ex;
    }

    /**
     * Extension non autorisée
     */
    public static FileException invalidExtension(String fileName, String extension) {
        FileException ex = new FileException(
                "L'extension '" + extension + "' n'est pas autorisée pour le fichier '" + fileName + "'",
                ErrorCode.FILE_INVALID_EXTENSION);
        ex.fileName = fileName;
        ex.fileExtension = extension;
        return ex;
    }

    /**
     * Fichier trop volumineux
     */
    public static FileException tooLarge(String fileName, long fileSize, long maxSize) {
        FileException ex = new FileException(
                "Le fichier '" + fileName + "' est trop volumineux. " +
                        "Taille: " + formatSize(fileSize) + ", Maximum: " + formatSize(maxSize),
                ErrorCode.FILE_TOO_LARGE);
        ex.fileName = fileName;
        ex.fileSize = fileSize;
        ex.maxSize = maxSize;
        return ex;
    }

    /**
     * Échec d'upload
     */
    public static FileException uploadFailed(String fileName, Throwable cause) {
        FileException ex = new FileException(
                "Échec de l'upload du fichier '" + fileName + "'",
                ErrorCode.FILE_UPLOAD_FAILED,
                cause);
        ex.fileName = fileName;
        return ex;
    }

    /**
     * Échec de téléchargement
     */
    public static FileException downloadFailed(String fileName, Throwable cause) {
        FileException ex = new FileException(
                "Échec du téléchargement du fichier '" + fileName + "'",
                ErrorCode.FILE_DOWNLOAD_FAILED,
                cause);
        ex.fileName = fileName;
        return ex;
    }

    /**
     * Erreur de stockage générale
     */
    public static FileException storageError(String message, Throwable cause) {
        return new FileException(
                "Erreur de stockage: " + message,
                ErrorCode.FILE_STORAGE_ERROR,
                cause);
    }

    // Helper pour formater les tailles
    private static String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Long getMaxSize() {
        return maxSize;
    }
}
