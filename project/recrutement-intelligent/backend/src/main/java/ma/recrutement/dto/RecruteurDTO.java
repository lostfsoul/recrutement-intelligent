package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les informations d'un recruteur.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruteurDTO {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Boolean verified;
    private String nomEntreprise;
    private String poste;
    private String linkedinUrl;
    private String statut;
    private Integer nombreEntreprises;
    private Integer nombreOffres;
    private LocalDateTime dateCreation;
}
