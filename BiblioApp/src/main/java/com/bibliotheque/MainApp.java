package com.bibliotheque;

import com.bibliotheque.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        DatabaseUtil.initializeDatabase();

        loadScene("login");
        stage.setTitle("📚 Gestion Bibliothèque");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void loadScene(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/bibliotheque/fxml/" + fxmlName + ".fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                MainApp.class.getResource("/com/bibliotheque/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
