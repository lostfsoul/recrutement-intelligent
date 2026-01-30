package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse d'extraction des compétences par IA.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillExtractionDTO {

    private List<ExtractedSkill> skills;
    private String summary;
    private Integer confidenceScore;
    private Boolean success;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedSkill {
        private String name;
        private String category;
        private String level;
        private Integer yearsOfExperience;
        private Boolean certified;
    }
}
