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
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.IOException;
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

    public void logging(ActionEvent event) throws IOException, TimeoutException, InterruptedException {
        // считывание логина и пароля
        userLogin = login.getText();
        userPassword = password.getText();
        System.out.println("Your login is: " + userLogin);
        System.out.println("Your password is: " + userPassword);
        // Process p = Runtime.getRuntime().exec("cmd cd");

        String output = new ProcessExecutor().command("git", "--version")
                .readOutput(true).execute()
                .outputUTF8();

        System.out.println(output);

        // переход к сцене таблицы репозиториев
        root = FXMLLoader.load(getClass().getResource("/fxml/RepositoryTable.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void getBackToLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
