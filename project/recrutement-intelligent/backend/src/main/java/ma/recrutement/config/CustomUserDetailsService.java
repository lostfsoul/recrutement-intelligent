package ma.recrutement.config;

import lombok.RequiredArgsConstructor;
import ma.recrutement.entity.Utilisateur;
import ma.recrutement.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service personnalisé pour charger les détails utilisateur pour Spring Security.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));

        return User.builder()
            .username(utilisateur.getEmail())
            .password(utilisateur.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())))
            .accountLocked(utilisateur.getStatut() == Utilisateur.StatutUtilisateur.SUSPENDU)
            .disabled(utilisateur.getStatut() == Utilisateur.StatutUtilisateur.INACTIF)
            .build();
    }
}
