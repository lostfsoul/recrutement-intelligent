package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour les informations d'une expérience professionnelle.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {

    private Long id;
    private String titre;
    private String entreprise;
    private String typeEntreprise;
    private String secteur;
    private String localisation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean emploiActuel;
    private String description;
    private String responsabilites;
    private String accomplissements;
    private String outilsUtilises;
}
