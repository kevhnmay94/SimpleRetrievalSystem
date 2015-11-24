package Utils;

import Feedback.PseudoRelevanceFeedback;
import Feedback.RelevanceFeedback;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by khaidzir on 11/23/2015.
 */
public class RelevanceFeedbackInteractive {

    int qTfMode, qIdfMode;
    boolean isStem;
    PreprocessWords wordProcessor;
    Map<document, Double> result;
    String query;
    query newQuery;
    double exectime;

    documentsRelevancesFeedback documentsRelevances;

    int topS, topN;
    boolean isPseudo, useSameCollection, useQueryExpansion, isNormalize;

    Map<document, Double> result2;

    public Map<document, Double> getResult() {
        return result;
    }
    public query getNewQuery() { return newQuery; }

    public RelevanceFeedbackInteractive() {
        wordProcessor = new PreprocessWords();
        documentsRelevances = new documentsRelevancesFeedback();
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
    public void setIsPseudo(boolean p) { isPseudo = p; }

    public void setDocumentMode(int tfmode, int idfmode, boolean stem){
        System.out.println("Indexing documents...");
        double start = System.currentTimeMillis();

        wordProcessor.loadIndexTabel(stem);
        TermsWeight.termFrequencyWeighting(tfmode, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeighting(idfmode, wordProcessor.getInvertedFile(),wordProcessor.getNormalFile());

        double finish = System.currentTimeMillis();
        System.out.println("Indexing documents done in " + (finish-start) + " ms.\n");
    }

    public void setQueryMode(int tfmode, int idfmode, boolean stem) {
        qTfMode = tfmode;
        qIdfMode = idfmode;
        isStem = stem;
    }

    public void setDocumentsRelevances(documentsRelevancesFeedback d) {
        documentsRelevances = d;
    }

    public void setInvertedFile (indexTabel idxTable, boolean isInvertedFileCreated, boolean isStemmingApplied) {
        wordProcessor.setInvertedFile(idxTable);
        wordProcessor.loadDocumentsFinal(isInvertedFileCreated,isStemmingApplied);
    }

    public void setNormalFile (normalTabel normalFile) {
        wordProcessor.setNormalFile(normalFile);
    }

    public void SearchDocumentsUsingQuery(String query, boolean isNormalize) {
        double start, finish;
        this.query = query;
        documentsRelevances.setQuery(new query(0,query));
        result = new HashMap<>();

        // Proses query
        System.out.println("Indexing queries...");
        start = System.currentTimeMillis();

        wordProcessor.loadIndexTabelForManualQuery(query, isStem);
        TermsWeight.termFrequencyWeightingQuery(qTfMode, wordProcessor.getInvertedFileQueryManual(), wordProcessor.getNormalFile());
        TermsWeight.inverseDocumentWeightingQuery(qIdfMode, wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getInvertedFile(), wordProcessor.getNormalFile());

        finish = System.currentTimeMillis();
        System.out.println("Indexing queries done in " + (finish-start) + " ms.\n");

        // Hitung similarity query dengan dokumen
        System.out.println("Calculating similarity...");
        start = System.currentTimeMillis();

        model.query q = new query(0, query);

        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            double weight = DocumentRanking.countSimilarityDocument(q, wordProcessor.getInvertedFileQueryManual(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), wordProcessor.getNormalFileQueryManual(), isNormalize);
            result.put(Document, weight);
        }
        result = DocumentRanking.rankDocuments(result);

        // potong sebanyak S
        if(result.size()>topS) {
            ArrayList<document> deletedEl=new ArrayList<>();
            int counter=1;
            for(document d : result.keySet()) {
                if(counter>topS) {
                    deletedEl.add(d);
                }
                counter++;
            }
            for(document d : deletedEl) {
                result.remove(d);
            }
        }

        for(document d : result.keySet()) {
            documentsRelevances.insertDocumentRelevance(d.getIndex(), false);
        }

        finish = System.currentTimeMillis();
        exectime = finish-start;
        System.out.println("Calculating similarity done in " + exectime + " ms.\n");
    }

    public void setRelevanceDocuments(ArrayList<Integer> docNumList) {
        for(int a : docNumList)
            documentsRelevances.insertDocumentRelevance(a, true);
    }

    public void secondRetrieval(int tipe) {
        if(isPseudo) pseudoFeedback(tipe);
        else feedback(tipe);
    }

    private void feedback(int tipe) {
        // query reweighting & query expansion
        RelevanceFeedback feedback = new RelevanceFeedback(wordProcessor.getInvertedFile(), wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getNormalFileQueryManual(), documentsRelevances);
        feedback.updateTermInThisQuery(tipe);
        if(useQueryExpansion)
            feedback.updateUnseenTermInThisQuery(tipe);

        // retrieval kedua
        result2 = new HashMap<>();
        newQuery = feedback.convertNewQueryComposition();
        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            if(!useSameCollection) {
                if( result.containsKey(Document) ) continue;
            }
            double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFileQueryManual(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), feedback.getNormalFileQueryManual(),
                    isNormalize);
            result2.put(Document, weight);
        }
        result2 = (HashMap)DocumentRanking.rankDocuments(result2);
    }

    private void pseudoFeedback(int tipe) {
        // menandai dokumen yang relevan dan yang tidak
        documentsPseudoRelevanceFeedback relevance = new documentsPseudoRelevanceFeedback(topN,new query(0, query));
        for (document d : result.keySet()) {
            relevance.insertDocumentRetrieved(d.getIndex());
        }

        // query reweighting & query expansion
        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback(wordProcessor.getInvertedFile(),wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getNormalFileQueryManual(),relevance);
        feedback.updateTermInThisQuery(tipe);
        if(useQueryExpansion)
            feedback.updateUnseenTermInThisQuery(tipe);

        // retrieval kedua
        result2 = new HashMap<>();
        newQuery = feedback.convertNewQueryComposition();
        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            if(!useSameCollection) {
                if( result.containsKey(Document) ) continue;
            }
            double weight = DocumentRanking.countSimilarityDocument(newQuery, feedback.getInvertedFileQuery(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), feedback.getNormalFileQuery(),
                    isNormalize);
            result2.put(Document, weight);
        }
        result2 = (HashMap)DocumentRanking.rankDocuments(result2);
    }

    public String getSummaryResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.size());
        sb.append(result.size() > 1 ? " results" : " result");
        sb.append("(" + (exectime/1000) + " seconds)\n\n");
        for(document doc : result.keySet()) {
            sb.append("Title : " + doc.getJudul());       //sb.append("\n");
            sb.append("Author : " + doc.getAuthor());     //sb.append("\n");
            sb.append("Content : \n" + doc.getKonten());  sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getSummaryResult2() {
        StringBuilder sb = new StringBuilder();
        sb.append(result2.size());
        sb.append(result2.size() > 1 ? " results" : " result");
        sb.append("(" + (exectime/1000) + " seconds)\n\n");
        for(document doc : result2.keySet()) {
            sb.append("Title : " + doc.getJudul());       //sb.append("\n");
            sb.append("Author : " + doc.getAuthor());     //sb.append("\n");
            sb.append("Content : \n" + doc.getKonten());  sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Setting awal awal
       /* EksternalFile.setPathDocumentsFile("test\\CACM\\CACM.ALL");
        EksternalFile.setPathQueriesFile("test\\CACM\\QUERYAAF");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */
        EksternalFile.setPathDocumentsFile("test\\ADI\\ADI.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        RelevanceFeedbackInteractive rfi = new RelevanceFeedbackInteractive();

        // Setting mode
        rfi.setDocumentMode(1, 1, true);
        rfi.setQueryMode(1, 1, true);
        rfi.setTopS(10);
        rfi.setTopN(5);
        rfi.setUseSameCollection(false);
        rfi.setUseQueryExpansion(false);
        rfi.setIsPseudo(true);

        // Query dan hasil
        String query = "computer science in medical research";
        rfi.SearchDocumentsUsingQuery(query, false);

        // Print
        System.out.println(rfi.getSummaryResult());

        System.out.println("Second retrieval : ");

        rfi.secondRetrieval(3);
        System.out.println(rfi.getSummaryResult2());

        System.out.println("\nQuery lama : " + rfi.query);
        System.out.println("\nQuery baru : " + rfi.newQuery.getQueryContent());
    }

}
