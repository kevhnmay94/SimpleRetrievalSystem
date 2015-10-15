package Utils;

import model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by steve on 10/10/2015.
 */
public class TermsWeight {

    /**
     * Calculate raw term frequency in a document with index : indexDocument
     * @param indexDocument
     * @param term
     * @param invertedFile
     * @return termCounter
     */
    private static int rawTermsInOneDocument(int indexDocument, String term, indexTabel invertedFile) {
        int termCounter = 0;
        if (invertedFile.getListTermWeights().containsKey(term)) {
            termWeightingDocument relation = invertedFile.getListTermWeights().get(term);
            for (int i=0; i<relation.getDocumentPerTerm().size(); i++) {
                if (relation.getDocumentPerTerm().get(i).getIndex() == indexDocument) {
                    termCounter = relation.getDocumentCountersPerTerm().get(i);
                }
            }
        }
        return termCounter;
    }

    /**
     * Calculate raw term frequency in a query
     * @param termWeighting
     * @param term
     * @return integer
     */
    private static int rawTermsInOneQuery(termWeightingQuery termWeighting, String term) {
        return termWeighting.getTermCounterInOneQuery().get(term);
    }

    /**
     * Calculate binary term frequency in a document with index : indexDocument
     * @param indexDocument
     * @param term
     * @param invertedFile
     * @return termCounter[0,1]
     */
    private static int binaryTermInOneDocument(int indexDocument, String term, indexTabel invertedFile) {
        int termCounter = 0;
        if (invertedFile.getListTermWeights().containsKey(term)) {
            termWeightingDocument relation = invertedFile.getListTermWeights().get(term);
            for (int i=0; i<relation.getDocumentPerTerm().size(); i++) {
                if (relation.getDocumentPerTerm().get(i).getIndex() == indexDocument) {
                    termCounter = 1;
                }
            }
        }
        return termCounter;
    }

    /**
     * Calculate binary term frequency in one query with counterTermInQuery
     * @param termWeighting
     * @param term
     * @return binaryCounter
     */
    private static int binaryTermInOneQuery(termWeightingQuery termWeighting, String term) {
        int termCounter = 0;
        for(Map.Entry m : termWeighting.getTermCounterInOneQuery().entrySet()) {
            if (((String) m.getKey()).equals(term)) {
                termCounter = 1;
            }
        }
        return termCounter;
    }

    /**
     * Count max term in one document with index : indexDocument
     * @param indexDocument
     * @param invertedFile
     * @return maxTerm
     */
    private static int maxTermInOneDocument(int indexDocument, indexTabel invertedFile) {
        int maxTerm = 0;
        for(Map.Entry m: invertedFile.getListTermWeights().entrySet()) {
            int numberDocument = numDocumentsContainTerm(m.getKey().toString(),invertedFile);
            for (int i=0; i<numberDocument; i++) {
                if (((termWeightingDocument) m.getValue()).getDocumentPerTerm().get(i).getIndex() == indexDocument) {
                    if (((termWeightingDocument) m.getValue()).getDocumentCountersPerTerm().get(i) > maxTerm) {
                        maxTerm = ((termWeightingDocument) m.getValue()).getDocumentCountersPerTerm().get(i);
                    }
                }
            }
        }
        return maxTerm;
    }

    /**
     * Count max term in one query using counter term in that query
     * @param termWeighting
     * @return maxTerm
     */
    private static int maxTermInOneQuery(termWeightingQuery termWeighting) {
        int maxTerm = 0;
        for(Map.Entry m : termWeighting.getTermCounterInOneQuery().entrySet()) {
            if (((Integer) m.getValue()) > maxTerm) {
                maxTerm = (Integer) m.getValue();
            }
        }
        return maxTerm;
    }

    /**
     * Count number of documents contain that term
     * @param term
     * @param invertedFile
     * @return numDocuments
     */
    private static int numDocumentsContainTerm(String term, indexTabel invertedFile) {
        int numDocuments = 0;
        if (invertedFile.getListTermWeights().containsKey(term)) {
            termWeightingDocument relation = invertedFile.getListTermWeights().get(term);
            numDocuments = relation.getDocumentPerTerm().size();
        }
        return numDocuments;
    }

    /**
     * Count total number of documents from external source
     * @return number of documents processed
     */
    private static int numDocumentsTotal() {
        PreprocessWords processingWord = new PreprocessWords();
        processingWord.loadDocumentsFinal();
        return processingWord.getListDocumentsFinal().size();
    }


    /**
     * Update inverted file (weight per term per document) based on Term Frequency
     * @param termFrequencyCode (0-4)
     * @param invertedFile
     */
    public static void termFrequencyWeighting(int termFrequencyCode, indexTabel invertedFile) {
        indexTabel copyInvertedFile = invertedFile;
        for(Map.Entry m: invertedFile.getListTermWeights().entrySet()) {
            int numberDocument = numDocumentsContainTerm(m.getKey().toString(),invertedFile);
            for (int i=0; i<numberDocument; i++) {
                String keyTerm = m.getKey().toString();
                int indexDocument = ((termWeightingDocument) m.getValue()).getDocumentPerTerm().get(i).getIndex();
                int termCountInDocument = rawTermsInOneDocument(indexDocument, keyTerm, copyInvertedFile);
                int binaryCountInDocument = binaryTermInOneDocument(indexDocument, keyTerm, copyInvertedFile);
                int maxTermInDocument = maxTermInOneDocument(indexDocument, copyInvertedFile);
                double oldWeight = ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().get(i);
                switch (termFrequencyCode)  {
                    case 0 :        // No TF
                        oldWeight *= 1; break;
                    case 1 :        // Raw TF
                        oldWeight *= termCountInDocument; break;
                    case 2 :        // Logarithmic TF
                        oldWeight *= 1 + Math.log10(termCountInDocument); break;
                    case 3 :        // Binary TF
                        oldWeight *= binaryCountInDocument; break;
                    case 4 :        // Augmented TF
                        oldWeight *= 0.5 + (0.5 * (((double) termCountInDocument) / ((double) maxTermInDocument))); break;
                    default :       // No valid option
                        oldWeight *= 1; break;
                }
                ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().set(i, oldWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on Term Frequency
     * @param termFrequencyCode
     * @param invertedFileQuery
     */
    public static void termFrequencyWeightingQuery(int termFrequencyCode, indexTabelQuery invertedFileQuery) {
        for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
            for (Map.Entry m : invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().entrySet()) {
                int rawTerm = rawTermsInOneQuery(invertedFileQuery.getListQueryWeighting().get(i),(String) m.getKey());
                int maxTerm = maxTermInOneQuery(invertedFileQuery.getListQueryWeighting().get(i));
                int binaryTerm = binaryTermInOneQuery(invertedFileQuery.getListQueryWeighting().get(i), (String) m.getKey());
                double weightTerm = invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().get(m.getKey());
                switch (termFrequencyCode) {
                    case 0:        // No TF
                        weightTerm *= 1.0; break;
                    case 1:        // Raw TF
                        weightTerm *= (double) rawTerm; break;
                    case 2:        // Logarithmic TF
                        weightTerm *= 1 + Math.log10((double) rawTerm); break;
                    case 3:        // Binary TF
                        weightTerm *= (double) binaryTerm; break;
                    case 4:        // Augmented TF
                        weightTerm *= 0.5 + 0.5 * ((double) rawTerm / (double) maxTerm); break;
                    default:        // No option valid
                        weightTerm *= 1.0; break;
                }
                invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().replace((String) m.getKey(),weightTerm);
            }
        }
    }

    /**
     * Update inverted file (weight per term per document) based on Inverse Document
     * @param inverseDocumentCode
     * @param invertedFile
     */
    public static void inverseDocumentWeighting(int inverseDocumentCode, indexTabel invertedFile) {
        int numDocumentTotal = numDocumentsTotal();
        for(Map.Entry m: invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = m.getKey().toString();
            int numDocumentContainTerm = numDocumentsContainTerm(keyTerm, invertedFile);
            for (int i=0; i<numDocumentContainTerm; i++) {
                double oldWeight = ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().get(i);
                switch (inverseDocumentCode)  {
                    case 0 :        // No IDS
                        oldWeight *= 1; break;
                    case 1 :        // With IDS
                        oldWeight *= Math.log10(((double) numDocumentTotal) / ((double) numDocumentContainTerm)); break;
                    default :       // No valid options
                        oldWeight *= 1; break;
                }
                ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().set(i, oldWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on inverse document
     * Assumption : inverse document value in invertedFileDocument equals with value in invertedFileQuery
     * @param inverseDocumentCode
     * @param invertedFileQuery
     * @param inverseDocumentValue
     */
    public static void inverseDocumentWeightingQuery(int inverseDocumentCode, indexTabelQuery invertedFileQuery, double inverseDocumentValue) {
        for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
            for (Map.Entry m : invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().entrySet()) {
                double weightTerm = invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().get(m.getKey());
                switch (inverseDocumentCode) {
                    case 0:        // No IDS
                        weightTerm *= 1.0; break;
                    case 1:        // With IDS
                        weightTerm *= inverseDocumentValue; break;
                    default:        // No option valid
                        weightTerm *= 1.0; break;
                }
                invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().replace((String) m.getKey(),weightTerm);
            }
        }
    }

    public static void main(String[] arg) {
        /* PreprocessWords.loadIndexTabel(true);
        TermsWeight.termFrequencyWeighting(2, PreprocessWords.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(1, PreprocessWords.getInvertedFile());
        for(Map.Entry m : PreprocessWords.getInvertedFile().getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString());
            for (document Document:((termWeightingDocument) m.getValue()).getDocumentPerTerm()) {
                System.out.println("Id : " + Document.getIndex());
                System.out.println("Judul : " + Document.getJudul());
                // System.out.println("Author : " + Document.getAuthor());
                // System.out.println("Konten : " + Document.getKonten());
            }
            System.out.println("BOBOT TERM INI PER DOKUMEN DI ATAS : ");
            for (double weights :((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm()) {
                System.out.println("Bobot : " + weights);
            }
            System.out.println("COUNTER TERM INI PER DOKUMEN DI ATAS : ");
            for (int counter:((termWeightingDocument) m.getValue()).getDocumentCountersPerTerm()) {
                System.out.println("Counter : " + counter);
            }
            System.out.println("====================================================================================");
        } */

        PreprocessWords processingWord = new PreprocessWords();
        processingWord.loadIndexTabelForQueries(true);
        TermsWeight.termFrequencyWeightingQuery(2, processingWord.getInvertedFileQuery());
        for (int i=0; i<processingWord.getInvertedFileQuery().getListQueryWeighting().size(); i++) {
            termWeightingQuery relation = processingWord.getInvertedFileQuery().getListQueryWeighting().get(i);
            System.out.println("QUERY DIPROSES : " + relation.getCurrentQuery().getQueryContent());
            System.out.println("COUNTER PER TERM DARI QUERY DI ATAS : ");
            for (Map.Entry m : relation.getTermCounterInOneQuery().entrySet()) {
                System.out.println("Term : " + (String) m.getKey());
                System.out.println("Counter : " + (Integer) m.getValue());
            }
            for (Map.Entry m : relation.getTermWeightInOneQuery().entrySet()) {
                System.out.println("Term : " + (String) m.getKey());
                System.out.println("Weight : " + (Double) m.getValue());
            }
            System.out.println("BANYAK STOPWORDS : " + processingWord.getListStopWordsFinal().size());
            System.out.println("====================================================================================");
        }
    }
}
