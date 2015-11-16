package model;

import Utils.PreprocessWords;
import Utils.StemmingPorter;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by steve on 14/10/2015.
 */
public class indexTabelQuery {
    private boolean isStemmingApplied;
    private ArrayList<termWeightingQuery> listQueryWeighting = new ArrayList<termWeightingQuery>();

    // GETTER DAN SETTER
    public ArrayList<termWeightingQuery> getListQueryWeighting() {
        return listQueryWeighting;
    }

    public boolean isStemmingApplied() {
        return isStemmingApplied;
    }

    public void setListQueryWeighting(ArrayList<termWeightingQuery> listQueryWeighting) {
        this.listQueryWeighting = listQueryWeighting;
    }

    public void setStemmingApplied(boolean isStemmingApplied) {
        this.isStemmingApplied = isStemmingApplied;
    }
}
