package ma.recrutement.repository;

import ma.recrutement.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Entreprise.
 * Fournit les opérations CRUD et de recherche spécifiques aux entreprises.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long>, JpaSpecificationExecutor<Entreprise> {

    /**
     * Trouve les entreprises par ID du recruteur.
     *
     * @param recruteurId l'ID du recruteur
     * @return la liste des entreprises du recruteur
     */
    List<Entreprise> findByRecruteurId(Long recruteurId);

    /**
     * Trouve une entreprise par son nom (recherche exacte).
     *
     * @param nom le nom de l'entreprise
     * @return l'entreprise trouvée ou Optional vide
     */
    Optional<Entreprise> findByNom(String nom);

    /**
     * Trouve les entreprises par secteur d'activité.
     *
     * @param secteur le secteur d'activité
     * @return la liste des entreprises
     */
    List<Entreprise> findBySecteur(String secteur);

    /**
     * Trouve les entreprises par localisation (ville).
     *
     * @param ville la ville
     * @return la liste des entreprises
     */
    List<Entreprise> findByVille(String ville);

    /**
     * Trouve les entreprises par statut de validation.
     *
     * @param statutValidation le statut de validation
     * @return la liste des entreprises
     */
    List<Entreprise> findByStatutValidation(Entreprise.StatutValidation statutValidation);

    /**
     * Trouve les entreprises en attente de validation.
     *
     * @return la liste des entreprises en attente
     */
    List<Entreprise> findByStatutValidationOrderByDateCreationAsc(Entreprise.StatutValidation statutValidation);

    /**
     * Trouve les entreprises actives.
     *
     * @param active le statut actif
     * @return la liste des entreprises actives
     */
    List<Entreprise> findByActive(Boolean active);

    /**
     * Recherche plein texte sur les entreprises.
     *
     * @param terme le terme de recherche
     * @return la liste des entreprises correspondantes
     */
    @Query(value = "SELECT * FROM entreprises e WHERE " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(e.secteur) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(e.ville) LIKE LOWER(CONCAT('%', :terme, '%'))",
           nativeQuery = true)
    List<Entreprise> recherchePleinText(@Param("terme") String terme);

    /**
     * Trouve les entreprises par pays.
     *
     * @param pays le pays
     * @return la liste des entreprises
     */
    List<Entreprise> findByPays(String pays);

    /**
     * Vérifie si un recruteur possède une entreprise avec ce nom.
     *
     * @param nom le nom de l'entreprise
     * @param recruteurId l'ID du recruteur
     * @return true si l'entreprise existe pour ce recruteur
     */
    boolean existsByNomAndRecruteurId(String nom, Long recruteurId);

    /**
     * Compte le nombre d'entreprises par statut de validation.
     *
     * @param statutValidation le statut de validation
     * @return le nombre d'entreprises
     */
    long countByStatutValidation(Entreprise.StatutValidation statutValidation);

    /**
     * Compte le nombre d'entreprises par secteur.
     *
     * @return liste de statistiques
     */
    @Query("SELECT e.secteur, COUNT(e) FROM Entreprise e WHERE e.secteur IS NOT NULL GROUP BY e.secteur")
    List<Object[]> countBySecteur();
}
