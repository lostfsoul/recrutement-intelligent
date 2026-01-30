package ma.recrutement.dto;

import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "L'ID de l'offre est obligatoire")
    private Long offreId;

    private String lettreMotivation;
}
