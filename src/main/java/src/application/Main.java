package src.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/Login.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("GitLab CB client");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Stage stage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, Color.BLUEVIOLET);
        stage.setWidth(840);
        stage.setHeight(630);
        Text text = new Text();
        text.setText("gitlab CB client");
        text.setX(50);
        text.setY(50);
        text.setFont(Font.font("Verdana", 50));
        text.setFill(Color.LIMEGREEN);

        Line line = new Line();
        line.setStartX(200);
        line.setStartY(200);
        line.setEndX(500);
        line.setEndY(200);
        line.setStrokeWidth(5);
        line.setStroke(Color.MEDIUMVIOLETRED);
        line.setRotate(60);

        Rectangle rectangle = new Rectangle();
        rectangle.setX(100);
        rectangle.setY(100);
        rectangle.setWidth(100);
        rectangle.setHeight(100);
        rectangle.setFill(Color.MEDIUMVIOLETRED);
        rectangle.setStrokeWidth(7);
        rectangle.setStroke(Color.AZURE);

        root.getChildren().add(rectangle);
        root.getChildren().add(line);
        root.getChildren().add(text);
        // stage.setX(50);
        // stage.setY(50);

        stage.setTitle("Git");
        stage.setScene(scene);
        stage.show();*/
    }
}
