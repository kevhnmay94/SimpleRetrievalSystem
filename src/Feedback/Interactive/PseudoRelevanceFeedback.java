package Feedback.Interactive;

import Utils.*;
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

    // GETTER-GETTER
    public indexTabel getInvertedFile() {
        return invertedFile;
    }

    public documentsPseudoRelevanceFeedback getListDocumentRetrievedForThisQuery() {
        return listDocumentRetrievedForThisQuery;
    }

    public indexTabel getInvertedFileQuery() {
        return invertedFileQuery;
    }

    public normalTabel getNormalFileQuery() {
        return normalFileQuery;
    }

    public ArrayList<Integer> getListDocumentsRelevant() {
        return listDocumentsRelevant;
    }

    public HashMap<String, Double> getNewQueryComposition() {
        return newQueryComposition;
    }

    /**
     * Convert query composition into new query with previously same index
     * @return
     */
    public query convertNewQueryComposition() {
        int index = listDocumentRetrievedForThisQuery.getQuery().getIndex();
        StringBuffer queryContent = new StringBuffer();
        for (Map.Entry m : newQueryComposition.entrySet()) {
            String keyTerm = (String) m.getKey();
            queryContent.append(keyTerm + " ");
        }
        query Query = new query(index,queryContent.toString());
        return Query;
    }

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
        query thisQuery = listDocumentRetrievedForThisQuery.getQuery();
        StringTokenizer token = new StringTokenizer(thisQuery.getQueryContent(), " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String keyTerm = token.nextToken();
            String filteredWord;
            if (invertedFileQuery.isStemmingApplied()) {
                filteredWord = StemmingPorter.stripAffixes(keyTerm);
            } else {
                filteredWord = keyTerm;
            }
            try {
                termWeightingDocument relation = invertedFileQuery.getListTermWeights().get(filteredWord);
                if (relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()) != null) {
                    double oldWeight = relation.getDocumentWeightCounterInOneTerm().get(thisQuery.getIndex()).getWeight();
                    double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,filteredWord,oldWeight);
                    if (newWeight > 0) {
                        newQueryComposition.put(keyTerm, newWeight);
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
        int thisQueryIndex = listDocumentRetrievedForThisQuery.getQuery().getIndex();
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            if (!isTermAppearInQuery(keyTerm)) {
                double newWeight = computeNewWeightTerm(relevanceFeedbackMethod,keyTerm,0.0);
                if (newWeight > 0) {
                    String filteredWord = "";
                    if (invertedFile.isStemmingApplied()) {
                        filteredWord = invertedFile.getMappingStemmedTermToNormalTerm().get(keyTerm);
                    } else {
                        filteredWord = keyTerm;
                    }
                    newQueryComposition.put(filteredWord,newWeight);
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
        double sumWeightDocumentRelevant = computeSumWeightDocuments(term, listDocumentsRelevant);
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
     * Check term in inverted file query appears or not
     * @param term
     * @return
     */
    private boolean isTermAppearInQuery(String term) {
        boolean isTermAppear = false;
        query thisQuery = listDocumentRetrievedForThisQuery.getQuery();
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
        // CISI
       /* EksternalFile.setPathDocumentsFile("test\\CISI\\cisi.all");
        EksternalFile.setPathQueriesFile("test\\CISI\\query.text");
        EksternalFile.setPathQrelsFile("test\\CISI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */
        // ADI
       /* EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */
        // CRAN
        EksternalFile.setPathDocumentsFile("test\\CRAN\\CRAN.all");
        EksternalFile.setPathQueriesFile("test\\CRAN\\QUERYADG");
        EksternalFile.setPathQrelsFile("test\\CRAN\\QRELSADE");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");
        // MED
       /* EksternalFile.setPathDocumentsFile("test\\MED\\MED.all");
        EksternalFile.setPathQueriesFile("test\\MED\\QUERYABW");
        EksternalFile.setPathQrelsFile("test\\MED\\QRELSABT");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */
        // NPL
       /* EksternalFile.setPathDocumentsFile("test\\NPL\\NPL.all");
        EksternalFile.setPathQueriesFile("test\\NPL\\QUERYACB");
        EksternalFile.setPathQrelsFile("test\\NPL\\QRELSACA");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus dokumen)
        TermsWeight.inverseDocumentWeighting(1, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY (EKSPERIMENT)
        wordProcessor.loadIndexTabelForQueries(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query

        // PROSES BUAT INVERTED FILE BUAT QUERY (INTERACTIVE)
        String contentQuery = "computer science";
        wordProcessor.loadIndexTabelForManualQuery(contentQuery,true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getNormalFile()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(1, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getInvertedFile(), wordProcessor.getNormalFile()); // IDS khusus query */

        // DO EKSPERIMENT FOR GETTING RETRIEVED DOCUMENTS FOR EACH QUERY
        Experiment exp = new Experiment();
        exp.setInvertedFile(wordProcessor.getInvertedFile(),false,true);
        exp.setInvertedFileQuery(wordProcessor.getInvertedFileQuery(), false, true);
        exp.setNormalFile(wordProcessor.getNormalFile());
        exp.setNormalFileQuery(wordProcessor.getNormalFileQuery());
        exp.evaluate(false);
        /* query manualQuery = new query(0,contentQuery);
        InputQuery iq = new InputQuery();
        iq.setInvertedFile(wordProcessor.getInvertedFile(),false,true);
        iq.setNormalFile(wordProcessor.getNormalFile());
<<<<<<< HEAD
        iq.setQueryMode(1,1,true);;
=======
        iq.SearchDocumentsUsingQuery(manualQuery.getQueryContent(),false);
        iq.setQueryMode(1,1,true);
>>>>>>> 7252d0e22b79d66136c0fe57ab94411a5dce2b30
        iq.SearchDocumentsUsingQuery(manualQuery.getQueryContent(),false); */

        /*
        =======================================RELEVANCE FEEDBACK (NEW EKSPERIMENT) ============================================
         */

        // ARRAYLIST PSEUDO FEEDBACK
        ArrayList<documentsPseudoRelevanceFeedback> listFeedbacksEachQueries = new ArrayList<>();

        // ISI FORM PSEUDO RELEVANCE FEEDBACK (EKSPERIMENT)
        int counter = 0;
        for (SingleQueryEvaluation m : exp.getEvals()) {
            query Query = (query) wordProcessor.getListQueriesFinal().get(counter);
            documentsPseudoRelevanceFeedback relevances = new documentsPseudoRelevanceFeedback(3,Query);    // 3 TOP DOCUMENT RELEVANT
            for (Integer index : m.getRetDocNums()) {
                relevances.insertDocumentRetrieved(index);
            }
            listFeedbacksEachQueries.add(relevances);
            counter++;
        }

        // ISI FORM PSEUDO RELEVANCE FEEDBACK (INTERACTIVE)
       /* documentsPseudoRelevanceFeedback relevances = new documentsPseudoRelevanceFeedback(2,manualQuery);
        for (Map.Entry m : InputQuery.getResult().entrySet()) {
            document Document = (document) m.getKey();
            relevances.insertDocumentRetrieved(Document.getIndex());
        }
        listFeedbacksEachQueries.add(relevances); */

        // RELEVANCE FEEDBACK (SEMUA QUERY)
        ArrayList<PseudoRelevanceFeedback> listRelevanceFeedbackExperiment = new ArrayList<>();
        for (documentsPseudoRelevanceFeedback relevance : listFeedbacksEachQueries) {
            // EKSPERIMEN SAJA
            PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback(wordProcessor.getInvertedFile(),wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getNormalFileQuery(),relevance);
            // INTERACTIVE SAJA
           /* PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback(wordProcessor.getInvertedFile(),wordProcessor.getInvertedFileQueryManual(),
                    wordProcessor.getNormalFileQueryManual(),relevance); */
            feedback.updateTermInThisQuery(1);
            // feedback.updateUnseenTermInThisQuery(1);
            listRelevanceFeedbackExperiment.add(feedback);
        }

        // LIST NEW QUERIES BASED ON RELEVANCE FEEDBACK
        for (PseudoRelevanceFeedback feedback : listRelevanceFeedbackExperiment) {
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
