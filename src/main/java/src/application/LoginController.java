package src.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ТОКЕН ДЛЯ АВТОРИЗАЦИИ: glpat-u_xNuwLFH-dW66uHigMz
 * ТОКЕН БЕЗ API: glpat-AGW_F2nA9uzypQVA9B6Q
 * ТЕСТОВЫЙ ПУТЬ КЛОНИРОВАНИЯ: C:\Users\Администратор\Downloads\testFolder
 */
public class LoginController implements Initializable {
    private String curProtocol;
    private RepositoryContainer repositories = new RepositoryContainer();
    private final ObservableList<String> protocols =
            FXCollections.observableArrayList("http://", "https://");

    @FXML
    private TextField tokenField;
    @FXML
    private TextField domainField;
    @FXML
    private ChoiceBox<String> protocolChoice;
    @FXML
    public Button logIn;
    @FXML
    private Label errorLabel = new Label();

    static final Logger rootLogger = LogManager.getRootLogger();
    static final Logger repoContainerLogger = LogManager.getLogger(RepositoryContainer.class);

    /**
     * Функция начальной инициализации сцены авторизации
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootLogger.info("Initialization");
        rootLogger.debug("Root Logger debug message!");
        /**/
        try {
            if (protocols.isEmpty())
                throw new Exception("Variable protocols not configured");
            protocolChoice.getItems().setAll(protocols);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            rootLogger.error(e.getMessage());
            return;
        }
        protocolChoice.setOnAction(event -> curProtocol = protocolChoice.getValue());
    }

    /**
     * Получить список репозиториев как результат API запроса
     */
/*    public HashMap<String, Repository> request(String url) {
        rootLogger.info("Calling request(" + url + ")");
        try {
            HashMap<String, Repository> repoMap = new HashMap<>();

            RestTemplate restTemplate = new RestTemplate();
            System.out.println(url);
            String json = restTemplate.getForObject(url, String.class);
            if (json == null || json.isEmpty()) {
                throw new Exception("Protocol not specified");
            }
            rootLogger.debug("JSON in request(): " + json);
            ObjectMapper mapper = new ObjectMapper();
            List<Repository> repos = Arrays.asList(mapper.readValue(json, Repository[].class));
            for (Repository repo : repos)
                repoMap.put(repo.getRepoName(), repo);
            if (!repoMap.isEmpty())
                repoContainerLogger.info("Request result in this.repositories: " + repoMap.toString());
            return repoMap;
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().contains("403 Forbidden"))
                errorLabel.setText("Токен не обладает нужными правами\n(Требуется доступ к API)\nИли неверно указан домен");
            else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found"))
                errorLabel.setText("Неверный домен");
            else if (e.getMessage().equals("Protocol not specified") || e.getMessage().contains("Response 301"))
                errorLabel.setText("Неверно указан протокол");
            else if (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("I/O error"))
                errorLabel.setText("Неверный токен");
            return null;
        }
    }*/

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и отображение списка репозиториев
     */
    @FXML
    public void authorization(ActionEvent event) {
        rootLogger.info("Calling authorization()");
        this.repositories.setToken(tokenField.getText());
        String domain = domainField.getText();
        try {
            if (curProtocol == null || curProtocol.isEmpty()) {
                throw new Exception("Wrong protocol specification");
            } else if (domain == null || domain.isEmpty()) {
                throw new Exception("Domain is wrong");
            } else if (this.repositories.getToken() == null || this.repositories.getToken().isEmpty()) {
                throw new Exception("Token is empty");
            }
            this.repositories.setRequestUrl(
                    curProtocol
                            + domain
                            + "/api/v4/projects/"
                            + "?membership=true"
                            + "&simple=true"
                            + "&private_token="
                            + this.repositories.getToken()
            );
            rootLogger.debug("Created url for request: " + this.repositories.getRequestUrl());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RepositoryTable.fxml"));
            Parent root = loader.load();
            RepoController repoController = loader.getController();
            try {
                this.repositories
                        .setRepoMap(
                                repoController.request(this.repositories.getRequestUrl())
                        );
            } catch (Exception e) {
                rootLogger.error(e.getMessage());
                if (e.getMessage().contains("403 Forbidden"))
                    errorLabel.setText("Токен не обладает нужными правами\n(Требуется доступ к API)\nИли неверно указан домен");
                else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found"))
                    errorLabel.setText("Неверный домен");
                else if (e.getMessage().equals("Protocol not specified") || e.getMessage().contains("Response 301"))
                    errorLabel.setText("Неверно указан протокол");
                else if (e.getMessage().contains("401 Unauthorized"))
                    errorLabel.setText("Неверный токен");
                else if (e.getMessage().contains("I/O error)"))
                    errorLabel.setText("Проверьте подключение к интернету");
            }
            if (this.repositories.getRepoMap() == null || this.repositories.getRepoMap().isEmpty()) {
                rootLogger.error("this.repositories.getRepoMap() is null or empty after calling request(): ");
            } else {
                rootLogger.debug("this.repositories after calling request(): " + this.repositories);
                repoController.setRepositories(this.repositories);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
            rootLogger.info("Result of authorization(): called update()");
        } catch (Exception e) {
            if (e.getMessage().equals("Wrong protocol specification"))
                errorLabel.setText("Неверно указан протокол");
            if (e.getMessage().equals("Domain is wrong"))
                errorLabel.setText("Неверно указан домен");
            if (e.getMessage().equals("Token is empty"))
                errorLabel.setText("Не указан токен");
            rootLogger.error(e.getMessage());
        }
    }

}
