package Utils;

import Feedback.Interactive.PseudoRelevanceFeedback;
import Feedback.Interactive.RelevanceFeedback;
import model.*;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by khaidzir on 11/23/2015.
 */
public class RelevanceFeedbackExperiment extends Experiment {

    int topS, topN;
    boolean isPseudo, useSameCollection, useQueryExpansion, isNormalize;

    ConcurrentHashMap<query, ConcurrentHashMap<document, Double> > resultMap2;
    ArrayList<SingleQueryEvaluation> evals2;

    public RelevanceFeedbackExperiment(boolean isPseudo) {
        super();
        this.isPseudo = isPseudo;
    }

    /* SETTER */
    public void setTopS(int s) {
        topS = s;
    }
    public void setTopN(int n) {
        topN = n;
    }
    public void setUseSameCollection(boolean b) {
        useSameCollection = b;
    }
    public void setUseQueryExpansion(boolean q) {
        useQueryExpansion = q;
    }

    /* GETTER */
    public ArrayList<SingleQueryEvaluation> getEvals2() {
        return evals2;
    }
    public ConcurrentHashMap<query, ConcurrentHashMap<document, Double>> getResultMap2() {
        return resultMap2;
    }

    @Override
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

            // potong sebanyak S
            if(docweightMap.size() > topS) {
                int counter = 1;
                for(document d : docweightMap.keySet()) {
                    if(counter > topS) {
                        docweightMap.remove(d);
                    }
                    counter++;
                }
            }

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
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
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

    public void secondRetrieval(int tipe) {
        if(isPseudo) pseudoFeedback(tipe);
        else feedback(tipe);
    }

    private void pseudoFeedback(int tipe) {
        // ARRAYLIST PSEUDO FEEDBACK
        ArrayList<documentsPseudoRelevanceFeedback> listFeedbacksEachQueries = new ArrayList<>();

        // ISI FORM PSEUDO RELEVANCE FEEDBACK (EKSPERIMENT)
        wordProcessor.loadQueryRelevancesFinal();
        queryRelevances thisQueryRelevances = wordProcessor.getListQueryRelevancesFinal();

        // menandai dokumen yang relevan dan yang tidak
        for (query q : resultMap.keySet()) {
            documentsPseudoRelevanceFeedback relevances = new documentsPseudoRelevanceFeedback(topN,q);
            for (document d : resultMap.get(q).keySet()) {
                relevances.insertDocumentRetrieved(d.getIndex());
            }
            listFeedbacksEachQueries.add(relevances);
        }

        // query reweighting & query expansion
        ArrayList<PseudoRelevanceFeedback> listRelevanceFeedbackExperiment = new ArrayList<>();
        for (documentsPseudoRelevanceFeedback relevance : listFeedbacksEachQueries) {
            // EKSPERIMENT
            PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback(wordProcessor.getInvertedFile(),wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getNormalFileQuery(),relevance);
            feedback.updateTermInThisQuery(1);
            if(useQueryExpansion)
                feedback.updateUnseenTermInThisQuery(tipe);
            listRelevanceFeedbackExperiment.add(feedback);
        }

        // retrieval kedua
        resultMap2 = new ConcurrentHashMap<>();
        for (PseudoRelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            ConcurrentHashMap<document, Double> docweightMap = new ConcurrentHashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                if(!useSameCollection) {
                    if( resultMap.get(feedback.getListDocumentRetrievedForThisQuery()).containsKey(Document) ) continue;
                }
                double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFile(),
                        Document, wordProcessor.getInvertedFile(), feedback.getNormalFileQuery(), wordProcessor.getNormalFileQuery(),
                        isNormalize);
                docweightMap.put(Document, weight);
            }
            docweightMap = DocumentRanking.rankDocuments(docweightMap);
            resultMap.put(newQuery, docweightMap);
        }

        // Build Query Evaluation
        evals2 = new ArrayList<>();
        for (query q : resultMap2.keySet()) {
            ArrayList<Integer> docsNum = new ArrayList<>();
            for (document doc : resultMap2.get(q).keySet()) {
                docsNum.add(doc.getIndex());
            }
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
                evals2.add( new SingleQueryEvaluation(q.getIndex(), docsNum, wordProcessor.getListQueryRelevancesFinal()) );
        }

        // Evaluate all results
        for (SingleQueryEvaluation sqe : evals2) {
            sqe.evaluate();
        }

        // Sort result by query number
        Collections.sort(evals2);

    }

    private void feedback(int tipe) {
        // ARRAY MENYIMPAN HASIL RELEVANCE FEEDBACK PER QUERY
        ArrayList<documentsRelevancesFeedback> listFeedbacksEachQueries = new ArrayList<>();

        // ISI FORM RELEVANCE FEEDBACK (EKSPERIMENT)
        wordProcessor.loadQueryRelevancesFinal();
        queryRelevances thisQueryRelevances = wordProcessor.getListQueryRelevancesFinal();

        // menandai dokumen yang relevan dan yang tidak
        for(query q : resultMap.keySet()) {
            documentsRelevancesFeedback relevances = new documentsRelevancesFeedback(q);
            for(document d : resultMap.get(q).keySet()) {
                if (wordProcessor.isDocumentRelevantForThisQuery(d.getIndex(),q.getIndex(),thisQueryRelevances)) {
                    relevances.insertDocumentRelevance(d.getIndex(), true);
                } else {
                    relevances.insertDocumentRelevance(d.getIndex(), false);
                }
            }
            listFeedbacksEachQueries.add(relevances);
        }

        // query reweighting & query expansion
        ArrayList<RelevanceFeedback> listRelevanceFeedbackExperiment = new ArrayList<>();
        for (documentsRelevancesFeedback relevance : listFeedbacksEachQueries) {
            // EKSPERIMENT SAJA
            RelevanceFeedback feedback = new RelevanceFeedback(wordProcessor.getInvertedFile(), wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getNormalFileQuery(), relevance);
            feedback.updateTermInThisQuery(tipe);
            if(useQueryExpansion)
                feedback.updateUnseenTermInThisQuery(tipe);
            listRelevanceFeedbackExperiment.add(feedback);
        }

        // retrieval kedua
        resultMap2 = new ConcurrentHashMap<>();
        for (RelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            System.out.println(feedback.getListDocumentRelevancesThisQuery().getQuery().getIndex());
            System.out.println("Query lama : " + feedback.getListDocumentRelevancesThisQuery().getQuery().getQueryContent());
            System.out.println("Query baru : " + newQuery.getQueryContent());
            System.out.println("-----------------------------------------------------------------");
            ConcurrentHashMap<document, Double> docweightMap = new ConcurrentHashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                if(!useSameCollection) {
                    if(resultMap.get(feedback.getListDocumentRelevancesThisQuery().getQuery()).containsKey(Document) ) continue;
                }
                double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFileQueryManual(),
                        Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), feedback.getNormalFileQueryManual(),
                        isNormalize);
                docweightMap.put(Document, weight);
            }
            docweightMap = DocumentRanking.rankDocuments(docweightMap);
            // potong sebanyak S
//            if(docweightMap.size() > topS) {
//                int counter = 1;
//                for(document d : docweightMap.keySet()) {
//                    if(counter > topS) {
//                        docweightMap.remove(d);
//                    }
//                    counter++;
//                }
//            }
            resultMap2.put(newQuery, docweightMap);
        }

        // Build Query Evaluation
        evals2 = new ArrayList<>();
        for (query q : resultMap2.keySet()) {
            ArrayList<Integer> docsNum = new ArrayList<>();
            for (document doc : resultMap2.get(q).keySet()) {
                docsNum.add(doc.getIndex());
            }
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
                evals2.add( new SingleQueryEvaluation(q.getIndex(), docsNum, wordProcessor.getListQueryRelevancesFinal()) );
        }

        // Evaluate all results
        for (SingleQueryEvaluation sqe : evals2) {
            sqe.evaluate();
        }

        // Sort result by query number
        Collections.sort(evals2);
    }

    public String getSummary2() {
        StringBuilder sb = new StringBuilder();
        double sumNonAVG = 0.0;
        for(SingleQueryEvaluation sqe : evals2) {
            sb.append(sqe.getEvalSummary());
            sb.append("\n");
            sumNonAVG += sqe.nonInterpolatedAvgPrecision;
        }
        sb.append("Noninterpollated Precision Average : " + (sumNonAVG / (double) wordProcessor.getListQueriesFinal().size()));
        return sb.toString();
    }

    public static void main(String[] args) {
        PreprocessWords wordProcessor = new PreprocessWords();
        // CISI
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus dokumen)
        TermsWeight.inverseDocumentWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY (EKSPERIMENT)
        wordProcessor.loadIndexTabelForQueries(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query

        // DO EKSPERIMENT FOR GETTING RETRIEVED DOCUMENTS FOR EACH QUERY (EKSPERIMENT)
        RelevanceFeedbackExperiment exp = new RelevanceFeedbackExperiment(false);
        exp.setTopS(10);
        exp.setUseQueryExpansion(true);
        exp.setUseSameCollection(true);
        exp.setInvertedFile(wordProcessor.getInvertedFile(),false,true);
        exp.setInvertedFileQuery(wordProcessor.getInvertedFileQuery(), false, true);
        exp.setNormalFile(wordProcessor.getNormalFile());
        exp.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp.evaluate(true);
        System.out.println(exp.getSummary());

        System.out.println("\nSecond retrieval : \n");

        exp.secondRetrieval(1);
        System.out.println(exp.getSummary2());
    }

}
