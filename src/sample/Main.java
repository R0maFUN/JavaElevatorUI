package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");

        Scene mainScene = new Scene(root, 600, 700);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
