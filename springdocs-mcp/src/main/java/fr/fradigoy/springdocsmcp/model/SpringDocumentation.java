package fr.fradigoy.springdocsmcp.model;

import java.util.List;

/**
 * Représente une ressource de documentation Spring.
 */
public class SpringDocumentation {

    private String title;
    private String description;
    private String category;
    private String url;
    private List<String> keywords;
    private String version;

    public SpringDocumentation() {
    }

    public SpringDocumentation(String title, String description, String category, String url, List<String> keywords, String version) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.url = url;
        this.keywords = keywords;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
