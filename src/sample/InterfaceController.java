package sample;

import Utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import model.document;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;

/**
 * Created by User on 24/11/2015.
 */
public class InterfaceController implements Initializable {


    public Button experimentSearchButton;
    public Button experimentSearchAgain;
    public Button interactiveSearchButton;
    public Button interactiveSearchAgain;
    public TableView interactiveTable;
    public TableColumn interactiveRank;
    public TableColumn interactiveDocNo;
    public TableColumn interactiveRelevance;
    public TextArea searchResult;
    public Tab interactiveTab;
    public Tab experimentTab;
    public Tab relevanceFeedbackTab;
    public Tab searchTab;
    public TextField searchQuery;
    public TableColumn interactiveSimiliarity;
    @FXML private Label notifRelevance;
    @FXML private CheckBox queryExpension;
    @FXML private Button setRelevanceFeedback;
    @FXML private TextField documentTextField;
    @FXML private TextField queryTextField;
    @FXML private TextField relevanceTextField;
    @FXML private TextField stopwordsTextField;
    @FXML private Button chooseRelevance;
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
    @FXML private Button search;
    private final ToggleGroup documenttf;
    private final ToggleGroup documentidf;
    private final ToggleGroup norm;
    private final ToggleGroup documentstem;
    private final ToggleGroup querytf;
    private final ToggleGroup queryidf;
    private final ToggleGroup querystem;
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
    private final ToggleGroup searchMethod;
    private final ToggleGroup feedbackMethod;
    private final ToggleGroup docCollection;
    private final ToggleGroup pseudo;
    RelevanceFeedbackExperiment exp;
    RelevanceFeedbackInteractive rfi = new RelevanceFeedbackInteractive();


    public InterfaceController() {
        documenttf = new ToggleGroup();
        documentidf = new ToggleGroup();
        norm = new ToggleGroup();
        documentstem = new ToggleGroup();
        querytf = new ToggleGroup();
        queryidf = new ToggleGroup();
        querystem = new ToggleGroup();
        searchMethod = new ToggleGroup();
        feedbackMethod = new ToggleGroup();
        docCollection = new ToggleGroup();
        pseudo = new ToggleGroup();
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
        interactiveRank.setCellValueFactory(new PropertyValueFactory<TableResult,Integer>("rank"));
        interactiveDocNo.setCellValueFactory(new PropertyValueFactory<TableResult,Integer>("docNo"));
        interactiveSimiliarity.setCellValueFactory(new PropertyValueFactory<TableResult,Double>("similiarity"));
        interactiveRelevance.setVisible(false);
        interactiveRelevance.setCellValueFactory(new PropertyValueFactory<TableResult,Boolean>("relevant"));
        /* interactiveRelevance.setCellFactory(new Callback<TableColumn<TableResult,Boolean>,TableCell<TableResult,Boolean>>() {
            @Override
            public TableCell<TableResult, Boolean> call(TableColumn<TableResult, Boolean> p) {

                return new CheckBoxTableCell<TableResult, Boolean>();

            }
        }); */
        interactiveSearchAgain.setDisable(true);
        experimentSearchAgain.setDisable(true);
    }

    public void handleStartIndex(ActionEvent actionEvent) {
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
        TermsWeight.termFrequencyWeighting(Vars.documenttf, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeighting(Vars.documentidf, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());

        // PROSES BIKIN INVERTED FILE BUAT QUERY
        wordProcessor.loadIndexTabelForQueries(Vars.querystem); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(Vars.querytf, wordProcessor.getInvertedFileQuery(),wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeightingQuery(Vars.queryidf, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());

        String path = "test\\invertedFile.csv", path2 = "test\\invertedFileQuery.csv", path3 = "test\\normalFile.csv", path4 = "test\\normalFileQuery.csv";
        EksternalFile file = new EksternalFile();
        file.writeInvertedFile(path, wordProcessor.getInvertedFile());
        file.writeInvertedFileQuery(path2, wordProcessor.getInvertedFileQuery());
        file.writeNormalFile(path3, wordProcessor.getNormalFile());
        file.writeNormalFileQuery(path4, wordProcessor.getNormalFileQuery());
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

    public void handleRelevanceFeedbackButton(ActionEvent actionEvent) {
        Vars.isPseudo = ((Boolean)pseudo.getSelectedToggle().getUserData());
        Vars.topS = (Integer.parseInt(topS.getText()));
        Vars.topN = (Integer.parseInt(topN.getText()));
        Vars.useQueryExpansion = (queryExpension.isSelected());
        Vars.useSameCollection = (boolean) docCollection.getSelectedToggle().getUserData();
        notifRelevance.setText("Relevance feedback saved");
    }


    public void handleExperimentSearchButton(ActionEvent actionEvent) {
        EksternalFile file = new EksternalFile();
        Vars.documentinvertedfile = file.loadInvertedFile("test\\invertedFile.csv");
        Vars.queryinvertedfile = file.loadInvertedFileQuery("test\\invertedFileQuery.csv");
        Vars.documentnormalfile = file.loadNormalFile("test\\normalFile.csv");
        Vars.querynormalfile = file.loadNormalFileQuery("test\\normalFileQuery.csv");
        exp = new RelevanceFeedbackExperiment();
        exp.setInvertedFile(Vars.documentinvertedfile,false,Vars.documentstem);
        exp.setInvertedFileQuery(Vars.queryinvertedfile,false,Vars.querystem);
        exp.setNormalFile(Vars.documentnormalfile);
        exp.setNormalFileQuery(Vars.querynormalfile);
        exp.setIsPseudo(Vars.isPseudo);
        exp.setTopS(Vars.topS);
        exp.setTopN(Vars.topN);
        exp.setUseQueryExpansion(Vars.useQueryExpansion);
        exp.setUseSameCollection(Vars.useSameCollection);
        exp.evaluate(Vars.norm);
        searchResult.setText(exp.getSummaryWithSimilarity());
        experimentSearchAgain.setDisable(false);
    }

    public void handleExperimentSearchAgain(ActionEvent actionEvent) {
        exp.setIsPseudo(Vars.isPseudo);
        exp.setTopS(Vars.topS);
        exp.setTopN(Vars.topN);
        exp.setUseQueryExpansion(Vars.useQueryExpansion);
        exp.setUseSameCollection(Vars.useSameCollection);
        exp.secondRetrieval((Integer) feedbackMethod.getSelectedToggle().getUserData());
        searchResult.setText(exp.getSummary2WithSimilarity());
        experimentSearchAgain.setDisable(true);
    }

    public void handleInteractiveSearchButton(ActionEvent actionEvent) {
        rfi.setDocumentMode(Vars.documenttf, Vars.documentidf, Vars.documentstem);
        rfi.setQueryMode(Vars.querytf, Vars.queryidf, Vars.querystem);
        rfi.setTopS(Vars.topS);
        rfi.setTopN(Vars.topN);
        rfi.setUseSameCollection(Vars.useQueryExpansion);
        rfi.setUseQueryExpansion(Vars.useSameCollection);
        rfi.setIsPseudo(Vars.isPseudo);
        rfi.SearchDocumentsUsingQuery(searchQuery.getText(), false);

        if(!Vars.isPseudo){
            interactiveRelevance.setVisible(true);
            ObservableList<TableResult> ol = FXCollections.observableArrayList();
            int count = 1;
            for(Map.Entry<document, Double> m : rfi.result.entrySet()) {
                TableResult tr = new TableResult(count,m.getKey().getIndex(),m.getValue());
                ol.add(tr);
                count++;
                if(count == Vars.topS+1)
                    break;
            }
            interactiveTable.setItems(ol);
        }
        else{
            interactiveRelevance.setVisible(false);
            ObservableList<PseudoTableResult> ol = FXCollections.observableArrayList();
            int count = 1;
            for(Map.Entry<document, Double> m : rfi.result.entrySet()) {
                PseudoTableResult tr = new PseudoTableResult(count,m.getKey().getIndex(),m.getValue());
                ol.add(tr);
                count++;
                if(count == Vars.topS)
                    break;
            }
            interactiveTable.setItems(ol);
        }
        interactiveSearchAgain.setDisable(false);
    }

    public void handleInteractiveSearchAgain(ActionEvent actionEvent) {
        rfi.setTopS(Vars.topS);
        rfi.setTopN(Vars.topN);
        rfi.setUseSameCollection(Vars.useQueryExpansion);
        rfi.setUseQueryExpansion(Vars.useSameCollection);
        rfi.setIsPseudo(Vars.isPseudo);
        if(!Vars.isPseudo){
            ArrayList<Integer> hm = new ArrayList<>();
            ObservableList<TableResult> obs = interactiveTable.getItems();
            for(TableResult t : obs){
                if(t.isRelevant())
                    hm.add(t.getDocNo());
            }
            rfi.setRelevanceDocuments(hm);
            rfi.secondRetrieval((Integer) feedbackMethod.getSelectedToggle().getUserData());
            interactiveRelevance.setVisible(true);
            ObservableList<TableResult> ol = FXCollections.observableArrayList();
            int count = 1;
            for(Map.Entry<document, Double> m : rfi.result2.entrySet()) {
                TableResult tr = new TableResult(count,m.getKey().getIndex(),m.getValue());
                ol.add(tr);
                count++;
                if(count == Vars.topS+1)
                    break;
            }
            interactiveTable.setItems(ol);
        }
        else{
            rfi.secondRetrieval((Integer) feedbackMethod.getSelectedToggle().getUserData());
            interactiveRelevance.setVisible(false);
            ObservableList<PseudoTableResult> ol = FXCollections.observableArrayList();
            int count = 1;
            for(Map.Entry<document, Double> m : rfi.result.entrySet()) {
                PseudoTableResult tr = new PseudoTableResult(count,m.getKey().getIndex(),m.getValue());
                ol.add(tr);
                count++;
                if(count == Vars.topS)
                    break;
            }
            interactiveTable.setItems(ol);
        }
        interactiveSearchAgain.setDisable(true);
    }

    public void handleSearchTab(Event event) {
        try {
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
            EksternalFile file = new EksternalFile();
            Vars.documentinvertedfile = file.loadInvertedFile("test\\invertedFile.csv");
            Vars.queryinvertedfile = file.loadInvertedFileQuery("test\\invertedFileQuery.csv");
            Vars.documentnormalfile = file.loadNormalFile("test\\normalFile.csv");
            Vars.querynormalfile = file.loadNormalFileQuery("test\\normalFileQuery.csv");
        }
        catch(NullPointerException e){

        }
    }

    public void handleRelevanceTab(Event event) {
        notifRelevance.setText("");
    }

    public void handleExperimentTab(Event event) {
    }

    public void handleInteractiveTab(Event event) {
    }
}
