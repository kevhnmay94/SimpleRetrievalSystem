package Feedback.Interactive;

import Utils.*;
import jdk.internal.util.xml.impl.Input;
import model.*;

import java.util.*;

/**
 * Created by steve on 14/11/2015.
 */
public class RelevanceFeedback {
    indexTabel invertedFile;
    indexTabel invertedFileQueryManual;
    normalTabel normalFileQueryManual;
    ArrayList<Integer> listDocumentRelevant;
    ArrayList<Integer> listDocumentIrrelevant;
    documentsRelevancesFeedback listDocumentRelevancesThisQuery;
    HashMap<String,Double> newQueryComposition;

    // GETTER ATTRIBUTE

    public HashMap<String, Double> getNewQueryComposition() {
        return newQueryComposition;
    }

    public indexTabel getInvertedFileQueryManual() {
        return invertedFileQueryManual;
    }

    public ArrayList<Integer> getListDocumentRelevant() {
        return listDocumentRelevant;
    }

    public indexTabel getInvertedFile() {
        return invertedFile;
    }

    public normalTabel getNormalFileQueryManual() {
        return normalFileQueryManual;
    }

    public ArrayList<Integer> getListDocumentIrrelevant() {
        return listDocumentIrrelevant;
    }

    public documentsRelevancesFeedback getListDocumentRelevancesThisQuery() {
        return listDocumentRelevancesThisQuery;
    }

    /**
     * Constructor before Relevance Feedback Method implemented
     * @param invertedFile
     * @param invertedFileQueryManual
     * @param normalFileQueryManual
     * @param listDocumentRelevances
     */
    public RelevanceFeedback(indexTabel invertedFile, indexTabel invertedFileQueryManual, normalTabel normalFileQueryManual,
                             documentsRelevancesFeedback listDocumentRelevances) //, queryRelevances updatedQueryRelevances)
    {
        this.invertedFile = invertedFile;
        this.invertedFileQueryManual = invertedFileQueryManual;
        this.normalFileQueryManual = normalFileQueryManual;
        this.listDocumentRelevancesThisQuery = listDocumentRelevances;
        newQueryComposition = new HashMap<>();
        listDocumentRelevant = new ArrayList<>();
        listDocumentIrrelevant = new ArrayList<>();
        // Split relevant and irrelevant document from all user feedback
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
        query thisQuery = listDocumentRelevancesThisQuery.getQuery();
        StringTokenizer token = new StringTokenizer(thisQuery.getQueryContent(), " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String keyTerm = token.nextToken();
            String filteredWord = "";
            if (invertedFileQueryManual.isStemmingApplied()) {
                filteredWord = StemmingPorter.stripAffixes(keyTerm);
            } else {
                filteredWord = keyTerm;
            }
            try {
                termWeightingDocument relation = invertedFileQueryManual.getListTermWeights().get(filteredWord);
                if (relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()) != null) {
                    double oldWeight = relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()).getWeight();
                    double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,filteredWord,oldWeight);
                    if (newWeight > 0) {
                        newQueryComposition.put(filteredWord, newWeight);
                        relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()).setWeight(newWeight);
                    } else {
                        relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()).setWeight(0.0);
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
                    invertedFileQueryManual.insertRowTable(keyTerm,thisQueryIndex,newWeight);
                    normalFileQueryManual.insertElement(thisQueryIndex,keyTerm);
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
    private double findWeightTopIrrelevantDocument(String term,ArrayList<Integer> listDocumentsIrrelevant) {
        int topIndexDocumentIrrelevant = listDocumentsIrrelevant.get(0);
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
        query thisQuery = listDocumentRelevancesThisQuery.getQuery();
        StringTokenizer token = new StringTokenizer(thisQuery.getQueryContent(), " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String keyTerm = token.nextToken();
            if (invertedFile.isStemmingApplied()) {
                keyTerm = StemmingPorter.stripAffixes(keyTerm);
            }
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
        TermsWeight.inverseDocumentWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY (EKSPERIMENT)
        wordProcessor.loadIndexTabelForQueries(false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query

        // PROSES BUAT INVERTED FILE BUAT QUERY (INTERACTIVE)
      /*  String contentQuery = "computer science";
        wordProcessor.loadIndexTabelForManualQuery(contentQuery,false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query */

        // DO EKSPERIMENT FOR GETTING RETRIEVED DOCUMENTS FOR EACH QUERY
        Experiment exp = new Experiment();
        exp.setInvertedFile(wordProcessor.getInvertedFile(),false,false);
        exp.setInvertedFileQuery(wordProcessor.getInvertedFileQuery(), false, false);
        exp.setNormalFile(wordProcessor.getNormalFile());
        exp.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp.evaluate(false);
       /* query manualQuery = new query(0,contentQuery);
        InputQuery iq = new InputQuery();
        iq.setInvertedFile(wordProcessor.getInvertedFile(),false,false);
        iq.setNormalFile(wordProcessor.getNormalFile());
        iq.SearchDocumentsUsingQuery(manualQuery.getQueryContent(),false); */

        /*
        =======================================RELEVANCE FEEDBACK (NEW EKSPERIMENT) ============================================
         */

        // ARRAY MENYIMPAN HASIL RELEVANCE FEEDBACK PER QUERY
        ArrayList<documentsRelevancesFeedback> listFeedbacksEachQueries = new ArrayList<>();

        // ISI FORM RELEVANCE FEEDBACK (EKSPERIMENT)
        wordProcessor.loadQueryRelevancesFinal();
        queryRelevances thisQueryRelevances = wordProcessor.getListQueryRelevancesFinal();
        int counter = 0;
        for (SingleQueryEvaluation m : exp.getEvals()) {
            query Query = (query) wordProcessor.getListQueriesFinal().get(counter);
            documentsRelevancesFeedback relevances = new documentsRelevancesFeedback(Query);
            for (Integer index : m.getRetDocNums()) {
                if (wordProcessor.isDocumentRelevantForThisQuery(index,Query.getIndex(),thisQueryRelevances)) {
                    relevances.insertDocumentRelevance(index, true);
                } else {
                    relevances.insertDocumentRelevance(index, false);
                }
            }
            listFeedbacksEachQueries.add(relevances);
            counter++;
        }

        // ISI FORM RELEVANCE FEEDBACK (INTERACTIVE)
       /* documentsRelevancesFeedback relevances = new documentsRelevancesFeedback(manualQuery);
        for (Map.Entry m : InputQuery.getResult().entrySet()) {
            document Document = (document) m.getKey();
            if (Document.getIndex() % 2 == 0) {         // Index dokumen genap : relevant (Asumsi)
                relevances.insertDocumentRelevance(Document.getIndex(),true);
            } else {                                    // Index dokumen ganjil : irrelevant (Asumsi)
                relevances.insertDocumentRelevance(Document.getIndex(),false);
            }
        }
        listFeedbacksEachQueries.add(relevances); */

        // RELEVANCE FEEDBACK (SEMUA QUERY)
        ArrayList<RelevanceFeedback> listRelevanceFeedbackExperiment = new ArrayList<>();
        for (documentsRelevancesFeedback relevance : listFeedbacksEachQueries) {
            // Hati-hati inverted dan normal file query antara eksperiment / interactive harus benar
            RelevanceFeedback feedback = new RelevanceFeedback(wordProcessor.getInvertedFile(), wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getNormalFileQuery(), relevance);
            feedback.updateTermInThisQuery(1);
           // feedback.updateUnseenTermInThisQuery(1);
            listRelevanceFeedbackExperiment.add(feedback);
        }

        // LIST NEW QUERIES BASED ON RELEVANCE FEEDBACK
        for (RelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query newQuery = feedback.convertNewQueryComposition();
            System.out.println("Nomor Query : " + newQuery.getIndex());
            System.out.println("Konten Query : " + newQuery.getQueryContent());
            System.out.println("Bobot tiap term di query ini : ");
            for (Map.Entry m : feedback.getNewQueryComposition().entrySet()) {
                String term = (String) m.getKey();
                double bobot = (Double) m.getValue();
                System.out.println("Term : " + term);
                System.out.println("Bobot : " + bobot);
            }
            System.out.println("===================================================================");
        }

        // RE-EKSPERIMENT AFTER RELEVANCE FEEDBACK
      /*  ArrayList<document> newListDocumentForThisQuery = null;
        for (RelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
            query queryProcessed = feedback.getListDocumentRelevancesThisQuery().getQuery();
            query newQueryResult = feedback.convertNewQueryComposition();
            newListDocumentForThisQuery = wordProcessor.recreateDocumentList(wordProcessor.getListDocumentsFinal(),feedback.getListDocumentIrrelevant());
            queryRelevances newQueryRelevanceForThisQuery = wordProcessor.recreateQueryRelevances(queryProcessed,wordProcessor.getListQueryRelevancesFinal(),feedback.getListDocumentIrrelevant());
            indexTabel newInvertedFileQuery = feedback.getInvertedFileQuery();
            normalTabel newNormalFileQuery = feedback.getNormalFileQuery();

            // CEK QUERY RELEVANCES
            for (Map.Entry m : newQueryRelevanceForThisQuery.getListQueryRelevances().entrySet()) {
                int thisQueryIndex = (Integer) m.getKey();
                ArrayList<Integer> listDocumentRelevant = (ArrayList<Integer>) m.getValue();
                for (Integer indexDocument : listDocumentRelevant) {
                    System.out.println(indexDocument);
                }
                System.out.println("=========================================================================");
            }
        } */
    }
}
