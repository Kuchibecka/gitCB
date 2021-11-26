package src.application.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    @JsonProperty("name")
    private String repoName;

    @JsonProperty("http_url_to_repo")
    private String repoUrl;

    public Repository(String repoName, String repoUrl) {
        this.repoName = repoName;
        this.repoUrl = repoUrl;
    }

    public Repository() {
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
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
                "repoName='" + repoName + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                '}';
    }
}
