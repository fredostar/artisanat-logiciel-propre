# Springdocs MCP Server

Un serveur MCP (Model Context Protocol) conçu pour mettre à disposition l'univers vaste de Spring Boot et de l'écosystème Spring directement sur votre client MCP préféré.

## Fonctionnalités

- **Recherche de documentation** : Accédez à la documentation officielle de Spring Boot, Spring Framework, Spring Data, Spring Security, Spring Cloud, Spring Integration et Spring Batch
- **Recherche par catégorie** : Filtrez la documentation par domaine (Spring Boot, Spring Security, etc.)
- **Recherche par mots-clés** : Trouvez rapidement les ressources pertinentes

## Outils MCP disponibles

| Outil | Description |
|-------|-------------|
| `getAllDocumentation` | Récupère toute la documentation Spring disponible |
| `getDocumentationByCategory` | Filtre par catégorie (paramètre: `category`) |
| `searchDocumentation` | Recherche par mots-clés (paramètre: `keywords`) |
| `getCategories` | Liste les catégories disponibles |

## Installation

```bash
./mvnw clean package -DskipTests
```

## Utilisation avec IntelliJ IDEA + GitHub Copilot

### Option 1: Transport HTTP (Recommandé pour IntelliJ)

Le serveur expose un endpoint HTTP pour MCP :

1. Lancez le serveur :
```bash
java -jar target/springdocs-mcp-0.0.1-SNAPSHOT.jar
```

2. Le serveur expose :
   - **SSE**: `http://localhost:8080/mcp/sse` (pour les connexions entrantes)
   - **Message**: `http://localhost:8080/mcp/message` (pour les requêtes POST)

3. Dans IntelliJ IDEA :
   - Allez dans **Settings > Tools > Developer Tools > HTTP Client**
   - Ou utilisez le plugin "MCP Client" pour IntelliJ si disponible

### Option 2: Stdio (pour Claude Desktop)

Pour utiliser avec Claude Desktop, créez un script de lancement :

```bash
#!/bin/bash
java -jar target/springdocs-mcp-0.0.1-SNAPSHOT.jar
```

Configurez Claude Desktop pour utiliser ce script.

## Configuration du serveur

Dans `application.yaml` :

```yaml
server:
  port: 8080

spring-docs-mcp:
  server:
    name: spring    version: 0.1.0
```

## Structure du projet

```
springdocs-mdocs-mcp
cp/
├── src/main/java/fr/fradigoy/springdocsmcp/
│   ├── SpringdocsMcpApplication.java       # Application principale avec contrôleur REST
│   ├── model/
│   │   └── SpringDocumentation.java        # Modèle de données
│   └── service/
│       └── SpringDocumentationService.java # Service avec 11 documentations Spring
```

## Dépendances

- Spring Boot 4.0.2
- Spring Boot Starter Web (REST + SSE)
- Jackson pour JSON

## Licence

MIT
