package ma.recrutement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'une entreprise.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseCreateDTO {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Size(max = 50, message = "Le secteur ne peut pas dépasser 50 caractères")
    private String secteur;

    private String tailleEntreprise;

    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String localisation;

    private String adresse;

    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String codePostal;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String ville;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String pays;

    @Size(max = 255, message = "L'URL du site web ne peut pas dépasser 255 caractères")
    private String siteWeb;

    @Email(message = "Email de contact invalide")
    private String emailContact;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephoneContact;

    private Integer dateFondation;
}
