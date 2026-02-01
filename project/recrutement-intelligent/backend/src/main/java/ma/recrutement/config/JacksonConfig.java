package ma.recrutement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Jackson pour l'ObjectMapper.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * Configure l'ObjectMapper pour la sérialisation/désérialisation JSON.
     *
     * @return l'ObjectMapper configuré
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
