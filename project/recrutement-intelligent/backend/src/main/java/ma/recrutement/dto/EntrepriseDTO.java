package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
    import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les informations d'une entreprise.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseDTO {

    private Long id;
    private String nom;
    private String description;
    private String secteur;
    private String tailleEntreprise;
    private String localisation;
    private String adresse;
    private String codePostal;
    private String ville;
    private String pays;
    private String siteWeb;
    private String logoPath;
    private String emailContact;
    private String telephoneContact;
    private Integer dateFondation;
    private String statutValidation;
    private Boolean active;
    private Long recruteurId;
    private Integer nombreOffres;
    private LocalDateTime dateCreation;
}
