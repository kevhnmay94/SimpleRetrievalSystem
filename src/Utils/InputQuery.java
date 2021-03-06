package Utils;

import model.document;
import model.indexTabel;
import model.normalTabel;
import model.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by khaidzir on 27/10/2015.
 */
public class InputQuery {

    int qTfMode, qIdfMode;
    boolean isStem;
    PreprocessWords wordProcessor;
    static HashMap<document, Double> result;
    String query;
    double exectime;

    public static HashMap<document, Double> getResult() {
        return result;
    }

    public InputQuery() {
        wordProcessor = new PreprocessWords();
    }

    public void setDocumentMode(int tfmode, int idfmode, boolean stem){
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfmode, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeighting(idfmode, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish-start) + " ms.\n");
    }

    public void setQueryMode(int tfmode, int idfmode, boolean stem) {
        qTfMode = tfmode;
        qIdfMode = idfmode;
        isStem = stem;
    }

    public void setInvertedFile (indexTabel idxTable, boolean isInvertedFileCreated, boolean isStemmingApplied) {
        wordProcessor.setInvertedFile(idxTable);
        wordProcessor.loadDocumentsFinal(isInvertedFileCreated,isStemmingApplied);
    }

    public void setNormalFile (normalTabel normalFile) {
        wordProcessor.setNormalFile(normalFile);
    }

    public void SearchDocumentsUsingQuery(String query, boolean isNormalize) {
        double start, finish;
        this.query = query;
        result = new HashMap<>();

        // Proses query
        System.out.println("Indexing queries...");
        start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForManualQuery(query, isStem);
        TermsWeight.termFrequencyWeightingQuery(qTfMode, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeightingQuery(qIdfMode, wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getInvertedFile(), wordProcessor.getNormalFile());

        finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");

        // Hitung similarity query dengan dokumen
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        query q = new query(0, query);

        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQueryManual(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), wordProcessor.getNormalFileQueryManual(), isNormalize);
            result.put(Document, weight);
        }
        result = (HashMap)DocumentRanking.rankDocuments(result);


        finish = System.currentTimeMillis();
        exectime = finish-start;
        System.out.println("Calculating similarity done in " + exectime + " ms.\n");
    }

    public String getSummaryResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.size());
        sb.append(result.size() > 1 ? " results" : " result");
        sb.append("(" + (exectime/1000) + " seconds)\n\n");
        for(document doc : result.keySet()) {
            sb.append("Title : " + doc.getJudul());       //sb.append("\n");
            sb.append("Author : " + doc.getAuthor());     //sb.append("\n");
            sb.append("Content : \n" + doc.getKonten());  sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Setting awal awal
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        InputQuery iq = new InputQuery();

        // Setting mode
        iq.setDocumentMode(1, 0, true);
        iq.setQueryMode(1, 0, true);

        // Query dan hasil
        String query = "computer";
        iq.SearchDocumentsUsingQuery(query, false);

        // Print
        System.out.println(iq.getSummaryResult());
    }

}
