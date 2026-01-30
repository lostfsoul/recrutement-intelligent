package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour le profil complet d'un candidat avec compétences et expériences.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatProfileDTO {

    private CandidatDTO candidat;
    private List<CompetenceDTO> competences;
    private List<ExperienceDTO> experiences;
    private Integer nombreCandidatures;
    private Integer nombreCompetences;
    private Integer nombreExperiences;
}
