package src.application;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ТОКЕН ДЛЯ АВТОРИЗАЦИИ: glpat-u_xNuwLFH-dW66uHigMz
 * ТЕСТОВЫЙ ПУТЬ КЛОНИРОВАНИЯ: C:\Users\Администратор\Downloads\testFolder
 */
public class LoginController implements Initializable {
    private String curRepoUrl;
    private String curProtocol;
    private RepositoryContainer repositories = new RepositoryContainer();
    private final ObservableList<String> protocols =
            FXCollections.observableArrayList("http://", "https://");
    @FXML
    private TextField tokenField;
    @FXML
    private TextField pathField;
    @FXML
    private TextField domainField;
    @FXML
    private ListView<String> nameList;
    @FXML
    private ChoiceBox<String> protocolChoice;
    @FXML
    public Button logIn;
    @FXML
    private Button cloneButton;
    @FXML
    private Button backToLogin;
    @FXML
    private Button updateButton;

    static final Logger rootLogger = LogManager.getRootLogger();
    static final Logger repoContainerLogger = LogManager.getLogger(RepositoryContainer.class);

    /**
     * Сцена отображения
     * Функция начальной инициализации сцены
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootLogger.info("Initialization");
        rootLogger.debug("Root Logger debug message!");
        cloneButton.setDisable(true);
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            curRepoUrl = repositories
                    .getRepoMap()
                    .get(nameList.getSelectionModel().getSelectedItem())
                    .getRepoUrl()
                    .substring(8);
            cloneButton.setDisable(false);
            rootLogger.debug("Selected repo with name: " + nameList.getSelectionModel().getSelectedItem() + " and URL: " + curRepoUrl);
        });
        try {
            if (protocols.isEmpty())
                throw new Exception("Variable protocols not configured");
            protocolChoice.getItems().setAll(protocols);
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            return;
        }
        protocolChoice.setOnAction(event -> curProtocol = protocolChoice.getValue());
    }

    /**
     * Сцена отображения и сцена авторизации!
     * Получает список репозиториев как результат API запроса и записывает в this.repositories.repoMap
     */
    public void request(String url) {
        rootLogger.info("Calling request(" + url + ")");
        try {
            assert url != null;
        } catch (AssertionError e) {
            rootLogger.error("URL must not be null");
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        try {
            System.out.println(url);
            String json = restTemplate.getForObject(url, String.class);
            if (json == null || json.isEmpty()) {
                throw new Exception("Protocol not specified");
            }
            rootLogger.debug("JSON in request(): " + json);
            ObjectMapper mapper = new ObjectMapper();
            List<Repository> repos = Arrays.asList(mapper.readValue(json, Repository[].class));
            for (Repository repo : repos)
                this.repositories.addRepoMap(repo.getRepoName(), repo);
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            return;
        }
        repoContainerLogger.info("Request result in this.repositories: " + this.repositories.toString());
    }

    /**
     * Сцена отображения
     * Функция обновления списка репозиториев
     */
    @FXML
    public void update() {
        rootLogger.info("Calling update()");
        request(this.repositories.getRequestUrl());
        nameList.setItems(FXCollections.observableArrayList());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
        rootLogger.info("Result of update(), nameList: " + nameList.toString());
    }

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и отображение списка репозиториев
     */
    @FXML
    public void authorization() {
        rootLogger.info("Calling authorization()");
        this.repositories.setToken(tokenField.getText());
        try {
            if (curProtocol == null || curProtocol.isEmpty()) {
                throw new Exception("Wrong protocol specification");
            }
            this.repositories.setRequestUrl(
                    curProtocol
                            + domainField.getText()
                            + "/api/v4/projects/"
                            + "?membership=true"
                            + "&simple=true"
                            + "&private_token="
                            + this.repositories.getToken()
            );
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            return;
        }
        rootLogger.debug("Created url for request: " + this.repositories.getRequestUrl());
        update();

        // Переход к сцене таблицы репозиториев
        /*
        root = FXMLLoader.load(getClass().getResource("/fxml/RepositoryTable.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        */
        rootLogger.info("Result of authorization(): called update");
    }

    /**
     * Сцена отображения
     * Возврат к сцене авторизации
     */
    @FXML
    public void getBackToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Сцена отображения
     * Клонирование репозитория в заданную директорию
     */
    @FXML
    public void cloneRepo(ActionEvent event) throws IOException {
        rootLogger.info("Calling cloneRepo()");
        // http://gitlab.dev.ppod.cbr.ru/
        // todo: Куда ^тут^ стучаться чтобы сделать git clone?
        String command = "git clone https://gitlab-ci-token:"
                + this.repositories.getToken() + "@" + curRepoUrl;
        rootLogger.debug("Command for ProcessBuilder is: " + command);
        String pbCommand = null;
        try {
            String path = pathField.getText();
            assert path != null;
            rootLogger.debug("Path for ProcessBuilder is: " + path);
            pbCommand = "cd " + path + " && " + command;
            rootLogger.debug("Full command for ProcessBuilder is: " + pbCommand);
        } catch (AssertionError e) {
            rootLogger.error("Variable path is null");
            return;
        }
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", pbCommand);
        builder.redirectErrorStream(true);
        try {
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                // Вывод командной строки
                rootLogger.debug("Trace from ProcessBuilder: " + line);
            }
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
            return;
        }
        rootLogger.info("Result of cloneRepo(): executed attempt to clone selected repository");
    }
}
