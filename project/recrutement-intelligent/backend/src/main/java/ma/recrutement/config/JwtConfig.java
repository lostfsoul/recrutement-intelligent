package ma.recrutement.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration et utilitaires pour la gestion des tokens JWT.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private Long refreshExpiration;

    /**
     * Extrait le username (email) du token JWT.
     *
     * @param token le token JWT
     * @return le username extrait
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une claim spécifique du token JWT.
     *
     * @param token le token JWT
     * @param claimsResolver la fonction pour extraire la claim
     * @return la claim extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token JWT pour un utilisateur.
     *
     * @param userDetails les détails de l'utilisateur
     * @return le token JWT généré
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token JWT pour un email (username).
     *
     * @param email l'email de l'utilisateur
     * @return le token JWT généré
     */
    public String generateToken(String email) {
        return generateToken(new HashMap<>(), email);
    }

    /**
     * Génère un token JWT pour un email avec des claims additionnelles.
     *
     * @param extraClaims les claims additionnelles
     * @param email l'email de l'utilisateur
     * @return le token JWT généré
     */
    public String generateToken(Map<String, Object> extraClaims, String email) {
        return buildTokenWithEmail(extraClaims, email, jwtExpiration);
    }

    /**
     * Génère un token JWT avec des claims additionnelles.
     *
     * @param extraClaims les claims additionnelles
     * @param userDetails les détails de l'utilisateur
     * @return le token JWT généré
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Génère un token de rafraîchissement.
     *
     * @param userDetails les détails de l'utilisateur
     * @return le token de rafraîchissement
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Génère un token de rafraîchissement pour un email.
     *
     * @param email l'email de l'utilisateur
     * @return le token de rafraîchissement
     */
    public String generateRefreshToken(String email) {
        return buildTokenWithEmail(new HashMap<>(), email, refreshExpiration);
    }

    /**
     * Construit un token JWT avec les paramètres spécifiés.
     *
     * @param extraClaims les claims additionnelles
     * @param userDetails les détails de l'utilisateur
     * @param expiration le délai d'expiration en millisecondes
     * @return le token JWT construit
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Long expiration
    ) {
        return Jwts
            .builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
    }

    /**
     * Construit un token JWT avec email.
     *
     * @param extraClaims les claims additionnelles
     * @param email l'email de l'utilisateur
     * @param expiration le délai d'expiration en millisecondes
     * @return le token JWT construit
     */
    private String buildTokenWithEmail(
            Map<String, Object> extraClaims,
            String email,
            Long expiration
    ) {
        return Jwts
            .builder()
            .claims(extraClaims)
            .subject(email)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
    }

    /**
     * Vérifie si un token JWT est valide pour un utilisateur.
     *
     * @param token le token JWT
     * @param userDetails les détails de l'utilisateur
     * @return true si le token est valide
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Vérifie si un token JWT est expiré.
     *
     * @param token le token JWT
     * @return true si le token est expiré
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token le token JWT
     * @return la date d'expiration
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les claims du token JWT.
     *
     * @param token le token JWT
     * @return toutes les claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts
            .parser()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Obtient la clé de signature pour les tokens JWT.
     *
     * @return la clé de signature
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Calcule le temps restant avant expiration du token en secondes.
     *
     * @param token le token JWT
     * @return le temps restant en secondes, ou 0 si expiré
     */
    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}
