package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant une compétence d'un candidat.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "competences", indexes = {
    @Index(name = "idx_competence_candidat", columnList = "candidat_id"),
    @Index(name = "idx_competence_nom", columnList = "nom")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "candidat")
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "categorie", length = 50)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", length = 20)
    @Builder.Default
    private NiveauCompetence niveau = NiveauCompetence.INTERMEDIAIRE;

    @Column(name = "annees_experience")
    private Integer anneesExperience;

    @Column(name = "certifiee")
    @Builder.Default
    private Boolean certifiee = false;

    @Column(name = "derniere_utilisation")
    private String derniereUtilisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    /**
     * Enumération des niveaux de compétence
     */
    public enum NiveauCompetence {
        DEBUTANT,
        INTERMEDIAIRE,
        AVANCE,
        EXPERT
    }
}
