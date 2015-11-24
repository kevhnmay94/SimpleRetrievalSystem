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
import model.termWeightingDocument;
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
    @FXML private TextField topN;
    @FXML private TextField topS;
    @FXML private RadioButton sameRetrieval;
    @FXML private RadioButton diffRetrieval;
    @FXML private RadioButton rocchio;
    @FXML private RadioButton ideregular;
    @FXML private RadioButton idedechi;
    private final ToggleGroup searchMethod = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        experimentQuery.setUserData(true);
        interactiveQuery.setUserData(false);
        experimentQuery.setToggleGroup(searchMethod);
        interactiveQuery.setToggleGroup(searchMethod);
        experimentQuery.setSelected(true);
        experimentQuery.requestFocus();
        EksternalFile file = new EksternalFile();
        Vars.documentinvertedfile = file.loadInvertedFile("test\\invertedFile.csv");
        Vars.queryinvertedfile = file.loadInvertedFileQuery("test\\invertedFileQuery.csv");
        Vars.documentnormalfile = file.loadNormalFile("test\\normalFile.csv");
        Vars.querynormalfile = file.loadNormalFileQuery("test\\normalFileQuery.csv");
    }

    public void handleSearchButton(ActionEvent actionEvent) {
        String result = "";
        if((Boolean)searchMethod.getSelectedToggle().getUserData()){
            // do something with the freaking query from text
            Experiment exp = new Experiment();
            exp.setInvertedFile(Vars.documentinvertedfile,false,Vars.documentstem);
            exp.setInvertedFileQuery(Vars.queryinvertedfile,false,Vars.querystem);
            exp.setNormalFile(Vars.documentnormalfile);
            exp.setNormalFileQuery(Vars.querynormalfile);
            exp.evaluate(Vars.norm);
            result = exp.getSummary();
        }
        else{
            String query = searchTextField.getText();
            // do something with the query
            InputQuery iq = new InputQuery();
            iq.setInvertedFile(Vars.documentinvertedfile,false,Vars.documentstem);
            iq.setNormalFile(Vars.documentnormalfile);
            iq.SearchDocumentsUsingQuery(query, Vars.norm);
            result = iq.getSummaryResult();
        }
        resultText.setText(result);
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("interface1.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void handleRelevanceFeedbackButton(ActionEvent actionEvent){

    }
}
