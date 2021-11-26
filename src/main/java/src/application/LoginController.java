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
import javafx.scene.control.*;
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
 * ТОКЕН БЕЗ API: glpat-AGW_F2nA9uzypQVA9B6Q
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
    @FXML
    private Label errorLabel = new Label();
    @FXML
    private Label errorLabel2 = new Label();

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
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            curRepoUrl = repositories
                    .getRepoMap()
                    .get(nameList.getSelectionModel().getSelectedItem())
                    .getRepoUrl()
                    .substring(8);
            rootLogger.debug("Selected repo with name: " + nameList.getSelectionModel().getSelectedItem() + " and URL: " + curRepoUrl);
        });
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
     * Сцена отображения и сцена авторизации!
     * Получает список репозиториев как результат API запроса и записывает в this.repositories.repoMap
     */
    public void request(String url) {
        rootLogger.info("Calling request(" + url + ")");
        try {
            // оставить только при НЕ разнесении на разные сцены
            // срабатывает если ты не нажал Log In и сразу нажал на update
            /*if (url == null || url.isEmpty()) {
                throw new Exception("Url is empty");
            }*/
         /*catch (AssertionError e) {
            rootLogger.error("URL must not be null");
            return;
        }*/
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
                this.repositories.addRepoMap(repo.getRepoName(), repo);
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().contains("403 Forbidden"))
                errorLabel.setText("Токен не обладает нужными правами\n(Требуется доступ к API)\nИли неверно указан домен");
            else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found") || e.getMessage().contains("I/O error"))
                errorLabel.setText("Неверный домен");
            else if (e.getMessage().equals("Protocol not specified"))
                errorLabel.setText("Неверно указан протокол");
            else if (e.getMessage().contains("401 Unauthorized"))
                errorLabel.setText("Неверный токен");
            /*else if (e.getMessage().equals("Url is empty"))
                errorLabel.setText("URL пуст");*/
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
        String domain = domainField.getText();
        try {
            if (curProtocol == null || curProtocol.isEmpty()) {
                throw new Exception("Wrong protocol specification");
            }
            if (domain == null || domain.isEmpty()) {
                throw new Exception("Domain is empty");
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
        } catch (Exception e) {
            if (e.getMessage().equals("Wrong protocol specification"))
                errorLabel.setText("Неверно указан протокол");
            if (e.getMessage().equals("Domain is empty"))
                errorLabel.setText("Не указан домен");
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
    public void cloneRepo(ActionEvent event) {
        rootLogger.info("Calling cloneRepo()");
        // http://gitlab.dev.ppod.cbr.ru/
        // А куда ^тут^ потом стучаться чтобы сделать git clone? v Сюда v же ?
        String command = "git clone https://gitlab-ci-token:"
                + this.repositories.getToken() + "@" + curRepoUrl;
        rootLogger.debug("Command for ProcessBuilder is: " + command);
        try {
            String path = pathField.getText();
            if (path == null || path.isEmpty()) {
                throw new Exception("Path not specified");
            }
            if (curRepoUrl == null || curRepoUrl.isEmpty()) {
                throw new Exception("Cloning repository not specified");
            }
            rootLogger.debug("Path for ProcessBuilder is: " + path);
            String pbCommand = "cd " + path + " && " + command;
            rootLogger.debug("Full command for ProcessBuilder is: " + pbCommand);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", pbCommand);
            builder.redirectErrorStream(true);
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
            errorLabel2.setText("Ошибка выполнения команды git clone");
            return;
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().equals("Cloning repository not specified"))
                errorLabel2.setText("Репозиторий для клонирования не выбран");
            if (e.getMessage().equals("Path not specified"))
                errorLabel2.setText("Путь клонирования не указан");
            return;
        }
        rootLogger.info("Result of cloneRepo(): executed attempt to clone selected repository");
    }
}
