package Analysis;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class AnalysisClass {

    private static ArrayList<QuestionRecord> questionRecords = new ArrayList<>();
    static double[] similarityTracker = null;

    public void readCsvFile(String filepath) {
        System.out.println("In ReadCSVFile");
        CSVReader reader = null;
        FileReader fr = null;

        try {
            fr = new FileReader(filepath);
            reader = new CSVReader(fr);
            String[] record = null;
            System.out.println("Populating question records...");
            while ((record = reader.readNext()) != null) {
                //System.out.println("record :" + record[3]);
                QuestionRecord qr = new QuestionRecord();
                qr.setId(record[0]);
                qr.setQId1(record[1]);
                qr.setQId2(record[2]);
                qr.setQ1(record[3]);
                qr.setQ2(record[4]);
                //qr.setIsDup(record[5]);
                questionRecords.add(qr);
            }
            System.out.println("QuestionRecords COMPLETE!!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public double calculateTf(String[] doc, String term) {
        double result = 0;

        for (String word : doc) {
            if (term.equalsIgnoreCase(word)) {
                result++;
            }
        }
        return result / doc.length;
    }

    public double calculateIdf(ArrayList<String[]> docs, String term) {
        double result = 0;

        for (String[] doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    result++;
                    break;
                }
            }
        }

        return Math.log((docs.size() / result));
    }

    // Reference : https://blog.nishtahir.com/2015/09/19/fuzzy-string-matching-using-cosine-similarity/
    public static Map<String, Double> getTermFrequencyMap(String[] terms) {
        Map<String, Double> termFrequencyMap = new HashMap<>();
        for (String term : terms) {
            Double n = termFrequencyMap.get(term);
            n = (n == null) ? 1.0 : ++n;
            termFrequencyMap.put(term, n);
        }
        return termFrequencyMap;
    }


    // Reference : https://blog.nishtahir.com/2015/09/19/fuzzy-string-matching-using-cosine-similarity/
    public static double cosineSimilarity(String text1, String text2) {

        // Get vectors of the questions
        Map<String, Double> a = getTermFrequencyMap(text1.split("\\W+"));
        Map<String, Double> b = getTermFrequencyMap(text2.split("\\W+"));

//        String[] questionsPerRow = new String[2];
//        questionsPerRow[0] = text1;
//        questionsPerRow[1] = text2;


        // get unique words from both questions
        HashSet<String> intersection = new HashSet<>(a.keySet());
        intersection.retainAll(b.keySet());

//        Map<String, Double> c = getInverseDocumentFrequencyMap(questionsPerRow, a);
//        Map<String, Double> d = getInverseDocumentFrequencyMap(questionsPerRow, b);
//
//        Map<String, Double> e = getTfIdfMap(a, c);
//        Map<String, Double> f = getTfIdfMap(b, d);


        double dotProduct = 0, magnitudeA = 0, magnitudeB = 0;

        // Calculate dot product
        for (String item : intersection) {
            dotProduct += a.get(item) * b.get(item);
        }

        // Calculate magnitude of question1
        for (String k : a.keySet()) {
            magnitudeA += Math.pow(a.get(k), 2);
        }

        // calculate magnitude of question2
        for (String k : b.keySet()) {
            magnitudeB += Math.pow(b.get(k), 2);
        }

        return dotProduct / (Math.sqrt(magnitudeA * magnitudeB));
    }

    private static Map<String, Double> getInverseDocumentFrequencyMap(String[] questionsPerRow, Map<String, Double> a) {
        Map<String, Double> idfMap = new HashMap<>();
        double result = 0.0;
        for (String question : questionsPerRow) {
            if (a.containsKey(question)) {
                result++;
                idfMap.put(question, result);
            }
        }
        return idfMap;
    }


    public static void main(String[] args) {
        // test idf for q1


        long startTime = System.currentTimeMillis();
        AnalysisClass ac = new AnalysisClass();
        ac.readCsvFile("train.csv");
        int k = 0;
        System.out.println("Question Record size: " + questionRecords.size());

        similarityTracker = new double[questionRecords.size()];

        while (k < questionRecords.size()) {
            String question1 = questionRecords.get(k).getQ1();
            String question2 = questionRecords.get(k).getQ2();

            similarityTracker[k] = cosineSimilarity(question1, question2);


            if (Double.isNaN(similarityTracker[k])) {
                similarityTracker[k] = 0.0;
            }

            k++;
        }

        // compute the average of the similarity to use on our test
        double sumSimilarity = 0.0;
        for (int i = 0; i < similarityTracker.length; i++) {
            // TODO: Improve on avoiding getting NaN
            if (Double.isNaN(similarityTracker[i])) {
                System.out.println("Index i = " + i);
            }
            sumSimilarity += similarityTracker[i];
        }

        double averageSimilarity = sumSimilarity / similarityTracker.length;

        System.out.println("Average Similarity = " + averageSimilarity);

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elapsed = " + elapsedTime);

        ac.writeToFile(similarityTracker);
        //ac.UseModelToPredict();
    }

    private void writeToFile(double[] similarityTracker) {
        int i = 0;
        File fileLinear = null;
        FileWriter writerLinear = null;
        try {
            fileLinear = new File("./similarity.csv");
            writerLinear = new FileWriter(fileLinear);
            writerLinear.write("similarity\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(similarityTracker.length);
        while (i < similarityTracker.length) {


            // write the number to csv file with that id
            try {
                writerLinear.write(similarityTracker[i] + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }

            i++;
        }
        try {
            writerLinear.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerLinear.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        class QuestionRecord {
            private String id = null;
            private String qid1 = null;
            private String qid2 = null;
            private String q1 = null;
            private String q2 = null;
            private String isDup = null;

            public String getisDup() {
                return isDup;
            }

            public String getQ2() {
                return q2;
            }

            public String getQ1() {
                return q1;
            }

            public String getQId2() {
                return qid2;
            }

            public String getQId1() {
                return qid1;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public void setQId1(String QId1) {
                this.qid1 = QId1;
            }

            public void setQId2(String QId2) {
                this.qid2 = QId2;
            }

            public void setQ1(String q1) {
                this.q1 = q1;
            }

            public void setQ2(String q2) {
                this.q2 = q2;
            }

            public void setIsDup(String isDup) {
                this.isDup = isDup;
            }
        }


    public void UseModelToPredict() {
        // double goldenSimilarity = 0.8715326395155435; // submission1 score: 0.54910
        //double goldenSimilarity = 0.47962165669905826; // submission2 score: 0.65225

        double goldenSimilarity = 0.48012771473507015; // submission3 score: 0.65230
        int i = 0;
        File fileLinear = null;
        FileWriter writerLinear = null;
        try {
            fileLinear = new File("./output3.csv");
            writerLinear = new FileWriter(fileLinear);
            writerLinear.write("id,is_duplicate\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(similarityTracker.length);
        while (i < similarityTracker.length) {

            if (similarityTracker[i] < goldenSimilarity) {

                // write the number to csv file with that id
                try {
                    writerLinear.write(questionRecords.get(i).getId() + "," + "0" + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    writerLinear.write(questionRecords.get(i).getId() + "," + "1" + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
        try {
            writerLinear.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerLinear.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
