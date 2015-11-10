package model;

import sample.Interface1Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 07/10/2015.
 */
public class termWeightingDocument {
    private ConcurrentHashMap<Integer,counterWeightPair> documentWeightCounterInOneTerm;

    public ConcurrentHashMap<Integer, counterWeightPair> getDocumentWeightCounterInOneTerm() {
        return documentWeightCounterInOneTerm;
    }

    public void setDocumentWeightCounterInOneTerm(ConcurrentHashMap<Integer, counterWeightPair> documentWeightCounterInOneTerm) {
        this.documentWeightCounterInOneTerm = documentWeightCounterInOneTerm;
    }

    public termWeightingDocument() {
        documentWeightCounterInOneTerm = new ConcurrentHashMap<>();
    }

    public void insertNewDocument(int indexDocument, double weight, int counter) {
        documentWeightCounterInOneTerm.put(indexDocument,new counterWeightPair(counter,weight));
    }
}
