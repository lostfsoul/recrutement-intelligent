package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un administrateur de la plateforme.
 * Hérite de Utilisateur avec le rôle ADMINISTRATEUR.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@DiscriminatorValue("ADMINISTRATEUR")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "permissions")
public class Administrateur extends Utilisateur {

    @Column(name = "niveau_acces", nullable = false)
    @Builder.Default
    private Integer niveauAcces = 1;

    @Column(name = "departement", length = 50)
    private String departement;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission", length = 50)
    @Builder.Default
    private java.util.List<String> permissions = new java.util.ArrayList<>();

    @Override
    public Role getRole() {
        return Role.ADMINISTRATEUR;
    }

    /**
     * Ajoute une permission à l'administrateur
     *
     * @param permission la permission à ajouter
     */
    public void ajouterPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    /**
     * Retire une permission de l'administrateur
     *
     * @param permission la permission à retirer
     */
    public void retirerPermission(String permission) {
        permissions.remove(permission);
    }

    /**
     * Vérifie si l'administrateur a une permission spécifique
     *
     * @param permission la permission à vérifier
     * @return true si l'administrateur a la permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
