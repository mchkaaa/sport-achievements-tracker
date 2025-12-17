import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/splash.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("FitMood - Завантаження");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("Запуск додатку...");
        long startTime = System.currentTimeMillis();

        launch(args);

        long endTime = System.currentTimeMillis();
        System.out.println("Час завантаження: " + (endTime - startTime) + " мс");
    }
}