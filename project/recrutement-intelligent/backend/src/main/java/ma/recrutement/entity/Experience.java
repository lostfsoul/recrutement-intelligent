package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entité représentant une expérience professionnelle d'un candidat.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "experiences", indexes = {
    @Index(name = "idx_experience_candidat", columnList = "candidat_id"),
    @Index(name = "idx_experience_date_debut", columnList = "date_debut")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "candidat")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "titre", nullable = false, length = 150)
    private String titre;

    @Column(name = "entreprise", nullable = false, length = 150)
    private String entreprise;

    @Column(name = "type_entreprise", length = 50)
    private String typeEntreprise;

    @Column(name = "secteur", length = 100)
    private String secteur;

    @Column(name = "localisation", length = 255)
    private String localisation;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "emploi_actuel")
    @Builder.Default
    private Boolean emploiActuel = false;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "responsabilites", columnDefinition = "TEXT")
    private String responsabilites;

    @Column(name = "accomplissements", columnDefinition = "TEXT")
    private String accomplissements;

    @Column(name = "outils_utilises", length = 500)
    private String outilsUtilises;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    /**
     * Calcule la durée de l'expérience en mois
     *
     * @return nombre de mois de l'expérience
     */
    public int calculerDureeMois() {
        if (dateDebut == null) {
            return 0;
        }

        LocalDate fin = emploiActuel ? LocalDate.now() : dateFin;
        if (fin == null) {
            fin = LocalDate.now();
        }

        return (int) java.time.temporal.ChronoUnit.MONTHS.between(dateDebut, fin);
    }
}
