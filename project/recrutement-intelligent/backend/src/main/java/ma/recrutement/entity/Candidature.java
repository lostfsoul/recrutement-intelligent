package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant une candidature à une offre d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "candidatures", indexes = {
    @Index(name = "idx_candidature_candidat", columnList = "candidat_id"),
    @Index(name = "idx_candidature_offre", columnList = "offre_id"),
    @Index(name = "idx_candidature_statut", columnList = "statut")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"candidat", "offre"})
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreEmploi offre;

    @Lob
    @Column(name = "lettre_motivation")
    private String lettreMotivation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(name = "score_matching")
    private Integer scoreMatching;

    @Column(name = "note_interne", length = 1000)
    private String noteInterne;

    @Column(name = "cv_personnalise_path", length = 255)
    private String cvPersonnalisePath;

    @Column(name = "vu_par_recruteur")
    @Builder.Default
    private Boolean vuParRecruteur = false;

    @Column(name = "date_vue")
    private LocalDateTime dateVue;

    @CreationTimestamp
    @Column(name = "date_candidature", nullable = false, updatable = false)
    private LocalDateTime dateCandidature;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "date_statut_change")
    private LocalDateTime dateStatutChange;

    @Column(name = "raison_refus", length = 500)
    private String raisonRefus;

    @Column(name = "feedback_candidat", length = 1000)
    private String feedbackCandidat;

    /**
     * Enumération des statuts possibles d'une candidature
     */
    public enum StatutCandidature {
        EN_ATTENTE,
        EN_REVUE,
        PRESELECTIONNE,
        ENTRETIENT_PLANIFIE,
        ENTRETIENT_PASSE,
        OFFRE_ACCEPT,
        REFUSE,
        RETIRE_PAR_CANDIDAT,
        OFFRE_REFUSE_PAR_CANDIDAT
    }

    /**
     * Marque la candidature comme vue par le recruteur
     */
    public void marquerCommeVue() {
        this.vuParRecruteur = true;
        this.dateVue = LocalDateTime.now();
    }

    /**
     * Change le statut de la candidature
     *
     * @param nouveauStatut le nouveau statut
     */
    public void changerStatut(StatutCandidature nouveauStatut) {
        this.statut = nouveauStatut;
        this.dateStatutChange = LocalDateTime.now();
    }
}
