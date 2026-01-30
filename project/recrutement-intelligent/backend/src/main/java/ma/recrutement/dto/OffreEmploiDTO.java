package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les informations d'une offre d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffreEmploiDTO {

    private Long id;
    private String reference;
    private String titre;
    private String description;
    private String competencesRequises;
    private String profilRecherche;
    private Integer salaireMin;
    private Integer salaireMax;
    private String devise;
    private String frequencePaiement;
    private String localisation;
    private String ville;
    private String pays;
    private Boolean teletravail;
    private String typeContrat;
    private String statut;
    private Integer experienceMinAnnees;
    private String niveauEtudesMin;
    private String languesRequises;
    private LocalDate datePublication;
    private LocalDate dateLimiteCandidature;
    private Integer nombrePostes;
    private Integer nombreCandidatures;
    private Boolean actif;
    private Long entrepriseId;
    private String nomEntreprise;
    private String logoEntreprise;
    private LocalDateTime dateCreation;
}
