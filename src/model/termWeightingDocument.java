package model;

import sample.Interface1Controller;

import java.util.ArrayList;

/**
 * Created by steve on 07/10/2015.
 */
public class termWeightingDocument {
    private ArrayList<Integer> documentPerTerm = new ArrayList<Integer>();
    private ArrayList<Double> documentWeightingsPerTerm = new ArrayList<Double>();
    private ArrayList<Integer> documentCountersPerTerm = new ArrayList<Integer>();

    public ArrayList<Integer> getDocumentPerTerm() {
        return documentPerTerm;
    }

    public void setDocumentPerTerm(ArrayList<Integer> documentPerTerm) {
        this.documentPerTerm = documentPerTerm;
    }

    public ArrayList<Double> getDocumentWeightingsPerTerm() {
        return documentWeightingsPerTerm;
    }

    public void setDocumentWeightingsPerTerm(ArrayList<Double> documentWeightingsPerTerm) {
        this.documentWeightingsPerTerm = documentWeightingsPerTerm;
    }

    public ArrayList<Integer> getDocumentCountersPerTerm() {
        return documentCountersPerTerm;
    }

    public void setDocumentCountersPerTerm(ArrayList<Integer> documentCountersPerTerm) {
        this.documentCountersPerTerm = documentCountersPerTerm;
    }

    public void insertNewDocument(int NewDocument, double weight, int counter) {
        documentPerTerm.add(NewDocument);
        documentWeightingsPerTerm.add(weight);
        documentCountersPerTerm.add(counter);
    }
}
