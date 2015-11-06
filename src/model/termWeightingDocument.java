package model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by steve on 07/10/2015.
 */
public class termWeightingDocument {
    private HashMap<Integer,counterWeightPair> documentWeightCounterInOneTerm;

    public HashMap<Integer, counterWeightPair> getDocumentWeightCounterInOneTerm() {
        return documentWeightCounterInOneTerm;
    }

    public void setDocumentWeightCounterInOneTerm(HashMap<Integer, counterWeightPair> documentWeightCounterInOneTerm) {
        this.documentWeightCounterInOneTerm = documentWeightCounterInOneTerm;
    }

    public termWeightingDocument() {
        documentWeightCounterInOneTerm = new HashMap<>();
    }

    public void insertNewDocument(int indexDocument, double weight, int counter) {
        documentWeightCounterInOneTerm.put(indexDocument,new counterWeightPair(counter,weight));
    }
}
