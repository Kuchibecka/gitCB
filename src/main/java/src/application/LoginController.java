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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * ТОКЕН ДЛЯ АВТОРИЗАЦИИ: glpat-u_xNuwLFH-dW66uHigMz
 * ТЕСТОВЫЙ ПУТЬ КЛОНИРОВАНИЯ: C:\Users\Администратор\Downloads\testFolder
 */
// todo: Добавить обработку всех ошибок
public class LoginController implements Initializable {
    private ArrayList<String> bufferRepo = new ArrayList<>();
    // todo: Field can be converted to a local variable in method request()
    private String url;
    private String token;
    private String repoUrl;
    private String curProtocol;
    private String curDomain;
    private Map<String, String> repoMap = new HashMap<>();
    private final ObservableList<String> protocols = FXCollections.observableArrayList("http://", "https://");
    private final ObservableList<String> domains = FXCollections.observableArrayList("gitlab.com", "gitlab.dev.ppod.cbr.ru");

    @FXML
    private TextField tokenField;
    @FXML
    private TextField urlField;
    @FXML
    private TextField pathField;
    // todo: убрать  = new ListView() ??
    // todo: !никогда не используйте new для создания присвоения значения членам с тегом @FXML! (https://coderoad.ru/23067256/Заполнить-Choicebox-определенный-в-FXML#23068017)
    @FXML
    private ListView<String> nameList = new ListView();
    @FXML
    private ChoiceBox<String> protocolChoice;
    @FXML
    private ChoiceBox<String> domainChoice;
    /*@FXML
    private Button cloneButton;
    @FXML
    private Button logIn;
    @FXML
    private Button updateButton;*/


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            // Функция отлавливания изменений поля nameList (извлечение url выбранного репозитория)
            // todo: Сделать что-то, если пользователь ничего не выбрал (заблокировать кнопку, к примеру)
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                repoUrl = repoMap.get(nameList.getSelectionModel().getSelectedItem()).substring(8);
                System.out.println(repoUrl);
            }
        });
        protocolChoice.getItems().setAll(protocols);
        domainChoice.getItems().setAll(domains);
        protocolChoice.setOnAction(event -> curProtocol = protocolChoice.getValue());
        domainChoice.setOnAction(event -> curDomain = domainChoice.getValue());
    }

    /**
     * @return Список репозиториев как результат API запроса
     */
    public ArrayList<String> request() throws JsonProcessingException {
        System.out.println("____________________");
        System.out.println(token);
        System.out.println("____________________");
        ArrayList<String> bufferRepo = new ArrayList<>();
        url = curProtocol
                + curDomain
                + "/api/v4/projects/"
                + "?membership=true"
                + "&simple=true"
                + "&private_token="
                + token;
        System.out.println("Created url for request: " + url);
        RestTemplate restTemplate = new RestTemplate();

        String json = restTemplate.getForObject(url, String.class);
        System.out.println("Input json: " + json);
        // glpat-u_xNuwLFH-dW66uHigMz
        ObjectMapper mapper = new ObjectMapper();

        List<Repository> repository = Arrays.asList(mapper.readValue(json, Repository[].class));
        for (Repository repo : repository) {
            bufferRepo.add(repo.getName());
            repoMap.put(
                    repo.getName(),
                    repo.getRepoUrl()
            );
        }

        /*Repository[] list = mapper.readValue(json, Repository[].class);
        for (Repository rep : list) {
            bufferRepo.add(rep.getName());
            repoMap.put(
                    rep.getName(),
                    rep.getRepoUrl()
            );
        }*/

        System.out.println("My map: " + repoMap);
        this.bufferRepo = bufferRepo;
        return bufferRepo;
    }

    /**
     * Функция обновления списка репозиториев
     */
    @FXML
    public void update() throws JsonProcessingException {
        nameList.getItems().removeAll(bufferRepo);
        nameList.getItems().addAll(request());
    }

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и считывание списка репозиториев
     */
    @FXML
    public void logging() throws JsonProcessingException {
        token = tokenField.getText();
        /*url = urlField.getText();*/
        // token = "glpat-u_xNuwLFH-dW66uHigMz";

        /*  todo: 1) Ввод url из текстового поля
            todo: 2) log4j
            todo: 3) Обработка exception'ов:
                    - ошибки соединения
                    - ошибки полей
                    - ошибки выгрузки
                    - несоответствие токена
        */
        /*url = "https://gitlab.com/api/v4/projects/"*/
        nameList.getItems().removeAll(bufferRepo);
        nameList.getItems().addAll(request());
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
     * Клонирование репозитория в заданную директорию
     */
    @FXML
    public void clone(ActionEvent event) throws IOException {
        // http://gitlab.dev.ppod.cbr.ru/
        // todo: Куда ^тут^ стучаться чтобы сделать git clone?
        String command = "git clone https://gitlab-ci-token:" + token + "@" + repoUrl;
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
