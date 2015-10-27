package sample;

import Utils.EksternalFile;
import Utils.PreprocessWords;
import Utils.TermsWeight;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Interface1Controller implements Initializable {

    @FXML private TextField documentTextField;
    @FXML private TextField queryTextField;
    @FXML private TextField relevanceTextField;
    @FXML private TextField stopwordsTextField;
    @FXML private Button chooseDocument;
    @FXML private Button chooseStopwords;
    @FXML private Button chooseQuery;
    @FXML private Button startIndex;
    @FXML private RadioButton documentNoTF;
    @FXML private RadioButton documentRawTF;
    @FXML private RadioButton documentBinaryTF;
    @FXML private RadioButton documentAugTF;
    @FXML private RadioButton documentLogTF;
    @FXML private RadioButton documentNoIDF;
    @FXML private RadioButton documentIDF;
    @FXML private RadioButton NoNorm;
    @FXML private RadioButton Norm;
    @FXML private RadioButton documentNoStem;
    @FXML private RadioButton documentStem;
    @FXML private RadioButton queryNoTF;
    @FXML private RadioButton queryRawTF;
    @FXML private RadioButton queryBinaryTF;
    @FXML private RadioButton queryAugTF;
    @FXML private RadioButton queryLogTF;
    @FXML private RadioButton queryNoIDF;
    @FXML private RadioButton queryIDF;
    @FXML private RadioButton queryNoStem;
    @FXML private RadioButton queryStem;
    private final ToggleGroup documenttf;
    private final ToggleGroup documentidf;
    private final ToggleGroup norm;
    private final ToggleGroup documentstem;
    private final ToggleGroup querytf;
    private final ToggleGroup queryidf;
    private final ToggleGroup querystem;


    public Interface1Controller() {
        documenttf = new ToggleGroup();
        documentidf = new ToggleGroup();
        norm = new ToggleGroup();
        documentstem = new ToggleGroup();
        querytf = new ToggleGroup();
        queryidf = new ToggleGroup();
        querystem = new ToggleGroup();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        documentNoTF.setUserData(0);
        documentRawTF.setUserData(1);
        documentLogTF.setUserData(2);
        documentBinaryTF.setUserData(3);
        documentAugTF.setUserData(4);
        documentNoIDF.setUserData(0);
        documentIDF.setUserData(1);
        NoNorm.setUserData(false);
        Norm.setUserData(true);
        documentNoStem.setUserData(false);
        documentStem.setUserData(true);
        queryNoTF.setUserData(0);
        queryRawTF.setUserData(1);
        queryLogTF.setUserData(2);
        queryBinaryTF.setUserData(3);
        queryAugTF.setUserData(4);
        queryNoIDF.setUserData(0);
        queryIDF.setUserData(1);
        queryNoStem.setUserData(false);
        queryStem.setUserData(true);
        documentNoTF.setToggleGroup(documenttf);
        documentRawTF.setToggleGroup(documenttf);
        documentBinaryTF.setToggleGroup(documenttf);
        documentAugTF.setToggleGroup(documenttf);
        documentLogTF.setToggleGroup(documenttf);
        documentNoIDF.setToggleGroup(documentidf);
        documentIDF.setToggleGroup(documentidf);
        NoNorm.setToggleGroup(norm);
        Norm.setToggleGroup(norm);
        documentNoStem.setToggleGroup(documentstem);
        documentStem.setToggleGroup(documentstem);
        queryNoTF.setToggleGroup(querytf);
        queryRawTF.setToggleGroup(querytf);
        queryBinaryTF.setToggleGroup(querytf);
        queryAugTF.setToggleGroup(querytf);
        queryLogTF.setToggleGroup(querytf);
        queryNoIDF.setToggleGroup(queryidf);
        queryIDF.setToggleGroup(queryidf);
        queryNoStem.setToggleGroup(querystem);
        queryStem.setToggleGroup(querystem);
        if(Vars.documenttf == 0) {
            documentNoTF.setSelected(true);
            documentNoTF.requestFocus();
        }
        else if(Vars.documenttf == 1){
            documentRawTF.setSelected(true);
            documentRawTF.requestFocus();
        }
        else if(Vars.documenttf == 2){
            documentLogTF.setSelected(true);
            documentLogTF.requestFocus();
        }
        else if(Vars.documenttf == 3){
            documentBinaryTF.setSelected(true);
            documentBinaryTF.requestFocus();
        }
        else if(Vars.documenttf == 4){
            documentAugTF.setSelected(true);
            documentAugTF.requestFocus();
        }
        if(Vars.documentidf == 0){
            documentNoIDF.setSelected(true);
            documentNoIDF.requestFocus();
        }
        else if(Vars.documentidf == 1){
            documentIDF.setSelected(true);
            documentIDF.requestFocus();
        }
        if(!Vars.documentstem){
            documentNoStem.setSelected(true);
            documentNoStem.requestFocus();
        }
        else{
            documentStem.setSelected(true);
            documentStem.requestFocus();
        }
        if(Vars.querytf == 0) {
            queryNoTF.setSelected(true);
            queryNoTF.requestFocus();
        }
        else if(Vars.querytf == 1){
            queryRawTF.setSelected(true);
            queryRawTF.requestFocus();
        }
        else if(Vars.querytf == 2){
            queryLogTF.setSelected(true);
            queryLogTF.requestFocus();
        }
        else if(Vars.querytf == 3){
            queryBinaryTF.setSelected(true);
            queryBinaryTF.requestFocus();
        }
        else if(Vars.querytf == 4){
            queryAugTF.setSelected(true);
            queryAugTF.requestFocus();
        }
        if(Vars.queryidf == 0){
            queryNoIDF.setSelected(true);
            queryNoIDF.requestFocus();
        }
        else if(Vars.queryidf == 1){
            queryIDF.setSelected(true);
            queryIDF.requestFocus();
        }
        if(!Vars.querystem){
            queryNoStem.setSelected(true);
            queryNoStem.requestFocus();
        }
        else{
            queryStem.setSelected(true);
            queryStem.requestFocus();
        }
        if(!Vars.norm) {
            NoNorm.setSelected(true);
            NoNorm.requestFocus();
        }
        else {
            Norm.setSelected(true);
            Norm.requestFocus();
        }
        documentTextField.setText(Vars.documentlocation);
        queryTextField.setText(Vars.querylocation);
        relevanceTextField.setText(Vars.rellocation);
        stopwordsTextField.setText(Vars.stoplocation);

    }

    public void handleChooseDocument(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose document");
        fileChooser.setInitialDirectory(new File("test"));
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.documentlocation = file.toString();
            documentTextField.setText(Vars.documentlocation);
        }
    }

    public void handleChooseStopwords(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose stopwords");
        fileChooser.setInitialDirectory(new File("test"));
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.stoplocation = file.toString();
            stopwordsTextField.setText(Vars.stoplocation);
        }
    }

    public void handleStartIndex(ActionEvent actionEvent) throws IOException {
        Vars.documentidf = (Integer) documentidf.getSelectedToggle().getUserData();
        Vars.documenttf = (Integer) documenttf.getSelectedToggle().getUserData();
        Vars.documentstem = (Boolean) documentstem.getSelectedToggle().getUserData();
        Vars.queryidf = (Integer) queryidf.getSelectedToggle().getUserData();
        Vars.querytf = (Integer) querytf.getSelectedToggle().getUserData();
        Vars.querystem = (Boolean) querystem.getSelectedToggle().getUserData();
        Vars.norm = (Boolean) norm.getSelectedToggle().getUserData();
        Vars.documentlocation = documentTextField.getText();
        Vars.stoplocation = stopwordsTextField.getText();
        Vars.querylocation = queryTextField.getText();
        Vars.rellocation = relevanceTextField.getText();

        PreprocessWords wordProcessor = new PreprocessWords();
        EksternalFile.setPathDocumentsFile(Vars.documentlocation);
        EksternalFile.setPathQueriesFile(Vars.querylocation);
        EksternalFile.setPathQrelsFile(Vars.rellocation);
        EksternalFile.setPathStopWordsFile(Vars.stoplocation);

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(Vars.documentstem);
        TermsWeight.termFrequencyWeighting(Vars.documenttf, wordProcessor.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(Vars.documentidf, wordProcessor.getInvertedFile());

        // PROSES BUAT INVERTED FILE BUAT QUERY
        wordProcessor.loadIndexTabelForQueries(Vars.querystem); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(Vars.querytf, wordProcessor.getInvertedFileQuery());
        TermsWeight.inverseDocumentWeightingQuery(Vars.queryidf, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile());

        String path = "test\\invertedFile.txt";
        EksternalFile.writeInvertedFile(path, wordProcessor.getInvertedFile());

        Parent root = FXMLLoader.load(getClass().getResource("interface2.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));

    }

    public void handleChooseQuery(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose query");
        fileChooser.setInitialDirectory(new File("test"));
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.querylocation = file.toString();
            queryTextField.setText(Vars.querylocation);
        }
    }

    public void handleChooseRelevance(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose relevance judgement");
        fileChooser.setInitialDirectory(new File("test"));
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.rellocation = file.toString();
            relevanceTextField.setText(Vars.rellocation);
        }

    }
}
