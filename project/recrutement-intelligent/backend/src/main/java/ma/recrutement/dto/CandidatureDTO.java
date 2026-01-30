package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les informations d'une candidature.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureDTO {

    private Long id;
    private Long candidatId;
    private String candidatNom;
    private String candidatPrenom;
    private Long offreId;
    private String offreTitre;
    private String nomEntreprise;
    private String lettreMotivation;
    private String statut;
    private Integer scoreMatching;
    private Boolean vuParRecruteur;
    private LocalDateTime dateCandidature;
    private LocalDateTime dateStatutChange;
    private String raisonRefus;
}
