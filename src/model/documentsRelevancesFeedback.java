package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by steve on 14/11/2015.
 */
public class documentsRelevancesFeedback {
    private HashMap<Integer,Boolean> isDocumentsRelevantList;
    private query Query;

    // GETTER AND SETTER
    public HashMap<Integer, Boolean> getIsDocumentsRelevantList() {
        return isDocumentsRelevantList;
    }

    public void setIsDocumentsRelevantList(HashMap<Integer, Boolean> isDocumentsRelevantList) {
        this.isDocumentsRelevantList = isDocumentsRelevantList;
    }

    public query getQuery() {
        return Query;
    }

    public void setQuery(query query) {
        Query = query;
    }

    // CONSTRUCTOR 1
    public documentsRelevancesFeedback() {
        isDocumentsRelevantList = new HashMap<>();
        Query = new query(0,"computer science");
    }

    // CONSTRUCTOR 2
    public documentsRelevancesFeedback(query Query) {
        isDocumentsRelevantList = new HashMap<>();
        this.Query = Query;
    }

    // INSERT ELEMENT
    public void insertDocumentRelevance(int indexDocument, boolean isRelevantInThisQuery) {
        isDocumentsRelevantList.put(indexDocument,isRelevantInThisQuery);
    }
}
