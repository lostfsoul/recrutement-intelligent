package ma.recrutement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.config.JwtConfig;
import ma.recrutement.dto.*;
import ma.recrutement.entity.*;
import ma.recrutement.exception.AuthenticationException;
import ma.recrutement.exception.BusinessException;
import ma.recrutement.exception.ResourceNotFoundException;
import ma.recrutement.repository.CandidatRepository;
import ma.recrutement.repository.RecruteurRepository;
import ma.recrutement.repository.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour la gestion des utilisateurs et l'authentification.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final CandidatRepository candidatRepository;
    private final RecruteurRepository recruteurRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param request les données d'inscription
     * @return la réponse avec le token
     */
    @Transactional
    public TokenResponseDTO register(RegisterRequestDTO request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());

        // Vérifier que les mots de passe correspondent
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new BusinessException("Les mots de passe ne correspondent pas");
        }

        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un utilisateur avec cet email existe déjà");
        }

        // Créer l'utilisateur selon le rôle
        Utilisateur utilisateur = createUtilisateurByRole(request);
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));

        utilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur créé avec succès: ID={}, Role={}", utilisateur.getId(), utilisateur.getRole());

        // Générer le token
        String token = jwtConfig.generateToken(utilisateur.getEmail());

        return TokenResponseDTO.builder()
            .accessToken(token)
            .refreshToken(jwtConfig.generateRefreshToken(utilisateur.getEmail()))
            .tokenType("Bearer")
            .expiresIn(86400L) // 24 heures
            .userInfo(mapToUserInfo(utilisateur))
            .build();
    }

    /**
     * Authentifie un utilisateur.
     *
     * @param request les données de connexion
     * @return la réponse avec le token
     */
    public TokenResponseDTO login(LoginRequestDTO request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Get the email from the authentication
            String email = authentication.getName();
            Utilisateur utilisateur = getUtilisateurByEmail(email);

            // Mettre à jour la dernière connexion
            utilisateur.setDerniereConnexion(java.time.LocalDateTime.now());
            utilisateurRepository.save(utilisateur);

            String token = jwtConfig.generateToken(email);

            return TokenResponseDTO.builder()
                .accessToken(token)
                .refreshToken(jwtConfig.generateRefreshToken(email))
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userInfo(mapToUserInfo(utilisateur))
                .build();

        } catch (Exception e) {
            log.error("Erreur de connexion: {}", e.getMessage());
            throw AuthenticationException.invalidCredentials();
        }
    }

    /**
     * Rafraîchit le token JWT.
     *
     * @param refreshToken le token de rafraîchissement
     * @return la nouvelle réponse avec token
     */
    public TokenResponseDTO refreshToken(String refreshToken) {
        // Implémentation à compléter
        throw new UnsupportedOperationException("Refresh token non implémenté");
    }

    /**
     * Déconnecte un utilisateur.
     */
    public void logout() {
        log.info("Déconnexion de l'utilisateur");
        // Pour JWT stateless, la déconnexion est gérée côté client
    }

    /**
     * Obtient un utilisateur par son email.
     *
     * @param email l'email de l'utilisateur
     * @return l'utilisateur trouvé
     */
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
    }

    /**
     * Obtient un utilisateur par son ID.
     *
     * @param id l'ID de l'utilisateur
     * @return l'utilisateur trouvé
     */
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    /**
     * Crée un utilisateur selon son rôle.
     */
    private Utilisateur createUtilisateurByRole(RegisterRequestDTO request) {
        return switch (request.getRole()) {
            case CANDIDAT -> Candidat.builder()
                .email(request.getEmail())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .emailVerifie(false)
                .build();
            case RECRUTEUR -> Recruteur.builder()
                .email(request.getEmail())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .nomEntreprise(request.getNomEntreprise())
                .poste(request.getPoste())
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .verified(false)
                .build();
            default -> throw new BusinessException("Rôle non reconnu: " + request.getRole());
        };
    }

    private Map<String, Object> mapToUserDetails(Utilisateur utilisateur) {
        Map<String, Object> details = new HashMap<>();
        details.put("username", utilisateur.getEmail());
        details.put("userId", utilisateur.getId());
        details.put("role", utilisateur.getRole().name());
        return details;
    }

    private TokenResponseDTO.UserInfo mapToUserInfo(Utilisateur utilisateur) {
        return TokenResponseDTO.UserInfo.builder()
            .id(utilisateur.getId())
            .email(utilisateur.getEmail())
            .nom(utilisateur.getNom())
            .prenom(utilisateur.getPrenom())
            .role(utilisateur.getRole().name())
            .emailVerifie(utilisateur.getEmailVerifie())
            .build();
    }
}
