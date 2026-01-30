package ma.recrutement.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import ma.recrutement.entity.*;
import ma.recrutement.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.Key;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for all controller tests.
 * Provides common setup, teardown, and utility methods for testing REST endpoints.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Slf4j
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ma.recrutement.RecrutementApplication.class
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
public abstract class BaseControllerTest {

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
        "postgres:16-alpine"
    )
        .withDatabaseName("recrutement_test")
        .withUsername("test")
        .withPassword("test")
        .withExposedPorts(5432);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UtilisateurRepository utilisateurRepository;

    @Autowired
    protected CandidatRepository candidatRepository;

    @Autowired
    protected RecruteurRepository recruteurRepository;

    @Autowired
    protected EntrepriseRepository entrepriseRepository;

    @Autowired
    protected OffreEmploiRepository offreEmploiRepository;

    @Autowired
    protected CandidatureRepository candidatureRepository;

    @Autowired
    protected AdministrateurRepository administrateurRepository;

    @MockBean
    protected UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    protected String jwtSecret;

    @Value("${jwt.expiration}")
    protected Long jwtExpiration;

    // Test data holders
    protected Candidat testCandidat;
    protected Recruteur testRecruteur;
    protected Entreprise testEntreprise;
    protected OffreEmploi testOffre;
    protected String candidatToken;
    protected String recruteurToken;
    protected String adminToken;

    /**
     * Static initialization block for TestContainers.
     */
    static {
        postgresContainer.start();
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @BeforeEach
    void setUpBase() {
        log.info("Setting up test environment");
        cleanDatabase();
        createTestData();
    }

    @AfterEach
    void tearDownBase() {
        log.info("Tearing down test environment");
        SecurityContextHolder.clearContext();
    }

    /**
     * Cleans the database before each test.
     */
    protected void cleanDatabase() {
        candidatureRepository.deleteAll();
        offreEmploiRepository.deleteAll();
        entrepriseRepository.deleteAll();
        candidatRepository.deleteAll();
        recruteurRepository.deleteAll();
        utilisateurRepository.deleteAll();
    }

    /**
     * Creates test data for use in tests.
     */
    protected void createTestData() {
        // Create test candidate
        testCandidat = createTestCandidat("candidat@test.com", "Candidat", "Test");
        testCandidat = candidatRepository.save(testCandidat);

        // Create test recruiter
        testRecruteur = createTestRecruteur("recruteur@test.com", "Recruteur", "Test");
        testRecruteur = recruteurRepository.save(testRecruteur);

        // Create test company
        testEntreprise = createTestEntreprise("Test Company", "Description");
        testEntreprise.setRecruteur(testRecruteur);
        testEntreprise = entrepriseRepository.save(testEntreprise);

        // Create test job offer
        testOffre = createTestOffre("Test Job Offer", "Description");
        testOffre.setRecruteur(testRecruteur);
        testOffre.setEntreprise(testEntreprise);
        testOffre = offreEmploiRepository.save(testOffre);

        // Generate tokens
        candidatToken = generateTokenForUser(testCandidat.getEmail(), "CANDIDAT");
        recruteurToken = generateTokenForUser(testRecruteur.getEmail(), "RECRUTEUR");
        adminToken = generateTokenForUser("admin@test.com", "ADMINISTRATEUR");

        log.info("Test data created - Candidat ID: {}, Recruteur ID: {}, Entreprise ID: {}, Offre ID: {}",
            testCandidat.getId(), testRecruteur.getId(), testEntreprise.getId(), testOffre.getId());
    }

    /**
     * Creates a test candidate entity.
     */
    protected Candidat createTestCandidat(String email, String nom, String prenom) {
        return Candidat.builder()
            .email(email)
            .password("$2a$10$encodedPassword")
            .nom(nom)
            .prenom(prenom)
            .telephone("0612345678")
            .statut(Utilisateur.StatutUtilisateur.ACTIF)
            .emailVerifie(true)
            .cvPath(null)
            .titrePosteRecherche(null)
            .disponibiliteImmediate(true)
            .competences(new ArrayList<>())
            .experiences(new ArrayList<>())
            .candidatures(new ArrayList<>())
            .build();
    }

    /**
     * Creates a test recruiter entity.
     */
    protected Recruteur createTestRecruteur(String email, String nom, String prenom) {
        return Recruteur.builder()
            .email(email)
            .password("$2a$10$encodedPassword")
            .nom(nom)
            .prenom(prenom)
            .telephone("0687654321")
            .nomEntreprise("Test Company")
            .poste("RH Manager")
            .statut(Utilisateur.StatutUtilisateur.ACTIF)
            .verified(false)
            .entreprises(new ArrayList<>())
            .offres(new ArrayList<>())
            .build();
    }

    /**
     * Creates a test company entity.
     */
    protected Entreprise createTestEntreprise(String nom, String description) {
        return Entreprise.builder()
            .nom(nom)
            .description(description)
            .secteur("Technology")
            .tailleEntreprise("50-250")
            .siteWeb("https://testcompany.com")
            .logoPath(null)
            .statutValidation(Entreprise.StatutValidation.EN_ATTENTE)
            .localisation("Paris")
            .offres(new ArrayList<>())
            .build();
    }

    /**
     * Creates a test job offer entity.
     */
    protected OffreEmploi createTestOffre(String titre, String description) {
        return OffreEmploi.builder()
            .titre(titre)
            .description(description)
            .typeContrat(OffreEmploi.TypeContrat.CDI)
            .teletravail(false)
            .salaireMin(40000)
            .salaireMax(60000)
            .experienceMinAnnees(3)
            .statut(OffreEmploi.StatutOffre.BROUILLON)
            .competencesRequises("Java, Spring Boot")
            .localisation("Paris")
            .candidatures(new ArrayList<>())
            .build();
    }

    /**
     * Creates a test administrator entity.
     */
    protected Administrateur createTestAdmin() {
        return Administrateur.builder()
            .email("admin@test.com")
            .password("$2a$10$encodedPassword")
            .nom("Admin")
            .prenom("Super")
            .statut(Utilisateur.StatutUtilisateur.ACTIF)
            .build();
    }

    /**
     * Generates a JWT token for a user.
     */
    protected String generateTokenForUser(String email, String role) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", 1L);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Sets up the security context for a user.
     */
    protected void setupSecurityContext(String email, String role) {
        User user = new User(
            email,
            "",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );

        when(userDetailsService.loadUserByUsername(email))
            .thenReturn(user);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Performs a GET request with authorization.
     */
    protected ResultActions performGet(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Performs a POST request with authorization and body.
     */
    protected ResultActions performPost(String url, String token, String body) throws Exception {
        return mockMvc.perform(post(url)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    /**
     * Performs a PUT request with authorization and body.
     */
    protected ResultActions performPut(String url, String token, String body) throws Exception {
        return mockMvc.perform(put(url)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    /**
     * Performs a DELETE request with authorization.
     */
    protected ResultActions performDelete(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON));
    }
}
