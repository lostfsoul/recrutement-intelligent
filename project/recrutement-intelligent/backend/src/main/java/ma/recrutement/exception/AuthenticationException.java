package ma.recrutement.exception;

/**
 * Exception levée lors d'une erreur d'authentification.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_ERROR", message, cause);
    }

    public AuthenticationException(String code, String message) {
        super(code, message);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("INVALID_CREDENTIALS", "Email ou mot de passe incorrect");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("INVALID_TOKEN", "Token invalide ou expiré");
    }

    public static AuthenticationException accessDenied() {
        return new AuthenticationException("ACCESS_DENIED", "Accès refusé");
    }

    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("ACCOUNT_DISABLED", "Compte désactivé");
    }

    public static AuthenticationException emailNotVerified() {
        return new AuthenticationException("EMAIL_NOT_VERIFIED", "Email non vérifié");
    }
}
