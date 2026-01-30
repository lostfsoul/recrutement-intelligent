package ma.recrutement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration JPA/Hibernate.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "ma.recrutement.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // La configuration JPA est principalement gérée via application.properties
    // Cette classe active les fonctionnalités avancées de Spring Data JPA
}
