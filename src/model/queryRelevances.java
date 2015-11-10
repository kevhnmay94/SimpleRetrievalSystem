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
}
