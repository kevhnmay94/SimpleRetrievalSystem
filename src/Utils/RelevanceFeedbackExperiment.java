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
    RelevanceFeedback feedback;

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
                    counter++;
                    if(counter > topS) {
                        docweightMap.remove(d);
                    }
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

    private void pseudoFeedback(int tipe) {
        // ARRAYLIST PSEUDO FEEDBACK
        ArrayList<documentsPseudoRelevanceFeedback> listFeedbacksEachQueries = new ArrayList<>();

        // array menyimpan query lama
        ArrayList<query> listOldQuery = new ArrayList<>();

        // ISI FORM PSEUDO RELEVANCE FEEDBACK (EKSPERIMENT)
        for (query q : resultMap.keySet()) {
            query Query = (query) wordProcessor.getListQueriesFinal().get(counter);
            documentsPseudoRelevanceFeedback relevances = new documentsPseudoRelevanceFeedback(topN,Query);
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
        int i=0;
        for (PseudoRelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            ConcurrentHashMap<document, Double> docweightMap = new ConcurrentHashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                if(!useSameCollection) {
                    if( resultMap.get(listOldQuery.get(i)).containsKey(Document) ) continue;
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

        // array menyimpan query lama
        ArrayList<query> listOldQuery = new ArrayList<>();

        // ISI FORM RELEVANCE FEEDBACK (EKSPERIMENT)
        wordProcessor.loadQueryRelevancesFinal();
        queryRelevances thisQueryRelevances = wordProcessor.getListQueryRelevancesFinal();
        int counter = 0;

        // menandai dokumen yang relevan dan yang tidak
        for(query q : resultMap.keySet()) {
            listOldQuery.add(q);
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
        int i=0;
        for (RelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            ConcurrentHashMap<document, Double> docweightMap = new ConcurrentHashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                if(!useSameCollection) {
                    if( resultMap.get(listOldQuery.get(i)).containsKey(Document) ) continue;
                }
                double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFile(),
                        Document, wordProcessor.getInvertedFile(), feedback.getNormalFileQueryManual(), wordProcessor.getNormalFileQuery(),
                        isNormalize);
                docweightMap.put(Document, weight);
                i++;
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


}
