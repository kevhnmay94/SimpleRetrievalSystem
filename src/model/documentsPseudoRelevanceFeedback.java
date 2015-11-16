package model;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by steve on 16/11/2015.
 */
public class documentsPseudoRelevanceFeedback {
    private int topDocumentsRelevant;
    private query Query;
    private ArrayList<Integer> listDocumentsRetrieved;

    // GETTER AND SETTER
    public int getTopDocumentsRelevant() {
        return topDocumentsRelevant;
    }

    public void setTopDocumentsRelevant(int topDocumentsRelevant) {
        this.topDocumentsRelevant = topDocumentsRelevant;
    }

    public query getQuery() {
        return Query;
    }

    public void setQuery(query query) {
        Query = query;
    }

    public ArrayList<Integer> getListDocumentsRetrieved() {
        return listDocumentsRetrieved;
    }

    public void setListDocumentsRetrieved(ArrayList<Integer> listDocumentsRetrieved) {
        this.listDocumentsRetrieved = listDocumentsRetrieved;
    }

    // CONSTRUCTOR 1
    public documentsPseudoRelevanceFeedback() {
        topDocumentsRelevant = 2;
        Query = new query(0,"Computer Science");
        listDocumentsRetrieved = new ArrayList<>();
    }

    // CONSTRUCTOR 2
    public documentsPseudoRelevanceFeedback(int topDocumentsRelevant, query Query) {
        this.topDocumentsRelevant = topDocumentsRelevant;
        this.Query = Query;
        listDocumentsRetrieved = new ArrayList<>();
    }

    // INSERT LIST INDEX OF DOCUMENTS RETRIEVED
    public void insertDocumentRetrieved(int indexDocumentRetrieved) {
        listDocumentsRetrieved.add(indexDocumentRetrieved);
    }
}
