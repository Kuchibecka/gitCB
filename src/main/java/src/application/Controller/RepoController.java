package src.application.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.CloneTask;
import src.application.Entity.Repository;
import src.application.Entity.RepositoryContainer;
import src.application.Entity.UpdateTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RepoController implements Initializable {
    private RepositoryContainer repositories = new RepositoryContainer();
    private String curRepoUrl;
    private String curRepoName;
    private String curRepoDescription;
    private String curDirectory;

    @FXML
    private ListView<String> nameList;
    @FXML
    private Label errorLabel2 = new Label();
    @FXML
    private Label progressLabel = new Label();
    @FXML
    private Label descriptionLabelLabel = new Label();
    @FXML
    private Button cloneButton;
    @FXML
    private Button backButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button pathButton;
    @FXML
    private Label pathLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ProgressBar progressBar = new ProgressBar(0);


    private final Tooltip backButtonTooltip = new Tooltip("Назад к авторизации");
    private final Tooltip updateButtonTooltip = new Tooltip("Обновить список репозиториев");
    private final Tooltip cloneButtonTooltip = new Tooltip("Клонировать репозиторий в выбарнную папку");
    private final Tooltip pathButtonTooltip = new Tooltip("Выбрать папку клонирования");

    static final Logger rootLogger = LogManager.getRootLogger();

    /**
     * Функция начальной инициализации сцены отображения репозиториев
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setTooltip(backButtonTooltip);
        updateButton.setTooltip(updateButtonTooltip);
        cloneButton.setTooltip(cloneButtonTooltip);
        pathButton.setTooltip(pathButtonTooltip);
        progressBar.setVisible(false);
        descriptionLabelLabel.setVisible(false);
        nameList.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            Repository repository = repositories.getRepoMap().get(nameList.getSelectionModel().getSelectedItem());
            if (repository != null) {
                curRepoUrl = repository
                        .getRepoUrl()
                        .substring(8);
                curRepoName = repository
                        .getRepoName();
                curRepoDescription = repository
                        .getRepoDescription();
                descriptionLabel.setText(repository.getRepoName());
                descriptionLabelLabel.setVisible(true);
            }
            progressBar.setVisible(false);
            progressLabel.setVisible(false);
            rootLogger.debug("Selected repo with name: " + nameList.getSelectionModel().getSelectedItem() + " and URL: " + curRepoUrl);
        });
    }

    /**
     * Сеттер контейнера репозиториев
     *
     * @param repositories - контейнер репозиториев, передающийся со сцены авторизации
     */
    public void setRepositories(RepositoryContainer repositories) {
        rootLogger.info("Calling setRepositories(" + repositories.toString() + ")");
        this.repositories = repositories;
        nameList.setItems(FXCollections.observableArrayList());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
        rootLogger.info("Result of setRepositories(...), nameList: {" + nameList.getItems().toString() + "}");
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
     * Блокировка/разблокировка кнопок для выполняющихся процессов
     *
     * @param b: true - включение кнопок, false - отключение
     */
    public void enableButtons(boolean b) {
        rootLogger.info("Calling enableButtons(" + b + ")");
        cloneButton.setDisable(!b);
        updateButton.setDisable(!b);
        backButton.setDisable(!b);
        pathButton.setDisable(!b);
        rootLogger.info("Result of enableButtons(), is enabled: {"
                + "cloneButton: " + !cloneButton.isDisabled()
                + ", updateButton: " + !updateButton.isDisabled()
                + ", backButton: " + !backButton.isDisabled()
                + ", pathButton: " + !pathButton.isDisabled()
                + "}"
        );
    }

    /**
     * Функция выбора пути для клонирования удалённого репозитория / актуализации локального репозитория
     */
    @FXML
    public void pickPath() {
        rootLogger.info("Calling pickPath()");
        progressBar.setVisible(false);
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
        rootLogger.info("Result of pickPath() picked directory: " + this.curDirectory);
    }

    /**
     * Функция обновления списка удалённых репозиториев
     */
    @FXML
    public void update() {
        rootLogger.info("Calling update()");
        enableButtons(false);
        nameList.setItems(FXCollections.observableArrayList());
        errorLabel2.setText("");
        try {
            this.repositories
                    .setRepoMap(
                            request(this.repositories.getRequestUrl())
                    );
        } catch (Exception e) {
            rootLogger.error(e.getMessage());
            if (e.getMessage().contains("403 Forbidden"))
                errorLabel2.setText("Токен не обладает нужными правами (Требуется доступ к API) или неверно указан домен");
            else if (e.getMessage().contains("410 Gone") || e.getMessage().contains("404 Not Found"))
                errorLabel2.setText("Неверный домен");
            else if (e.getMessage().equals("Protocol not specified"))
                errorLabel2.setText("Неверно указан протокол");
            else if (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("I/O error"))
                errorLabel2.setText("Неверный токен");
        } finally {
            enableButtons(true);
        }
        rootLogger.info("Result of update(), this.repositories: " + repositories.toString());
        nameList.getItems().addAll(
                new ArrayList<>(
                        this.repositories.getRepoMap().keySet()
                )
        );
        rootLogger.info("Result of update(), nameList: " + nameList.getItems().toString());
    }

    /**
     * Возврат к сцене авторизации
     */
    @FXML
    public void getBackToLogin(ActionEvent event) {
        rootLogger.info("Calling getBackToLogin()");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            rootLogger.info("Result of getBackToLogin(): Login scene setting");
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
        }
    }

    /**
     * Клонирование репозитория в заданную директорию
     */
    @FXML
    public void cloneRepo() {
        rootLogger.info("Calling cloneRepo()");
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        Scene scene = cloneButton.getScene();
        scene.setCursor(Cursor.WAIT);
        enableButtons(false);
        errorLabel2.setText("");
        if (curRepoUrl == null || curRepoUrl.isEmpty()) {
            rootLogger.error("Cloning repository URL not specified");
            errorLabel2.setText("Репозиторий для клонирования не выбран");
            enableButtons(true);
            scene.setCursor(Cursor.DEFAULT);
            progressBar.setVisible(false);
            return;
        }
        if (this.curDirectory == null || this.curDirectory.isEmpty()) {
            rootLogger.error("Directory not specified");
            errorLabel2.setText("Путь клонирования не указан");
            enableButtons(true);
            scene.setCursor(Cursor.DEFAULT);
            progressBar.setVisible(false);
            return;
        }
        CloneTask cloneTask = new CloneTask(curDirectory, repositories.getToken(), curRepoUrl, rootLogger);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(cloneTask.progressProperty());
        progressLabel.textProperty().unbind();
        progressLabel.textProperty().bind(cloneTask.messageProperty());

        cloneTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            rootLogger.info("Caught event in cloneTask.EventHandler()");
            Integer result = cloneTask.getValue();
            rootLogger.debug("Got cloneTask result: " + result);
            progressLabel.textProperty().unbind();
            if (result == 1) {
                rootLogger.debug("cloneTask ended successfully, repository cloned");
                progressLabel.setText("Готово");
                progressLabel.setVisible(true);
            } else if (result == 0) {
                rootLogger.debug("cloneTask ended with 'already exists' warning. Trying to update repository.");
                UpdateTask updateTask = new UpdateTask(curRepoName, curDirectory, repositories.getToken(), curRepoUrl, rootLogger);
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(updateTask.progressProperty());
                updateTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                    rootLogger.info("Caught event in updateTask.EventHandler()");
                    Integer res = updateTask.getValue();
                    rootLogger.debug("Got updateTask result: " + res);
                    switch (res) {
                        case 0:
                            rootLogger.debug("updateTask ended successfully: repository is actual");
                            progressLabel.setText("Репозиторий актуален, изменений нет");
                            break;
                        case 1:
                            rootLogger.debug("updateTask ended successfully: repository updated to actual");
                            progressLabel.setText("Репозиторий обновлён до актуальной версии");
                            break;
                        case -1:
                            rootLogger.debug("updateTask ended with error: chosen directory isn't a git neither empty repository");
                            errorLabel2.setText("Выбранная папка не пуста и не является репозиторием git");
                            progressBar.setVisible(false);
                            break;
                        case -2:
                            rootLogger.debug("updateTask ended with error: chosen directory is an incompatible git repository");
                            errorLabel2.setText("Выбранная папка является git-репозиторием, несовместимым с выбранным");
                            progressBar.setVisible(false);
                            break;
                        default:
                            rootLogger.error("updateTask ended with error: unknown error");
                            errorLabel2.setText("Неизвестная ошибка при выполнении команды git pull");
                            progressBar.setVisible(false);
                            break;
                    }
                });
                rootLogger.debug("Running updateTask");
                new Thread(updateTask).start();
            } else if (result == -1) {
                rootLogger.debug("cloneTask ended with error");
                errorLabel2.setText("Ошибка выполнения команды git clone");
            }
            else { //todo: проверить на сдвоенные логи
                rootLogger.error("cloneTask ended with error: unknown error");
                errorLabel2.setText("Неизвестная ошибка");
            }
            scene.setCursor(Cursor.DEFAULT);
            enableButtons(true);
        });
        rootLogger.debug("Running cloneTask");
        new Thread(cloneTask).start();
        rootLogger.info("Result of cloneRepo(): executed attempt to clone selected repository");
    }
}
