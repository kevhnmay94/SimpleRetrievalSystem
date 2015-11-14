package Feedback;

import Utils.PreprocessWords;
import model.*;

import javax.management.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
            if (isRelevant) {
                listDocumentRelevant.add(indexDocument);
            } else {
                listDocumentIrrelevant.add(indexDocument);
            }
        }
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
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            if (!isTermAppearInQuery(keyTerm)) {
                double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,keyTerm,0.0);
                if (newWeight > 0) {
                    newQueryComposition.put(keyTerm,newWeight);
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
