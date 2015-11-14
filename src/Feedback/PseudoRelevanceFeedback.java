package Feedback;

import model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by steve on 14/11/2015.
 */
public class PseudoRelevanceFeedback {
    indexTabel invertedFile;
    indexTabel invertedFileQuery;
    normalTabel normalFileQuery;
    HashSet<Integer> listDocumentsRetrieved;    // Sorted / Ranked based on similarity
    HashMap<String,Double> newQueryComposition;
    documentsRelevancesFeedback listDocumentRelevancesThisQuery;

    public PseudoRelevanceFeedback(indexTabel invertedFile, indexTabel invertedFileQuery, normalTabel normalFileQuery, HashSet<Integer> listDocumentsRetrieved, documentsRelevancesFeedback listDocumentRelevancesThisQuery) {
        this.invertedFile = invertedFile;
        this.invertedFileQuery = invertedFileQuery;
        this.normalFileQuery = normalFileQuery;
        this.listDocumentsRetrieved = listDocumentsRetrieved;
        this.listDocumentRelevancesThisQuery = listDocumentRelevancesThisQuery;
        newQueryComposition = new HashMap<>();
    }

    /**
     * Create new query composition from inverted file query
     * with 3 options Pseudo Relevance Feedback Method
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
        double sumWeightDocumentRelevant = computeSumWeightDocuments(term,listDocumentsRetrieved);
        double sumDocumentRelevant = listDocumentRelevancesThisQuery.getTopDocumentsNumber();
        double newWeight;
        switch (relevanceFeedbackMethod) {
            case 1  :   newWeight = oldWeight + (sumWeightDocumentRelevant / sumDocumentRelevant);
                break;
            case 2  :   newWeight = oldWeight + sumWeightDocumentRelevant;
                break;
            case 3  :   newWeight = oldWeight + sumWeightDocumentRelevant;
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
}
