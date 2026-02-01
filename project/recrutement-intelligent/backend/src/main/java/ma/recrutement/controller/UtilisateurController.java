package ma.recrutement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.recrutement.dto.LoginRequestDTO;
import ma.recrutement.dto.RegisterRequestDTO;
import ma.recrutement.dto.TokenResponseDTO;
import ma.recrutement.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'authentification et la gestion des utilisateurs.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Tag(name = "Authentification", description = "API d'authentification et d'inscription")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * Inscription d'un nouvel utilisateur.
     *
     * @param request les données d'inscription
     * @return la réponse avec le token
     */
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur")
    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        TokenResponseDTO response = utilisateurService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un utilisateur.
     *
     * @param request les données de connexion
     * @return la réponse avec le token
     */
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        TokenResponseDTO response = utilisateurService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchit le token JWT.
     *
     * @param refreshToken le token de rafraîchissement
     * @return la nouvelle réponse avec token
     */
    @Operation(summary = "Rafraîchir le token", description = "Génère un nouveau token à partir du token de rafraîchissement")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestBody String refreshToken) {
        TokenResponseDTO response = utilisateurService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

}
