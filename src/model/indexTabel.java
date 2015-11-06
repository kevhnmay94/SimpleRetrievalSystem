package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve on 07/10/2015.
 */
public class indexTabel {
    private HashMap<String,termWeightingDocument> listTermWeights;

    public HashMap<String,termWeightingDocument> getListTermWeights() {
        return listTermWeights;
    }

    public indexTabel() {
        listTermWeights = new HashMap<>();
    }

    public void insertRowTable(String term, int indexDocument, double weight) {
        if (listTermWeights.containsKey(term)) {
            termWeightingDocument relation = listTermWeights.get(term);
            if (relation.getDocumentWeightCounterInOneTerm().containsKey(indexDocument)) {
                int oldCounter = relation.getDocumentWeightCounterInOneTerm().get(indexDocument).getCounter();
                relation.getDocumentWeightCounterInOneTerm().get(indexDocument).setCounter(oldCounter++);
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
        tabel.insertRowTable("kata",document2.getIndex(),1.0);
        tabel.insertRowTable("kata",document3.getIndex(),2.0);
        tabel.insertRowTable("katah",document4.getIndex(),3.0);
        // Keluarkan isi hashmap
        for(Map.Entry m:tabel.getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString());
            for (Map.Entry n:((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                System.out.println("Nomor Dokumen : " + n.getKey());
                System.out.println("Counter term di dokumen ini : " + ((counterWeightPair) n.getValue()).getCounter());
                System.out.println("Bobot term di dokumen ini : " + ((counterWeightPair) n.getValue()).getWeight());
            }
            System.out.println("====================================================================================");
        }
    }
}
