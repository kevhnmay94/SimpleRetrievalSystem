package Utils;

import Feedback.PseudoRelevanceFeedback;
import Feedback.RelevanceFeedback;
import model.*;

import java.util.*;

/**
 * Created by khaidzir on 11/23/2015.
 */
public class RelevanceFeedbackExperiment extends Experiment {

    int topS, topN;
    boolean isPseudo, useSameCollection, useQueryExpansion, isNormalize;

    Map<query, Map<document, Double> > resultMap2;
    ArrayList<SingleQueryEvaluation> evals2;

    public RelevanceFeedbackExperiment() {
        super();
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
    public void setIsPseudo(boolean p) { this.isPseudo = p; }

    /* GETTER */
    public ArrayList<SingleQueryEvaluation> getEvals2() {
        return evals2;
    }
    public Map<query, Map<document, Double>> getResultMap2() {
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
            Map<document, Double> docweightMap = new HashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQuery(),
                        Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), wordProcessor.getNormalFileQuery(),isNormalize);
                docweightMap.put(Document, weight);
            }

            docweightMap = DocumentRanking.rankDocuments(docweightMap);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            System.out.println("Query number " + q.getIndex() + " : " + q.getQueryContent());
//            for(Map.Entry<document, Double> m : docweightMap.entrySet()) {
//                System.out.print(m.getKey().getIndex() + " : " + m.getValue() + ", " + "\n" + m.getKey().getKonten());
//                break;
//            }
//            System.out.println("\n");
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // potong sebanyak S
            if(docweightMap.size()>topS) {
                ArrayList<document> deletedEl=new ArrayList<>();
                int counter=1;
                for(document d : docweightMap.keySet()) {
                    if(counter>topS) {
                        deletedEl.add(d);
                    }
                    counter++;
                }
                for(document d : deletedEl) {
                    docweightMap.remove(d);
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
            ArrayList<Double> docsSim = new ArrayList<>();
            for (Map.Entry<document, Double> m : resultMap.get(q).entrySet()) {
                docsNum.add(m.getKey().getIndex());
                docsSim.add(m.getValue());
            }
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
                if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex()).size()>0)
                evals.add( new SingleQueryEvaluation(q.getIndex(), docsNum, docsSim, wordProcessor.getListQueryRelevancesFinal()) );
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
            feedback.updateTermInThisQuery(tipe);
            if(useQueryExpansion)
                feedback.updateUnseenTermInThisQuery(tipe);
            listRelevanceFeedbackExperiment.add(feedback);
        }

        // retrieval kedua
        resultMap2 = new HashMap<>();
        for (PseudoRelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            Map<document, Double> docweightMap = new HashMap<>();
            Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
            while (listDocuments.hasNext()) {
                document Document = (document) listDocuments.next();
                if(!useSameCollection) {
                    if(resultMap.get(feedback.getListDocumentRetrievedForThisQuery().getQuery()).containsKey(Document) ) continue;
                }
                double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFileQuery(),
                        Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), feedback.getNormalFileQuery(),
                        isNormalize);
                docweightMap.put(Document, weight);
            }
            docweightMap = DocumentRanking.rankDocuments(docweightMap);
            resultMap2.put(newQuery, docweightMap);
        }

        // Build Query Evaluation
        evals2 = new ArrayList<>();
        for (query q : resultMap2.keySet()) {
            ArrayList<Integer> docsNum = new ArrayList<>();
            ArrayList<Double> docsSim = new ArrayList<>();
            for (Map.Entry<document, Double> m : resultMap2.get(q).entrySet()) {
                docsNum.add(m.getKey().getIndex());
                docsSim.add(m.getValue());
            }
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
                if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex()).size()>0)
                evals2.add( new SingleQueryEvaluation(q.getIndex(), docsNum, docsSim, wordProcessor.getListQueryRelevancesFinal()) );
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
            ArrayList<Integer> relevantDocs = new ArrayList<>();
            documentsRelevancesFeedback relevances = new documentsRelevancesFeedback(q);
            for(document d : resultMap.get(q).keySet()) {
                if (wordProcessor.isDocumentRelevantForThisQuery(d.getIndex(),q.getIndex(),thisQueryRelevances)) {
                    relevances.insertDocumentRelevance(d.getIndex(), true);
                    relevantDocs.add(d.getIndex());
                } else {
                    relevances.insertDocumentRelevance(d.getIndex(), false);
                }
            }
            if(!useSameCollection)
                wordProcessor.getListQueryRelevancesFinal().removeDocumentFromQrels(relevantDocs, q.getIndex());
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
        resultMap2 = new HashMap<>();
        for (RelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();

//            System.out.println(feedback.getListDocumentRelevancesThisQuery().getQuery().getIndex());
//            System.out.println("Query lama : " + feedback.getListDocumentRelevancesThisQuery().getQuery().getQueryContent());
//            System.out.println("Query baru : " + newQuery.getQueryContent());
//            System.out.println("-----------------------------------------------------------------");

           /* System.out.println(feedback.getListDocumentRelevancesThisQuery().getQuery().getIndex());
            System.out.println("Query lama : " + feedback.getListDocumentRelevancesThisQuery().getQuery().getQueryContent());
            System.out.println("Query baru : " + newQuery.getQueryContent());
            System.out.println("-----------------------------------------------------------------"); */

            Map<document, Double> docweightMap = new HashMap<>();
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
//            if(docweightMap.size()>topS) {
//                ArrayList<document> deletedEl=new ArrayList<>();
//                int counter=1;
//                for(document d : docweightMap.keySet()) {
//                    if(counter>topS) {
//                        deletedEl.add(d);
//                    }
//                    counter++;
//                }
//                for(document d : deletedEl) {
//                    docweightMap.remove(d);
//                }
//            }
            resultMap2.put(newQuery, docweightMap);
        }

        // Build Query Evaluation
        evals2 = new ArrayList<>();
        for (query q : resultMap2.keySet()) {
            ArrayList<Integer> docsNum = new ArrayList<>();
            ArrayList<Double> docsSim = new ArrayList<>();
            for (Map.Entry<document, Double> m : resultMap2.get(q).entrySet()) {
                docsNum.add(m.getKey().getIndex());
                docsSim.add(m.getValue());
            }
            if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex())!=null)
                if(wordProcessor.getListQueryRelevancesFinal().getListQueryRelevances().get(q.getIndex()).size()>0)
                evals2.add( new SingleQueryEvaluation(q.getIndex(), docsNum, docsSim, wordProcessor.getListQueryRelevancesFinal()) );
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
    public String getSummary2WithSimilarity() {
        StringBuilder sb = new StringBuilder();
        double sumNonAVG = 0.0;
        for(SingleQueryEvaluation sqe : evals2) {
            sb.append(sqe.getEvalSummaryWithSimilarity());
            sb.append("\n");
            sumNonAVG += sqe.nonInterpolatedAvgPrecision;
        }
        sb.append("Noninterpollated Precision Average : " + (sumNonAVG / (double) wordProcessor.getListQueriesFinal().size()));
        return sb.toString();
    }

    public static void main(String[] args) {
        PreprocessWords wordProcessor = new PreprocessWords();

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
        RelevanceFeedbackExperiment exp = new RelevanceFeedbackExperiment();
        exp.setIsPseudo(false);
        exp.setTopS(20);
        exp.setTopN(5);
        exp.setUseQueryExpansion(false);
        exp.setUseSameCollection(true);
        exp.setInvertedFile(wordProcessor.getInvertedFile(),false,true);
        exp.setInvertedFileQuery(wordProcessor.getInvertedFileQuery(), false, true);
        exp.setNormalFile(wordProcessor.getNormalFile());
        exp.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp.evaluate(false);
        System.out.println(exp.getSummaryWithSimilarity());

        System.out.println("\nSecond retrieval : \n");

        exp.secondRetrieval(1);
        System.out.println(exp.getSummary2WithSimilarity());
    }

}
