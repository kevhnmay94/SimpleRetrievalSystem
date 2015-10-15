package model;

import Utils.QueryRelevancesLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by khaidzir on 14/10/2015.
 */

/* Kelas untuk mengevaluasi SEBUAH query */
public class SingleQueryEvaluation {

    public enum evalType {cisi, adi};
    public double precision, recall, nonInterpolatedAvgPrecision;
    query query;
    ArrayList<document> retrievedDocuments;
    int queryNum;
    ArrayList<Integer> retDocNums;
    evalType type;

    // Retrieved document yg relevan
    HashMap <Integer, Boolean> relDocMap;

    // Konstruktor - konstruktor
    public SingleQueryEvaluation() {}

    public SingleQueryEvaluation(query query, ArrayList<document> docs, evalType type) {
        this.query = query;
        this.retrievedDocuments = docs;
        this.type = type;
        queryNum = query.getIndex();
        retDocNums = new ArrayList<Integer>();
        for (document doc : retrievedDocuments) {
            retDocNums.add(doc.getIndex());
        }
    }
    public SingleQueryEvaluation(int qNum, ArrayList<Integer> docNums, evalType type) {
        this.queryNum = qNum;
        this.retDocNums = docNums;
        this.type = type;
    }

    // Setter
    public void setQuery(query query) {
        this.query = query;
        this.queryNum = query.getIndex();
    }
    public void setRetrievedDocuments(ArrayList<document> docs) {
        this.retrievedDocuments = docs;
        retDocNums = new ArrayList<Integer>();
        for (document doc : retrievedDocuments) {
            retDocNums.add(doc.getIndex());
        }
    }
    public void setQueryNum(int qnum) {
        this.queryNum = qnum;
    }
    public void setRetDocNums(ArrayList<Integer> docNums) {
        this.retDocNums = docNums;
    }

    public void evaluate() {
        if (!QueryRelevancesLoader.isLoaded)
            QueryRelevancesLoader.ReadQrelsFile();

        // Evaluasi
        /* Perhatian ! Urutan jangan ditukar!
           Yang pertama dihitung harus non interpolated average precision !
         */
        calculateNonInterpolatedAvgPrecision();
        calculatePrecision();
        calculateRecall();
    }

    private void calculatePrecision() {
        precision = (double)relDocMap.size()/(double) retDocNums.size();
    }

    private void calculateRecall() {
        double numRel = type==evalType.adi ? QueryRelevancesLoader.qrelsAdi.get(queryNum).size() :
                                             QueryRelevancesLoader.qrelsCisi.get(queryNum).size();
        recall = (double)relDocMap.size()/numRel;
    }

    private void calculateNonInterpolatedAvgPrecision() {
        // ADI / CISI
        ArrayList<Integer> docRels = type==evalType.adi ? QueryRelevancesLoader.qrelsAdi.get(queryNum) :
                QueryRelevancesLoader.qrelsCisi.get(queryNum);

        // Map untuk mengecek agar tidak menghitung dokumen yang sama 2 kali
        relDocMap = new HashMap<Integer, Boolean>();

        nonInterpolatedAvgPrecision = 0.0f;
        int counter = 1;
        for (int dnum : retDocNums) {
            if (docRels.contains(dnum) && !relDocMap.containsKey(dnum)) {
                relDocMap.put(dnum, true);
                nonInterpolatedAvgPrecision += (double)relDocMap.size() / (double)counter;
            }
            counter++;
        }
        nonInterpolatedAvgPrecision /= (double)docRels.size();
    }

    public void printEvalSummary() {
        String stype = type==evalType.adi ? "ADI" : "CISI";
        System.out.println(stype + " file");
        System.out.println("Query number : " + queryNum);
        System.out.print("Retrieved document numbers : ");
        for(int dnum : retDocNums) {
            System.out.print (dnum + " ");
        }
        System.out.print("\nRelevant document numbers in collection : ");
        ArrayList<Integer> docRels = type==evalType.adi ? QueryRelevancesLoader.qrelsAdi.get(queryNum) :
                QueryRelevancesLoader.qrelsCisi.get(queryNum);
        for(int dnum : docRels) {
            System.out.print (dnum + " ");
        }
        System.out.print("\nRelevant retrieved document numbers : ");
        for (int dnum : relDocMap.keySet()) {
            System.out.print(dnum + " ");
        }
        System.out.println("\nRecall : " + recall);
        System.out.println("Precision : " + precision);
        System.out.println("Non Interpolated Average Precision : " + nonInterpolatedAvgPrecision);
    }

    public static void main(String [] args) {
        ArrayList<Integer> docNums = new ArrayList<Integer> ();
        docNums.add(1);
        docNums.add(14);
        docNums.add(21);
        docNums.add(33);
        docNums.add(33);
        docNums.add(33);
        docNums.add(45);
        docNums.add(45);
        docNums.add(45);
        SingleQueryEvaluation sqe = new SingleQueryEvaluation(14,docNums, evalType.adi);
        sqe.evaluate();
        sqe.printEvalSummary();
    }

}
