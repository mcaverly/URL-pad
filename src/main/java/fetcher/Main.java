package fetcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFx Starting point.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Launcher.fxml"));
        primaryStage.setTitle("URL pad");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        Scene scene = new Scene(root, 300, 64);
        primaryStage.setScene(scene);
        primaryStage.show();
        }

    public static void main(String[] args) {
        launch(args);
    }

}
