package io.github.bambooisland.bamboo;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static UIController con = null;

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            con = loader.getController();
            PlayerManager.init();
            stage.setMaximized(true);
            stage.setTitle("Bamboo Music Player");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
