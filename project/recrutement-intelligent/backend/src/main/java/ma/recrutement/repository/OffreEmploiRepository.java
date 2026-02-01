package ma.recrutement.repository;

import ma.recrutement.entity.OffreEmploi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité OffreEmploi.
 * Fournit les opérations CRUD et de recherche spécifiques aux offres d'emploi.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long>, JpaSpecificationExecutor<OffreEmploi> {

    /**
     * Trouve les offres par entreprise.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @return la liste des offres
     */
    List<OffreEmploi> findByEntrepriseId(Long entrepriseId);

    /**
     * Trouve les offres par entreprise avec pagination.
     *
     * @param entrepriseId l'ID de l'entreprise
     * @param pageable les informations de pagination
     * @return la page des offres
     */
    Page<OffreEmploi> findByEntrepriseId(Long entrepriseId, Pageable pageable);

    /**
     * Trouve les offres par recruteur.
     *
     * @param recruteurId l'ID du recruteur
     * @return la liste des offres
     */
    List<OffreEmploi> findByRecruteurId(Long recruteurId);

    /**
     * Trouve les offres par statut.
     *
     * @param statut le statut de l'offre
     * @return la liste des offres
     */
    List<OffreEmploi> findByStatut(OffreEmploi.StatutOffre statut);

    /**
     * Trouve les offres publiées et actives.
     *
     * @return la liste des offres publiées
     */
    List<OffreEmploi> findByStatutAndActifTrueOrderByDatePublicationDesc(OffreEmploi.StatutOffre statut);

    /**
     * Trouve les offres publiées et actives avec pagination.
     *
     * @param pageable les informations de pagination
     * @return la page des offres publiées
     */
    Page<OffreEmploi> findByStatutAndActifTrue(OffreEmploi.StatutOffre statut, Pageable pageable);

    /**
     * Recherche les offres par titre (recherche partielle, insensible à la casse).
     *
     * @param titre le titre ou partie du titre
     * @param pageable les informations de pagination
     * @return la page des offres correspondantes
     */
    Page<OffreEmploi> findByTitreContainingIgnoreCase(String titre, Pageable pageable);

    /**
     * Recherche les offres par localisation.
     *
     * @param localisation la localisation
     * @param pageable les informations de pagination
     * @return la page des offres correspondantes
     */
    Page<OffreEmploi> findByLocalisationContainingIgnoreCase(String localisation, Pageable pageable);

    /**
     * Recherche les offres par type de contrat.
     *
     * @param typeContrat le type de contrat
     * @return la liste des offres
     */
    List<OffreEmploi> findByTypeContrat(OffreEmploi.TypeContrat typeContrat);

    /**
     * Recherche les offres par type de contrat avec pagination.
     *
     * @param typeContrat le type de contrat
     * @param pageable les informations de pagination
     * @return la page des offres
     */
    Page<OffreEmploi> findByTypeContrat(OffreEmploi.TypeContrat typeContrat, Pageable pageable);

    /**
     * Recherche les offres par salaire minimum.
     *
     * @param salaireMin le salaire minimum
     * @return la liste des offres
     */
    @Query("SELECT o FROM OffreEmploi o WHERE o.salaireMin >= :salaireMin AND o.statut = 'PUBLIEE' AND o.actif = true")
    List<OffreEmploi> findBySalaireMinGreaterThanEqual(@Param("salaireMin") Integer salaireMin);

    /**
     * Recherche les offres par salaire dans une fourchette.
     *
     * @param salaireMin salaire minimum
     * @param salaireMax salaire maximum
     * @return la liste des offres
     */
    @Query("SELECT o FROM OffreEmploi o WHERE " +
           "o.salaireMin <= :salaireMax AND o.salaireMax >= :salaireMin AND " +
           "o.statut = 'PUBLIEE' AND o.actif = true")
    List<OffreEmploi> findBySalaireBetween(
            @Param("salaireMin") Integer salaireMin,
            @Param("salaireMax") Integer salaireMax
    );

    /**
     * Recherche les offres par ville.
     *
     * @param ville la ville
     * @param pageable les informations de pagination
     * @return la page des offres
     */
    Page<OffreEmploi> findByVilleIgnoreCase(String ville, Pageable pageable);

    /**
     * Trouve les offres par référence.
     *
     * @param reference la référence de l'offre
     * @return l'offre trouvée ou Optional vide
     */
    Optional<OffreEmploi> findByReference(String reference);

    /**
     * Recherche plein texte sur les offres.
     *
     * @param terme le terme de recherche
     * @param pageable les informations de pagination
     * @return la page des offres correspondantes
     */
    @Query(value = "SELECT * FROM offres_emploi o WHERE " +
           "o.statut = 'PUBLIEE' AND o.actif = true AND (" +
           "o.titre ILIKE CONCAT('%', :terme, '%') OR " +
           "o.description ILIKE CONCAT('%', :terme, '%') OR " +
           "o.competences_requises ILIKE CONCAT('%', :terme, '%') OR " +
           "o.localisation ILIKE CONCAT('%', :terme, '%'))",
           countQuery = "SELECT COUNT(*) FROM offres_emploi o WHERE " +
           "o.statut = 'PUBLIEE' AND o.actif = true AND (" +
           "o.titre ILIKE CONCAT('%', :terme, '%') OR " +
           "o.description ILIKE CONCAT('%', :terme, '%') OR " +
           "o.competences_requises ILIKE CONCAT('%', :terme, '%') OR " +
           "o.localisation ILIKE CONCAT('%', :terme, '%'))",
           nativeQuery = true)
    Page<OffreEmploi> recherchePleinText(@Param("terme") String terme, Pageable pageable);

    /**
     * Trouve les offres dont la date limite est passée.
     *
     * @param date la date actuelle
     * @return la liste des offres expirées
     */
    @Query("SELECT o FROM OffreEmploi o WHERE o.dateLimiteCandidature < :date AND o.statut = 'PUBLIEE'")
    List<OffreEmploi> findOffresExpirees(@Param("date") LocalDate date);

    /**
     * Trouve les offres qui permettent le télétravail.
     *
     * @param teletravail si le télétravail est autorisé
     * @param pageable les informations de pagination
     * @return la page des offres
     */
    Page<OffreEmploi> findByTeletravailAndStatutAndActifTrue(
            Boolean teletravail,
            OffreEmploi.StatutOffre statut,
            Pageable pageable
    );

    /**
     * Compte le nombre d'offres par statut.
     *
     * @param statut le statut
     * @return le nombre d'offres
     */
    long countByStatut(OffreEmploi.StatutOffre statut);

    /**
     * Trouve les offres qui ne sont pas encore indexées dans le vector store.
     *
     * @return la liste des offres sans vector_id
     */
    @Query("SELECT o FROM OffreEmploi o WHERE o.statut = 'PUBLIEE' AND o.vectorId IS NULL")
    List<OffreEmploi> findOffresNonIndexees();

    /**
     * Trouve les offres récentes (publiées depuis une date).
     *
     * @param date la date de début
     * @return la liste des offres récentes
     */
    @Query("SELECT o FROM OffreEmploi o WHERE o.datePublication >= :date AND o.statut = 'PUBLIEE' AND o.actif = true ORDER BY o.datePublication DESC")
    List<OffreEmploi> findOffresRecentes(@Param("date") LocalDate date);

    /**
     * Compte le nombre d'offres par type de contrat.
     *
     * @return liste de statistiques
     */
    @Query("SELECT o.typeContrat, COUNT(o) FROM OffreEmploi o WHERE o.statut = 'PUBLIEE' GROUP BY o.typeContrat")
    List<Object[]> countByTypeContrat();

    /**
     * Trouve une offre par ID avec l'entreprise chargée.
     *
     * @param offreId l'ID de l'offre
     * @return l'offre trouvée avec entreprise
     */
    @Query("SELECT o FROM OffreEmploi o LEFT JOIN FETCH o.entreprise WHERE o.id = :offreId")
    OffreEmploi findByIdWithEntreprise(@Param("offreId") Long offreId);
}
