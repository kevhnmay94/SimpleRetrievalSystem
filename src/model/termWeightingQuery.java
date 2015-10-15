package model;

import java.util.HashMap;

/**
 * Created by steve on 14/10/2015.
 */
public class termWeightingQuery {
    private query currentQuery;
    private HashMap<String,Double> termWeightInOneQuery = new HashMap<String, Double>();
    private HashMap<String,Integer> termCounterInOneQuery = new HashMap<String,Integer>();

    public query getCurrentQuery() {
        return currentQuery;
    }

    public void setCurrentQuery(query currentQuery) {
        this.currentQuery = currentQuery;
    }

    public HashMap<String, Double> getTermWeightInOneQuery() {
        return termWeightInOneQuery;
    }

    public void setTermWeightInOneQuery(HashMap<String, Double> termWeightInOneQuery) {
        this.termWeightInOneQuery = termWeightInOneQuery;
    }

    public HashMap<String, Integer> getTermCounterInOneQuery() {
        return termCounterInOneQuery;
    }

    public void setTermCounterInOneQuery(HashMap<String, Integer> termCounterInOneQuery) {
        this.termCounterInOneQuery = termCounterInOneQuery;
    }
}
