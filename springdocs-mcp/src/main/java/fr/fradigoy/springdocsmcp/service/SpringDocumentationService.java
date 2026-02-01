package fr.fradigoy.springdocsmcp.service;

import fr.fradigoy.springdocsmcp.model.SpringDocumentation;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service fournissant l'accès à la documentation Spring.
 */
@Service
public class SpringDocumentationService {

    private static final List<SpringDocumentation> DOCUMENTATIONS = Arrays.asList(
            // Spring Boot
            new SpringDocumentation(
                    "Spring Boot Reference Documentation",
                    "Documentation officielle de Spring Boot couvrant l'auto-configuration, les starters, et les outils de développement.",
                    "Spring Boot",
                    "https://docs.spring.io/spring-boot/reference/index.html",
                    Arrays.asList("spring-boot", "auto-configuration", "starters", "actuator"),
                    "3.4.x"
            ),
            new SpringDocumentation(
                    "Spring Boot API Documentation",
                    "API reference pour Spring Boot avec tous les modules et annotations.",
                    "Spring Boot",
                    "https://docs.spring.io/spring-boot/docs/current/api/",
                    Arrays.asList("spring-boot", "api", "annotations", "classes"),
                    "3.4.x"
            ),
            // Spring Framework
            new SpringDocumentation(
                    "Spring Framework Reference Documentation",
                    "Documentation complète du framework Spring core, beans, MVC, et integration.",
                    "Spring Framework",
                    "https://docs.spring.io/spring-framework/reference/index.html",
                    Arrays.asList("spring", "ioc", "aop", "mvc", "transactions"),
                    "6.2.x"
            ),
            new SpringDocumentation(
                    "Spring Framework API Documentation",
                    "API reference pour Spring Framework.",
                    "Spring Framework",
                    "https://docs.spring.io/spring-framework/docs/current/api/",
                    Arrays.asList("spring", "api", "core", "beans"),
                    "6.2.x"
            ),
            // Spring Data
            new SpringDocumentation(
                    "Spring Data JPA Reference",
                    "Documentation pour Spring Data JPA avec repositories et queries.",
                    "Spring Data",
                    "https://docs.spring.io/spring-data/jpa/reference/index.html",
                    Arrays.asList("spring-data", "jpa", "repository", "query"),
                    "3.4.x"
            ),
            new SpringDocumentation(
                    "Spring Data MongoDB Reference",
                    "Documentation pour Spring Data MongoDB.",
                    "Spring Data",
                    "https://docs.spring.io/spring-data/mongodb/reference/index.html",
                    Arrays.asList("spring-data", "mongodb", "repository"),
                    "4.5.x"
            ),
            // Spring Security
            new SpringDocumentation(
                    "Spring Security Reference",
                    "Documentation complète de Spring Security pour l'authentification et l'autorisation.",
                    "Spring Security",
                    "https://docs.spring.io/spring-security/reference/index.html",
                    Arrays.asList("spring-security", "authentication", "authorization", "oauth2"),
                    "6.4.x"
            ),
            // Spring Cloud
            new SpringDocumentation(
                    "Spring Cloud Reference",
                    "Documentation pour les patterns de microservices avec Spring Cloud.",
                    "Spring Cloud",
                    "https://docs.spring.io/spring-cloud/reference/index.html",
                    Arrays.asList("spring-cloud", "microservices", "config", "discovery", "gateway"),
                    "2024.0.x"
            ),
            new SpringDocumentation(
                    "Spring Gateway Reference",
                    "Documentation pour Spring Cloud Gateway.",
                    "Spring Cloud",
                    "https://docs.spring.io/spring-cloud-gateway/reference/index.html",
                    Arrays.asList("spring-cloud-gateway", "gateway", "routing", "filter"),
                    "4.2.x"
            ),
            // Spring Integration
            new SpringDocumentation(
                    "Spring Integration Reference",
                    "Documentation pour l'intégration d'entreprise avec Spring Integration.",
                    "Spring Integration",
                    "https://docs.spring.io/spring-integration/reference/index.html",
                    Arrays.asList("spring-integration", "eip", "messaging", "channels"),
                    "6.4.x"
            ),
            // Spring Batch
            new SpringDocumentation(
                    "Spring Batch Reference",
                    "Documentation pour le traitement par lots avec Spring Batch.",
                    "Spring Batch",
                    "https://docs.spring.io/spring-batch/reference/index.html",
                    Arrays.asList("spring-batch", "batch", "jobs", "steps", "readers", "writers"),
                    "5.2.x"
            )
    );

    /**
     * Récupère toute la documentation Spring disponible.
     */
    public List<SpringDocumentation> getAllDocumentation() {
        return DOCUMENTATIONS;
    }

    /**
     * Recherche de la documentation par catégorie.
     */
    public List<SpringDocumentation> getDocumentationByCategory(String category) {
        return DOCUMENTATIONS.stream()
                .filter(doc -> doc.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Recherche de la documentation par mots-clés.
     */
    public List<SpringDocumentation> searchByKeywords(List<String> keywords) {
        return DOCUMENTATIONS.stream()
                .filter(doc -> doc.getKeywords().stream()
                        .anyMatch(keyword -> keywords.stream()
                                .anyMatch(k -> keyword.toLowerCase().contains(k.toLowerCase()))))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les catégories disponibles.
     */
    public List<String> getCategories() {
        return DOCUMENTATIONS.stream()
                .map(SpringDocumentation::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
}
