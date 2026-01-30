package ma.recrutement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les informations d'un candidat.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatDTO {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String cvPath;
    private String titrePosteRecherche;
    private Boolean disponibiliteImmediate;
    private LocalDate dateDisponibilite;
    private Integer pretentionSalarialeMin;
    private Integer pretentionSalarialeMax;
    private String mobilite;
    private String linkedinUrl;
    private String githubUrl;
    private String portefolioUrl;
    private String presentation;
    private String statut;
    private Boolean emailVerifie;
    private LocalDateTime dateCreation;
}
