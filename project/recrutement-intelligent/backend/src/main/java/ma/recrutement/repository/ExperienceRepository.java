package ma.recrutement.repository;

import ma.recrutement.entity.Experience;
import ma.recrutement.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Experience.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Trouve toutes les expériences d'un candidat.
     *
     * @param candidat le candidat
     * @return la liste des expériences
     */
    List<Experience> findByCandidat(Candidat candidat);

    /**
     * Supprime toutes les expériences d'un candidat.
     *
     * @param candidat le candidat
     */
    void deleteByCandidat(Candidat candidat);
}
