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
    private indexTabel invertedFileQueryManual;
    private queryRelevances listQueryRelevancesFinal;
    private ArrayList<query> listQueriesFinal;
    private ArrayList<document> listDocumentsFinal;
    private ArrayList<String> listStopWordsFinal;
    private normalTabel normalFile;
    private normalTabel normalFileQuery;
    private normalTabel normalFileQueryManual;

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
    public indexTabel getInvertedFileQueryManual() {
        return invertedFileQueryManual;
    }

    public normalTabel getNormalFileQueryManual() {
        return normalFileQueryManual;
    }

    // SETTER ATTRIBUTE
    public void setInvertedFileQuery(indexTabel invertedFileQuery) { this.invertedFileQuery = invertedFileQuery; }
    public void setInvertedFile(indexTabel invertedFile) {
        this.invertedFile = invertedFile;
    }
    public void setNormalFile(normalTabel normalFile) {
        this.normalFile = normalFile;
    }
    public void setNormalFileQuery(normalTabel normalFileQuery) {
        this.normalFileQuery = normalFileQuery;
    }
    public void setListDocumentsFinal(ArrayList<document> listDocumentsFinal) {
        this.listDocumentsFinal = listDocumentsFinal;
    }
    public void setListQueryRelevancesFinal(queryRelevances listQueryRelevancesFinal) {
        this.listQueryRelevancesFinal = listQueryRelevancesFinal;
    }

    // CONSTRUCTOR
    public PreprocessWords() {
        invertedFile = new indexTabel();
        invertedFileQuery = new indexTabel();
        invertedFileQueryManual = new indexTabel();
        listQueryRelevancesFinal = new queryRelevances();
        listQueriesFinal = new ArrayList<>();
        listDocumentsFinal = new ArrayList<>();
        listStopWordsFinal = new ArrayList<>();
        normalFile = new normalTabel();
        normalFileQuery = new normalTabel();
        normalFileQueryManual = new normalTabel();
    }

    /**
     * Create new list of documents based on relevance feedback yields list of irrelevants documents
     * @param listDocumentIrrelevant
     * @return
     */
    public ArrayList<document> recreateDocumentList(ArrayList<Integer> listDocumentIrrelevant) {
        ArrayList<document> newListDocuments = new ArrayList<>();
        for (document Document : listDocumentsFinal) {
            if (isDocumentRelevant(Document.getIndex(),listDocumentIrrelevant)) {
                // Tambahkan ke list document baru jika ternyata tidak termasuk dokumen irrelevant
                newListDocuments.add(Document);
            }
        }
        return newListDocuments;
    }

    /**
     * Create new query relevances based on relevance feedback yields list of irrelevant documents
     * @param listDocumentIrrelevant
     * @return
     */
    public queryRelevances recreateQueryRelevances(ArrayList<Integer> listDocumentIrrelevant) {
        queryRelevances newListQueryRelevances = new queryRelevances();
        for (Map.Entry m : listQueryRelevancesFinal.getListQueryRelevances().entrySet()) {
            int indexQuery = (Integer) m.getKey();
            ArrayList<Integer> listDocumentRelevant = (ArrayList) m.getValue();
            for (int indexDocument : listDocumentRelevant) {
                if (isDocumentRelevant(indexDocument,listDocumentIrrelevant)) {
                    // Tambahkan ke query relevance yang baru jika document relevant
                    newListQueryRelevances.insertQueryRelevances(indexQuery,indexDocument);
                }
            }
        }
        return newListQueryRelevances;
    }

    /**
     * Check if document with indexDocument appears in list of irrelevant documents
     * @param indexDocument
     * @param listDocumentIrrelevant
     * @return
     */
    public boolean isDocumentRelevant(int indexDocument, ArrayList<Integer> listDocumentIrrelevant) {
        boolean isRelevant = true;
        Iterator iterator = listDocumentIrrelevant.iterator();
        while (iterator.hasNext()) {
            int indexIrrelevant = (Integer) iterator.next();
            if (indexDocument == indexIrrelevant) {
                isRelevant = false;
            }
        }
        return isRelevant;
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
        loadDocumentsFinal(true,isStemmingApplied);
    }

    /**
     * Create small inverted file for each query in experimental / interactive
     * Assumption : list of queries must have been loaded first
     * @param isStemmingApplied
     */
    public void loadIndexTabelForManualQuery(String query, boolean isStemmingApplied) {
        loadStopWordsFinal();
        StringTokenizer token = new StringTokenizer(query, " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String word = token.nextToken();
            if (!isStopWords(word)) {
                String filteredWord = "";
                if (isStemmingApplied) {
                    filteredWord = StemmingPorter.stripAffixes(word);
                } else {
                    filteredWord = word;
                }
                invertedFileQueryManual.insertRowTable(filteredWord.toLowerCase(),0,1.0);
                normalFileQueryManual.insertElement(0,filteredWord.toLowerCase());
            }
        }
    }

    /**
     * Create inverted file for all queries in experiment
     * @param isStemmingApplied
     */
    public void loadIndexTabelForQueries(boolean isStemmingApplied) {
        loadQueriesFinal(true,isStemmingApplied);
    }

    /**
     * Arrange list of documents before extracting words
     * @param isInvertedFileCreated
     * @param isStemmingApplied
     */
    public void loadDocumentsFinal(boolean isInvertedFileCreated, boolean isStemmingApplied) {
        loadStopWordsFinal();
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("documents"));
        Iterator rawSegmentsDocuments = file.getListPartStringBetweenTokens().iterator();
        int indexDocument = 0;
        String authorDocument = null, topicDocument = null;
        while (rawSegmentsDocuments.hasNext()) {
            String rawSegments = (String) rawSegmentsDocuments.next();
            if (rawSegments.contains(file.INDEX_TOKEN)) {
                indexDocument = Integer.parseInt(rawSegments.replace(file.INDEX_TOKEN,""));
            } else if (rawSegments.contains(file.JUDUL_TOKEN)) {
                topicDocument = rawSegments.replace(file.JUDUL_TOKEN,"");
            } else if (rawSegments.contains(file.AUTHOR_TOKEN)) {
                authorDocument = rawSegments.replace(file.AUTHOR_TOKEN,"");
            } else if (rawSegments.contains(file.KONTEN_TOKEN)) {
                String contentDocument = rawSegments.replace(file.KONTEN_TOKEN,"");
                listDocumentsFinal.add(new document(indexDocument,topicDocument,authorDocument,contentDocument));
                // Add this document into inverted file
                if (isInvertedFileCreated) {       // Membuat inverted file sekaligus
                    StringTokenizer token = new StringTokenizer(contentDocument, " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
                    while (token.hasMoreTokens()) {
                        String currentWord = token.nextToken();
                        if (!isStopWords(currentWord)) {
                            String filteredWord;
                            if (isStemmingApplied) {
                                filteredWord = StemmingPorter.stripAffixes(currentWord);
                            } else {
                                filteredWord = currentWord;
                            }
                            invertedFile.insertRowTable(filteredWord.toLowerCase(), indexDocument, 0.0);
                            normalFile.insertElement(indexDocument, filteredWord.toLowerCase());
                        }
                    }
                }
            }
        }
    }

    /**
     * Load list of queries from eksternal source
     */
    public void loadQueriesFinal(boolean isInvertedFileCreated, boolean isStemmingApplied) {
        loadStopWordsFinal();
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("queries"));
        Iterator partString = file.getListPartStringBetweenTokens().iterator();
        int indexQuery = 0;
        while (partString.hasNext()) {
            String part = (String) partString.next();
            if (part.contains(file.INDEX_TOKEN)) {
                indexQuery = Integer.parseInt(part.replace(file.INDEX_TOKEN, ""));
            } else if (part.contains(file.KONTEN_TOKEN)) {
                String contentQuery = part.replace(file.KONTEN_TOKEN,"");
                listQueriesFinal.add(new query(indexQuery,contentQuery));
                // Add this query into inverted file query
                if (isInvertedFileCreated) {       // Membuat inverted file sekaligus
                    StringTokenizer token = new StringTokenizer(contentQuery, " %&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
                    while (token.hasMoreTokens()) {
                        String word = token.nextToken();
                        if (!isStopWords(word)) {
                            String filteredWord = "";
                            if (isStemmingApplied) {
                                filteredWord = StemmingPorter.stripAffixes(word);
                            } else {
                                filteredWord = word;
                            }
                            invertedFileQuery.insertRowTable(filteredWord.toLowerCase(), indexQuery, 1.0);
                            normalFileQuery.insertElement(indexQuery, filteredWord.toLowerCase());
                        }
                    }
                }
            }
        }
    }

    /**
     * Load list of query relevances in documents from external source
     */
    public void loadQueryRelevancesFinal() {
        EksternalFile file = new EksternalFile();
        file.loadQueryRelevances(file.readDocuments("qrels").toString());
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
        String rawFileContent = file.readDocuments("stopwords").toString();
        String[] lines = rawFileContent.split("\\r?\\n");
        for (String line : lines) {
            listStopWordsFinal.add(line);
        }
    }

    public static void main(String[] arg) {
        PreprocessWords word = new PreprocessWords();
        // CEK LOAD DOKUMEN DARI FILE ADI.ALL ATAU CISI.ALL
        word.loadDocumentsFinal(false,false);
        Iterator listDocuments = word.getListDocumentsFinal().iterator();
        while (listDocuments.hasNext()) {
            document Document = (document) listDocuments.next();
            System.out.println("No : " + Document.getIndex());
            System.out.println("Judul : " + Document.getJudul());
            System.out.println("Author : " + Document.getAuthor());
            System.out.println("Konten : " + Document.getKonten());
            System.out.println("================================================================================");
        }
        System.out.println("TOTAL DOKUMEN : " + word.getListDocumentsFinal().size());

        // CEK LOAD QUERY RELEVANCE DARI FILE QUERYRELEVANCE.TEXT
      /*  PreprocessWords.loadQueryRelevancesFinal();
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
       /* word.loadQueriesFinal(true);
        Iterator listQueries = word.getListQueriesFinal().iterator();
        while (listQueries.hasNext()) {
            query Query = (query) listQueries.next();
            System.out.println("Isi Query : " + Query.getQueryContent());
            System.out.println("====================================================================================");
        } */

    }
}
