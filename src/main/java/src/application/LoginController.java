package src.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//todo: Источник: http://tutorials.jenkov.com/javafx/tableview.html
public class LoginController {
    @FXML
    private TextField login;
    @FXML
    private TextField password;
    @FXML
    private Button logIn;
    @FXML
    private TableView<String> tableView = new TableView();
    @FXML
    TableColumn<Map, String> nameColumn;
    @FXML
    TableColumn<Map, String> descriptionColumn;

    private Stage stage;
    private Scene scene;
    private Parent root;

    String userLogin;
    String userPassword;
    String token;
    JSONObject response;
    JSONArray repositories;

    /**
     * Сцена авторизации
     * Авторизация по считываемому токену и считывание списка репозиториев
     */
    public void logging(ActionEvent event) throws IOException {
        // считывание логина и пароля todo: Поменять на считывание токена
        userLogin = login.getText();
        userPassword = password.getText();
        System.out.println("Your login is: " + userLogin);
        System.out.println("Your password is: " + userPassword);

        /* Process p = Runtime.getRuntime().exec("cmd cd");
        Future<ProcessResult> future = new ProcessExecutor()
                .command("java", "-version")
                .readOutput(true)
                .start().getFuture();
        String output = future.get(30, TimeUnit.SECONDS).outputUTF8();
        System.out.println(output);*/

        // todo++: Сохранять токены в файле, чтобы не вводить заново
        // todo: Убрать, считывать из формы:
        token = "glpat-bx7MvSdzYup2vB1fnMoK";
        String url = "https://gitlab.com/api/v4/projects/";
        url += "?membership=true";
        url += "&simple=true";
        url += "&private_token=" + token;
        RestTemplate restTemplate = new RestTemplate();
        response = new JSONObject("{ repositories: " + restTemplate.getForObject(url, String.class) + "}");
        System.out.println(response);

        repositories = response.getJSONArray("repositories");
        // TableView tableView = new TableView();
        nameColumn = new TableColumn<>("Название репозитория");
        nameColumn.setCellValueFactory(new MapValueFactory<String>("name"));
        descriptionColumn = new TableColumn<>("Описание");
        descriptionColumn.setCellValueFactory(new MapValueFactory<String>("description"));
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(descriptionColumn);
        ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(); //ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

        // ArrayList<Map<String, String>> tableData = new ArrayList<>();
        for (int i = 0; i < repositories.length(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", repositories.getJSONObject(i).get("name")==JSONObject.NULL ? "" : repositories.getJSONObject(i).getString("name"));
            item.put("description", repositories.getJSONObject(i).get("description")==JSONObject.NULL ? "" : repositories.getJSONObject(i).getString("description"));
            items.add(item);
            // tableData.add(i, item);
        }
        System.out.println("Мапы в таблицу: " + items);
        tableView.getItems().addAll(String.valueOf(items));


        // переход к сцене таблицы репозиториев
        root = FXMLLoader.load(getClass().getResource("/fxml/RepositoryTable.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        //todo: Добавить placeholder в таблицу, когда нет доступных репозиториев
    }

    /**
     * Возврат к сцене авторизации
     */
    public void getBackToLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
