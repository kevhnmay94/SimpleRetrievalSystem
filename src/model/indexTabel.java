package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 07/10/2015.
 */
public class indexTabel {
    private boolean isStemmingApplied;
    private ConcurrentHashMap<String,termWeightingDocument> listTermWeights;

    // GETTER DAN SETTER
    public ConcurrentHashMap<String,termWeightingDocument> getListTermWeights() {
        return listTermWeights;
    }

    public boolean isStemmingApplied() {
        return isStemmingApplied;
    }

    public void setStemmingApplied(boolean isStemmingApplied) {
        this.isStemmingApplied = isStemmingApplied;
    }

    public void setListTermWeights(ConcurrentHashMap<String, termWeightingDocument> listTermWeights) {
        this.listTermWeights = listTermWeights;
    }

    // CONSTRUCTOR
    public indexTabel() {
        listTermWeights = new ConcurrentHashMap<>();
    }

    // INSERT NEW ELEMENT INVERTED FILE
    public void insertRowTable(String term, int indexDocument, double weight) {
        if (listTermWeights.containsKey(term)) {
            termWeightingDocument relation = listTermWeights.get(term);
            if (relation.getDocumentWeightCounterInOneTerm().containsKey(indexDocument)) {
                int oldCounter = relation.getDocumentWeightCounterInOneTerm().get(indexDocument).getCounter();
                int newCounter = oldCounter + 1;
                relation.getDocumentWeightCounterInOneTerm().get(indexDocument).setCounter(newCounter);
            } else {
                relation.insertNewDocument(indexDocument, weight, 1);
            }
        } else {
            termWeightingDocument newRelation = new termWeightingDocument();
            newRelation.insertNewDocument(indexDocument, weight, 1);
            listTermWeights.put(term, newRelation);
        }
    }

    public static void main(String[] arg) {
        indexTabel tabel = new indexTabel();
        // Masukkan data ke tabel
        document document1 = new document(1,"b","bara","kucing");
        document document2 = new document(2,"b","biri","kucing");
        document document3 = new document(3,"b","buru","kucing");
        document document4 = new document(4,"b","bere","kucing");
        tabel.insertRowTable("kata",document1.getIndex(),0.0);
        tabel.insertRowTable("kata",document1.getIndex(),0.5);
        tabel.insertRowTable("kata",document2.getIndex(),1.0);
        tabel.insertRowTable("kata",document3.getIndex(),2.0);
        tabel.insertRowTable("katah",document4.getIndex(),3.0);
        // Keluarkan isi hashmap
        for(Map.Entry m:tabel.getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString() + "\n");
            for (Map.Entry n:((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                System.out.println("Nomor Dokumen : " + n.getKey());
                System.out.println("Counter term di dokumen ini : " + ((counterWeightPair) n.getValue()).getCounter());
                System.out.println("Bobot term di dokumen ini : " + ((counterWeightPair) n.getValue()).getWeight() + "\n");
            }
            System.out.println("====================================================================================");
        }
    }
}
