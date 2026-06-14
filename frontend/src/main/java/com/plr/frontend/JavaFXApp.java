package com.plr.frontend;

import com.plr.frontend.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        primaryStage.getIcons().add(new Image(
            JavaFXApp.class.getClassLoader().getResourceAsStream("images/TikiTikiPhonk Logo.png")
        ));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(true);

        showScene("fxml/login.fxml", "TikiTikiPhonk - Login", 480, 580);
        primaryStage.show();
    }

    /**
     * Switches the primary stage to a new scene from the given FXML classpath path.
     * The window stays maximized across all scene transitions.
     */
    public static void showScene(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                JavaFXApp.class.getClassLoader().getResource(fxmlPath)
            );
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            
            if (currentScene == null) {
                // Untuk pertama kali buka aplikasi
                Scene scene = new Scene(root, width, height);
                String css = Objects.requireNonNull(
                    JavaFXApp.class.getClassLoader().getResource("css/style.css"),
                    "Global stylesheet 'css/style.css' not found on classpath"
                ).toExternalForm();
                scene.getStylesheets().add(css);
                ThemeManager.getInstance().applyToScene(scene);
                
                primaryStage.setTitle(title);
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.setMaximized(true);
            } else {
                // Untuk transisi halaman berikutnya (Login -> Main atau Main -> Login)
                // Hanya ganti isinya (root) agar status Full Screen OS tidak rusak/overshoot
                currentScene.setRoot(root);
                primaryStage.setTitle(title);
                
                // Menerapkan tema saat ini ke root yang baru
                ThemeManager.getInstance().applyToScene(currentScene);

                // Memastikan maximized tetap true tanpa memancing flicker
                if (!primaryStage.isMaximized()) {
                    primaryStage.setMaximized(true);
                }
            }
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
