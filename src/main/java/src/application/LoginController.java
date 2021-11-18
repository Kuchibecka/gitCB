package src.application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class LoginController {

    @FXML
    private TextField login;
    @FXML
    private TextField password;
    @FXML
    private Button logIn;

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
        System.out.println(restTemplate.getForObject(url, String.class));
        // response = new JSONObject(restTemplate.getForObject(url, String.class));
        response = new JSONObject("{ repositories: " + restTemplate.getForObject(url, String.class) + "}");
        System.out.println(response);

        repositories = response.getJSONArray("repositories");
        for (int i = 0; i<repositories.length(); i++) {
            System.out.println(repositories.getJSONObject(i).getString("name"));
            //todo: сформировать по массиву (?если таблица javafx принимает массив) на каждый столбец будущей таблицы репозиториев
        }

        // переход к сцене таблицы репозиториев
        root = FXMLLoader.load(getClass().getResource("/fxml/RepositoryTable.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        // stage.setScene(scene);
        // stage.show();
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
