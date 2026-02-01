package ma.recrutement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration de OpenAPI/Swagger pour la documentation de l'API.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.application.name:Recrutement API}")
    private String applicationName;

    /**
     * Configure le bean OpenAPI avec toutes les informations de documentation.
     *
     * @return la configuration OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Plateforme de Recrutement Intelligente - API")
                .description("""
                    API REST pour la plateforme de recrutement intelligente utilisant Spring Boot et Spring AI.

                    ## Fonctionnalités principales
                    * Gestion des utilisateurs (Candidats, Recruteurs)
                    * Gestion des offres d'emploi
                    * Gestion des candidatures
                    * Matching intelligent CV/Offres avec IA
                    * Extraction et parsing automatique de CV
                    * Recommandations basées sur les compétences

                    ## Authentification
                    L'API utilise l'authentification JWT. Pour accéder aux endpoints sécurisés, incluez le token dans le header Authorization:
                    `Authorization: Bearer <votre-token>`

                    ## Rôles
                    * **CANDIDAT**: Peut créer un profil, uploader un CV, postuler aux offres
                    * **RECRUTEUR**: Peut créer des entreprises, publier des offres, gérer les candidatures
                    * **ADMINISTRATEUR**: Peut valider les comptes, modérer le contenu, accéder aux statistiques
                    """)
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Équipe Recrutement")
                    .email("contact@recrutement.ma")
                    .url("https://recrutement.ma"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Serveur de développement"),
                new Server()
                    .url("https://api.recrutement.ma")
                    .description("Serveur de production")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token JWT d'authentification. Obtenu via /api/v1/auth/login")));
    }
}
