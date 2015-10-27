package Utils;

import model.document;
import model.query;

import java.util.HashMap;

/**
 * Created by khaidzir on 27/10/2015.
 */
public class InputQuery {

    static int qTfMode, qIdfMode;
    static boolean isStem;
    static PreprocessWords wordProcessor = new PreprocessWords();

    public static void setDocumentMode(int tfmode, int idfmode, boolean stem){
//        qTfMode = tfmode;
//        qIdfMode = idfmode;
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfmode, wordProcessor.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(idfmode, wordProcessor.getInvertedFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish-start) + " ms.\n");
    }

    public static void setQueryMode(int tfmode, int idfmode, boolean stem) {
        qTfMode = tfmode;
        qIdfMode = idfmode;
        isStem = stem;
    }

    public static HashMap<document, Double> SearchDocumentsUsingQuery(String query, boolean isNormalize) {
        double start, finish;

        // Proses query
        System.out.println("Indexing queries...");
        start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForManualQuery(query, isStem);
        TermsWeight.termFrequencyWeightingQuery(qTfMode, wordProcessor.getInvertedFileQuery());
        TermsWeight.inverseDocumentWeightingQuery(qIdfMode, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile());

        finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");

        // Hitung similarity query dengan dokumen
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        query q = new query(0, query);

        HashMap<document, Double> docweightMap = new HashMap<>();
        for (document doc : wordProcessor.getListDocumentsFinal()) {
            double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileManualQuery(),
                    doc, wordProcessor.getInvertedFile(), isNormalize);
            docweightMap.put(doc, weight);
        }
        docweightMap = DocumentRanking.rankDocuments(docweightMap);


        finish = System.currentTimeMillis();
        System.out.println("Calculating similarity done in " + (finish-start) + " ms.\n");

        return docweightMap;
    }

    public static void main(String[] args) {
        // Setting awal awal
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        InputQuery.setDocumentMode(1, 0 , true);
        InputQuery.setQueryMode(1, 0, true);
        HashMap<document, Double> result = InputQuery.SearchDocumentsUsingQuery("What methods are there for encoding, automatically matching,            \n" +
                "and automatically drawing structures extended in two dimensions,        \n" +
                "like the structural formulas for chemical compounds?", false);
        for(document doc : result.keySet()) {
            System.out.println(doc.getIndex() + " : " + doc.getKonten());
        }
    }

}
