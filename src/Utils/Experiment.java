package Utils;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import model.document;
import model.indexTabel;
import model.query;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by khaidzir on 15/10/2015.
 */
public class Experiment {

    ArrayList<SingleQueryEvaluation> evals;
    PreprocessWords wordProcessor;
    HashMap<query, HashMap<document, Double> > resultMap;

    public Experiment() {
        evals = new ArrayList<>();
        wordProcessor = new PreprocessWords();
        resultMap = new HashMap<>();
    }

    public indexTabel getInvertedFile() {
        return wordProcessor.getInvertedFile();
    }

    public void processDocuments(int tfcode, int idfcode, boolean stem) {
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfcode, wordProcessor.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(idfcode, wordProcessor.getInvertedFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish-start) + " ms.\n");
    }

    public void processQueries(int tfcode, int idfcode, boolean stem) {
        System.out.println("Indexing queries...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForQueries(stem);
        TermsWeight.termFrequencyWeightingQuery(tfcode, wordProcessor.getInvertedFileQuery());
        TermsWeight.inverseDocumentWeightingQuery(idfcode, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");
    }

    public void evaluate(boolean isNormalize) {
        double start, finish;

        // Hitung similarity setiap query setiap dokumen, simpan dalam resultMap
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        for (query q : wordProcessor.getListQueriesFinal()) {
            HashMap<document, Double> docweightMap = new HashMap<>();
            for (document doc : wordProcessor.getListDocumentsFinal()) {
                double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQuery(),
                        doc, wordProcessor.getInvertedFile(), isNormalize);
                docweightMap.put(doc, weight);
            }
            docweightMap = DocumentRanking.rankDocuments(docweightMap);
            resultMap.put(q, docweightMap);
        }

        finish = System.currentTimeMillis();
        System.out.println("Calculating similarity done in " + (finish-start) + " ms.\n");


        System.out.println("Evaluating result...");
        start = System.currentTimeMillis();

        // Load Query relevance
        wordProcessor.loadQueryRelevancesFinal();

        // Build Query Evaluation
        for (query q : resultMap.keySet()) {
            ArrayList<Integer> docsNum = new ArrayList<>();
            for (document doc : resultMap.get(q).keySet()) {
                docsNum.add(doc.getIndex());
            }
            evals.add( new SingleQueryEvaluation(q.getIndex(), docsNum, wordProcessor.getListQueryRelevancesFinal()) );
        }

        // Evaluate all results
        for (SingleQueryEvaluation sqe : evals) {
            sqe.evaluate();
        }

        finish = System.currentTimeMillis();
        System.out.println("Evaluating result done in " + (finish-start) + " ms.\n");
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        for(SingleQueryEvaluation sqe : evals) {
            sb.append(sqe.getEvalSummary());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Setting awal awal
        EksternalFile.setPathDocumentsFile("test\\CISI\\cisi.all");
        EksternalFile.setPathQueriesFile("test\\CISI\\query.text");
        EksternalFile.setPathQrelsFile("test\\CISI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        Experiment exp = new Experiment();
        exp.processDocuments(1, 0, true);
        exp.processQueries(1, 0, true);
        exp.evaluate(false);
        System.out.println(exp.getSummary());
    }

}

