package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 10/10/2015.
 */
public class queryRelevances {
    private ConcurrentHashMap<Integer,ArrayList<Integer>> listQueryRelevances = new ConcurrentHashMap<Integer, ArrayList<Integer>>();

    public ConcurrentHashMap<Integer, ArrayList<Integer>> getListQueryRelevances() {
        return listQueryRelevances;
    }

    public void insertQueryRelevances(int indexQuery, int indexDocument) {
        if (listQueryRelevances.containsKey(indexQuery)) {
            ArrayList<Integer> listDocumentsRelevant = listQueryRelevances.get(indexQuery);
            listDocumentsRelevant.add(indexDocument);
        } else {
            ArrayList<Integer> newListDocumentsRelevant = new ArrayList<Integer>();
            newListDocumentsRelevant.add(indexDocument);
            listQueryRelevances.put(indexQuery,newListDocumentsRelevant);
        }
    }

    /**
     * Remove document (one or more) that is judged relevant by qrels
     * if query index is indexQuery from listIndexDocument
     * @param listIndexDocument
     * @param indexQuery
     */
    public void removeDocumentFromQrels (ArrayList<Integer> listIndexDocument, int indexQuery) {
        ArrayList<Integer> listDocumentThisQuery = listQueryRelevances.get(indexQuery);
        ArrayList<Integer> listDocumentRemoved = new ArrayList<>();
        if(listDocumentThisQuery == null) return;
        for (Integer indexDocument : listDocumentThisQuery) {
            if (isDocumentContainedInList(indexDocument,listIndexDocument)) {
                listDocumentRemoved.add(indexDocument);
            }
        }
        if (!listDocumentRemoved.isEmpty()) {
            for (Integer index : listDocumentRemoved) {
                listQueryRelevances.get(indexQuery).remove(index);
            }
        }
    }

    /**
     * Check if one document with index indexDocumentChecked
     * contained in list listDocumentsIndex
     * @param indexDocumentChecked
     * @param listDocumentsIndex
     * @return
     */
    public boolean isDocumentContainedInList(int indexDocumentChecked, ArrayList<Integer> listDocumentsIndex) {
        boolean isDocumentContained = false;
        for (Integer index : listDocumentsIndex) {
            if (index == indexDocumentChecked) {
                isDocumentContained = true; break;
            }
        }
        return isDocumentContained;
    }
}
