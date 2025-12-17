package controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDate;
import javafx.stage.FileChooser;
import javafx.scene.Parent;
import database.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Training;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;


import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator; // Додано для порівняння рекордів


public class MainController {

    @FXML private TableView<Training> tableView;
    @FXML private TableColumn<Training, String> colDate;
    @FXML private TableColumn<Training, String> colType;
    @FXML private TableColumn<Training, Integer> colDuration;
    @FXML private TableColumn<Training, String> colMoodEmoji;
    @FXML private TableColumn<Training, String> colNote;

    @FXML private TextField searchField;
    @FXML private DatePicker filterDatePicker;
    @FXML private ChoiceBox<String> themeChoice; // Light / Dark

    @FXML private HBox dashboardBox;
    @FXML private PieChart activityPieChart;

    // !!! НОВЕ ПОЛЕ: КОНТЕЙНЕР ДЛЯ ПЕРСОНАЛЬНИХ РЕКОРДІВ !!!
    @FXML private VBox recordsBox;

    private ObservableList<Training> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Колонки
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colMoodEmoji.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getMoodEmoji()));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Клітинка для duration з кольором
        colDuration.setCellFactory(column -> new TableCell<Training, Integer>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value.toString());
                    if (value >= 60) {
                        setStyle("-fx-background-color: #2ECC71"); // зелений
                    } else if (value >= 30) {
                        setStyle("-fx-background-color: #F1C40F"); // жовтий
                    } else {
                        setStyle("-fx-background-color: #E74C3C"); // червоний
                    }
                }
            }
        });

        themeChoice.getItems().addAll("Light", "Dark");
        themeChoice.setValue("Light");
        themeChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> applyTheme(newV));

        loadAll();

        // Пошук при натисканні Enter
        searchField.setOnAction(e -> doSearch());
        filterDatePicker.setOnAction(e -> filterByDate());
    }

    private void applyTheme(String theme) {
        Scene scene = tableView.getScene();
        if (scene == null) return;
        scene.getStylesheets().clear();
        if ("Dark".equals(theme)) {
            scene.getStylesheets().add(getClass().getResource("/views/styles/dark.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/views/styles/light.css").toExternalForm());
        }
    }

    @FXML
    public void loadAll() {
        long startTime = System.currentTimeMillis();
        List<Training> list = DatabaseHelper.getAllTrainings();
        long endTime = System.currentTimeMillis(); // Засікаємо час фінішу
        System.out.println("Час виконання запиту до БД: " + (endTime - startTime) + " мс");
        data.clear();
        data.addAll(list);
        tableView.setItems(data);

        updateDashboard();
        updatePieChart();
        updateRecords(); // !!! ВИКЛИК ОНОВЛЕННЯ РЕКОРДІВ

    }

    @FXML
    public void onExportClicked() {
        // 1. Перевірка наявності даних
        if (data.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Немає даних для експорту.");
            alert.showAndWait();
            return;
        }

        // 2. Створення діалогу вибору файлу
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Експорт даних у CSV");

        // Встановлюємо початкову назву файлу (наприклад, fitmood_export_2025-11-21.csv)
        String defaultFileName = "fitmood_export_" + LocalDate.now().toString() + ".csv";
        fileChooser.setInitialFileName(defaultFileName);

        // Додаємо фільтр розширення
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        // Показуємо діалог збереження (використовуємо Stage з TableView)
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            // 3. Запис даних у файл
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

                // Заголовок CSV (колонки)
                writer.println("ID,Дата,Тип,Тривалість (хв),Настрій (1-5),Коментар");

                // Запис даних (ітерація по ObservableList data)
                for (Training t : data) {
                    // Використовуємо String.format для коректного форматування CSV
                    // Коментарі беремо в лапки і екрануємо внутрішні лапки (стандарт CSV)
                    String line = String.format("%d,%s,%s,%d,%d,\"%s\"",
                            t.getId(),
                            t.getDate(),
                            t.getType(),
                            t.getDuration(),
                            t.getMood(),
                            t.getNote().replace("\"", "\"\"")); // Екранування лапок
                    writer.println(line);
                }

                // 4. Повідомлення про успіх
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Дані успішно експортовано у:\n" + file.getAbsolutePath());
                alert.setTitle("Експорт успішний");
                alert.setHeaderText("Операція завершена");
                alert.showAndWait();

            } catch (IOException e) {
                // 5. Обробка помилок запису
                Alert alert = new Alert(Alert.AlertType.ERROR, "Помилка при записі файлу: " + e.getMessage());
                alert.setTitle("Помилка файлової системи");
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void onDeleteClicked() {
        Training selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити тренування від " + selected.getDate() +
                            " (" + selected.getType() + ")?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Підтвердження видалення");
            confirm.setHeaderText("Видалення запису");
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                DatabaseHelper.deleteTraining(selected.getId());
                loadAll();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Будь ласка, оберіть запис для видалення.");
            alert.setTitle("Попередження");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    @FXML
    public void onEditClicked() {
        Training selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add.fxml"));
                Parent root = loader.load();

                AddController formController = loader.getController();
                formController.setMainController(this);
                formController.setTrainingToEdit(selected);

                Scene scene = new Scene(root);
                scene.getStylesheets().addAll(tableView.getScene().getStylesheets());

                Stage stage = new Stage();
                stage.setTitle("Редагувати тренування #" + selected.getId());

                stage.setScene(scene);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(tableView.getScene().getWindow());
                stage.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не вдалося відкрити форму редагування. " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Будь ласка, оберіть запис для редагування.");
            alert.showAndWait();
        }
    }

    @FXML
    public void onAddClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add.fxml"));
            Parent root = loader.load();

            AddController formController = loader.getController();
            formController.setMainController(this);

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(tableView.getScene().getStylesheets());

            Stage stage = new Stage();
            stage.setTitle("Додати нове тренування");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openProgress() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/progress.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Графік прогресу");
            stage.setScene(scene);
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            loadAll();
        } else {
            List<Training> list = DatabaseHelper.searchTrainings(q);
            data.setAll(list);
        }
        updateDashboard();
        updatePieChart();
        updateRecords(); // !!! ВИКЛИК ОНОВЛЕННЯ РЕКОРДІВ
    }

    @FXML
    public void filterByDate() {
        if (filterDatePicker.getValue() == null) {
            loadAll();
            return;
        }
        String dateStr = filterDatePicker.getValue().toString();
        List<Training> all = DatabaseHelper.getAllTrainings();
        data.setAll(all.stream().filter(t -> dateStr.equals(t.getDate())).collect(Collectors.toList()));

        updateDashboard();
        updatePieChart();
        updateRecords(); // !!! ВИКЛИК ОНОВЛЕННЯ РЕКОРДІВ
    }

    // --- МЕТОД: DASHBOARD ---
    private void updateDashboard() {
        int count = data.size();
        int total = data.stream().mapToInt(Training::getDuration).sum();
        int avg = count == 0 ? 0 : Math.round((float)total / count);

        dashboardBox.getChildren().clear();

        // 1. Картка "Записів"
        dashboardBox.getChildren().add(createStatCard("📝", "Записів", String.valueOf(count), "#5E81AC"));

        // 2. Картка "Загальна тривалість"
        dashboardBox.getChildren().add(createStatCard("⏱️", "Загальна тривалість", total + " хв", "#5E81AC"));

        // 3. Картка "Середня тривалість"
        dashboardBox.getChildren().add(createStatCard("⚖️", "Середня тривалість", avg + " хв", "#5E81AC"));
    }

    // --- МЕТОД: ОНОВЛЕННЯ КРУГОВОЇ ДІАГРАМИ ---
    private void updatePieChart() {
        List<PieChart.Data> pieData = data.stream()
                .collect(Collectors.groupingBy(
                        Training::getType,
                        Collectors.summingInt(Training::getDuration)
                ))
                .entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        activityPieChart.setData(FXCollections.observableArrayList(pieData));

        if (pieData.isEmpty()) {
            activityPieChart.setTitle("Немає даних");
        } else {
            activityPieChart.setTitle("Розподіл за типом");
        }
    }

    // --- НОВИЙ МЕТОД: ОНОВЛЕННЯ ПЕРСОНАЛЬНИХ РЕКОРДІВ ---
    private void updateRecords() {
        // 1. Отримання рекордів (Знаходимо MAX duration для кожного типу)
        List<Training> bests = data.stream()
                .collect(Collectors.groupingBy(
                        Training::getType,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingInt(Training::getDuration)),
                                opt -> opt.orElse(null)
                        )
                ))
                .values().stream()
                .filter(t -> t != null)
                .collect(Collectors.toList());

        recordsBox.getChildren().clear();

        if (bests.isEmpty()) {
            recordsBox.getChildren().add(new Label("Немає записів для рекордів."));
            return;
        }

        // 2. Відображення рекордів
        for (Training t : bests) {
            Label recordLabel = new Label(t.getType() + ": " + t.getDuration() + " хв");
            recordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FFFFF;");

            Label dateLabel = new Label("  (" + t.getDate() + ")");
            dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #755A57;");

            HBox recordItem = new HBox(5);
            recordItem.getChildren().addAll(recordLabel, dateLabel);
            recordItem.setPadding(new Insets(2, 0, 2, 0));

            recordsBox.getChildren().add(recordItem);
        }
    }


    // --- ДОПОМІЖНИЙ МЕТОД ДЛЯ СТВОРЕННЯ КАРТОК ---
    private VBox createStatCard(String icon, String title, String value, String color) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title"); // !!! ВИКОРИСТОВУЄМО КЛАС CSS ДЛЯ СТИЛІЗАЦІЇ ПІДПИСУ !!!

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value"); // !!! ВИКОРИСТОВУЄМО КЛАС CSS ДЛЯ СТИЛІЗАЦІЇ ВЕЛИКОГО ЗНАЧЕННЯ !!!

        VBox card = new VBox(5);
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);

        // Стилі для картки
        card.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #ff99aa;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.0, 0, 3);"
        );
        return card;
    }
}