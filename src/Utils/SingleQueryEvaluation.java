package Utils;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import model.document;
import model.query;
import model.queryRelevances;

import java.util.*;

/**
 * Created by khaidzir on 14/10/2015.
 */

/* Kelas untuk mengevaluasi SEBUAH query */
public class SingleQueryEvaluation implements Comparable<SingleQueryEvaluation> {

    public double precision, recall, nonInterpolatedAvgPrecision;
    model.query query;
    ArrayList<document> retrievedDocuments;
    int queryNum;
    ArrayList<Integer> retDocNums;
    queryRelevances qRelevances;

    // Retrieved document yg relevan
    HashMap <Integer, Boolean> relDocMap;

    // Konstruktor - konstruktor
    public SingleQueryEvaluation() {}

    @Override
    public int compareTo(SingleQueryEvaluation another) {
        if (this.queryNum < another.queryNum){
            return -1;
        }else{
            return 1;
        }
    }

    public SingleQueryEvaluation(query query, ArrayList<document> docs, queryRelevances qRel) {
        this.query = query;
        this.retrievedDocuments = docs;
        this.qRelevances = qRel;
        queryNum = query.getIndex();
        retDocNums = new ArrayList<Integer>();
        for (document doc : retrievedDocuments) {
            retDocNums.add(doc.getIndex());
        }
    }
    public SingleQueryEvaluation(int qNum, ArrayList<Integer> docNums, queryRelevances qRel) {
        this.queryNum = qNum;
        this.retDocNums = docNums;
        this.qRelevances = qRel;
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
        // Evaluasi
        /* Perhatian ! Urutan jangan ditukar!
           Yang pertama dihitung harus non interpolated average precision !
         */
        calculateNonInterpolatedAvgPrecision();
        calculatePrecision();
        calculateRecall();
    }

    private void calculatePrecision() {
        if (retDocNums.size() == 0) precision = 0.0f;
        else precision = (double)relDocMap.size()/(double) retDocNums.size();
    }

    private void calculateRecall() {
        double numRel = qRelevances.getListQueryRelevances().get(queryNum).size();
        recall = (double)relDocMap.size()/numRel;
    }

    private void calculateNonInterpolatedAvgPrecision() {
        ArrayList<Integer> docRels = qRelevances.getListQueryRelevances().get(queryNum);

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
        System.out.println("Query number : " + queryNum);
        System.out.print("Retrieved document numbers : ");
        for(int dnum : retDocNums) {
            System.out.print (dnum + " ");
        }
        System.out.print("\nRelevant document numbers in collection : ");
        ArrayList<Integer> docRels = qRelevances.getListQueryRelevances().get(queryNum);

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

    public String getEvalSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Query number : "); sb.append(queryNum); sb.append("\n");
        sb.append("Retrieved document numbers : ");
        for(int dnum : retDocNums) {
            sb.append (dnum); sb.append(" ");
        }
        sb.append("\nRelevant document numbers in collection : ");

        ArrayList<Integer> docRels = qRelevances.getListQueryRelevances().get(queryNum);

        for(int dnum : docRels) {
            sb.append(dnum); sb.append(" ");
        }
        sb.append("\nRelevant retrieved document numbers : ");
        for (int dnum : relDocMap.keySet()) {
            sb.append(dnum); sb.append(" ");
        }
        sb.append("\nRecall : "); sb.append(recall);
        sb.append("\nPrecision : "); sb.append(precision);
        sb.append("\nNon Interpolated Average Precision : "); sb.append(nonInterpolatedAvgPrecision);
        sb.append("\n");

        return sb.toString();
    }

    public static void main(String [] args) {
        /*ArrayList<Integer> docNums = new ArrayList<Integer> ();
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
        sqe.printEvalSummary();*/
    }

}
