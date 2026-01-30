package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une entreprise sur la plateforme.
 * Une entreprise est associée à un recruteur.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "entreprises", indexes = {
    @Index(name = "idx_entreprise_recruteur", columnList = "recruteur_id"),
    @Index(name = "idx_entreprise_secteur", columnList = "secteur")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"offres", "recruteur"})
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "secteur", length = 50)
    private String secteur;

    @Column(name = "taille_entreprise", length = 50)
    private String tailleEntreprise;

    @Column(name = "localisation", length = 255)
    private String localisation;

    @Column(name = "adresse", length = 500)
    private String adresse;

    @Column(name = "code_postal", length = 20)
    private String codePostal;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "pays", length = 100)
    @Builder.Default
    private String pays = "Maroc";

    @Column(name = "site_web", length = 255)
    private String siteWeb;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    @Column(name = "email_contact", length = 100)
    private String emailContact;

    @Column(name = "telephone_contact", length = 20)
    private String telephoneContact;

    @Column(name = "date_fondation")
    private Integer dateFondation;

    @Column(name = "numero_registre", length = 100)
    private String numeroRegistre;

    @Column(name = "numero_taxe", length = 100)
    private String numeroTaxe;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", length = 20)
    @Builder.Default
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "valide_par_id")
    private Long valideParId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruteur_id", nullable = false)
    private Recruteur recruteur;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OffreEmploi> offres = new ArrayList<>();

    /**
     * Enumération des statuts de validation d'une entreprise
     */
    public enum StatutValidation {
        EN_ATTENTE,
        VALIDEE,
        REFUSEE
    }
}
