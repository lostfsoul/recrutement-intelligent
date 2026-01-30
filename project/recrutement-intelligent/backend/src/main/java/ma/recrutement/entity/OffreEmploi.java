package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une offre d'emploi sur la plateforme.
 * Une offre est associée à une entreprise et un recruteur.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "offres_emploi", indexes = {
    @Index(name = "idx_offre_entreprise", columnList = "entreprise_id"),
    @Index(name = "idx_offre_recruteur", columnList = "recruteur_id"),
    @Index(name = "idx_offre_statut", columnList = "statut"),
    @Index(name = "idx_offre_type_contrat", columnList = "type_contrat"),
    @Index(name = "idx_offre_localisation", columnList = "localisation")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"entreprise", "recruteur", "candidatures"})
public class OffreEmploi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "titre", nullable = false, length = 150)
    private String titre;

    @Column(name = "reference", unique = true, length = 50)
    private String reference;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Lob
    @Column(name = "competences_requises")
    private String competencesRequises;

    @Lob
    @Column(name = "profil_recherche")
    private String profilRecherche;

    @Column(name = "salaire_min")
    private Integer salaireMin;

    @Column(name = "salaire_max")
    private Integer salaireMax;

    @Column(name = "devise", length = 10)
    @Builder.Default
    private String devise = "MAD";

    @Column(name = "frequence_paiement", length = 20)
    @Builder.Default
    private String frequencePaiement = "Mensuel";

    @Column(name = "localisation", length = 255)
    private String localisation;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "pays", length = 100)
    @Builder.Default
    private String pays = "Maroc";

    @Column(name = "teletravail")
    @Builder.Default
    private Boolean teletravail = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false, length = 50)
    private TypeContrat typeContrat;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutOffre statut = StatutOffre.BROUILLON;

    @Column(name = "experience_min_annees")
    private Integer experienceMinAnnees;

    @Column(name = "experience_max_annees")
    private Integer experienceMaxAnnees;

    @Column(name = "niveau_etudes_min", length = 100)
    private String niveauEtudesMin;

    @Column(name = "langues_requises", length = 255)
    private String languesRequises;

    @Column(name = "heure_debut", length = 10)
    private String heureDebut;

    @Column(name = "heure_fin", length = 10)
    private String heureFin;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(name = "date_limite_candidature")
    private LocalDate dateLimiteCandidature;

    @Column(name = "nombre_postes")
    @Builder.Default
    private Integer nombrePostes = 1;

    @Column(name = "nombre_candidatures")
    @Builder.Default
    private Integer nombreCandidatures = 0;

    @Column(name = "vector_id", length = 255)
    private String vectorId;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruteur_id", nullable = false)
    private Recruteur recruteur;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Candidature> candidatures = new ArrayList<>();

    /**
     * Enumération des types de contrat possibles
     */
    public enum TypeContrat {
        CDI,
        CDD,
        STAGE,
        ALTERNANCE,
        FREELANCE,
        TEMPS_PLEIN,
        TEMPS_PARTIEL
    }

    /**
     * Enumération des statuts possibles d'une offre
     */
    public enum StatutOffre {
        BROUILLON,
        EN_ATTENTE_VALIDATION,
        PUBLIEE,
        CLOSE,
        ANNULEE,
        EXPIREE
    }

    /**
     * Incrémente le nombre de candidatures
     */
    public void incrementerCandidatures() {
        this.nombreCandidatures++;
    }

    /**
     * Décrémente le nombre de candidatures
     */
    public void decrementerCandidatures() {
        if (this.nombreCandidatures > 0) {
            this.nombreCandidatures--;
        }
    }

    /**
     * Vérifie si l'offre est toujours ouverte aux candidatures
     *
     * @return true si l'offre est ouverte
     */
    public boolean estOuverte() {
        return actif && statut == StatutOffre.PUBLIEE &&
               (dateLimiteCandidature == null || dateLimiteCandidature.isAfter(LocalDate.now()));
    }
}
