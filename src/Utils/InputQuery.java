package Utils;

import model.document;
import model.indexTabel;
import model.query;

import java.util.HashMap;

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

    public InputQuery() {
        wordProcessor = new PreprocessWords();
    }

    public void setDocumentMode(int tfmode, int idfmode, boolean stem){
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfmode, wordProcessor.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(idfmode, wordProcessor.getInvertedFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish-start) + " ms.\n");
    }

    public void setQueryMode(int tfmode, int idfmode, boolean stem) {
        qTfMode = tfmode;
        qIdfMode = idfmode;
        isStem = stem;
    }

    public void setInvertedFile(indexTabel idxtab) {
        wordProcessor.setInvertedFile(idxtab);
        wordProcessor.loadDocumentsFinal();
    }

    public void SearchDocumentsUsingQuery(String query, boolean isNormalize) {
        double start, finish;
        this.query = query;
        result = new HashMap<>();

        // Proses query
        System.out.println("Indexing queries...");
        start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForManualQuery(query, isStem);
        TermsWeight.termFrequencyWeightingQuery(qTfMode, wordProcessor.getInvertedFileManualQuery());
        TermsWeight.inverseDocumentWeightingQuery(qIdfMode, wordProcessor.getInvertedFileManualQuery(),
                wordProcessor.getInvertedFile());

        finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");

        // Hitung similarity query dengan dokumen
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        query q = new query(0, query);


        for (document doc : wordProcessor.getListDocumentsFinal()) {
            double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileManualQuery(),
                    doc, wordProcessor.getInvertedFile(), isNormalize);
            result.put(doc, weight);
        }
        result = DocumentRanking.rankDocuments(result);


        finish = System.currentTimeMillis();
        exectime = finish-start;
        System.out.println("Calculating similarity done in " + exectime + " ms.\n");
    }

    public String getSummaryResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.size());
        sb.append(result.size() > 1 ? " results" : "result");
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
