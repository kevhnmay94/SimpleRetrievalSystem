package Utils;

import javafx.beans.property.*;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * Created by User on 25/11/2015.
 */
public class TableResult {
    public SimpleIntegerProperty rank;
    public SimpleIntegerProperty docNo;
    public SimpleDoubleProperty similiarity;
    public SimpleBooleanProperty relevant;

    public TableResult(int rank, int docNo, double similiarity) {
        this.rank = new SimpleIntegerProperty(rank);
        this.docNo = new SimpleIntegerProperty(docNo);
        this.similiarity = new SimpleDoubleProperty(similiarity);
        this.relevant = new SimpleBooleanProperty(false);
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

    public boolean isRelevant() {
        return relevant.get();
    }

    public void setRelevant(boolean relevant) {
        this.relevant.set(relevant);
    }

    public SimpleBooleanProperty relevantProperty() {
        return this.relevant;
    }

    public Boolean getRelevant() {
        return this.relevantProperty().get();
    }

    public void setChecked(final Boolean checked) {
        this.relevantProperty().set(checked);
    }
}
