package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un recruteur sur la plateforme.
 * Hérite de Utilisateur avec le rôle RECRUTEUR.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@DiscriminatorValue("RECRUTEUR")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"entreprises", "offres"})
public class Recruteur extends Utilisateur {

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "nom_entreprise", length = 100)
    private String nomEntreprise;

    @Column(name = "poste", length = 100)
    private String poste;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Entreprise> entreprises = new ArrayList<>();

    @OneToMany(mappedBy = "recruteur", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OffreEmploi> offres = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.RECRUTEUR;
    }

    /**
     * Ajoute une entreprise au recruteur
     *
     * @param entreprise l'entreprise à ajouter
     */
    public void ajouterEntreprise(Entreprise entreprise) {
        entreprises.add(entreprise);
        entreprise.setRecruteur(this);
    }

    /**
     * Retire une entreprise du recruteur
     *
     * @param entreprise l'entreprise à retirer
     */
    public void retirerEntreprise(Entreprise entreprise) {
        entreprises.remove(entreprise);
        entreprise.setRecruteur(null);
    }
}
