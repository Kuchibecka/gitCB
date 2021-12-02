package src.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class RepoController implements Initializable {
    private RepositoryContainer repositories = new RepositoryContainer();
    private String curRepoUrl;
    private String curDirectory;

    @FXML
    private ListView<String> nameList;
    @FXML
    private Label errorLabel2 = new Label();
    @FXML
    private Button cloneButton;
    @FXML
    private Button backToLogin;
    @FXML
    private Button updateButton;
    @FXML
    private Button pathPicker;
    @FXML
    private TextField pathField;
    @FXML
    private Label pathLabel;

    static final Logger rootLogger = LogManager.getRootLogger();

    /**
     * Функция начальной инициализации сцены отображения репозиториев
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            curRepoUrl = repositories
                    .getRepoMap()
                    .get(nameList.getSelectionModel().getSelectedItem())
                    .getRepoUrl()
                    .substring(8);
            rootLogger.debug("Selected repo with name: " + nameList.getSelectionModel().getSelectedItem() + " and URL: " + curRepoUrl);
        });
    }

    /**
     * Сеттер для контейнера репозиториев
     *
     * @param repositories - контейнер репозиториев, передающийся со сцены авторизации
     */
    public void setRepositories(RepositoryContainer repositories) {
        this.repositories = repositories;
        nameList.setItems(FXCollections.observableArrayList());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
    }

    /**
     * Функция выбора пути для клонирования
     */
    @FXML
    public void pickPath() {
        errorLabel2.setText("");
        DirectoryChooser dirChooser = new DirectoryChooser();
        Stage stage = new Stage();
        File file = dirChooser.showDialog(stage);
        if (file != null) {
            this.curDirectory = file.getAbsolutePath();
            pathLabel.setText(this.curDirectory);
        }
        System.out.println("Picked directory: " + this.curDirectory);
    }

    /**
     * Функция обновления списка репозиториев
     */
    @FXML
    public void update() {
        nameList.setItems(FXCollections.observableArrayList());
        rootLogger.info("Calling update()");
        errorLabel2.setText("");
        try {
            this.repositories
                    .setRepoMap(
                            request(this.repositories.getRequestUrl())
                    );
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().contains("403 Forbidden"))
                errorLabel2.setText("Токен не обладает нужными правами\n(Требуется доступ к API)\nИли неверно указан домен");
            else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found"))
                errorLabel2.setText("Неверный домен");
            else if (e.getMessage().equals("Protocol not specified"))
                errorLabel2.setText("Неверно указан протокол");
            else if (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("I/O error"))
                errorLabel2.setText("Неверный токен");
        }
        rootLogger.info("Result of update(), this.repositories: " + repositories.toString());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
        rootLogger.info("Result of update(), nameList: " + nameList);
    }

    /**
     * Получить список репозиториев как результат API запроса
     */
    public HashMap<String, Repository> request(String url) throws Exception {
        rootLogger.info("Calling request(" + url + ")");
        /*try {*/
        HashMap<String, Repository> repoMap = new HashMap<>();

        RestTemplate restTemplate = new RestTemplate();
        System.out.println(url);
        String json = null;
        json = restTemplate.getForObject(url, String.class);
        if (json == null || json.isEmpty()) {
            throw new Exception("Protocol not specified");
        }
        rootLogger.debug("JSON in request(): " + json);
        ObjectMapper mapper = new ObjectMapper();
        List<Repository> repos = Arrays.asList(mapper.readValue(json, Repository[].class));
        for (Repository repo : repos)
            repoMap.put(repo.getRepoName(), repo);
        rootLogger.info("Request result in this.repositories: " + repoMap.toString());
        return repoMap;
        /*} catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().contains("403 Forbidden"))
                errorLabel2.setText("Токен не обладает нужными правами\n(Требуется доступ к API)\nИли неверно указан домен");
            else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found"))
                errorLabel2.setText("Неверный домен");
            else if (e.getMessage().equals("Protocol not specified"))
                errorLabel2.setText("Неверно указан протокол");
            else if (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("I/O error"))
                errorLabel2.setText("Неверный токен");
            *//*else if (e.getMessage().equals("Url is empty"))
                errorLabel.setText("URL пуст");*//*
            return null;
        }*/
    }


    /**
     * Возврат к сцене авторизации
     */
    @FXML
    public void getBackToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        rootLogger.info("Result of getBackToLogin(): Login scene setting");
    }

    /**
     * Клонирование репозитория в заданную директорию
     */
    @FXML
    public void cloneRepo() {
        errorLabel2.setText("");
        rootLogger.info("Calling cloneRepo()");
        String command = "git clone https://gitlab-ci-token:"
                + this.repositories.getToken() + "@" + curRepoUrl;
        rootLogger.debug("Command for ProcessBuilder is: " + command);
        if (curRepoUrl == null || curRepoUrl.isEmpty()) {
            rootLogger.error("Cloning repository URL not specified");
            errorLabel2.setText("Репозиторий для клонирования не выбран");
            return;
        }
        if (this.curDirectory == null || this.curDirectory.isEmpty()) {
            rootLogger.error("Directory not specified");
            errorLabel2.setText("Путь клонирования не указан");
            return;
        }
        try {
            rootLogger.debug("Path for ProcessBuilder is: " + curDirectory);
            String pbCommand = "cd " + curDirectory + " && " + command;
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
                if (line.contains("already exists and is not an empty directory.")) {
                    rootLogger.error("Directory already exists");
                    errorLabel2.setText("Непустая папка с таким названием уже существует");
                }
                if (line.contains("Receiving objects: 100%")) {
                    // System.out.println("СТООООООООООООООООООООООООООООООООООООООООООООО");
                    /*rootLogger.error("Directory already exists");
                    errorLabel2.setText("Непустая папка с таким названием уже существует");*/
                }
            }
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
            errorLabel2.setText("Ошибка выполнения команды git clone");
            return;
        }
        rootLogger.info("Result of cloneRepo(): executed attempt to clone selected repository");
    }
}
