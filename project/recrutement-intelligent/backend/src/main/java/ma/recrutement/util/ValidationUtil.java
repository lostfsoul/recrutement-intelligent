package ma.recrutement.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Utilitaires pour la validation des données de l'application.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    );

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Valide un email.
     *
     * @param email l'email à valider
     * @return true si l'email est valide
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valide un numéro de téléphone.
     *
     * @param phone le numéro de téléphone à valider
     * @return true si le numéro est valide
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }

    /**
     * Valide une URL.
     *
     * @param url l'URL à valider
     * @return true si l'URL est valide
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Valide la force d'un mot de passe.
     *
     * @param password le mot de passe à valider
     * @return true si le mot de passe est assez fort
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecialChar = true;
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    /**
     * Valide qu'un texte n'est pas vide.
     *
     * @param text le texte à valider
     * @return true si le texte n'est pas vide
     */
    public boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Valide qu'une chaîne a une longueur minimum.
     *
     * @param text la chaîne à valider
     * @param minLength la longueur minimum
     * @return true si la chaîne respecte la longueur minimum
     */
    public boolean hasMinLength(String text, int minLength) {
        return text != null && text.length() >= minLength;
    }

    /**
     * Valide qu'une chaîne a une longueur maximum.
     *
     * @param text la chaîne à valider
     * @param maxLength la longueur maximum
     * @return true si la chaîne respecte la longueur maximum
     */
    public boolean hasMaxLength(String text, int maxLength) {
        return text == null || text.length() <= maxLength;
    }

    /**
     * Valide qu'une valeur est dans une plage.
     *
     * @param value la valeur à valider
     * @param min la valeur minimum
     * @param max la valeur maximum
     * @return true si la valeur est dans la plage
     */
    public boolean isInRange(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    /**
     * Valide une date de naissance (doit être dans le passé et l'âge raisonnable).
     *
     * @param dateOfBirth la date de naissance
     * @return true si la date est valide
     */
    public boolean isValidDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        if (dateOfBirth.isAfter(now)) {
            return false;
        }

        int age = Period.between(dateOfBirth, now).getYears();
        return age >= 16 && age <= 100;
    }

    /**
     * Valide qu'une date est dans le futur.
     *
     * @param date la date à valider
     * @return true si la date est dans le futur
     */
    public boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Valide qu'une plage de salaire est cohérente.
     *
     * @param min salaire minimum
     * @param max salaire maximum
     * @return true si la plage est valide
     */
    public boolean isValidSalaryRange(Integer min, Integer max) {
        if (min == null && max == null) {
            return true;
        }

        if (min != null && max == null) {
            return min > 0;
        }

        if (min == null) {
            return max > 0;
        }

        return min > 0 && max > min;
    }

    /**
     * Valide un code postal marocain.
     *
     * @param postalCode le code postal à valider
     * @return true si le code postal est valide
     */
    public boolean isValidMoroccanPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return false;
        }
        return postalCode.matches("^\\d{5}$");
    }

    /**
     * Valide un numéro de téléphone marocain.
     *
     * @param phone le numéro de téléphone à valider
     * @return true si le numéro est valide
     */
    public boolean isValidMoroccanPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String normalized = phone.replaceAll("[\\s+]", "");
        return normalized.matches("^(0[5-9]|\\+212[5-9])\\d{8}$");
    }

    /**
     * Nettoie et normalise un numéro de téléphone.
     *
     * @param phone le numéro de téléphone à nettoyer
     * @return le numéro normalisé
     */
    public String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("\\s+", "");
    }

    /**
     * Valide un nom ou prénom (lettres, espaces, tirets autorisés).
     *
     * @param name le nom à valider
     * @return true si le nom est valide
     */
    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.matches("^[a-zA-Z\\s'-]{2,50}$");
    }

    /**
     * Valide qu'un entier est positif.
     *
     * @param value la valeur à valider
     * @return true si la valeur est positive
     */
    public boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    /**
     * Valide qu'un entier est positif ou nul.
     *
     * @param value la valeur à valider
     * @return true si la valeur est positive ou nulle
     */
    public boolean isPositiveOrZero(Integer value) {
        return value != null && value >= 0;
    }

    /**
     * Valide un pourcentage (entre 0 et 100).
     *
     * @param value la valeur à valider
     * @return true si la valeur est un pourcentage valide
     */
    public boolean isValidPercentage(Integer value) {
        return value != null && value >= 0 && value <= 100;
    }
}
