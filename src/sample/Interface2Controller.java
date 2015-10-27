package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Interface2Controller implements Initializable {

    @FXML private Button backButton;
    @FXML private RadioButton experimentQuery;
    @FXML private RadioButton interactiveQuery;
    @FXML private Button searchButton;
    @FXML private TextArea resultText;
    @FXML private TextField searchTextField;
    private final ToggleGroup searchMethod = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        experimentQuery.setUserData(true);
        interactiveQuery.setUserData(false);
        experimentQuery.setToggleGroup(searchMethod);
        interactiveQuery.setToggleGroup(searchMethod);
        experimentQuery.setSelected(true);
        experimentQuery.requestFocus();
    }

    public void handleSearchButton(ActionEvent actionEvent) {

    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("interface1.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
