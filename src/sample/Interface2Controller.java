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
import java.util.Map;
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
        /* if((Boolean)searchMethod.getSelectedToggle().getUserData()){
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
            InputQuery iq = new InputQuery();
            iq.setInvertedFile(Vars.documentinvertedfile);
            iq.SearchDocumentsUsingQuery(query, Vars.norm);
            result = iq.getSummaryResult();
        }
        */
        for (int i=0; i<Vars.queryinvertedfile.getListQueryWeighting().size(); i++) {
            termWeightingQuery relation = Vars.queryinvertedfile.getListQueryWeighting().get(i);
            result += "QUERY DIPROSES : " + relation.getCurrentQuery().getQueryContent() + "\n";
            result += "COUNTER PER TERM DARI QUERY DI ATAS : \n";
            for (Map.Entry m : relation.getTermCounterInOneQuery().entrySet()) {
                result += "Term : " + (String) m.getKey() + "\n";
                result += "Counter : " + (Integer) m.getValue() + "\n";
            }
            for (Map.Entry m : relation.getTermWeightInOneQuery().entrySet()) {
                result += "Term : " + (String) m.getKey() + "\n";
                result += "Weight : " + (Double) m.getValue() + "\n";
            }
            result += "\n";
        }
        resultText.setText(result);
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("interface1.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
