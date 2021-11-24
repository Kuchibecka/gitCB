package src.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;
import src.application.Entity.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * ТОКЕН ДЛЯ АВТОРИЗАЦИИ: glpat-u_xNuwLFH-dW66uHigMz
 * ТЕСТОВЫЙ ПУТЬ КЛОНИРОВАНИЯ: C:\Users\Администратор\Downloads\testFolder
 */
// todo: Добавить обработку всех ошибок
public class LoginController implements Initializable {
    @FXML
    private TextField tokenField;
    @FXML
    private TextField pathField;
    /*@FXML
    private Button cloneButton;
    @FXML
    private Button logIn;
    @FXML
    private Button updateButton;*/
    @FXML
    private ListView<String> nameList = new ListView();

    private ArrayList<String> bufferRepo = new ArrayList<>();
    private String url;
    private String token;
    private String repoUrl;
    private Map<String, String> repoMap = new HashMap<>();


    /**
     * @return Список репозиториев как результат API запроса
     */
    public ArrayList<String> request() throws JsonProcessingException {
        System.out.println("____________________");
        System.out.println(token);
        System.out.println("____________________");
        ArrayList<String> bufferRepo = new ArrayList<>();
        Map<Repository, String> repositoryMap = new HashMap<>();
        url = "https://gitlab.com/api/v4/projects/"
                + "?membership=true"
                + "&simple=true"
                + "&private_token="
                + token;
        RestTemplate restTemplate = new RestTemplate();

        // todo: ??? Убрать для Jackson? Оставить вхоодным параметром String?
        String json = restTemplate.getForObject(url, String.class);
        JSONObject response = new JSONObject("{ repositories: " + json + "}");
        JSONArray repositories = response.getJSONArray("repositories");

        // jackson
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<Repository, String>> typeRef =
                new TypeReference<HashMap<Repository, String>>() {};
        repositoryMap = mapper.readValue("{" + json + "}", typeRef);
        System.out.println("My brand new repository map: " + repositoryMap.toString());

        for (int i = 0; i < repositories.length(); i++) {
            String name = repositories.getJSONObject(i).get("name") == JSONObject.NULL ?
                    ""
                    : repositories.getJSONObject(i).getString("name");
            String repoUrl = repositories.getJSONObject(i).get("http_url_to_repo") == JSONObject.NULL ?
                    ""
                    : repositories.getJSONObject(i).getString("http_url_to_repo");
            repoUrl = repoUrl.substring(8);
            repoMap.put(name, repoUrl);
            bufferRepo.add(name);
        }

        System.out.println("My map: " + repoMap);
        this.bufferRepo = bufferRepo;
        return bufferRepo;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            // Функция отлавливания изменений поля nameList (извлечение url выбранного репозитория)
            // todo: Сделать что-то, если пользователь ничего не выбрал (заблокировать кнопку, к примеру)
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                repoUrl = repoMap.get(nameList.getSelectionModel().getSelectedItem());
                System.out.println(repoUrl);
            }
        });
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
        // todo+: Сохранять токены в файле, чтобы не вводить заново
        // todo: Обработка исключения несоответствия токена
        // token = "glpat-u_xNuwLFH-dW66uHigMz";
        // todo: Ввод url из
        // todo: log4j
        // todo: Обработка exception'ов
        // todo: ошибки соединения, ошибки полей, выгрузка
        // todo: сериализация билиотека json -> Jackson
        url = "https://gitlab.com/api/v4/projects/"
                + "?membership=true"
                + "&simple=true"
                + "&private_token="
                + token;
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
        String command = "git clone https://gitlab-ci-token:" + token + "@" + repoUrl;
        String path = pathField.getText();
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd " + path + " && " + command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        // todo: Добавлять ли вывод командной строки в окно программы?
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
