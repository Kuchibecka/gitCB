package src.application.Entity;

import java.util.HashMap;

public class RepositoryContainer {
    private String token;
    private HashMap<String, Repository> repoMap = new HashMap<>();
    private String requestUrl;

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

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void addRepoMap(String name, Repository repo) {
        this.repoMap.put(name, repo);
    }

    @Override
    public String toString() {
        return "RepositoryContainer{" +
                "token='" + token + "', " +
                "requestUrl='" + requestUrl + "', " +
                "repoMap=" + repoMap.toString() +
                '}';
    }
}
