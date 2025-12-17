package controllers;

import database.DatabaseHelper;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;

public class SplashController {
    @FXML
    public void initialize() {
        // Створюємо таблицю якщо її ще немає
        DatabaseHelper.createTableIfNotExists();

        // Пауза 1.5 секунди, потім відкриваємо main.fxml
        PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
        pt.setOnFinished(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
                Scene scene = new Scene(loader.load());
                // завантажити світлу тему як дефолт
                scene.getStylesheets().add(getClass().getResource("/views/styles/light.css").toExternalForm());

                // Знаходимо поточне вікно (правильний спосіб)
                Stage currentStage = getCurrentStage();
                if (currentStage != null) {
                    currentStage.setScene(scene);
                    currentStage.setTitle("FitMood - Щоденник тренувань");
                    currentStage.setResizable(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pt.play();
    }

    // Метод для отримання поточного Stage
    private Stage getCurrentStage() {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage && window.isShowing()) {
                return (Stage) window;
            }
        }
        return null;
    }
}