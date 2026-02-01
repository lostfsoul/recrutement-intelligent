package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un candidat sur la plateforme.
 * Hérite de Utilisateur avec le rôle CANDIDAT.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@DiscriminatorValue("CANDIDAT")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"candidatures", "competences", "experiences"})
public class Candidat extends Utilisateur {

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "cv_path", length = 255)
    private String cvPath;

    @Column(name = "cv_text", length = 10000)
    private String cvText;

    @Column(name = "cv_vector_id", length = 255)
    private String cvVectorId;

    @Column(name = "titre_poste_recherche", length = 100)
    private String titrePosteRecherche;

    @Column(name = "disponibilite_immediate")
    @Builder.Default
    private Boolean disponibiliteImmediate = true;

    @Column(name = "date_disponibilite")
    private LocalDate dateDisponibilite;

    @Column(name = "pretention_salariale_min")
    private Integer pretentionSalarialeMin;

    @Column(name = "pretention_salariale_max")
    private Integer pretentionSalarialeMax;

    @Column(name = "mobilite", length = 255)
    private String mobilite;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "portefolio_url", length = 255)
    private String portefolioUrl;

    @Column(name = "presentation", length = 2000)
    private String presentation;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateDebut DESC")
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nom ASC")
    @Builder.Default
    private List<Competence> competences = new ArrayList<>();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Candidature> candidatures = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.CANDIDAT;
    }

    /**
     * Ajoute une compétence au candidat
     *
     * @param competence la compétence à ajouter
     */
    public void ajouterCompetence(Competence competence) {
        competences.add(competence);
        competence.setCandidat(this);
    }

    /**
     * Retire une compétence du candidat
     *
     * @param competence la compétence à retirer
     */
    public void retirerCompetence(Competence competence) {
        competences.remove(competence);
        competence.setCandidat(null);
    }

    /**
     * Ajoute une expérience au candidat
     *
     * @param expérience l'expérience à ajouter
     */
    public void ajouterExperience(Experience experience) {
        experiences.add(experience);
        experience.setCandidat(this);
    }

    /**
     * Retire une expérience du candidat
     *
     * @param experience l'expérience à retirer
     */
    public void retirerExperience(Experience experience) {
        experiences.remove(experience);
        experience.setCandidat(null);
    }
}
