package ma.recrutement.repository;

import ma.recrutement.entity.Competence;
import ma.recrutement.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Competence.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    /**
     * Trouve toutes les compétences d'un candidat.
     *
     * @param candidat le candidat
     * @return la liste des compétences
     */
    List<Competence> findByCandidat(Candidat candidat);

    /**
     * Supprime toutes les compétences d'un candidat.
     *
     * @param candidat le candidat
     */
    void deleteByCandidat(Candidat candidat);
}
