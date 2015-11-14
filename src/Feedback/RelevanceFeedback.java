package Feedback;

import Utils.*;
import model.*;
import sample.Vars;

import javax.management.Query;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 14/11/2015.
 */
public class RelevanceFeedback {
    indexTabel invertedFile;
    indexTabel invertedFileQuery;
    normalTabel normalFileQuery;
    HashSet<Integer> listDocumentRelevant;
    HashSet<Integer> listDocumentIrrelevant;
    documentsRelevancesFeedback listDocumentRelevancesThisQuery;
    HashMap<String,Double> newQueryComposition;

    /**
     * Getter for NewQueryComposition
     * @return
     */
    public HashMap<String, Double> getNewQueryComposition() {
        return newQueryComposition;
    }

    /**
     * Getter for inverted file query modified relevance feedback
     * @return
     */
    public indexTabel getInvertedFileQuery() {
        return invertedFileQuery;
    }

    /**
     * Constructor before Relevance Feedback Method implemented
     * @param invertedFile
     * @param invertedFileQuery
     * @param normalFileQuery
     * @param listDocumentRelevances
     */
    public RelevanceFeedback(indexTabel invertedFile, indexTabel invertedFileQuery, normalTabel normalFileQuery, documentsRelevancesFeedback listDocumentRelevances) {
        this.invertedFile = invertedFile;
        this.invertedFileQuery = invertedFileQuery;
        this.normalFileQuery = normalFileQuery;
        this.listDocumentRelevancesThisQuery = listDocumentRelevances;
        newQueryComposition = new HashMap<>();
        listDocumentRelevant = new HashSet<>();
        listDocumentIrrelevant = new HashSet<>();
        for (Map.Entry m : listDocumentRelevances.getIsDocumentsRelevantList().entrySet()) {
            int indexDocument = (Integer) m.getKey();
            boolean isRelevant = (Boolean) m.getValue();
           // System.out.println("Index Document : " + indexDocument);
            if (isRelevant) {
                listDocumentRelevant.add(indexDocument);
            } else {
                listDocumentIrrelevant.add(indexDocument);
            }
        }
       /* Iterator iterator1 = listDocumentRelevant.iterator();
        while (iterator1.hasNext()) {
            System.out.println("Relevant Index Document : " + (Integer) iterator1.next());
        }
        Iterator iterator2 = listDocumentIrrelevant.iterator();
        while (iterator2.hasNext()) {
            System.out.println("Irrelevant Index Document : " + (Integer) iterator2.next());
        }
        System.out.println("==================================================================="); */
    }

    /**
     * Convert query composition into new query with previously same index
     * @return
     */
    public query convertNewQueryComposition() {
        int index = listDocumentRelevancesThisQuery.getQuery().getIndex();
        StringBuffer queryContent = new StringBuffer();
        for (Map.Entry m : newQueryComposition.entrySet()) {
            String keyTerm = (String) m.getKey();
            queryContent.append(keyTerm + " ");
        }
        query Query = new query(index,queryContent.toString());
        return Query;
    }

    /**
     * Create new query composition from inverted file query
     * with 3 options Relevance Feedback Method
     */
    public void updateTermInThisQuery(int relevanceFeedbackMethod) {
        int thisQueryIndex = listDocumentRelevancesThisQuery.getQuery().getIndex();
        HashSet<String> listTermsInQuery = normalFileQuery.getNormalFile().get(thisQueryIndex);
        Iterator listTerms = listTermsInQuery.iterator();
        while (listTerms.hasNext()) {
            String keyTerm = (String) listTerms.next();
            termWeightingDocument relation = invertedFileQuery.getListTermWeights().get(keyTerm);
            try {
                if (relation.getDocumentWeightCounterInOneTerm().get(thisQueryIndex) != null) {
                    double oldWeight = relation.getDocumentWeightCounterInOneTerm().get(thisQueryIndex).getWeight();
                    double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,keyTerm,oldWeight);
                    if (newWeight > 0) {
                        newQueryComposition.put(keyTerm,newWeight);
                        relation.getDocumentWeightCounterInOneTerm().get(thisQueryIndex).setWeight(newWeight);
                    } else {
                        relation.getDocumentWeightCounterInOneTerm().get(thisQueryIndex).setWeight(0.0);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * Create new query composition from unseen term
     * in inverted file document with Relevance Feedback Method
     */
    public void updateUnseenTermInThisQuery(int relevanceFeedbackMethod) {
        int thisQueryIndex = listDocumentRelevancesThisQuery.getQuery().getIndex();
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            if (!isTermAppearInQuery(keyTerm)) {
                double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,keyTerm,0.0);
                if (newWeight > 0) {
                    newQueryComposition.put(keyTerm,newWeight);
                    invertedFileQuery.insertRowTable(keyTerm,thisQueryIndex,newWeight);
                }
            }
        }
    }

    /**
     * Calculate new weight of a term based on relevance feedback method
     * @param relevanceFeedbackMethod : 1 (Rocchio), 2 (Ide Regular), 3 (Ide De Chi)
     * @param term
     * @param oldWeight
     * @return
     */
    private double computeNewWeightTerm(int relevanceFeedbackMethod, String term, double oldWeight) {
        double sumWeightDocumentRelevant = computeSumWeightDocuments(term,listDocumentRelevant);
        double sumWeightDocumentIrrelevant = computeSumWeightDocuments(term,listDocumentIrrelevant);
        double weightTopDocumentIrrelevant = findWeightTopIrrelevantDocument(term,listDocumentIrrelevant);
        double sumDocumentRelevant = (double) listDocumentRelevant.size();
        double sumDocumentIrrelevant = (double) listDocumentIrrelevant.size();
        double newWeight;
        switch (relevanceFeedbackMethod) {
            case 1  :   newWeight = oldWeight + (sumWeightDocumentRelevant / sumDocumentRelevant) - (sumWeightDocumentIrrelevant / sumDocumentIrrelevant);
                        break;
            case 2  :   newWeight = oldWeight + sumWeightDocumentRelevant - sumWeightDocumentIrrelevant;
                        break;
            case 3  :   newWeight = oldWeight + sumWeightDocumentRelevant - weightTopDocumentIrrelevant;
                        break;
            default :   newWeight = oldWeight;
        }
        return newWeight;
    }

    /**
     * Find weight a term in top irrelevant document
     * from inverted file document
     * @param term
     * @param listDocumentsIrrelevant
     * @return
     */
    private double findWeightTopIrrelevantDocument(String term,HashSet<Integer> listDocumentsIrrelevant) {
        int counter = 1;
        int topIndexDocumentIrrelevant = 0;
        Iterator iterator = listDocumentsIrrelevant.iterator();
        while (iterator.hasNext()) {
            int indexDocument = (Integer) iterator.next();
            if (counter == 1)
                topIndexDocumentIrrelevant = indexDocument;
            counter++;
        }
        double weight = 0.0;
        try {
            if (invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(topIndexDocumentIrrelevant) != null) {
                weight = invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(topIndexDocumentIrrelevant).getWeight();
            }
        } catch (Exception e) {

        }
       /* System.out.println("Top index : " + topIndexDocumentIrrelevant);
        System.out.println("Term : " + term);
        System.out.println("Bobot : " + weight); */
        return weight;
    }

    /**
     * Check each term in inverted file document
     * @param term
     * @return
     */
    private boolean isTermAppearInQuery(String term) {
        boolean isTermAppear = false;
        int thisQueryIndex = listDocumentRelevancesThisQuery.getQuery().getIndex();
        HashSet<String> listTermsInQuery = normalFileQuery.getNormalFile().get(thisQueryIndex);
        Iterator listTermsQuery = listTermsInQuery.iterator();
        while (listTermsQuery.hasNext()) {
            String keyTerm = ((String) listTermsQuery.next()).toLowerCase();
            if (term.equals(keyTerm.toLowerCase())) {
                isTermAppear = true;
                break;
            }
        }
        return isTermAppear;
    }

    /**
     * Compute sum weights of a term in relevant or irrelevant documents
     * @param term
     * @param listDocumentsSameType : relevant or irrelevant
     * @return
     */
    private double computeSumWeightDocuments(String term, HashSet<Integer> listDocumentsSameType) {
        double sumWeightDoc = 0.0;
        Iterator iterator = listDocumentsSameType.iterator();
        while (iterator.hasNext()) {
            int indexDocument = (Integer) iterator.next();
            try {
                if (invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(indexDocument) != null) {
                    counterWeightPair counter = invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(indexDocument);
                    sumWeightDoc += counter.getWeight();
                }
            } catch (Exception e) {

            }
        }
        return sumWeightDoc;
    }

    public static void main(String[] arg) {
        PreprocessWords wordProcessor = new PreprocessWords();
       /* EksternalFile.setPathDocumentsFile("test\\CISI\\cisi.all");
        EksternalFile.setPathQueriesFile("test\\CISI\\query.text");
        EksternalFile.setPathQrelsFile("test\\CISI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus dokumen)
        TermsWeight.inverseDocumentWeighting(0, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY
        wordProcessor.loadIndexTabelForQueries(false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query

        // DO EKSPERIMENT FOR GETTING RETRIEVED DOCUMENTS FOR EACH QUERY
        Experiment exp = new Experiment();
        exp.setInvertedFile(wordProcessor.getInvertedFile(),false,false);
        exp.setInvertedFileQuery(wordProcessor.getInvertedFileQuery(), false, false);
        exp.setNormalFile(wordProcessor.getNormalFile());
        exp.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp.evaluate(false);

        // ISI FORM RELEVANCE FEEDBACK (SEMUA QUERY)
       /* for (Map.Entry m : exp.getResultMap().entrySet()) {
            query Query = (query) m.getKey();
            documentsRelevancesFeedback relevanceThisQuery = new documentsRelevancesFeedback(Query);
            ConcurrentHashMap<document,Double> relation = (ConcurrentHashMap<document,Double>) m.getValue();
            for (Map.Entry n : relation.entrySet()) {
                document Document = (document) n.getKey();
                if (Document.getIndex() % 2 == 0) {
                    relevanceThisQuery.insertDocumentRelevance(Document.getIndex(),true);
                } else {
                    relevanceThisQuery.insertDocumentRelevance(Document.getIndex(),false);
                }
            }
            listFeedbacksEachQueries.add(relevanceThisQuery);
        } */
        ArrayList<documentsRelevancesFeedback> listFeedbacksEachQueries = new ArrayList<>();
        int counter = 0;
        for (SingleQueryEvaluation m : exp.getEvals()) {
            query Query = (query) wordProcessor.getListQueriesFinal().get(counter);
            documentsRelevancesFeedback relevances = new documentsRelevancesFeedback(Query);
            for (Integer index : m.getRetDocNums()) {
                if (index % 2 == 0) {
                    relevances.insertDocumentRelevance(index,true);
                } else {
                    relevances.insertDocumentRelevance(index,false);
                }
            }
            listFeedbacksEachQueries.add(relevances);
            counter++;
        }


        // RELEVANCE FEEDBACK (SEMUA QUERY)
        indexTabel invertedFileQuery = wordProcessor.getInvertedFileQuery();        // Inverted File Di-update berkali-kali untuk setiap reformulasi queyr
        for (documentsRelevancesFeedback relevance : listFeedbacksEachQueries) {
            RelevanceFeedback feedback = new RelevanceFeedback(wordProcessor.getInvertedFile(), invertedFileQuery, wordProcessor.getNormalFileQuery(), relevance);
            feedback.updateTermInThisQuery(3);
            feedback.updateUnseenTermInThisQuery(3);
            query newQuery = feedback.convertNewQueryComposition();
            wordProcessor.getListQueriesFinal().set((newQuery.getIndex()-1),newQuery);
        }

        // LIST NEW QUERIES BASED ON RELEVANCE FEEDBACK
       /* for (query Query : (ArrayList<query>) wordProcessor.getListQueriesFinal()) {
            System.out.println("Nomor Query : " + Query.getIndex());
            System.out.println("Konten Query : " + Query.getQueryContent());
            System.out.println("===================================================================");
        } */

        // RE-EKSPERIMENT AFTER RELEVANCE FEEDBACK
        Experiment exp2 = new Experiment();
        exp2.setInvertedFile(wordProcessor.getInvertedFile(),false,false);
        exp2.setInvertedFileQuery(invertedFileQuery, false, false);
        exp2.setNormalFile(wordProcessor.getNormalFile());
        exp2.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp2.evaluate(false);
        System.out.println(exp2.getSummary());
    }
}
