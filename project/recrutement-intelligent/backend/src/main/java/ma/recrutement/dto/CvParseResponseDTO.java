package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse du parsing de CV par IA.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvParseResponseDTO {

    private String extractedText;
    private Boolean success;
    private String message;
    private Integer wordCount;
    private CvContentDTO content;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CvContentDTO {
    private String nom;
    private String email;
    private String telephone;
    private List<CompetenceDTO> competences;
    private List<ExperienceDTO> experiences;
    private String formation;
}
