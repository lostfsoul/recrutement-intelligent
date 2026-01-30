package ma.recrutement.repository;

import ma.recrutement.entity.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Administrateur.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    /**
     * Trouve un administrateur par son email.
     *
     * @param email l'email de l'administrateur
     * @return l'administrateur trouvé ou Optional vide
     */
    Optional<Administrateur> findByEmail(String email);

    /**
     * Vérifie si un administrateur existe avec cet email.
     *
     * @param email l'email à vérifier
     * @return true si un administrateur existe
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les administrateurs.
     *
     * @return la liste des administrateurs
     */
    List<Administrateur> findAll();
}
