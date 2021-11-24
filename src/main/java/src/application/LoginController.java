package src.application;

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

//todo: Источник: http://tutorials.jenkov.com/javafx/tableview.html
public class LoginController implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private String token;
    JSONObject response;
    JSONArray repositories;

    @FXML
    private TextField tokenField;

    @FXML
    private TextField pathField;

    @FXML
    private Button cloneButton;

    @FXML
    private Button logIn;

    @FXML
    private Button updateButton;

    @FXML
    private ListView<String> nameList = new ListView();

    private ArrayList<String> bufferRepo = new ArrayList<>();

    private String url;

    private String repoUrl;

    private Map<String, String> repoMap = new HashMap<>();

    public ArrayList<String> request() {
        System.out.println("____________________");
        System.out.println(token);
        System.out.println("____________________");
        ArrayList<String> bufferRepo = new ArrayList<>();
        url = "https://gitlab.com/api/v4/projects/"
                + "?membership=true"
                + "&simple=true"
                + "&private_token="
                + token;
        RestTemplate restTemplate = new RestTemplate();
        response = new JSONObject("{ repositories: " + restTemplate.getForObject(url, String.class) + "}");
        repositories = response.getJSONArray("repositories");
        System.out.println(response);

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
        System.out.println("INITIALIZE");
        System.out.println(token);

        nameList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                repoUrl = repoMap.get(nameList.getSelectionModel().getSelectedItem());
                System.out.println(repoUrl);
            }
        });
    }

    @FXML
    public void update(ActionEvent event) {
        System.out.println(bufferRepo);
        nameList.getItems().removeAll(bufferRepo);
        nameList.getItems().addAll(request());
    }

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и считывание списка репозиториев
     */
    @FXML
    public void logging(ActionEvent event) {
        token = tokenField.getText();
        System.out.println("Got token: " + token);

        // todo++: Сохранять токены в файле, чтобы не вводить заново
        // todo: Обработка исключения несоответствия токена
        // todo: Убрать, уже считывается из формы (вроде):
        // token = "glpat-u_xNuwLFH-dW66uHigMz";
        url = "https://gitlab.com/api/v4/projects/"
                + "?membership=true"
                + "&simple=true"
                + "&private_token="
                + token;
        System.out.println("My map: " + repoMap);
        nameList.getItems().removeAll(bufferRepo);
        nameList.getItems().addAll(request());
        System.out.println("I LOGGED IN " + nameList.getItems());

        // glpat-u_xNuwLFH-dW66uHigMz
        // переход к сцене таблицы репозиториев
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
        root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
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
        // todo: Добавить ли вывод командной строки в окно программы?
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
    }
}
