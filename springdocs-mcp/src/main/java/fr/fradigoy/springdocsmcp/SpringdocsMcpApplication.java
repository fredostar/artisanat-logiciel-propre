package fr.fradigoy.springdocsmcp.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.fradigoy.springdocsmcp.model.SpringDocumentation;
import fr.fradigoy.springdocsmcp.service.SpringDocumentationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serveur MCP pour la documentation Spring Boot.
 * Supporte les transports stdio et HTTP/SSE.
 */
@SpringBootApplication
public class SpringdocsMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringdocsMcpApplication.class, args);
    }

    private final SpringDocumentationService documentationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public SpringdocsMcpApplication(SpringDocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @Bean
    public CommandLineStdioRunner stdioRunner() {
        return new CommandLineStdioRunner(this);
    }

    // ============ MCP Server Implementation ============

    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @RestController
    @RequestMapping("/mcp")
    public static class McpController {

        private static SpringDocumentationService service;
        private static final ObjectMapper mapper = new ObjectMapper();

        public McpController(SpringDocumentationService service) {
            McpController.service = service;
        }

        @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter handleSse() {
            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
            String id = String.valueOf(System.currentTimeMillis());
            emitters.put(id, emitter);

            emitter.onCompletion(() -> emitters.remove(id));
            emitter.onTimeout(() -> emitters.remove(id));
            emitter.onError(e -> emitters.remove(id));

            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"type\":\"connection\",\"status\":\"ready\"}"));
            } catch (Exception e) {
                emitters.remove(id);
            }

            return emitter;
        }

        @PostMapping("/message")
        public JsonNode handleMessage(@RequestBody JsonNode request) {
            String method = request.path("method").asText();
            JsonNode params = request.path("params");
            JsonNode id = request.path("id");

            JsonNode result = switch (method) {
                case "initialize" -> handleInitialize();
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolsCall(params);
                default -> null;
            };

            if (result != null) {
                ObjectNode response = mapper.createObjectNode();
                response.put("jsonrpc", "2.0");
                response.set("result", result);
                if (!id.isMissingNode()) {
                    response.set("id", id);
                }
                return response;
            }
            return null;
        }

        private JsonNode handleInitialize() {
            ObjectNode result = mapper.createObjectNode();
            result.put("protocolVersion", "2024-11-05");
            ObjectNode capabilities = mapper.createObjectNode();
            ObjectNode tools = mapper.createObjectNode();
            tools.put("listChanged", true);
            capabilities.set("tools", tools);
            result.set("capabilities", capabilities);
            ObjectNode serverInfo = mapper.createObjectNode();
            serverInfo.put("name", "springdocs-mcp");
            serverInfo.put("version", "0.1.0");
            result.set("serverInfo", serverInfo);
            return result;
        }

        private JsonNode handleToolsList() {
            ObjectNode result = mapper.createObjectNode();
            ArrayNode toolsArray = mapper.createArrayNode();
            
            for (String tool : List.of("getAllDocumentation", "getDocumentationByCategory", 
                    "searchDocumentation", "getCategories")) {
                ObjectNode toolNode = mapper.createObjectNode();
                toolNode.put("name", tool);
                ObjectNode inputSchema = mapper.createObjectNode();
                inputSchema.put("type", "object");
                ObjectNode properties = mapper.createObjectNode();
                
                if (tool.equals("getDocumentationByCategory")) {
                    ObjectNode categoryParam = mapper.createObjectNode();
                    categoryParam.put("type", "string");
                    categoryParam.put("description", "Catégorie (Spring Boot, Spring Security, etc.)");
                    properties.set("category", categoryParam);
                } else if (tool.equals("searchDocumentation")) {
                    ObjectNode keywordsParam = mapper.createObjectNode();
                    keywordsParam.put("type", "string");
                    keywordsParam.put("description", "Mots-clés séparés par des virgules");
                    properties.set("keywords", keywordsParam);
                }
                
                inputSchema.set("properties", properties);
                toolNode.set("inputSchema", inputSchema);
                toolsArray.add(toolNode);
            }
            
            result.set("tools", toolsArray);
            return result;
        }

        private JsonNode handleToolsCall(JsonNode params) {
            String toolName = params.path("name").asText();
            JsonNode arguments = params.path("arguments");

            ObjectNode result = mapper.createObjectNode();
            ArrayNode contentArray = mapper.createArrayNode();

            switch (toolName) {
                case "getAllDocumentation" -> 
                    addTextContent(contentArray, formatDocs(service.getAllDocumentation()));
                case "getDocumentationByCategory" -> {
                    String category = arguments.path("category").asText("");
                    addTextContent(contentArray, formatDocs(service.getDocumentationByCategory(category)));
                }
                case "searchDocumentation" -> {
                    String keywords = arguments.path("keywords").asText("");
                    List<String> keywordList = List.of(keywords.split(","));
                    addTextContent(contentArray, formatDocs(service.searchByKeywords(keywordList)));
                }
                case "getCategories" -> 
                    addTextContent(contentArray, "Catégories disponibles:\n- " + 
                        String.join("\n- ", service.getCategories()));
            }

            result.set("content", contentArray);
            return result;
        }

        private void addTextContent(ArrayNode content, String text) {
            ObjectNode textContent = mapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", text);
            content.add(textContent);
        }

        private String formatDocs(List<SpringDocumentation> docs) {
            StringBuilder sb = new StringBuilder();
            sb.append("Documentation (").append(docs.size()).append(" ressources):\n\n");
            for (SpringDocumentation doc : docs) {
                sb.append("## ").append(doc.getTitle()).append("\n");
                sb.append("- Catégorie: ").append(doc.getCategory()).append("\n");
                sb.append("- Version: ").append(doc.getVersion()).append("\n");
                sb.append("- URL: ").append(doc.getUrl()).append("\n\n");
            }
            return sb.toString();
        }
    }

    // ============ Stdio Runner ============

    public static class CommandLineStdioRunner implements Runnable {
        private final SpringdocsMcpApplication app;

        public CommandLineStdioRunner(SpringdocsMcpApplication app) {
            this.app = app;
        }

        @Override
        public void run() {
            try {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                     PrintWriter writer = new PrintWriter(System.out)) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            JsonNode request = app.mapper.readTree(line);
                            JsonNode response = app.handleRequest(request);
                            if (response != null) {
                                writer.println(response.toString());
                                writer.flush();
                            }
                        } catch (Exception e) {
                            writer.println("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32700,\"message\":\"" + 
                                e.getMessage().replace("\"", "\\") + "\"}}");
                            writer.flush();
                        }
                    }
                }
            } catch (IOException e) {
                // Server stdin closed, exiting
            }
        }
    }

    private JsonNode handleRequest(JsonNode request) {
        String jsonrpc = request.path("jsonrpc").asText();
        if (!"2.0".equals(jsonrpc)) {
            return createErrorResponse(null, -32600, "Invalid JSON-RPC version");
        }

        String method = request.path("method").asText();
        JsonNode params = request.path("params");
        JsonNode id = request.path("id");

        JsonNode result = switch (method) {
            case "initialize" -> handleInitialize();
            case "tools/list" -> handleToolsList();
            case "tools/call" -> handleToolsCall(params);
            case "notifications/initialized" -> null;
            default -> null;
        };

        if (result != null) {
            ObjectNode response = mapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.set("result", result);
            if (!id.isMissingNode()) {
                response.set("id", id);
            }
            return response;
        }
        return null;
    }

    private JsonNode handleInitialize() {
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        ObjectNode capabilities = mapper.createObjectNode();
        ObjectNode tools = mapper.createObjectNode();
        tools.put("listChanged", true);
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        ObjectNode serverInfo = mapper.createObjectNode();
        serverInfo.put("name", "springdocs-mcp");
        serverInfo.put("version", "0.1.0");
        result.set("serverInfo", serverInfo);
        return result;
    }

    private JsonNode handleToolsList() {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode toolsArray = mapper.createArrayNode();
        
        for (String toolName : List.of("getAllDocumentation", "getDocumentationByCategory", 
                "searchDocumentation", "getCategories")) {
            ObjectNode tool = mapper.createObjectNode();
            tool.put("name", toolName);
            ObjectNode inputSchema = mapper.createObjectNode();
            inputSchema.put("type", "object");
            ObjectNode properties = mapper.createObjectNode();
            if (toolName.equals("getDocumentationByCategory")) {
                ObjectNode p = mapper.createObjectNode();
                p.put("type", "string");
                p.put("description", "Catégorie");
                properties.set("category", p);
            } else if (toolName.equals("searchDocumentation")) {
                ObjectNode p = mapper.createObjectNode();
                p.put("type", "string");
                p.put("description", "Mots-clés");
                properties.set("keywords", p);
            }
            inputSchema.set("properties", properties);
            tool.set("inputSchema", inputSchema);
            toolsArray.add(tool);
        }
        
        result.set("tools", toolsArray);
        return result;
    }

    private JsonNode handleToolsCall(JsonNode params) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        ObjectNode result = mapper.createObjectNode();
        ArrayNode content = mapper.createArrayNode();

        switch (toolName) {
            case "getAllDocumentation" -> 
                addText(content, formatDocs(documentationService.getAllDocumentation()));
            case "getDocumentationByCategory" -> 
                addText(content, formatDocs(documentationService.getDocumentationByCategory(
                    arguments.path("category").asText(""))));
            case "searchDocumentation" -> {
                String k = arguments.path("keywords").asText("");
                addText(content, formatDocs(documentationService.searchByKeywords(List.of(k.split(",")))));
            }
            case "getCategories" -> 
                addText(content, "Catégories: " + String.join(", ", documentationService.getCategories()));
        }

        result.set("content", content);
        return result;
    }

    private void addText(ArrayNode content, String text) {
        ObjectNode tc = mapper.createObjectNode();
        tc.put("type", "text");
        tc.put("text", text);
        content.add(tc);
    }

    private String formatDocs(List<SpringDocumentation> docs) {
        StringBuilder sb = new StringBuilder();
        for (SpringDocumentation doc : docs) {
            sb.append(doc.getTitle()).append(" (").append(doc.getCategory()).append("): ")
              .append(doc.getUrl()).append("\n");
        }
        return sb.toString();
    }

    private JsonNode createErrorResponse(JsonNode id, int code, String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        ObjectNode error = mapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);
        if (id != null && !id.isMissingNode()) {
            response.set("id", id);
        }
        return response;
    }
}
