package com.plr.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX Application entry point for TikiTikiPhonk.
 * Loads FXML from classpath (src/main/resources/).
 */
public class JavaFXApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("TikiTikiPhonk");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        showScene("fxml/login.fxml", "TikiTikiPhonk - Login", 480, 580);
        primaryStage.show();
    }

    /**
     * Switches the primary stage to a new scene from the given FXML classpath path.
     */
    public static void showScene(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                JavaFXApp.class.getClassLoader().getResource(fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);

            String css = Objects.requireNonNull(
                JavaFXApp.class.getClassLoader().getResource("css/style.css"),
                "Global stylesheet 'css/style.css' not found on classpath"
            ).toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot load FXML: " + fxmlPath, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
