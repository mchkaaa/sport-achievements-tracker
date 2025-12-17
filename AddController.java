package controllers;

import database.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Training;

// Потрібно, щоб MainController міг оновити таблицю
public class AddController {

    @FXML private TextField dateField;
    @FXML private TextField typeField;
    @FXML private TextField durationField;
    @FXML private TextField moodField;
    @FXML private TextField noteField;

    private MainController mainController;
    private Training editingTraining; // !!! НОВЕ: Поле для зберігання об'єкта, який редагується

    // Метод для встановлення посилання на MainController
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    // !!! НОВИЙ МЕТОД: Завантаження даних для редагування
    public void setTrainingToEdit(Training training) {
        this.editingTraining = training;

        if (training != null) {
            // Заповнюємо поля існуючими даними
            dateField.setText(training.getDate());
            typeField.setText(training.getType());
            durationField.setText(String.valueOf(training.getDuration()));
            moodField.setText(String.valueOf(training.getMood()));
            noteField.setText(training.getNote());


        }
    }

    @FXML
    private void onSaveClicked() {
        if (!isInputValid()) {
            return;
        }

        try {
            // 1. Збір та перетворення даних
            String date = dateField.getText().trim();
            String type = typeField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());
            int mood = Integer.parseInt(moodField.getText().trim());
            String note = noteField.getText().trim();

            if (editingTraining == null) {
                // РЕЖИМ ДОДАВАННЯ (Create)
                DatabaseHelper.insertTraining(date, type, duration, mood, note);
                showAlert("Успіх! 🎉", "Тренування успішно додано.", Alert.AlertType.INFORMATION);
            } else {
                // РЕЖИМ РЕДАГУВАННЯ (Update)
                DatabaseHelper.updateTraining(editingTraining.getId(), date, type, duration, mood, note);
                showAlert("Успіх! ✏️", "Тренування успішно оновлено.", Alert.AlertType.INFORMATION);
            }

            // 2. Оновлення головної таблиці
            if (mainController != null) {
                mainController.loadAll();
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Тривалість та Настрій мають бути цілими числами.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Помилка програми", "Не вдалося зберегти/оновити тренування. " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancelClicked() {
        closeWindow();
    }

    private void closeWindow() {
        if (dateField != null && dateField.getScene() != null) {
            Stage stage = (Stage) dateField.getScene().getWindow();
            stage.close();
        }
    }

    // --- Валідація вводу ---
    private boolean isInputValid() {
        String errorMessage = "";

        if (dateField.getText() == null || dateField.getText().isEmpty() || !dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorMessage += "Некоректна Дата (формат YYYY-MM-DD)!\n";
        }
        if (typeField.getText() == null || typeField.getText().isEmpty()) {
            errorMessage += "Не вказано Тип тренування!\n";
        }
        try {
            int duration = Integer.parseInt(durationField.getText());
            if (duration <= 0) {
                errorMessage += "Тривалість має бути більше 0!\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Тривалість має бути числом!\n";
        }
        try {
            int mood = Integer.parseInt(moodField.getText());
            if (mood < 1 || mood > 5) {
                errorMessage += "Настрій має бути числом від 1 до 5!\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Настрій має бути числом!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Помилка валідації", "Будь ласка, виправте некоректні поля:", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}