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
            double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQuery(),
                    doc, wordProcessor.getInvertedFile(), isNormalize);
            docweightMap.put(doc, weight);
        }
        docweightMap = DocumentRanking.rankDocuments(docweightMap);


        finish = System.currentTimeMillis();
        System.out.println("Calculating similarity done in " + (finish-start) + " ms.\n");

        return docweightMap;
    }

}
