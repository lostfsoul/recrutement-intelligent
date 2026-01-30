package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour le résultat de matching CV/Offre.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResultDTO {

    private Long offreId;
    private String offreTitre;
    private String nomEntreprise;
    private Long candidatId;
    private String candidatNom;
    private String candidatPrenom;
    private Integer scoreMatching;
    private Integer scoreCompetences;
    private Integer scoreExperience;
    private Integer scoreFormation;
    private List<String> competencesMatch;
    private List<String> competencesManquantes;
    private String recommendation;
    private Boolean recommande;
    private String reason;
}
