package ma.recrutement.repository;

import ma.recrutement.entity.Candidat;
import ma.recrutement.entity.Competence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Candidat.
 * Fournit les opérations CRUD et de recherche spécifiques aux candidats.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long>, JpaSpecificationExecutor<Candidat> {

    /**
     * Trouve un candidat par son email.
     *
     * @param email l'email du candidat
     * @return le candidat trouvé ou Optional vide
     */
    @Query("SELECT c FROM Candidat c WHERE c.email = :email")
    Optional<Candidat> findByEmail(@Param("email") String email);

    /**
     * Vérifie si un candidat existe avec cet email.
     *
     * @param email l'email à vérifier
     * @return true si un candidat existe
     */
    boolean existsByEmail(String email);

    /**
     * Trouve les candidats par disponibilité immédiate.
     *
     * @param disponibilite la disponibilité immédiate
     * @return la liste des candidats disponibles
     */
    List<Candidat> findByDisponibiliteImmediate(Boolean disponibilite);

    /**
     * Trouve les candidats dont la date de disponibilité est avant une date donnée.
     *
     * @param date la date limite de disponibilité
     * @return la liste des candidats disponibles
     */
    List<Candidat> findByDateDisponibiliteBefore(LocalDate date);

    /**
     * Trouve les candidats par titre de poste recherché (recherche partielle).
     *
     * @param titre le titre du poste recherché
     * @return la liste des candidats
     */
    List<Candidat> findByTitrePosteRechercheContainingIgnoreCase(String titre);

    /**
     * Trouve les candidats qui ont une compétence spécifique.
     *
     * @param nomCompetence le nom de la compétence
     * @return la liste des candidats
     */
    @Query("SELECT DISTINCT c FROM Candidat c JOIN c.competences comp WHERE comp.nom = :nomCompetence")
    List<Candidat> findByCompetenceNom(@Param("nomCompetence") String nomCompetence);

    /**
     * Trouve les candidats par niveau de compétence minimum.
     *
     * @param nomCompetence le nom de la compétence
     * @param niveau le niveau minimum requis
     * @return la liste des candidats
     */
    @Query("SELECT DISTINCT c FROM Candidat c JOIN c.competences comp WHERE comp.nom = :nomCompetence AND comp.niveau >= :niveau")
    List<Candidat> findByCompetenceNomAndNiveauMinimum(
            @Param("nomCompetence") String nomCompetence,
            @Param("niveau") Competence.NiveauCompetence niveau
    );

    /**
     * Trouve les candidats dont les prétentions salariales sont dans une fourchette.
     *
     * @param salaireMin salaire minimum
     * @param salaireMax salaire maximum
     * @return la liste des candidats
     */
    @Query("SELECT c FROM Candidat c WHERE " +
           "(c.pretentionSalarialeMin IS NULL OR c.pretentionSalarialeMin <= :salaireMax) AND " +
           "(c.pretentionSalarialeMax IS NULL OR c.pretentionSalarialeMax >= :salaireMin)")
    List<Candidat> findByPretentionSalarialeBetween(
            @Param("salaireMin") Integer salaireMin,
            @Param("salaireMax") Integer salaireMax
    );

    /**
     * Trouve les candidats par mobilité (recherche partielle sur la localisation).
     *
     * @param mobilite la mobilité recherchée
     * @return la liste des candidats
     */
    List<Candidat> findByMobiliteContainingIgnoreCase(String mobilite);

    /**
     * Recherche plein texte sur les profils des candidats (CV text, présentation).
     *
     * @param terme le terme de recherche
     * @return la liste des candidats correspondants
     */
    @Query(value = "SELECT * FROM candidats c WHERE " +
           "LOWER(c.cv_text) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(c.presentation) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "LOWER(c.titre_poste_recherche) LIKE LOWER(CONCAT('%', :terme, '%'))",
           nativeQuery = true)
    List<Candidat> recherchePleinText(@Param("terme") String terme);

    /**
     * Trouve les candidats qui n'ont pas de CV indexé dans le vector store.
     *
     * @return la liste des candidats sans CV indexé
     */
    @Query("SELECT c FROM Candidat c WHERE c.cvText IS NOT NULL AND c.cvVectorId IS NULL")
    List<Candidat> findCandidatsWithCvTextButNoVectorId();

    /**
     * Compte le nombre de candidats inscrits par mois.
     *
     * @return liste de statistiques
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', c.dateCreation), COUNT(c) " +
           "FROM Candidat c GROUP BY FUNCTION('DATE_TRUNC', 'month', c.dateCreation)")
    List<Object[]> countByMonth();
}
