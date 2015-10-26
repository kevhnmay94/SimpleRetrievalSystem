package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

import java.io.File;
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
        documentRawTF.setSelected(true);
        documentRawTF.requestFocus();
        documentNoIDF.setSelected(true);
        documentNoIDF.requestFocus();
        documentNoStem.setSelected(true);
        documentNoStem.requestFocus();
        queryRawTF.setSelected(true);
        queryRawTF.requestFocus();
        queryNoIDF.setSelected(true);
        queryNoIDF.requestFocus();
        queryNoStem.setSelected(true);
        queryNoStem.requestFocus();
        NoNorm.setSelected(true);
        NoNorm.requestFocus();

    }

    public void handleChooseDocument(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose document");
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.documentlocation = file.toString();
            documentTextField.setText(Vars.documentlocation);
        }
    }

    public void handleChooseStopwords(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose stopwords");
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.stoplocation = file.toString();
            documentTextField.setText(Vars.stoplocation);
        }
    }

    public void handleStartIndex(ActionEvent actionEvent) {

    }

    public void handleChooseQuery(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose query");
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.querylocation = file.toString();
            documentTextField.setText(Vars.querylocation);
        }
    }

    public void handleChooseRelevance(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose relevance judgement");
        File file = fileChooser.showOpenDialog(Vars.savedstage);
        if(file != null){
            Vars.rellocation = file.toString();
            documentTextField.setText(Vars.rellocation);
        }

    }
}
