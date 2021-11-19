package src.application;

import lombok.*;
import org.json.JSONPropertyIgnore;

@Data
public class Repository {
    private String name;
    private String description;

    public Repository(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
