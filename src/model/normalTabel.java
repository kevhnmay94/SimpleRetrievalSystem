package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by steve on 11/11/2015.
 */
public class normalTabel {
    private ConcurrentHashMap<Integer,HashSet<String>> normalFile;

    public normalTabel() {
        normalFile = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<Integer,HashSet<String>> getNormalFile() {
        return normalFile;
    }

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
