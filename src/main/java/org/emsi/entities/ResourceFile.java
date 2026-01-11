package org.emsi.entities;

import java.util.Date;

/**
 * Entité ResourceFile pour gérer les fichiers uploadés
 * 
 * Stocke les métadonnées des fichiers associés aux ressources LOM
 * Mapping Hibernate XML dans ResourceFile.hbm.xml
 * 
 * @author Projet LOM - EMSI
 */
public class ResourceFile {

    private Long id;
    private String fileName;
    private String originalName;
    private String storagePath;
    private String mimeType;
    private Long fileSize;
    private Date uploadedAt;
    private Integer downloadCount;

    // Relation many-to-one avec LomSchema
    private LomSchema lomSchema;

    // Constructeurs
    public ResourceFile() {
        this.uploadedAt = new Date();
        this.downloadCount = 0;
    }

    public ResourceFile(String fileName, String originalName, Long fileSize) {
        this();
        this.fileName = fileName;
        this.originalName = originalName;
        this.fileSize = fileSize;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LomSchema getLomSchema() {
        return lomSchema;
    }

    public void setLomSchema(LomSchema lomSchema) {
        this.lomSchema = lomSchema;
    }

    // Méthodes utilitaires
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * Obtenir la taille formatée du fichier
     */
    public String getFormattedSize() {
        if (fileSize == null)
            return "0 B";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Obtenir l'extension du fichier
     */
    public String getExtension() {
        if (originalName == null)
            return "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) {
            return originalName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResourceFile that = (ResourceFile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("ResourceFile[%s (%s)]", originalName, getFormattedSize());
    }
}
