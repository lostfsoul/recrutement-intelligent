package ma.recrutement.repository;

import ma.recrutement.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Utilisateur.
 * Fournit les opérations CRUD et de recherche pour les utilisateurs.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long>, JpaSpecificationExecutor<Utilisateur> {

    /**
     * Trouve un utilisateur par son email.
     *
     * @param email l'email de l'utilisateur
     * @return l'utilisateur trouvé ou Optional vide
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec cet email.
     *
     * @param email l'email à vérifier
     * @return true si un utilisateur existe
     */
    boolean existsByEmail(String email);

    /**
     * Trouve un utilisateur par son email avec le role spécifique
     *
     * @param email l'email de l'utilisateur
     * @param type le type exact de l'utilisateur
     * @return l'utilisateur trouvé ou Optional vide
     */
    <T extends Utilisateur> Optional<T> findByEmail(String email, Class<T> type);

    /**
     * Trouve tous les candidats (utilise CandidatRepository pour plus de méthodes).
     *
     * @return la liste des candidats
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Utilisateur u WHERE TYPE(u) = Candidat")
    List<Utilisateur> findAllCandidats();

    /**
     * Trouve tous les recruteurs (utilise RecruteurRepository pour plus de méthodes).
     *
     * @return la liste des recruteurs
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Utilisateur u WHERE TYPE(u) = Recruteur")
    List<Utilisateur> findAllRecruteurs();

    /**
     * Trouve un utilisateur par son token d'activation.
     *
     * @param token le token d'activation
     * @return l'utilisateur trouvé ou Optional vide
     */
    Optional<Utilisateur> findByTokenActivation(String token);

    /**
     * Trouve un utilisateur par son token de reset password.
     *
     * @param token le token de reset password
     * @return l'utilisateur trouvé ou Optional vide
     */
    Optional<Utilisateur> findByTokenResetPassword(String token);

    /**
     * Trouve les utilisateurs par leur statut.
     *
     * @param statut le statut des utilisateurs
     * @return la liste des utilisateurs
     */
    List<Utilisateur> findByStatut(Utilisateur.StatutUtilisateur statut);

    /**
     * Compte le nombre de candidats.
     *
     * @return le nombre de candidats
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM Utilisateur u WHERE TYPE(u) = Candidat")
    long countCandidats();

    /**
     * Compte le nombre de recruteurs.
     *
     * @return le nombre de recruteurs
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM Utilisateur u WHERE TYPE(u) = Recruteur")
    long countRecruteurs();
}
