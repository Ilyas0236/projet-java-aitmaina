package org.emsi.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service de gestion des fichiers pour l'upload et le téléchargement des
 * ressources
 * 
 * Ce service démontre l'utilisation avancée de :
 * - Java NIO (Files, Path) pour la manipulation de fichiers
 * - Stream API pour le listing et le filtrage de fichiers
 * - InputStream/OutputStream pour les transferts
 * 
 * @author Projet LOM - EMSI
 */
public class FileStorageService {

    private static FileStorageService instance;

    // Répertoire de stockage des fichiers uploadés
    private final Path storageDirectory;

    // Types MIME autorisés pour l'upload
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "txt", "html", "htm", "xml", "json",
            "zip", "rar", "7z",
            "mp4", "avi", "mkv", "mp3", "wav",
            "jpg", "jpeg", "png", "gif", "svg");

    private FileStorageService() {
        // Créer le répertoire de stockage dans le dossier utilisateur
        String userHome = System.getProperty("user.home");
        this.storageDirectory = Paths.get(userHome, ".lom-resources");

        try {
            // JAVA NIO: Création du répertoire s'il n'existe pas
            Files.createDirectories(storageDirectory);
            System.out.println("📁 Répertoire de stockage: " + storageDirectory);
        } catch (IOException e) {
            System.err.println("❌ Erreur création répertoire: " + e.getMessage());
        }
    }

    /**
     * Obtenir l'instance unique (Singleton Pattern)
     */
    public static FileStorageService getInstance() {
        if (instance == null) {
            instance = new FileStorageService();
        }
        return instance;
    }

    // =====================================================================
    // MÉTHODES D'UPLOAD - UTILISATION DES STREAMS I/O
    // =====================================================================

    /**
     * STREAM I/O: Uploader un fichier dans le stockage
     * 
     * Utilise:
     * - Files.copy() pour copier le fichier de manière efficace
     * - Path API pour la manipulation des chemins
     * - UUID pour générer des noms uniques
     * 
     * @param sourceFile fichier source à uploader
     * @param resourceId ID de la ressource LOM associée
     * @return String chemin relatif du fichier uploadé
     * @throws IOException en cas d'erreur d'E/S
     */
    public String uploadFile(File sourceFile, Long resourceId) throws IOException {
        // Validation de l'extension
        String extension = getFileExtension(sourceFile.getName());
        if (!isAllowedExtension(extension)) {
            throw new IOException("Extension de fichier non autorisée: " + extension);
        }

        // Générer un nom de fichier unique avec UUID
        String uniqueFileName = String.format("%d_%s_%s",
                resourceId,
                UUID.randomUUID().toString().substring(0, 8),
                sourceFile.getName());

        // JAVA NIO: Chemin de destination
        Path destinationPath = storageDirectory.resolve(uniqueFileName);

        // STREAM I/O: Copie du fichier avec Files.copy()
        // StandardCopyOption.REPLACE_EXISTING écrase le fichier s'il existe
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✅ Fichier uploadé: " + uniqueFileName);
        return uniqueFileName;
    }

    /**
     * STREAM I/O: Uploader depuis un InputStream
     * 
     * Utilise:
     * - InputStream pour lire les données
     * - Files.copy() version avec InputStream
     * 
     * @param inputStream      flux de données source
     * @param originalFileName nom du fichier original
     * @param resourceId       ID de la ressource associée
     * @return String nom du fichier uploadé
     * @throws IOException en cas d'erreur
     */
    public String uploadFromStream(InputStream inputStream, String originalFileName, Long resourceId)
            throws IOException {

        String extension = getFileExtension(originalFileName);
        if (!isAllowedExtension(extension)) {
            throw new IOException("Extension non autorisée: " + extension);
        }

        String uniqueFileName = String.format("%d_%s_%s",
                resourceId,
                UUID.randomUUID().toString().substring(0, 8),
                originalFileName);

        Path destinationPath = storageDirectory.resolve(uniqueFileName);

        // STREAM I/O: Copie depuis InputStream
        // La méthode gère automatiquement la fermeture du flux destination
        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✅ Fichier uploadé depuis stream: " + uniqueFileName);
        return uniqueFileName;
    }

    // =====================================================================
    // MÉTHODES DE TÉLÉCHARGEMENT - UTILISATION DES STREAMS
    // =====================================================================

    /**
     * STREAM I/O: Télécharger un fichier vers un répertoire de destination
     * 
     * Utilise:
     * - Files.copy() pour copier vers la destination
     * - Vérification d'existence avec Files.exists()
     * 
     * @param fileName       nom du fichier à télécharger
     * @param destinationDir répertoire de destination
     * @return Path chemin du fichier téléchargé
     * @throws IOException en cas d'erreur
     */
    public Path downloadFile(String fileName, Path destinationDir) throws IOException {
        Path sourcePath = storageDirectory.resolve(fileName);

        // Vérifier que le fichier existe
        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException("Fichier non trouvé: " + fileName);
        }

        // Créer le répertoire de destination si nécessaire
        Files.createDirectories(destinationDir);

        // Extraire le nom original (après les préfixes ID et UUID)
        String originalName = extractOriginalFileName(fileName);
        Path destinationPath = destinationDir.resolve(originalName);

        // STREAM I/O: Copie du fichier
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("📥 Fichier téléchargé: " + destinationPath);
        return destinationPath;
    }

    /**
     * STREAM I/O: Obtenir un InputStream pour le téléchargement
     * 
     * @param fileName nom du fichier
     * @return InputStream flux de lecture du fichier
     * @throws IOException en cas d'erreur
     */
    public InputStream getFileInputStream(String fileName) throws IOException {
        Path filePath = storageDirectory.resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Fichier non trouvé: " + fileName);
        }

        // STREAM I/O: Ouverture d'un flux de lecture
        return Files.newInputStream(filePath);
    }

    /**
     * STREAM I/O: Lire le contenu d'un fichier texte
     * 
     * Utilise:
     * - Files.readAllBytes() pour lire tout le contenu
     * - new String() avec charset pour la conversion
     * 
     * @param fileName nom du fichier
     * @return String contenu du fichier
     * @throws IOException en cas d'erreur
     */
    public String readFileContent(String fileName) throws IOException {
        Path filePath = storageDirectory.resolve(fileName);

        // STREAM I/O: Lecture complète du fichier en bytes puis conversion
        byte[] bytes = Files.readAllBytes(filePath);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    // =====================================================================
    // MÉTHODES DE LISTING - UTILISATION DES STREAMS JAVA
    // =====================================================================

    /**
     * STREAM API + NIO: Lister tous les fichiers du stockage
     * 
     * Utilise:
     * - Files.list() qui retourne un Stream<Path>
     * - filter() pour exclure les répertoires
     * - map() pour extraire les noms de fichiers
     * - collect() pour convertir en List
     * 
     * @return List<String> noms de tous les fichiers
     */
    public List<String> listAllFiles() {
        // STREAM API + NIO: Files.list() retourne un Stream
        // Le try-with-resources garantit la fermeture du stream
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile) // Exclure les répertoires
                    .map(path -> path.getFileName().toString()) // Extraire le nom
                    .sorted() // Trier alphabétiquement
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("❌ Erreur listing: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * STREAM API: Lister les fichiers d'une ressource spécifique
     * 
     * Utilise:
     * - filter() avec startsWith pour filtrer par préfixe
     * 
     * @param resourceId ID de la ressource
     * @return List<String> fichiers associés à cette ressource
     */
    public List<String> listFilesByResource(Long resourceId) {
        String prefix = resourceId + "_";

        // STREAM API: Filtrage par préfixe
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(prefix)) // Filtrer par ID ressource
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * STREAM API: Lister les fichiers par extension
     * 
     * Utilise:
     * - filter() avec endsWith pour filtrer par extension
     * 
     * @param extension extension recherchée (sans le point)
     * @return List<String> fichiers avec cette extension
     */
    public List<String> listFilesByExtension(String extension) {
        String suffix = "." + extension.toLowerCase();

        // STREAM API: Filtrage par extension
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(suffix))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * STREAM API: Rechercher des fichiers récursivement
     * 
     * Utilise:
     * - Files.walk() pour parcourir récursivement
     * - Profondeur configurée pour limiter la recherche
     * 
     * @param pattern motif de recherche (contenu dans le nom)
     * @return List<String> fichiers correspondants
     */
    public List<String> searchFiles(String pattern) {
        String lowerPattern = pattern.toLowerCase();

        // STREAM API + NIO: Files.walk() parcourt récursivement
        try (Stream<Path> pathStream = Files.walk(storageDirectory, 2)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.toLowerCase().contains(lowerPattern))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    // =====================================================================
    // MÉTHODES DE STATISTIQUES FICHIERS - STREAMS
    // =====================================================================

    /**
     * STREAM API: Calculer la taille totale du stockage
     * 
     * Utilise:
     * - mapToLong() pour convertir en LongStream
     * - sum() pour additionner les tailles
     * 
     * @return long taille totale en bytes
     */
    public long getTotalStorageSize() {
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            // STREAM API: mapToLong() et sum()
            return pathStream
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path); // Taille du fichier
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum(); // Somme de toutes les tailles
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * STREAM API: Compter les fichiers par extension
     * 
     * Utilise:
     * - Collectors.groupingBy() avec extraction d'extension
     * - Collectors.counting() pour compter
     * 
     * @return Map<String, Long> clé=extension, valeur=nombre de fichiers
     */
    public Map<String, Long> countFilesByExtension() {
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            // STREAM API: Groupement et comptage par extension
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.groupingBy(
                            this::getFileExtension, // Fonction d'extraction extension
                            Collectors.counting()));
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * STREAM API: Obtenir les N plus gros fichiers
     * 
     * @param limit nombre de fichiers à retourner
     * @return Map<String, Long> clé=nom du fichier, valeur=taille
     */
    public Map<String, Long> getLargestFiles(int limit) {
        try (Stream<Path> pathStream = Files.list(storageDirectory)) {
            // STREAM API: Tri par taille décroissante
            return pathStream
                    .filter(Files::isRegularFile)
                    .sorted((p1, p2) -> {
                        try {
                            return Long.compare(Files.size(p2), Files.size(p1)); // Ordre décroissant
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .limit(limit)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString(),
                            path -> {
                                try {
                                    return Files.size(path);
                                } catch (IOException e) {
                                    return 0L;
                                }
                            },
                            (a, b) -> a, // En cas de collision
                            LinkedHashMap::new // Préserver l'ordre
                    ));
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    // =====================================================================
    // MÉTHODES DE SUPPRESSION
    // =====================================================================

    /**
     * Supprimer un fichier du stockage
     * 
     * @param fileName nom du fichier à supprimer
     * @return boolean true si supprimé avec succès
     */
    public boolean deleteFile(String fileName) {
        Path filePath = storageDirectory.resolve(fileName);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("🗑️ Fichier supprimé: " + fileName);
            }
            return deleted;
        } catch (IOException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * STREAM API: Supprimer tous les fichiers d'une ressource
     * 
     * @param resourceId ID de la ressource
     * @return int nombre de fichiers supprimés
     */
    public int deleteFilesByResource(Long resourceId) {
        List<String> files = listFilesByResource(resourceId);

        // STREAM API: Comptage des suppressions réussies
        return (int) files.stream()
                .filter(this::deleteFile) // Référence de méthode
                .count();
    }

    // =====================================================================
    // MÉTHODES UTILITAIRES
    // =====================================================================

    /**
     * Extraire l'extension d'un fichier
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * COLLECTIONS: Vérifier si une extension est autorisée
     * 
     * Utilise:
     * - Set.contains() pour une recherche O(1)
     */
    private boolean isAllowedExtension(String extension) {
        // COLLECTIONS: Recherche dans un Set (O(1))
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Extraire le nom original du fichier (sans préfixes)
     */
    private String extractOriginalFileName(String storedFileName) {
        // Format: resourceId_uuid_originalName
        int secondUnderscore = storedFileName.indexOf('_', storedFileName.indexOf('_') + 1);
        if (secondUnderscore > 0) {
            return storedFileName.substring(secondUnderscore + 1);
        }
        return storedFileName;
    }

    /**
     * Obtenir le chemin du répertoire de stockage
     */
    public Path getStorageDirectory() {
        return storageDirectory;
    }

    /**
     * Obtenir les informations d'un fichier
     * 
     * @param fileName nom du fichier
     * @return Map<String, Object> informations du fichier
     */
    public Map<String, Object> getFileInfo(String fileName) {
        Path filePath = storageDirectory.resolve(fileName);
        Map<String, Object> info = new HashMap<>();

        try {
            info.put("name", fileName);
            info.put("originalName", extractOriginalFileName(fileName));
            info.put("size", Files.size(filePath));
            info.put("extension", getFileExtension(fileName));
            info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            info.put("exists", true);
        } catch (IOException e) {
            info.put("exists", false);
            info.put("error", e.getMessage());
        }

        return info;
    }
}
