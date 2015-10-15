package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve on 07/10/2015.
 */
public class indexTabel {
    private HashMap<String, termWeightingDocument> listTermWeights = new HashMap<String, termWeightingDocument>();

    public HashMap<String, termWeightingDocument> getListTermWeights() {
        return listTermWeights;
    }

    public void insertRowTable(String term, document Document, double weight) {
        if (listTermWeights.containsKey(term)) {
            termWeightingDocument relation = listTermWeights.get(term);
            boolean isTermRepeatedInDocument = false;
            for (int i=0; i<relation.getDocumentPerTerm().size(); i++) {
                if (relation.getDocumentPerTerm().get(i).getIndex() == Document.getIndex()) {
                    isTermRepeatedInDocument = true;
                    int newCounter = relation.getDocumentCountersPerTerm().get(i) + 1;
                    relation.getDocumentCountersPerTerm().set(i,newCounter);
                }
            }
            if (!isTermRepeatedInDocument) {
                relation.insertNewDocument(Document,weight,1);
            }
        } else {
            termWeightingDocument newRelation = new termWeightingDocument();
            newRelation.insertNewDocument(Document,weight,1);
            listTermWeights.put(term,newRelation);
        }
    }

    public static void main(String[] arg) {
        indexTabel tabel = new indexTabel();
        // Masukkan data ke tabel
        document document1 = new document(1,"b","bara","kucing");
        document document2 = new document(2,"b","biri","kucing");
        document document3 = new document(3,"b","buru","kucing");
        document document4 = new document(4,"b","bere","kucing");
        tabel.insertRowTable("kata",document1,0.0);
        tabel.insertRowTable("kata",document2,1.0);
        tabel.insertRowTable("kata",document3,2.0);
        tabel.insertRowTable("katah",document4,3.0);
        // Keluarkan isi hashmap
        for(Map.Entry m:tabel.getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString());
            for (document Document:((termWeightingDocument) m.getValue()).getDocumentPerTerm()) {
                System.out.println("Id : " + Document.getIndex());
                System.out.println("Judul : " + Document.getJudul());
                System.out.println("Konten : " + Document.getKonten());
            }
            for (double weights :((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm()) {
                System.out.println("Bobot : " + weights);
            }
            for (int counter:((termWeightingDocument) m.getValue()).getDocumentCountersPerTerm()) {
                System.out.println("Counter : " + counter);
            }
            System.out.println("====================================================================================");
        }
    }
}
