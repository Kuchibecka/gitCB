package src.application.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    private String name;

    @JsonProperty("http_url_to_repo")
    private String repoUrl;


    public Repository(String name, String repoUrl) {
        this.name = name;
        this.repoUrl = repoUrl;
    }

    public Repository() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                '}';
    }
}
