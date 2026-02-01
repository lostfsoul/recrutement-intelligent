package ma.recrutement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Classe de base pour tous les utilisateurs de la plateforme.
 * Utilise le pattern SINGLE_TABLE avec discriminateur pour l'héritage.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"password"})
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "email_verifie", nullable = false)
    @Builder.Default
    private Boolean emailVerifie = false;

    @Column(name = "token_activation", length = 255)
    private String tokenActivation;

    @Column(name = "token_reset_password", length = 255)
    private String tokenResetPassword;

    @Column(name = "token_reset_password_expiration")
    private LocalDateTime tokenResetPasswordExpiration;

    /**
     * Enumération des rôles utilisateurs (valeurs du discriminateur)
     */
    public enum Role {
        CANDIDAT,
        RECRUTEUR
    }

    /**
     * Enumération des statuts possibles d'un utilisateur
     */
    public enum StatutUtilisateur {
        ACTIF,
        INACTIF,
        SUSPENDU,
        EN_ATTENTE_VALIDATION
    }

    /**
     * Méthode abstraite pour obtenir le rôle de l'utilisateur
     *
     * @return le rôle de l'utilisateur
     */
    public abstract Role getRole();
}
