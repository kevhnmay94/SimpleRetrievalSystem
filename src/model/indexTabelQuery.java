package model;

import Utils.PreprocessWords;
import Utils.StemmingPorter;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by steve on 14/10/2015.
 */
public class indexTabelQuery {
    private ArrayList<termWeightingQuery> listQueryWeighting = new ArrayList<termWeightingQuery>();

    public ArrayList<termWeightingQuery> getListQueryWeighting() {
        return listQueryWeighting;
    }

    public void setListQueryWeighting(ArrayList<termWeightingQuery> listQueryWeighting) {
        this.listQueryWeighting = listQueryWeighting;
    }
}
