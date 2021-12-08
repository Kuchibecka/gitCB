package src.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.CloneTask;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;
import src.application.Entity.UpdateTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RepoController implements Initializable {
    private RepositoryContainer repositories = new RepositoryContainer();
    private String curRepoUrl;
    private String curRepoName;
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
    private Label pathLabel;
    @FXML
    private ProgressBar progressBar = new ProgressBar(0);
    @FXML
    private Label progressLabel = new Label();


    static final Logger rootLogger = LogManager.getRootLogger();

    /**
     * Функция начальной инициализации сцены отображения репозиториев
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            Repository repository = repositories.getRepoMap().get(nameList.getSelectionModel().getSelectedItem());
            if (repository != null) {
                curRepoUrl = repository
                        .getRepoUrl()
                        .substring(8);
                curRepoName = repository
                        .getRepoName();
            }
            progressBar.setVisible(false);
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
        if (file != null && Files.exists(Paths.get(file.getAbsolutePath()))) {
            this.curDirectory = file.getAbsolutePath();
            pathLabel.setText(this.curDirectory);
        } else {
            dirChooser.setInitialDirectory(new File("C:\\"));
            this.curDirectory = "C:\\";
            pathLabel.setText(this.curDirectory);
            errorLabel2.setText("Выбранной директории не существует");
            rootLogger.error("No such directory");
        }
        rootLogger.debug("Picked directory: " + this.curDirectory);
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
        HashMap<String, Repository> repoMap = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(url, String.class);
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
        String command = "git clone --progress https://gitlab-ci-token:"
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
        String pbCommand = "cd " + curDirectory + " && " + command;
        CloneTask cloneTask = new CloneTask(pbCommand, rootLogger);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(cloneTask.progressProperty());
        progressLabel.textProperty().unbind();
        progressLabel.textProperty().bind(cloneTask.messageProperty());

        cloneTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> {
                    progressBar.setVisible(false);
                    String result = cloneTask.getValue();
                    progressLabel.textProperty().unbind();
                    if (result.equals("none"))
                        progressLabel.setText("Готово");
                    else {
                        if (result.equals("Непустая папка с таким\nназванием уже существует")) {
                            String name = curRepoName.toLowerCase().replace(" ", "-");
                            String updateCommand = "cd " + curDirectory + "\\" + name + " && " + "git pull";
                            System.out.println("_____________command:" + updateCommand);
                            UpdateTask updateTask = new UpdateTask(rootLogger, updateCommand);
                            updateTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                                    e -> {
                                        String res = updateTask.getValue();
                                        // progressLabel.textProperty().unbind();
                                        switch (res) {
                                            case "No changes":
                                                progressLabel.setText("Репозиторий актуален,\nизменений нет");
                                                break;
                                            case "Репозиторий обновлён\nдо актуальной версии":
                                                progressLabel.setText("Репозиторий обновлён\nдо актуальной версии");
                                                break;
                                            default:
                                                errorLabel2.setText(res);
                                                break;
                                        }


                                    });
                            new Thread(updateTask).start();
                        }
                    }
                });
        progressBar.setVisible(true);
        new Thread(cloneTask).start();
        rootLogger.info("Result of cloneRepo(): executed attempt to clone selected repository");
    }
}
