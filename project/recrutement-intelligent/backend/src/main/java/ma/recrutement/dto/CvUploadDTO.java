package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse après upload d'un CV.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvUploadDTO {

    private String cvPath;
    private String cvText;
    private Boolean extractionSuccessful;
    private String message;
    private Integer nombreCompetences;
    private Integer nombreExperiences;
}
