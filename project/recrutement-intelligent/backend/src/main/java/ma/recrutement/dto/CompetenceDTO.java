package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.recrutement.entity.Competence;

/**
 * DTO pour les informations d'une compétence.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceDTO {

    private Long id;
    private String nom;
    private String categorie;
    private Competence.NiveauCompetence niveau;
    private Integer anneesExperience;
    private Boolean certifiee;
    private String derniereUtilisation;
}
