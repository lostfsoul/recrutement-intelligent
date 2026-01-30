package ma.recrutement.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;

/**
 * Utilitaires pour le stockage et la gestion des fichiers uploadés (CVs, logos, etc.).
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class FileStorageUtil {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.cv.max-size:10485760}") // 10 MB par défaut
    private long maxCvSize;

    @Value("${file.logo.max-size:2097152}") // 2 MB par défaut
    private long maxLogoSize;

    private static final List<String> CV_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final List<String> CV_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx"
    );

    private static final List<String> IMAGE_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif"
    );

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif"
    );

    /**
     * Initialise les répertoires de stockage.
     */
    public void init() {
        try {
            Path cvPath = Paths.get(uploadDir, "cv");
            Path logoPath = Paths.get(uploadDir, "logos");
            Path tempPath = Paths.get(uploadDir, "temp");

            Files.createDirectories(cvPath);
            Files.createDirectories(logoPath);
            Files.createDirectories(tempPath);

            log.info("Répertoires de stockage initialisés: {}", uploadDir);
        } catch (IOException e) {
            log.error("Erreur lors de l'initialisation des répertoires de stockage", e);
            throw new RuntimeException("Impossible de créer les répertoires de stockage", e);
        }
    }

    /**
     * Stocke un fichier CV.
     *
     * @param file le fichier CV
     * @param userId l'ID de l'utilisateur
     * @return le chemin relatif du fichier stocké
     * @throws IOException en cas d'erreur de stockage
     */
    public String storeCv(MultipartFile file, Long userId) throws IOException {
        validateCvFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename(), userId);
        Path targetPath = Paths.get(uploadDir, "cv", fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("CV stocké pour l'utilisateur {}: {}", userId, fileName);
        return "cv/" + fileName;
    }

    /**
     * Stocke un fichier de logo d'entreprise.
     *
     * @param file le fichier logo
     * @param entrepriseId l'ID de l'entreprise
     * @return le chemin relatif du fichier stocké
     * @throws IOException en cas d'erreur de stockage
     */
    public String storeLogo(MultipartFile file, Long entrepriseId) throws IOException {
        validateLogoFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename(), entrepriseId);
        Path targetPath = Paths.get(uploadDir, "logos", fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Logo stocké pour l'entreprise {}: {}", entrepriseId, fileName);
        return "logos/" + fileName;
    }

    /**
     * Stocke un fichier temporaire.
     *
     * @param file le fichier
     * @return le chemin relatif du fichier stocké
     * @throws IOException en cas d'erreur de stockage
     */
    public String storeTempFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename(), null);
        Path targetPath = Paths.get(uploadDir, "temp", fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "temp/" + fileName;
    }

    /**
     * Supprime un fichier.
     *
     * @param filePath le chemin du fichier à supprimer
     * @return true si le fichier a été supprimé
     */
    public boolean deleteFile(String filePath) {
        try {
            Path targetPath = Paths.get(uploadDir, filePath);
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.info("Fichier supprimé: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", filePath, e);
            return false;
        }
    }

    /**
     * Récupère le chemin absolu d'un fichier.
     *
     * @param relativePath le chemin relatif
     * @return le chemin absolu
     */
    public Path getAbsolutePath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
    }

    /**
     * Vérifie si un fichier existe.
     *
     * @param relativePath le chemin relatif
     * @return true si le fichier existe
     */
    public boolean fileExists(String relativePath) {
        return Files.exists(getAbsolutePath(relativePath));
    }

    /**
     * Obtient la taille d'un fichier en octets.
     *
     * @param relativePath le chemin relatif
     * @return la taille du fichier
     * @throws IOException en cas d'erreur
     */
    public long getFileSize(String relativePath) throws IOException {
        return Files.size(getAbsolutePath(relativePath));
    }

    /**
     * Valide un fichier CV.
     *
     * @param file le fichier à valider
     * @throws IllegalArgumentException si le fichier n'est pas valide
     */
    public void validateCvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CV est vide");
        }

        if (file.getSize() > maxCvSize) {
            throw new IllegalArgumentException(
                String.format("Le fichier CV dépasse la taille maximale de %d MB", maxCvSize / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !CV_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                "Le fichier CV doit être au format PDF, DOC ou DOCX"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!CV_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException(
                    "Le fichier CV doit avoir l'extension .pdf, .doc ou .docx"
                );
            }
        }
    }

    /**
     * Valide un fichier de logo.
     *
     * @param file le fichier à valider
     * @throws IllegalArgumentException si le fichier n'est pas valide
     */
    public void validateLogoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier logo est vide");
        }

        if (file.getSize() > maxLogoSize) {
            throw new IllegalArgumentException(
                String.format("Le fichier logo dépasse la taille maximale de %d MB", maxLogoSize / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                "Le fichier logo doit être au format JPG, PNG ou GIF"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!IMAGE_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException(
                    "Le fichier logo doit avoir l'extension .jpg, .jpeg, .png ou .gif"
                );
            }
        }
    }

    /**
     * Génère un nom de fichier unique.
     *
     * @param originalFilename le nom original du fichier
     * @param id l'ID associé (peut être null)
     * @return le nom de fichier unique
     */
    private String generateUniqueFileName(String originalFilename, Long id) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (id != null) {
            return id + "_" + uuid + extension;
        }
        return uuid + extension;
    }

    /**
     * Déplace un fichier du répertoire temporaire vers son emplacement final.
     *
     * @param tempPath le chemin temporaire
     * @param finalPath le chemin final
     * @return true si le déplacement a réussi
     */
    public boolean moveFromTemp(String tempPath, String finalPath) {
        try {
            Path source = Paths.get(uploadDir, tempPath);
            Path target = Paths.get(uploadDir, finalPath);

            // Créer le répertoire parent si nécessaire
            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Fichier déplacé de {} à {}", tempPath, finalPath);
            return true;
        } catch (IOException e) {
            log.error("Erreur lors du déplacement du fichier", e);
            return false;
        }
    }
}
