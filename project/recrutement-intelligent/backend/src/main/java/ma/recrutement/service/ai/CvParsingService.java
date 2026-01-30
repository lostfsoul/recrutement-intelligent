package ma.recrutement.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.dto.CvParseResponseDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour l'extraction de texte depuis les fichiers CV (PDF, DOCX).
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CvParsingService {

    /**
     * Extrait le texte d'un fichier CV.
     *
     * @param file le fichier CV
     * @return la réponse avec le texte extrait
     */
    public CvParseResponseDTO parseCv(MultipartFile file) {
        log.info("Parsing du CV: {}, taille: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            String extractedText = extractText(file);
            int wordCount = extractedText.split("\\s+").length;

            return CvParseResponseDTO.builder()
                .extractedText(extractedText)
                .success(true)
                .message("CV analysé avec succès")
                .wordCount(wordCount)
                .build();

        } catch (Exception e) {
            log.error("Erreur lors du parsing du CV", e);
            return CvParseResponseDTO.builder()
                .extractedText(null)
                .success(false)
                .message("Erreur lors de l'analyse du CV: " + e.getMessage())
                .wordCount(0)
                .build();
        }
    }

    /**
     * Extrait le texte d'un fichier selon son type.
     *
     * @param file le fichier
     * @return le texte extrait
     * @throws IOException en cas d'erreur de lecture
     */
    private String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            return switch (extension) {
                case "pdf" -> extractFromPdf(file.getInputStream());
                case "docx", "doc" -> extractFromDocx(file.getInputStream());
                default -> throw new IllegalArgumentException("Format de fichier non supporté: " + extension);
            };
        }

        throw new IllegalArgumentException("Impossible de déterminer le type de fichier");
    }

    /**
     * Extrait le texte d'un fichier PDF.
     *
     * @param inputStream le flux d'entrée
     * @return le texte extrait
     * @throws IOException en cas d'erreur de lecture
     */
    private String extractFromPdf(InputStream inputStream) throws IOException {
        byte[] bytes = readAllBytes(inputStream);
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            return textStripper.getText(document).trim();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * Extrait le texte d'un fichier DOCX.
     *
     * @param inputStream le flux d'entrée
     * @return le texte extrait
     * @throws IOException en cas d'erreur de lecture
     */
    private String extractFromDocx(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
        }

        return text.toString().trim();
    }

    /**
     * Extrait les emails depuis un texte.
     *
     * @param text le texte à analyser
     * @return la liste des emails trouvés
     */
    public List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            emails.add(matcher.group());
        }

        return emails;
    }

    /**
     * Extrait les numéros de téléphone depuis un texte.
     *
     * @param text le texte à analyser
     * @return la liste des téléphones trouvés
     */
    public List<String> extractPhones(String text) {
        List<String> phones = new ArrayList<>();
        String phonePattern = "(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(phonePattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            phones.add(matcher.group().replaceAll("\\s", ""));
        }

        return phones;
    }
}
