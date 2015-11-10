package model;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 14/10/2015.
 */
public class termWeightingQuery {
    private query currentQuery;
    private ConcurrentHashMap<String,Double> termWeightInOneQuery = new ConcurrentHashMap<String, Double>();
    private ConcurrentHashMap<String,Integer> termCounterInOneQuery = new ConcurrentHashMap<String,Integer>();

    public query getCurrentQuery() {
        return currentQuery;
    }

    public void setCurrentQuery(query currentQuery) {
        this.currentQuery = currentQuery;
    }

    public ConcurrentHashMap<String, Double> getTermWeightInOneQuery() {
        return termWeightInOneQuery;
    }

    public void setTermWeightInOneQuery(ConcurrentHashMap<String, Double> termWeightInOneQuery) {
        this.termWeightInOneQuery = termWeightInOneQuery;
    }

    public ConcurrentHashMap<String, Integer> getTermCounterInOneQuery() {
        return termCounterInOneQuery;
    }

    public void setTermCounterInOneQuery(ConcurrentHashMap<String, Integer> termCounterInOneQuery) {
        this.termCounterInOneQuery = termCounterInOneQuery;
    }
}
