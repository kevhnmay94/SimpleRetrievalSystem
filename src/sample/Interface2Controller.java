package sample;

import Utils.EksternalFile;
import Utils.Experiment;
import Utils.InputQuery;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.document;
import model.query;
import model.termWeightingQuery;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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
        Vars.documentinvertedfile = EksternalFile.loadInvertedFile("test\\invertedFile.txt");
        Vars.queryinvertedfile = EksternalFile.loadInvertedFileQuery("test\\invertedFileQuery.txt");
    }

    public void handleSearchButton(ActionEvent actionEvent) {
        String result = "";
        if((Boolean)searchMethod.getSelectedToggle().getUserData()){
            // do something with the freaking query from text
            Experiment exp = new Experiment();
            exp.setInvertedFile(Vars.documentinvertedfile);
            exp.setInvertedFileQuery(Vars.queryinvertedfile);
            exp.evaluate(Vars.norm);
            result = exp.getSummary();
        }
        else{
            String query = searchTextField.getText();
            // do something with the query
            HashMap<document, Double> mapResult = InputQuery.SearchDocumentsUsingQuery(query, Vars.norm);
        }
        resultText.setText(result);
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("interface1.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
