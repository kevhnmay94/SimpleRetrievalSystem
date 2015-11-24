package sample;

import Utils.*;
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
    @FXML private RadioButton pseudoFeedback;
    @FXML private RadioButton nonPseudoFeedback;
    private final ToggleGroup searchMethod = new ToggleGroup();
    private final ToggleGroup feedbackMethod = new ToggleGroup();
    private final ToggleGroup docCollection = new ToggleGroup();
    private final ToggleGroup pseudo = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        experimentQuery.setUserData(true);
        interactiveQuery.setUserData(false);
        experimentQuery.setToggleGroup(searchMethod);
        interactiveQuery.setToggleGroup(searchMethod);
        experimentQuery.setSelected(true);
        experimentQuery.requestFocus();
        rocchio.setUserData(1);
        ideregular.setUserData(2);
        idedechi.setUserData(3);
        pseudoFeedback.setUserData(true);
        nonPseudoFeedback.setUserData(false);
        sameRetrieval.setUserData(true);
        diffRetrieval.setUserData(false);
        rocchio.setToggleGroup(feedbackMethod);
        ideregular.setToggleGroup(feedbackMethod);
        idedechi.setToggleGroup(feedbackMethod);
        pseudoFeedback.setToggleGroup(pseudo);
        nonPseudoFeedback.setToggleGroup(pseudo);
        sameRetrieval.setToggleGroup(docCollection);
        diffRetrieval.setToggleGroup(docCollection);
        EksternalFile file = new EksternalFile();
        Vars.documentinvertedfile = file.loadInvertedFile("test\\invertedFile.csv");
        Vars.queryinvertedfile = file.loadInvertedFileQuery("test\\invertedFileQuery.csv");
        Vars.documentnormalfile = file.loadNormalFile("test\\normalFile.csv");
        Vars.querynormalfile = file.loadNormalFileQuery("test\\normalFileQuery.csv");
        Vars.rexp = new RelevanceFeedbackExperiment();
        Vars.rexp.setInvertedFile(Vars.documentinvertedfile,false,Vars.documentstem);
        Vars.rexp.setInvertedFileQuery(Vars.queryinvertedfile,false,Vars.querystem);
        Vars.rexp.setNormalFile(Vars.documentnormalfile);
        Vars.rexp.setNormalFileQuery(Vars.querynormalfile);
    }

    public void handleSearchButton(ActionEvent actionEvent) {
        String result = "";
        if((Boolean)searchMethod.getSelectedToggle().getUserData()){
            // do something with the freaking query from text

            Vars.rexp.evaluate(Vars.norm);
            result = Vars.rexp.getSummary();
            resultText.setText(result);
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

    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("interface1.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void handleRelevanceFeedbackButton(ActionEvent actionEvent){
        Vars.rexp.setIsPseudo(false);
        Vars.rexp.setTopS(10);
        Vars.rexp.setTopN(5);
        Vars.rexp.setUseQueryExpansion(true);
        Vars.rexp.setUseSameCollection(true);
        Vars.rexp.secondRetrieval(1);
        String result = Vars.rexp.getSummary2();
        resultText.setText(result);
    }
}
