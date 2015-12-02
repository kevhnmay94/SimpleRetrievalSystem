package Utils;

import model.*;

import java.util.*;

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
        counterWeightPair relation = invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(indexDocument);
        if (relation != null) {
            termCounter = relation.getCounter();
        }
        return termCounter;
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
        counterWeightPair relation = invertedFile.getListTermWeights().get(term).getDocumentWeightCounterInOneTerm().get(indexDocument);
        if (relation != null) {
            termCounter = 1;
        }
        return termCounter;
    }

    /**
     * Count max term in one document with index : indexDocument
     * @param indexDocument
     * @param invertedFile
     * @return maxTerm
     */
    private static int maxTermInOneDocument(int indexDocument, indexTabel invertedFile, normalTabel normalFile) {
        int maxTerm = 0;
        try {
            HashSet<String> listTermsInDocument = normalFile.getNormalFile().get(indexDocument);
            Iterator listTerms = listTermsInDocument.iterator();
            while (listTerms.hasNext()) {
                String term = (String) listTerms.next();
                termWeightingDocument relation = invertedFile.getListTermWeights().get(term);
                counterWeightPair counter = relation.getDocumentWeightCounterInOneTerm().get(indexDocument);
                if (counter != null) {
                    if (counter.getCounter() > maxTerm) {
                        maxTerm = counter.getCounter();
                    }
                }
            }
        } catch (Exception e) {

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
    private static int numDocumentsTotal(normalTabel normalFile) {
        return normalFile.getNormalFile().size();
    }


    /**
     * Update inverted file (weight per term per document) based on Term Frequency
     * @param termFrequencyCode (0-4)
     * @param invertedFile
     * @param normalFile
     */
    public static void termFrequencyWeighting(int termFrequencyCode, indexTabel invertedFile, normalTabel normalFile) {
        indexTabel copyInvertedFile = invertedFile;
        for(Map.Entry m: invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            for (Map.Entry n : invertedFile.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int indexDocument = (Integer) n.getKey();
                int termCountInDocument = rawTermsInOneDocument(indexDocument,keyTerm,copyInvertedFile);
                int binaryCountInDocument = binaryTermInOneDocument(indexDocument,keyTerm,copyInvertedFile);
                int maxTermInDocument = maxTermInOneDocument(indexDocument,copyInvertedFile,normalFile);
                double newWeight;
                switch (termFrequencyCode)  {
                    case 0 :        // No TF
                        newWeight = 1; break;
                    case 1 :        // Raw TF
                        newWeight = termCountInDocument; break;
                    case 2 :        // Logarithmic TF
                        newWeight = 1 + Math.log10(termCountInDocument); break;
                    case 3 :        // Binary TF
                        newWeight = binaryCountInDocument; break;
                    case 4 :        // Augmented TF
                        newWeight = 0.5 + (0.5 * (((double) termCountInDocument) / ((double) maxTermInDocument))); break;
                    default :       // No valid option
                        newWeight = 1.0; break;
                }
                ((counterWeightPair) n.getValue()).setWeight(newWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on Term Frequency
     * @param termFrequencyCode
     * @param invertedFileQuery
     * @param normalFile
     */
    public static void termFrequencyWeightingQuery(int termFrequencyCode, indexTabel invertedFileQuery, normalTabel normalFile) {
        termFrequencyWeighting(termFrequencyCode,invertedFileQuery,normalFile);
    }

    /**
     * Update inverted file (weight per term per document) based on Inverse Document
     * @param inverseDocumentCode
     * @param invertedFile
     * @param normalFile
     */
    public static void inverseDocumentWeighting(int inverseDocumentCode, indexTabel invertedFile, normalTabel normalFile) {
        int numDocumentTotal = numDocumentsTotal(normalFile);
        for(Map.Entry m: invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = m.getKey().toString();
            for(Map.Entry n : invertedFile.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int numDocumentContainTerm = numDocumentsContainTerm(keyTerm, invertedFile);
                double newWeight;
                switch (inverseDocumentCode)  {
                    case 0 :        // No IDS
                        newWeight = 1; break;
                    case 1 :        // With IDS
                        if (numDocumentContainTerm != 0) {
                            newWeight = Math.log10(((double) numDocumentTotal) / ((double) numDocumentContainTerm));
                        } else {
                            newWeight = 0.0;
                        }
                        break;
                    default :       // No valid options
                        newWeight = 1.0; break;
                }
                ((counterWeightPair) n.getValue()).setWeight(newWeight);
            }
        }
    }

    /**
     * Update inverted file for list of queries based on inverse document
     * Assumption : inverse document value in invertedFileDocument equals with value in invertedFileQuery
     * @param inverseDocumentCode
     * @param invertedFileQuery
     * @param invertedFile
     * @param normalFile
     */
    public static void inverseDocumentWeightingQuery(int inverseDocumentCode, indexTabel invertedFileQuery, indexTabel invertedFile, normalTabel normalFile) {
        int numDocumentTotal = numDocumentsTotal(normalFile);
        indexTabel copyInvertedFile = invertedFile;
        indexTabel copyInvertedFileQuery = invertedFileQuery;
        for(Map.Entry m: copyInvertedFileQuery.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            for (Map.Entry n : copyInvertedFileQuery.getListTermWeights().get(keyTerm).getDocumentWeightCounterInOneTerm().entrySet()) {
                int numDocumentContainTerm = numDocumentsContainTerm((String) m.getKey(),copyInvertedFile);
                double newWeight;
                switch (inverseDocumentCode) {
                    case 0:        // No IDS
                        newWeight = 1.0; break;
                    case 1:        // With IDS
                        if (numDocumentContainTerm != 0) {
                            newWeight = Math.log10(((double) numDocumentTotal) / ((double) numDocumentContainTerm));
                        } else {
                            newWeight = 0.0;
                        }
                        break;
                    default:        // No option valid
                        newWeight = 1.0; break;
                }
                ((counterWeightPair) n.getValue()).setWeight(newWeight);
            }
        }
    }

    public static void main(String[] arg) {
      //  PreprocessWords processingWord = new PreprocessWords();
      //  processingWord.loadIndexTabel(true);

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

      /* processingWord.loadIndexTabelForQueries(true);
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
        } */

       /* ArrayList<Integer> listInteger = new ArrayList<>();
        listInteger.add(66);
        listInteger.add(67);
        listInteger.add(68);
        listInteger.add(69);
        listInteger.add(70);
        Iterator iterator1 = listInteger.iterator();
        while (iterator1.hasNext()) {
            System.out.println((Integer) iterator1.next());
        }
        System.out.println("bobo");
        // Remove
        ArrayList<Integer> listInteger2 = listInteger;
        listInteger2.remove(0);
        listInteger2.remove(2);
        Iterator iterator2 = listInteger2.iterator();
        while (iterator2.hasNext()) {
            System.out.println((Integer) iterator2.next());
        } */
        List<Double> doubleList = new ArrayList<>();
        doubleList.add(23.0);
        doubleList.add(12.0);
        doubleList.add(15.0);
        Collections.sort(doubleList);
        for (Double b : doubleList) {
            System.out.println(b);
        }
    }
}
