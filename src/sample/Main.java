package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Scene interface1,interface2;

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
        stage.setScene(new Scene(root));
        stage.show();

        Vars.savedstage = stage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
