package com.oks.okslabs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainClass extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("find-ports.fxml")));
        Scene scene = new Scene(root);

        stage.setScene(scene);

        stage.setTitle("FindPorts");
        stage.setWidth(600);
        stage.setHeight(400);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}