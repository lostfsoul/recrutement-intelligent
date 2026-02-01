package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'une candidature.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureCreateDTO {

    // offreId is set from path variable, no validation needed here
    private Long offreId;

    private String lettreMotivation;
}
