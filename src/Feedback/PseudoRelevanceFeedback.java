package Feedback;

import model.*;

import java.util.*;

/**
 * Created by steve on 14/11/2015.
 */
public class PseudoRelevanceFeedback {
    indexTabel invertedFile;
    indexTabel invertedFileQuery;
    normalTabel normalFileQuery;
    ArrayList<Integer> listDocumentsRelevant;    // Top N of document retrieved
    HashMap<String,Double> newQueryComposition;
    documentsPseudoRelevanceFeedback listDocumentRetrievedForThisQuery;

    /**
     * Constructor
     * @param invertedFile
     * @param invertedFileQuery
     * @param normalFileQuery
     * @param listDocumentRetrievedForThisQuery
     */
    public PseudoRelevanceFeedback(indexTabel invertedFile, indexTabel invertedFileQuery, normalTabel normalFileQuery, documentsPseudoRelevanceFeedback listDocumentRetrievedForThisQuery) {
        this.invertedFile = invertedFile;
        this.invertedFileQuery = invertedFileQuery;
        this.normalFileQuery = normalFileQuery;
        this.listDocumentRetrievedForThisQuery = listDocumentRetrievedForThisQuery;
        newQueryComposition = new HashMap<>();
        // Isi top document relevant ke list dokumen relevant
        listDocumentsRelevant = new ArrayList<>();
        int topDocumentRelevant = listDocumentRetrievedForThisQuery.getTopDocumentsRelevant();
        ArrayList<Integer> listDocumentRetrieved = listDocumentRetrievedForThisQuery.getListDocumentsRetrieved();
        int counter = 0;
        Iterator iterator = listDocumentRetrieved.iterator();
        while (iterator.hasNext()) {
            int indexDocument = (Integer) iterator.next();
            if (counter < topDocumentRelevant) {
                listDocumentsRelevant.add(indexDocument);
            }
            counter++;
        }
    }

    /**
     * Create new query composition from inverted file query
     * with 3 options Pseudo Relevance Feedback Method
     */
    public void updateTermInThisQuery(int relevanceFeedbackMethod) {
        int thisQueryIndex = listDocumentRetrievedForThisQuery.getQuery().getIndex();
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
        int thisQueryIndex = listDocumentRetrievedForThisQuery.getQuery().getIndex();
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            if (!isTermAppearInQuery(keyTerm)) {
                double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,keyTerm,0.0);
                if (newWeight > 0) {
                    newQueryComposition.put(keyTerm,newWeight);
                    invertedFileQuery.insertRowTable(keyTerm,thisQueryIndex,newWeight);
                    normalFileQuery.insertElement(thisQueryIndex,keyTerm);
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
        double sumWeightDocumentRelevant = computeSumWeightDocuments(term,listDocumentsRelevant);
        double sumDocumentRelevant = listDocumentRetrievedForThisQuery.getTopDocumentsRelevant();
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
     * @return
     */
    private double findWeightTopIrrelevantDocument(String term) {
        int topRelevantDocument = listDocumentRetrievedForThisQuery.getTopDocumentsRelevant();
        int topIndexDocumentIrrelevant = listDocumentRetrievedForThisQuery.getListDocumentsRetrieved().get(topRelevantDocument);
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
     * Check term in inverted file query appears or not
     * @param term
     * @return
     */
    private boolean isTermAppearInQuery(String term) {
        boolean isTermAppear = false;
        int thisQueryIndex = listDocumentRetrievedForThisQuery.getQuery().getIndex();
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
    private double computeSumWeightDocuments(String term, ArrayList<Integer> listDocumentsSameType) {
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

    }
}
