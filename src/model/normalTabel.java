package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 11/11/2015.
 */
public class normalTabel {
    private boolean isStemmingApplied;
    private ConcurrentHashMap<Integer,HashSet<String>> normalFile;

    // CONSTRUCTOR
    public normalTabel() {
        normalFile = new ConcurrentHashMap<>();
    }

    // GETTER DAN SETTER
    public ConcurrentHashMap<Integer,HashSet<String>> getNormalFile() {
        return normalFile;
    }

    public boolean isStemmingApplied() {
        return isStemmingApplied;
    }

    public void setStemmingApplied(boolean isStemmingApplied) {
        this.isStemmingApplied = isStemmingApplied;
    }

    public void setNormalFile(ConcurrentHashMap<Integer, HashSet<String>> normalFile) {
        this.normalFile = normalFile;
    }

    // ADD NEW ROW NORMAL TABEL
    public void insertElement(int indexDocument, String word) {
        if (normalFile.containsKey(indexDocument)) {
            normalFile.get(indexDocument).add(word.toLowerCase());
        } else {
            HashSet<String> newListWords = new HashSet<>();
            newListWords.add(word.toLowerCase());
            normalFile.put(indexDocument,newListWords);
        }
    }
}
