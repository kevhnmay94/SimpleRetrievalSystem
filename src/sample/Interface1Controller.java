package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

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
    @FXML private RadioButton documentNoNorm;
    @FXML private RadioButton documentNorm;
    @FXML private RadioButton documentNoStem;
    @FXML private RadioButton documentStem;
    @FXML private RadioButton queryNoTF;
    @FXML private RadioButton queryRawTF;
    @FXML private RadioButton queryBinaryTF;
    @FXML private RadioButton queryAugTF;
    @FXML private RadioButton queryLogTF;
    @FXML private RadioButton queryNoIDF;
    @FXML private RadioButton queryIDF;
    @FXML private RadioButton queryNoNorm;
    @FXML private RadioButton queryNorm;
    @FXML private RadioButton queryNoStem;
    @FXML private RadioButton queryStem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
