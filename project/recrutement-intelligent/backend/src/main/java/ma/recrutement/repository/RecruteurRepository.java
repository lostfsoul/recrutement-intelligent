package ma.recrutement.repository;

import ma.recrutement.entity.Recruteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Recruteur.
 * Fournit les opérations CRUD et de recherche spécifiques aux recruteurs.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface RecruteurRepository extends JpaRepository<Recruteur, Long>, JpaSpecificationExecutor<Recruteur> {

    /**
     * Trouve un recruteur par son email.
     *
     * @param email l'email du recruteur
     * @return le recruteur trouvé ou Optional vide
     */
    @Query("SELECT r FROM Recruteur r WHERE r.email = :email")
    Optional<Recruteur> findByEmail(@Param("email") String email);

    /**
     * Vérifie si un recruteur existe avec cet email.
     *
     * @param email l'email à vérifier
     * @return true si un recruteur existe
     */
    boolean existsByEmail(String email);

    /**
     * Trouve les recruteurs par statut de vérification.
     *
     * @param verified le statut de vérification
     * @return la liste des recruteurs
     */
    List<Recruteur> findByVerified(Boolean verified);

    /**
     * Trouve les recruteurs par nom d'entreprise (recherche partielle).
     *
     * @param nomEntreprise le nom de l'entreprise
     * @return la liste des recruteurs
     */
    List<Recruteur> findByNomEntrepriseContainingIgnoreCase(String nomEntreprise);

    /**
     * Recherche plein texte sur les profils des recruteurs.
     *
     * @param terme le terme de recherche
     * @return la liste des recruteurs correspondants
     */
    @Query(value = "SELECT * FROM recruteurs r WHERE " +
           "LOWER(r.nom_entreprise) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(r.poste) LIKE LOWER(CONCAT('%', :terme, '%'))",
           nativeQuery = true)
    List<Recruteur> recherchePleinText(@Param("terme") String terme);

    /**
     * Compte le nombre d'entreprises par recruteur.
     *
     * @param recruteurId l'ID du recruteur
     * @return le nombre d'entreprises
     */
    @Query("SELECT COUNT(e) FROM Recruteur r JOIN r.entreprises e WHERE r.id = :recruteurId")
    long countEntreprisesByRecruteurId(@Param("recruteurId") Long recruteurId);

    /**
     * Compte le nombre d'offres actives par recruteur.
     *
     * @param recruteurId l'ID du recruteur
     * @return le nombre d'offres actives
     */
    @Query("SELECT COUNT(o) FROM Recruteur r JOIN r.offres o WHERE r.id = :recruteurId AND o.statut = 'PUBLIEE' AND o.actif = true")
    long countOffresActivesByRecruteurId(@Param("recruteurId") Long recruteurId);
}
