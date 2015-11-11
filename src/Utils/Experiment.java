package Utils;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import model.document;
import model.indexTabel;
import model.indexTabelQuery;
import model.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by khaidzir on 15/10/2015.
 */
public class Experiment {

    ArrayList<SingleQueryEvaluation> evals;
    PreprocessWords wordProcessor;
    ConcurrentHashMap<query, ConcurrentHashMap<document, Double> > resultMap;

    public Experiment() {
        evals = new ArrayList<>();
        wordProcessor = new PreprocessWords();
        resultMap = new ConcurrentHashMap<>();
    }

    public indexTabel getInvertedFile() {
        return wordProcessor.getInvertedFile();
    }
    public indexTabel getInvertedFileQuery() { return wordProcessor.getInvertedFileQuery(); }
    public void setInvertedFile (indexTabel idxTable) {
        wordProcessor.setInvertedFile(idxTable);
       // wordProcessor.loadDocumentsFinal();
    }
    public void setInvertedFileQuery (indexTabel idxTable) {
        wordProcessor.setInvertedFileQuery(idxTable);
      //  wordProcessor.loadQueriesFinal();
    }

    public void processDocuments(int tfcode, int idfcode, boolean stem) {
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfcode, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeighting(idfcode, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish - start) + " ms.\n");
    }

    public void processQueries(int tfcode, int idfcode, boolean stem) {
        System.out.println("Indexing queries...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForQueries(stem);
        TermsWeight.termFrequencyWeightingQuery(tfcode, wordProcessor.getInvertedFileQuery(), wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeightingQuery(idfcode, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");
    }

    public void evaluate(boolean isNormalize) {
        double start, finish;

        // Hitung similarity setiap query setiap dokumen, simpan dalam resultMap
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        Iterator listQueries = wordProcessor.getListQueriesFinal().iterator();
        while (listQueries.hasNext()) {
            query q = (query) listQueries.next();
            ConcurrentHashMap<document, Double> docweightMap = new ConcurrentHashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQuery(),
                        Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), wordProcessor.getNormalFileQuery(),isNormalize);
                docweightMap.put(Document, weight);
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

        // Sort result by query number
        Collections.sort(evals);

        finish = System.currentTimeMillis();
        System.out.println("Evaluating result done in " + (finish-start) + " ms.\n");
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        double sumNonAVG = 0.0;
        for(SingleQueryEvaluation sqe : evals) {
            sb.append(sqe.getEvalSummary());
            sb.append("\n");
            sumNonAVG += sqe.nonInterpolatedAvgPrecision;
        }
        sb.append("Noninterpollated Precision Average : " + (sumNonAVG / (double) wordProcessor.getListQueriesFinal().size()));
        return sb.toString();
    }

    public static void main(String[] args) {
        // Setting awal awal
       /* EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");*/

        EksternalFile.setPathDocumentsFile("test\\CISI\\cisi.all");
        EksternalFile.setPathQueriesFile("test\\CISI\\query.text");
        EksternalFile.setPathQrelsFile("test\\CISI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        int[] tfcode = {0, 1, 2, 3, 4};
        String[] stringTfcode = {"no", "raw", "log", "bin", "aug"};

        int[] idfcode = {0, 1};
        String[] stringIdfcode = {"noidf", "idf"};

        boolean[] stemcode = {true, false};
        String[] stringStemcode = {"stem", "nostem"};

        boolean[] normcode = {true, false};
        String[] stringNormCode = {"norm", "no-norm"};

        // ADI / CISI (ALL)
        for (int i=1; i<tfcode.length; i++) {
            ThreadExperiment thread = new ThreadExperiment(i);
            thread.start();
        }

        // STEVE

       /* for(int j=0; j<idfcode.length; j++) {
            for(int k=0; k<stemcode.length; k++) {
                for(int l=0; l<normcode.length; l++) {
                    ThreadExperiment thread = new ThreadExperiment(1,j,k,l);
                    thread.start();
                }
            }
        } */

     /*   for(int j=0; j<idfcode.length; j++) {
            for(int k=0; k<stemcode.length; k++) {
                for(int l=0; l<normcode.length; l++) {
                    ThreadExperiment thread = new ThreadExperiment(2,j,k,l);
                    thread.start();
                }
            }
        } */

        // KEVMAU

       /* for(int j=0; j<idfcode.length; j++) {
            for(int k=0; k<stemcode.length; k++) {
                for(int l=0; l<normcode.length; l++) {
                    ThreadExperiment thread = new ThreadExperiment(3,0,1,0);
                    thread.start();
                }
            }
        } */

       /* for(int j=0; j<idfcode.length; j++) {
            for(int k=0; k<stemcode.length; k++) {
                for(int l=0; l<normcode.length; l++) {
                    ThreadExperiment thread = new ThreadExperiment(4,0,1,0);
                    thread.start();
                }
            }
        }*/
    }

}

