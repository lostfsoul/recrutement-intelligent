package ma.recrutement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.recrutement.entity.OffreEmploi;

import java.time.LocalDate;

/**
 * DTO pour la création d'une offre d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffreCreateDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 50, message = "La description doit contenir au moins 50 caractères")
    private String description;

    private String competencesRequises;

    private String profilRecherche;

    @Positive(message = "Le salaire minimum doit être positif")
    private Integer salaireMin;

    @Positive(message = "Le salaire maximum doit être positif")
    private Integer salaireMax;

    @Size(max = 10, message = "La devise ne peut pas dépasser 10 caractères")
    private String devise;

    private String localisation;

    private String ville;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String pays;

    private Boolean teletravail;

    @NotNull(message = "Le type de contrat est obligatoire")
    private OffreEmploi.TypeContrat typeContrat;

    @Min(value = 0, message = "L'expérience minimum ne peut pas être négative")
    @Max(value = 50, message = "L'expérience minimum ne peut pas dépasser 50 ans")
    private Integer experienceMinAnnees;

    private String niveauEtudesMin;

    private String languesRequises;

    @Future(message = "La date limite doit être dans le futur")
    private LocalDate dateLimiteCandidature;

    @Positive(message = "Le nombre de postes doit être positif")
    private Integer nombrePostes;

    @NotNull(message = "L'ID de l'entreprise est obligatoire")
    private Long entrepriseId;
}
