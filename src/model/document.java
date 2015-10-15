package model;

/**
 * Created by steve on 07/10/2015.
 */
public class document {
    private int index;
    private String judul;
    private String author;
    private String konten;

    public document(int index, String judul, String author, String konten) {
        this.index = index;
        this.judul = judul;
        this.author = author;
        this.konten = konten;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getKonten() {
        return konten;
    }

    public void setKonten(String konten) {
        this.konten = konten;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

