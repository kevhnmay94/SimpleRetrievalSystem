package Utils;

import model.*;

import javax.print.Doc;
import java.util.*;

/**
 * Created by steve on 08/10/2015.
 */
public class PreprocessWords {
    // ATTRIBUTE
    private indexTabel invertedFile;
    private indexTabel invertedFileQuery;
    private queryRelevances listQueryRelevancesFinal;
    private ArrayList<query> listQueriesFinal;
    private ArrayList<document> listDocumentsFinal;
    private ArrayList<String> listStopWordsFinal;
    private indexTabel invertedFileManualQuery;
    private normalTabel normalFile;
    private normalTabel normalFileQuery;

    // COMPLEMENTER ATTRIBUTE
    private ArrayList<Integer> listIndexDocuments;
    private ArrayList<String> listTopicDocuments;
    private ArrayList<String> listAuthorDocuments;
    private ArrayList<String> listContentDocuments;

    // GETTER ATTRIBUTE

    public normalTabel getNormalFile() {
        return normalFile;
    }
    public normalTabel getNormalFileQuery() {
        return normalFileQuery;
    }
    public indexTabel getInvertedFileQuery() {
        return invertedFileQuery;
    }
    public ArrayList getListDocumentsFinal() {
        return listDocumentsFinal;
    }
    public indexTabel getInvertedFile() {
        return invertedFile;
    }
    public queryRelevances getListQueryRelevancesFinal() {
        return listQueryRelevancesFinal;
    }
    public ArrayList getListQueriesFinal() {
        return listQueriesFinal;
    }
    public ArrayList getListStopWordsFinal() {
        return listStopWordsFinal;
    }
    public indexTabel getInvertedFileManualQuery() {
        return invertedFileManualQuery;
    }

    // SETTER ATTRIBUTE
    public void setInvertedFileQuery(indexTabel invertedFileQuery) { this.invertedFileQuery = invertedFileQuery; }
    public void setInvertedFile(indexTabel invertedFile) {
        this.invertedFile = invertedFile;
    }

    // CONSTRUCTOR
    public PreprocessWords() {
        invertedFile = new indexTabel();
        invertedFileQuery = new indexTabel();
        listQueryRelevancesFinal = new queryRelevances();
        listQueriesFinal = new ArrayList<>();
        listDocumentsFinal = new ArrayList<>();
        listStopWordsFinal = new ArrayList<>();
        listIndexDocuments = new ArrayList<>();
        listTopicDocuments = new ArrayList<>();
        listAuthorDocuments = new ArrayList<>();
        listContentDocuments = new ArrayList<>();
        invertedFileManualQuery = new indexTabel();
        normalFile = new normalTabel();
        normalFileQuery = new normalTabel();
    }

    /**
     * Check if a word is stop word or not
     * @return boolean
     */
    public boolean isStopWords(String word) {
        boolean isStopWords = false;
        Iterator listStopWords = listStopWordsFinal.iterator();
        while (listStopWords.hasNext()) {
            String currentWord = (String) listStopWords.next();
            if (currentWord.equalsIgnoreCase(word.trim())) {
                isStopWords = true; break;
            }
        }
        return isStopWords;
    }

    /**
     * Create inverted file for document
     * Assumption : list of documents must have been loaded first
     * @param isStemmingApplied
     */
    public void loadIndexTabel(boolean isStemmingApplied) {
        loadStopWordsFinal();
        loadDocumentsFinal(isStemmingApplied);
       // loadStopWordsFinal();
      /* Iterator listDocuments = listDocumentsFinal.iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            String preprocessWords = Document.getKonten();
            StringTokenizer token = new StringTokenizer(preprocessWords," +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String currentWord = token.nextToken();
                if (!isStopWords(currentWord)) {
                    String filteredWord;
                    if (isStemmingApplied) {
                        filteredWord = StemmingPorter.stripAffixes(currentWord);
                    } else {
                        filteredWord = currentWord;
                    }
                    invertedFile.insertRowTable(filteredWord,Document.getIndex(),0.0);
                    normalFile.insertElement(Document.getIndex(),filteredWord);
                }
            }
        } */
    }

    /**
     * Create small inverted file for each query in experimental / interactive
     * Assumption : list of queries must have been loaded first
     * @param isStemmingApplied
     */
    public void loadIndexTabelForManualQuery(String query, boolean isStemmingApplied) {
        loadStopWordsFinal();
        StringTokenizer token = new StringTokenizer(query, " +=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String word = token.nextToken();
            if (!isStopWords(word)) {
                String filteredWord = "";
                if (isStemmingApplied) {
                    filteredWord = StemmingPorter.stripAffixes(word);
                } else {
                    filteredWord = word;
                }
                invertedFileQuery.insertRowTable(filteredWord,0,1.0);
            }
        }
    }

    /**
     * Create inverted file for all queries in experiment
     * @param isStemmingApplied
     */
    public void loadIndexTabelForQueries(boolean isStemmingApplied) {
        loadStopWordsFinal();
        loadQueriesFinal(isStemmingApplied);
      /*  Iterator listQueries = listQueriesFinal.iterator();
        while (listQueries.hasNext()) {
            query Query = (query) listQueries.next();
            int indexQuery = Query.getIndex();
            String contentQuery = Query.getQueryContent().replace(EksternalFile.KONTEN_TOKEN,"");
            StringTokenizer token = new StringTokenizer(contentQuery, " +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String word = token.nextToken();
                if (!isStopWords(word)) {
                    String filteredWord = "";
                    if (isStemmingApplied) {
                        filteredWord = StemmingPorter.stripAffixes(word);
                    } else {
                        filteredWord = word;
                    }
                    invertedFileQuery.insertRowTable(filteredWord,indexQuery,1.0);
                    normalFileQuery.insertElement(indexQuery,filteredWord);
                }
            }
        } */
    }

    /**
     * Save documents segments from eksternal database into memory
     */
    private void loadDocumentsPerSegments() {
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("documents"));
        Iterator rawSegmentsDocuments = file.getListPartStringBetweenTokens().iterator();
        while (rawSegmentsDocuments.hasNext()) {
            String rawSegments = (String) rawSegmentsDocuments.next();
            if (rawSegments.contains(file.INDEX_TOKEN)) {
                listIndexDocuments.add(Integer.parseInt(rawSegments.replace(file.INDEX_TOKEN, "")));
            } else if (rawSegments.contains(file.JUDUL_TOKEN)) {
                listTopicDocuments.add(rawSegments.replace(file.JUDUL_TOKEN, ""));
            } else if (rawSegments.contains(file.AUTHOR_TOKEN)) {
                listAuthorDocuments.add(rawSegments.replace(file.AUTHOR_TOKEN, ""));
            } else if (rawSegments.contains(file.KONTEN_TOKEN)) {
                listContentDocuments.add(rawSegments.replace(file.KONTEN_TOKEN, ""));
            }
        }
    }

    /**
     * Arrange list of documents before extracting words
     * @param isStemmingApplied
     */
    public void loadDocumentsFinal(boolean isStemmingApplied) {
        loadDocumentsPerSegments();
        for (int i=0; i<listIndexDocuments.size(); i++) {
            int indexDocument = listIndexDocuments.get(i);
            String contentDocument = listContentDocuments.get(i);
            String authorDocument = listAuthorDocuments.get(i);
            String topicDocument = listTopicDocuments.get(i);
            document Document = new document(indexDocument,topicDocument,authorDocument,contentDocument);
            listDocumentsFinal.add(Document);

            StringTokenizer token = new StringTokenizer(contentDocument," +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String currentWord = token.nextToken();
                if (!isStopWords(currentWord)) {
                    String filteredWord;
                    if (isStemmingApplied) {
                        filteredWord = StemmingPorter.stripAffixes(currentWord);
                    } else {
                        filteredWord = currentWord;
                    }
                    invertedFile.insertRowTable(filteredWord,indexDocument,0.0);
                    normalFile.insertElement(indexDocument,filteredWord);
                }
            }
        }
    }

    /**
     * Load list of queries from eksternal source
     * @param isStemmingApplied
     */
    public void loadQueriesFinal(boolean isStemmingApplied) {
        ArrayList<Integer> listQueryIndexes = new ArrayList<Integer>();
        ArrayList<String> listQueryContents = new ArrayList<String>();
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("queries"));
        Iterator partString = file.getListPartStringBetweenTokens().iterator();
        while (partString.hasNext()) {
            String part = (String) partString.next();
            if (part.contains(file.INDEX_TOKEN)) {
                String index = part.replace(file.INDEX_TOKEN, "");
                listQueryIndexes.add(Integer.parseInt(index));
            } else if (part.contains(file.KONTEN_TOKEN)) {
                listQueryContents.add(part.replace(file.KONTEN_TOKEN,""));
            }
        }
        for (int i=0; i<listQueryIndexes.size(); i++) {
            int indexQuery = listQueryIndexes.get(i);
            String contentQuery = listQueryContents.get(i);
            listQueriesFinal.add(new query(indexQuery,contentQuery));

            StringTokenizer token = new StringTokenizer(contentQuery, " +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String word = token.nextToken();
                if (!isStopWords(word)) {
                    String filteredWord = "";
                    if (isStemmingApplied) {
                        filteredWord = StemmingPorter.stripAffixes(word);
                    } else {
                        filteredWord = word;
                    }
                    invertedFileQuery.insertRowTable(filteredWord,indexQuery,1.0);
                    normalFileQuery.insertElement(indexQuery,filteredWord);
                }
            }
        }
    }

    /**
     * Load list of query relevances in documents from external source
     */
    public void loadQueryRelevancesFinal() {
        EksternalFile file = new EksternalFile();
        file.loadQueryRelevances(file.readDocuments("qrels"));
        ArrayList<Integer> listQueryIndexes = new ArrayList<Integer>();
        ArrayList<Integer> listDocumentIndexes = new ArrayList<Integer>();
        int sequenceCounter = 1;
        Iterator partIndex = file.getListPartIndexInQrels().iterator();
        while (partIndex.hasNext()) {
            int partQrels = (Integer) partIndex.next();
            if (sequenceCounter == 1) {
                listQueryIndexes.add(partQrels);
                sequenceCounter++;
            } else {
                listDocumentIndexes.add(partQrels);
                if (sequenceCounter == 2) {sequenceCounter = 1;}
            }
        }
        for (int i=0; i<listQueryIndexes.size(); i++) {
            int queryIndex = listQueryIndexes.get(i);
            int documentIndex = listDocumentIndexes.get(i);
            listQueryRelevancesFinal.insertQueryRelevances(queryIndex,documentIndex);
        }
    }

    /**
     * Load list of stopwords from external source
     */
    private void loadStopWordsFinal() {
        EksternalFile file = new EksternalFile();
        String rawFileContent = file.readDocuments("stopwords");
        String[] lines = rawFileContent.split("\\r?\\n");
        for (String line : lines) {
            listStopWordsFinal.add(line);
        }
    }

    public static void main(String[] arg) {
       /* // CEK LOAD DOKUMEN DARI FILE ADI.ALL ATAU CISI.ALL
        PreprocessWords.loadDocumentsFinal();
        for (int i=0; i<PreprocessWords.getListDocumentsFinal().size(); i++) {
            System.out.println("No : " + PreprocessWords.getListDocumentsFinal().get(i).getIndex());
            System.out.println("Judul : " + PreprocessWords.getListDocumentsFinal().get(i).getJudul());
            System.out.println("Author : " + PreprocessWords.getListDocumentsFinal().get(i).getAuthor());
            System.out.println("Konten : " + PreprocessWords.getListDocumentsFinal().get(i).getKonten());
            System.out.println("================================================================================");
        }
        System.out.println("TOTAL DOKUMEN : " + PreprocessWords.getListDocumentsFinal().size());

        // CEK LOAD QUERY RELEVANCE DARI FILE QUERYRELEVANCE.TEXT
        PreprocessWords.loadQueryRelevancesFinal();
        for (int i=0; i<PreprocessWords.getListQueryRelevancesFinal().getListQueryRelevances().size(); i++) {
            for (ArrayList<Integer> j : PreprocessWords.getListQueryRelevancesFinal().getListQueryRelevances().values()) {
                System.out.println("Daftar dokumen relevan dengan query X");
                for (int k : j) {
                    System.out.println(k);
                }
                System.out.println("====================================================================================");
            }
        } */

        // CEK LOAD QUERY DARI FILE QUERY.TEXT
        /* PreprocessWords.loadQueriesFinal();
        for (int i=0; i<PreprocessWords.getListQueriesFinal().size(); i++) {
            System.out.println("Isi Query : " + PreprocessWords.getListQueriesFinal().get(i).getQueryContent());
            System.out.println("====================================================================================");
        } */

    }
}
