package src.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Сцена отображения
     * Функция начальной инициализации сцены
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cloneButton.setDisable(true);
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            curRepoUrl = repositories
                    .getRepoMap()
                    .get(nameList.getSelectionModel().getSelectedItem())
                    .getRepoUrl()
                    .substring(8);
            cloneButton.setDisable(false);
            System.out.println("Selected repo with name: " + nameList.getSelectionModel().getSelectedItem() + " and URL: " + curRepoUrl);
        });
        protocolChoice.getItems().setAll(protocols);
        protocolChoice.setOnAction(event -> curProtocol = protocolChoice.getValue());
    }

    /**
     * Сцена отображения и сцена авторизации!
     * Получает список репозиториев как результат API запроса и записывает в this.repositories.repoMap
     *
     * @ return void todo: Исправить на boolean (ошибка/успех)
     */
    public void request(String url) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(url, String.class);
        System.out.println("Input json: " + json);
        ObjectMapper mapper = new ObjectMapper();
        List<Repository> repos = Arrays.asList(mapper.readValue(json, Repository[].class));
        for (Repository repo : repos)
            this.repositories.addRepoMap(repo.getRepoName(), repo);
        System.out.println("Repo map in this.repositories: " + this.repositories.getRepoMap());
    }

    /**
     * Сцена отображения
     * Функция обновления списка репозиториев
     */
    @FXML
    public void update() throws JsonProcessingException {
        request(this.repositories.getRequestUrl());
        nameList.setItems(FXCollections.observableArrayList());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
    }

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и отображение списка репозиториев
     */
    @FXML
    public void logging() throws JsonProcessingException {
        this.repositories.setToken(tokenField.getText());
        this.repositories.setRequestUrl(
                curProtocol
                        + domainField.getText()
                        + "/api/v4/projects/"
                        + "?membership=true"
                        + "&simple=true"
                        + "&private_token="
                        + this.repositories.getToken()
        );
        System.out.println("Created url for request: " + this.repositories.getRequestUrl());
        update();

        // Переход к сцене таблицы репозиториев
        /*
        root = FXMLLoader.load(getClass().getResource("/fxml/RepositoryTable.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        */
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
        // http://gitlab.dev.ppod.cbr.ru/
        // todo: Куда ^тут^ стучаться чтобы сделать git clone?
        String command = "git clone https://gitlab-ci-token:"
                + this.repositories.getToken() + "@" + curRepoUrl;
        System.out.println("Command is: " + command);
        String path = pathField.getText();
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd " + path + " && " + command);
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
            System.out.println(line);
        }
    }
}
