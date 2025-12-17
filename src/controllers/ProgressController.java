package controllers;

// Імпорт необхідних класів для роботи з базою даних, FXML та графіками
import database.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.util.Map;

/**
 * Контролер для вікна "Графік прогресу" (progress.fxml).
 * Відповідає за візуалізацію даних про тренування у вигляді лінійної діаграми.
 */
public class ProgressController {

    // Зв'язування з компонентом LineChart у FXML-файлі.
    // <String, Number> означає: вісь X - текст (дати), вісь Y - числа (хвилини).
    @FXML private LineChart<String, Number> lineChart;

    /**
     * Метод ініціалізації, який автоматично викликається JavaFX
     * після завантаження FXML-файлу.
     */
    @FXML
    public void initialize() {
        // Одразу після відкриття вікна завантажуємо та малюємо графік
        loadProgress();
    }

    /**
     * Метод для отримання даних з бази та заповнення графіка.
     */
    private void loadProgress() {
        // 1. Отримуємо агреговані дані з бази даних через DatabaseHelper.
        // Map містить пари: "Дата" (String) -> "Сумарна тривалість" (Integer).
        Map<String, Integer> data = DatabaseHelper.getProgressData();

        // 2. Створюємо нову серію даних (одну лінію на графіку).
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // 3. Встановлюємо назву для серії, яка відобразиться в легенді графіка.
        series.setName("Хвилини тренувань (день)");

        // 4. Проходимося по всіх отриманих даних у циклі.
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            // Додаємо кожну точку даних у серію:
            // e.getKey() - це дата (вісь X), e.getValue() - це хвилини (вісь Y).
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        // 5. Додаємо сформовану серію до компонента LineChart, щоб відобразити її на екрані.
        lineChart.getData().add(series);
    }
}
