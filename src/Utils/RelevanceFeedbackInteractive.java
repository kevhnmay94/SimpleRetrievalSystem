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
    public Map<document, Double> result;
    String query;
    query newQuery;
    double exectime;

    RelevanceFeedback relevanceFeedback;
    PseudoRelevanceFeedback pseudoFeedback;

    public documentsRelevancesFeedback documentsRelevances;

    int topS, topN;
    boolean isPseudo, useSameCollection, useQueryExpansion, isNormalize;

    public Map<document, Double> result2;

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
        relevanceFeedback = new RelevanceFeedback(wordProcessor.getInvertedFile(), wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getNormalFileQueryManual(), documentsRelevances);
        relevanceFeedback.updateTermInThisQuery(tipe);
        if(useQueryExpansion)
            relevanceFeedback.updateUnseenTermInThisQuery(tipe);

        // retrieval kedua
        result2 = new HashMap<>();
        newQuery = relevanceFeedback.convertNewQueryComposition();
        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            if(!useSameCollection) {
                if( result.containsKey(Document) ) continue;
            }
            double weight = DocumentRanking.countSimilarityDocument(newQuery, relevanceFeedback.getInvertedFileQueryManual(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), relevanceFeedback.getNormalFileQueryManual(),
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
        pseudoFeedback = new PseudoRelevanceFeedback(wordProcessor.getInvertedFile(),wordProcessor.getInvertedFileQueryManual(),
                wordProcessor.getNormalFileQueryManual(),relevance);
        pseudoFeedback.updateTermInThisQuery(tipe);
        if(useQueryExpansion)
            pseudoFeedback.updateUnseenTermInThisQuery(tipe);
        if (relevance.getQuery().getIndex() == 8) {
            String path = "test\\invertedFile2.csv", path2 = "test\\invertedFileQuery2.csv", path3 = "test\\normalFile2.csv", path4 = "test\\normalFileQuery2.csv";
            EksternalFile file = new EksternalFile();
            file.writeInvertedFile(path, pseudoFeedback.getInvertedFile());
            file.writeInvertedFileQuery(path2, pseudoFeedback.getInvertedFileQuery());
            file.writeNormalFileQuery(path4, pseudoFeedback.getNormalFileQuery());
        }

        // retrieval kedua
        result2 = new HashMap<>();
        newQuery = pseudoFeedback.convertNewQueryComposition();
        Iterator listDocuments = wordProcessor.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            if(!useSameCollection) {
                if( result.containsKey(Document) ) continue;
            }
            double weight = DocumentRanking.countSimilarityDocument(newQuery, pseudoFeedback.getInvertedFileQuery(),
                    Document, wordProcessor.getInvertedFile(), wordProcessor.getNormalFile(), pseudoFeedback.getNormalFileQuery(),
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
    public String getSummaryResultWithWeight() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.size());
        sb.append(result.size() > 1 ? " results" : " result");
        sb.append("(" + (exectime/1000) + " seconds)\n");
        sb.append("Bobot query : ");
        for(Map.Entry m : wordProcessor.getInvertedFileQueryManual().getListTermWeights().entrySet()) {
            sb.append(m.getKey()).append("(")
                    .append(((termWeightingDocument)m.getValue()).getDocumentWeightCounterInOneTerm().get(0).getWeight())
                    .append(") ");
        }
        sb.append("\n\n");
        for(Map.Entry<document, Double> m : result.entrySet()) {
            sb.append("Title : " + m.getKey().getJudul());       //sb.append("\n");
            sb.append("Author : " + m.getKey().getAuthor());     //sb.append("\n");
            sb.append("Content : \n" + m.getKey().getKonten());
            sb.append("Similarity with query : " + m.getValue());
            sb.append("\n\n");
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
    public String getSummaryResult2WithWeight() {
        StringBuilder sb = new StringBuilder();
        sb.append(result2.size());
        sb.append(result2.size() > 1 ? " results" : " result");
        sb.append("(" + (exectime/1000) + " seconds)\n");
        HashMap<String,Double> q = isPseudo ? pseudoFeedback.getNewQueryComposition()
                                    : relevanceFeedback.getNewQueryComposition();
        sb.append("Bobot query : ");
        for(Map.Entry<String, Double> m : q.entrySet()) {
            sb.append(m.getKey()).append("(").append(m.getValue()).append(") ");
        }
        sb.append("\n\n");
        for(Map.Entry<document, Double> m : result2.entrySet()) {
            sb.append("Title : " + m.getKey().getJudul());       //sb.append("\n");
            sb.append("Author : " + m.getKey().getAuthor());     //sb.append("\n");
            sb.append("Content : \n" + m.getKey().getKonten());
            sb.append("Similarity with query : " + m.getValue());
            sb.append("\n\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Setting awal awal
//        EksternalFile.setPathDocumentsFile("test\\CACM\\CACM.ALL");
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        RelevanceFeedbackInteractive rfi = new RelevanceFeedbackInteractive();

        // Setting mode
        rfi.setDocumentMode(1, 1, true);
        rfi.setQueryMode(1, 1, true);
        rfi.setTopS(10);
        rfi.setTopN(5);
        rfi.setUseSameCollection(false);
        rfi.setUseQueryExpansion(false);
        rfi.setIsPseudo(false);

        // Query dan hasil
        String query = "What problems and concerns are there in making up descriptive titles?  \n" +
                "What difficulties are involved in automatically retrieving articles from \n" +
                "approximate titles?  \n" +
                "What is the usual relevance of the content of articles to their titles?";
        rfi.SearchDocumentsUsingQuery(query, false);

        // Print
        System.out.println(rfi.getSummaryResultWithWeight());

        System.out.println("Second retrieval : ");


        ArrayList<Integer> d = new ArrayList<>();
        d.add(17);
        d.add(46);
        d.add(62);
        rfi.setRelevanceDocuments(d);
        rfi.secondRetrieval(1);
        System.out.println(rfi.getSummaryResult2WithWeight());

        System.out.println("\nQuery lama : " + rfi.query);
        System.out.println("\nQuery baru : " + rfi.newQuery.getQueryContent());
    }

}
