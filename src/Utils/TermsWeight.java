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
            for (Map.Entry m : relation.getDocumentWeightCounterInOneTerm().entrySet()) {
                if ((Integer) m.getKey() == indexDocument) {
                    termCounter = ((counterWeightPair) m.getValue()).getCounter();
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
            for (Map.Entry m : relation.getDocumentWeightCounterInOneTerm().entrySet()) {
                if ((Integer) m.getKey() == indexDocument) {
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
            // int numberDocument = numDocumentsContainTerm(m.getKey().toString(),invertedFile);
            for (Map.Entry n : ((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                if ((Integer) n.getKey() == indexDocument) {
                    if (((counterWeightPair) n.getValue()).getCounter() > maxTerm) {
                        maxTerm = ((counterWeightPair) n.getValue()).getCounter();
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
            numDocuments = relation.getDocumentWeightCounterInOneTerm().size();
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
            String keyTerm = (String) m.getKey();
            for (Map.Entry n : invertedFile.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int indexDocument = (Integer) n.getKey();
                int termCountInDocument = rawTermsInOneDocument(indexDocument,keyTerm,copyInvertedFile);
                int binaryCountInDocument = binaryTermInOneDocument(indexDocument,keyTerm,copyInvertedFile);
                int maxTermInDocument = maxTermInOneDocument(indexDocument,copyInvertedFile);
                double oldWeight = ((counterWeightPair) n.getValue()).getWeight();
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
                ((counterWeightPair) n.getValue()).setWeight(oldWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on Term Frequency
     * @param termFrequencyCode
     * @param invertedFileQuery
     */
    public static void termFrequencyWeightingQuery(int termFrequencyCode, indexTabel invertedFileQuery) {
        termFrequencyWeighting(termFrequencyCode,invertedFileQuery);
       /* for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
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
        } */
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
            for(Map.Entry n : invertedFile.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int numDocumentContainTerm = numDocumentsContainTerm(keyTerm, invertedFile);
                double oldWeight = ((counterWeightPair) n.getValue()).getWeight();
                switch (inverseDocumentCode)  {
                    case 0 :        // No IDS
                        oldWeight *= 1; break;
                    case 1 :        // With IDS
                        if (numDocumentContainTerm != 0) {
                            oldWeight *= Math.log10(((double) numDocumentTotal) / ((double) numDocumentContainTerm));
                        } else {
                            oldWeight *= 1.0;
                        }
                        break;
                    default :       // No valid options
                        oldWeight *= 1; break;
                }
                ((counterWeightPair) n.getValue()).setWeight(oldWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on inverse document
     * Assumption : inverse document value in invertedFileDocument equals with value in invertedFileQuery
     * @param inverseDocumentCode
     * @param invertedFileQuery
     * @param invertedFile
     */
    public static void inverseDocumentWeightingQuery(int inverseDocumentCode, indexTabel invertedFileQuery, indexTabel invertedFile) {
        int numDocumentTotal = numDocumentsTotal();
        indexTabel copyInvertedFile = invertedFile;
        indexTabel copyInvertedFileQuery = invertedFileQuery;
        for(Map.Entry m: copyInvertedFileQuery.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            for (Map.Entry n : copyInvertedFileQuery.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int numDocumentContainTerm = numDocumentsContainTerm((String) m.getKey(),copyInvertedFile);
                double oldWeight = ((counterWeightPair) n.getValue()).getWeight();
                switch (inverseDocumentCode) {
                    case 0:        // No IDS
                        oldWeight *= 1.0; break;
                    case 1:        // With IDS
                        if (numDocumentContainTerm != 0) {
                            oldWeight *= Math.log10(((double) numDocumentTotal) / ((double) numDocumentContainTerm));
                        } else {
                            oldWeight *= 1.0;
                        }
                        break;
                    default:        // No option valid
                        oldWeight *= 1.0; break;
                }
                ((counterWeightPair) n.getValue()).setWeight(oldWeight);
            }
        }
    }

    public static void main(String[] arg) {
        PreprocessWords processingWord = new PreprocessWords();
        processingWord.loadIndexTabel(true);

        /* TermsWeight.termFrequencyWeighting(2, processingWord.getInvertedFile());
        TermsWeight.inverseDocumentWeighting(1, processingWord.getInvertedFile());
        // Keluarkan isi hashmap
        for(Map.Entry m:processingWord.getInvertedFile().getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString() + "\n");
            for (Map.Entry n:((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                System.out.println("Nomor Dokumen : " + n.getKey());
                System.out.println("Counter term di dokumen ini : " + ((counterWeightPair) n.getValue()).getCounter());
                System.out.println("Bobot term di dokumen ini : " + ((counterWeightPair) n.getValue()).getWeight() + "\n");
            }
            System.out.println("====================================================================================");
        } */

        processingWord.loadIndexTabelForQueries(true);
        TermsWeight.termFrequencyWeightingQuery(2, processingWord.getInvertedFile());
        TermsWeight.inverseDocumentWeightingQuery(1, processingWord.getInvertedFileQuery(), processingWord.getInvertedFile());
        // Keluarkan isi hashmap
        for(Map.Entry m:processingWord.getInvertedFileQuery().getListTermWeights().entrySet()) {
            System.out.println("Key : " + m.getKey().toString() + "\n");
            for (Map.Entry n:((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                System.out.println("Nomor Query : " + n.getKey());
                System.out.println("Counter term di query ini : " + ((counterWeightPair) n.getValue()).getCounter());
                System.out.println("Bobot term di query ini : " + ((counterWeightPair) n.getValue()).getWeight() + "\n");
            }
            System.out.println("====================================================================================");
        }
    }
}
