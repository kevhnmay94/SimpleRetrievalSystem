package model;

/**
 * Created by steve on 10/10/2015.
 */
public class query {
    private int index;
    private String queryContent;

    public query(int index, String queryContent) {
        this.index = index;
        this.queryContent = queryContent;
    }

    public int getIndex() {
        return index;
    }

    public String getQueryContent() {
        return queryContent;
    }

    public void setQueryContent(String queryContent) {
        this.queryContent = queryContent;
    }
}
