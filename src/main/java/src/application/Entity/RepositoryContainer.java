package src.application.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryContainer {
    private String token;
    private HashMap<String, Repository> repoMap = new HashMap<>();

    public RepositoryContainer(String token, HashMap<String, Repository> repoMap) {
        this.token = token;
        this.repoMap = repoMap;
    }

    public RepositoryContainer() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HashMap<String, Repository> getRepoMap() {
        return repoMap;
    }

    public void setRepoMap(HashMap<String, Repository> repoMap) {
        this.repoMap = repoMap;
    }

    public void addRepoMap(String name, Repository repo) {
        this.repoMap.put(name, repo);
    }
}
