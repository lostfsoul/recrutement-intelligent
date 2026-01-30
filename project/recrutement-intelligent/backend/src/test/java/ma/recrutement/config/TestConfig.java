package ma.recrutement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test configuration class.
 * Provides test-specific beans and configurations.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Password encoder for tests.
     * Uses a lower strength for faster test execution.
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(4); // Lower strength for faster tests
    }
}
