package ma.recrutement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilitaires pour la gestion des tokens JWT.
 * Complémente JwtConfig avec des méthodes utilitaires supplémentaires.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * Extrait le username du token.
     *
     * @param token le token JWT
     * @return le username (email)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait l'ID de l'utilisateur depuis les claims.
     *
     * @param token le token JWT
     * @return l'ID de l'utilisateur
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extrait le rôle de l'utilisateur depuis les claims.
     *
     * @param token le token JWT
     * @return le rôle de l'utilisateur
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extrait une claim spécifique du token.
     *
     * @param token le token JWT
     * @param claimsResolver fonction pour extraire la claim
     * @return la claim extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token pour un utilisateur.
     *
     * @param userDetails les détails de l'utilisateur
     * @return le token JWT
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token avec des claims additionnelles.
     *
     * @param extraClaims les claims additionnelles (userId, role, etc.)
     * @param userDetails les détails de l'utilisateur
     * @return le token JWT
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
     * Construit un token avec les paramètres spécifiés.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, Long expiration) {
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
     * Vérifie si le token est valide.
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
     * Vérifie si le token est expiré.
     *
     * @param token le token JWT
     * @return true si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token.
     *
     * @param token le token JWT
     * @return la date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les claims du token.
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
     * Obtient la clé de signature.
     *
     * @return la clé de signature
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Calcule le temps restant avant expiration du token.
     *
     * @param token le token JWT
     * @return le temps restant en secondes
     */
    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    /**
     * Vérifie si le token doit être rafraîchi (moins de 30 minutes restantes).
     *
     * @param token le token JWT
     * @return true si le token doit être rafraîchi
     */
    public boolean shouldRefresh(String token) {
        long remainingTime = getRemainingTime(token);
        return remainingTime < 1800; // 30 minutes en secondes
    }
}
