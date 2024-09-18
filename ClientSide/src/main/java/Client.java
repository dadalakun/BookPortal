import controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class Client extends Application {
    @Override
    public void start(Stage applicationStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        applicationStage.setTitle("Main Application");
        applicationStage.setScene(scene);
        applicationStage.show();

        MainController mainController = fxmlLoader.getController();

        applicationStage.setOnCloseRequest(event -> {
            mainController.closeConnection();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
