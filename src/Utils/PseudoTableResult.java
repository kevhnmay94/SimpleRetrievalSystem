package Utils;

/**
 * Created by User on 25/11/2015.
 */

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Created by User on 25/11/2015.
 */
public class PseudoTableResult {
    public SimpleIntegerProperty rank;
    public SimpleIntegerProperty docNo;
    public SimpleDoubleProperty similiarity;

    public PseudoTableResult(int rank, int docNo, double similiarity) {
        this.rank = new SimpleIntegerProperty(rank);
        this.docNo = new SimpleIntegerProperty(docNo);
        this.similiarity = new SimpleDoubleProperty(similiarity);
    }

    public int getRank() {
        return rank.get();
    }

    public SimpleIntegerProperty rankProperty() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }

    public int getDocNo() {
        return docNo.get();
    }

    public SimpleIntegerProperty docNoProperty() {
        return docNo;
    }

    public void setDocNo(int docNo) {
        this.docNo.set(docNo);
    }

    public double getSimiliarity() {
        return similiarity.get();
    }

    public SimpleDoubleProperty similiarityProperty() {
        return similiarity;
    }

    public void setSimiliarity(double similiarity) {
        this.similiarity.set(similiarity);
    }
}
