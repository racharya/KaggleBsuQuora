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
            System.out.println("Populating question records ...");
            int i = 0;
            while ((record = reader.readNext()) != null) {
                //System.out.println("Line number" + i);
                QuestionRecord qr = new QuestionRecord();
                qr.setId(record[0]);
                qr.setQId1(record[1]);
                qr.setQId2(record[2]);
                qr.setQ1(record[3]);
                qr.setQ2(record[4]);
                //qr.setIsDup(record[5]);
                questionRecords.add(qr);
                i++;
            }
            System.out.println("QuestionRecords COMPLETE!!");
            System.out.println("Number of lines read = "+ i);
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

    public static Map<String, Double> getInverseDocFreq(String[] doc1, String[] doc2){
        Map<String, Double> idfMap1 = new HashMap<>();
        Map<String, Double> idfMap2 = new HashMap<>();

        Double n = 0.0;
        for(String terms1 : doc1){
            for(String terms2: doc2){
                if(terms1.equalsIgnoreCase(terms2)){
                    n = idfMap1.get(terms2);
                    n = (n == null) ? 1.0 : ++n;
                    idfMap1.put(terms1, n/2);
                    idfMap2.put(terms2, n/2);
                }
            }
        }

        return idfMap1;
    }


    // Reference : https://blog.nishtahir.com/2015/09/19/fuzzy-string-matching-using-cosine-similarity/
    public static Map<String, Double> getTermFrequencyMap(String[] terms) {
        Map<String, Double> termFrequencyMap = new HashMap<>();
        for (String term : terms) {
            Double n = termFrequencyMap.get(term);
            n = (n == null) ? 1.0 : ++n;
            termFrequencyMap.put(term, n/(terms.length));
        }
        return termFrequencyMap;
    }

    public static Map<String, Double> getIdfMap1(String[] doc1, String[] doc2) {
        Map<String, Double> idfMap1 = new HashMap<>();

        for(String terms1 : doc1){
            for(String terms2 : doc2){
                if(terms1.equalsIgnoreCase(terms2)) {
                    Double n = idfMap1.get(terms1);
                    n = (n == null) ? 1.0 : ++n;
                    double val = (n+1) /2;
                    idfMap1.put(terms1, val);
                }
            }
        }
        return idfMap1;
    }

    public static Map<String, Double> getIdfMap2(String[] doc2, String[] doc1) {
        Map<String, Double> idfMap2 = new HashMap<>();

        for(String terms2 : doc2){
            for(String terms1 : doc1){
                if(terms2.equalsIgnoreCase(terms1)) {
                    Double n = idfMap2.get(terms1);
                    n = (n == null) ? 1.0 : ++n;
                    double val = (n+1) /2;
                    idfMap2.put(terms2, val);
                }
            }
        }
        return idfMap2;
    }

    // Reference : https://blog.nishtahir.com/2015/09/19/fuzzy-string-matching-using-cosine-similarity/
    public static double cosineSimilarity(String text1, String text2) {

        // Get vectors of the questions
        Map<String, Double> a = getTermFrequencyMap(text1.split("\\W+"));
        Map<String, Double> b = getTermFrequencyMap(text2.split("\\W+"));


        // Calculate idf
        Map<String, Double> idfMap1 = getIdfMap1(text1.split("\\W+"), text2.split("\\W+"));
        Map<String, Double> idfMap2 = getIdfMap2(text2.split("\\W+"), text1.split("\\W+"));

        HashSet<String> intersectionIDF = new HashSet<>(idfMap1.keySet());
        intersectionIDF.retainAll(idfMap2.keySet());

        // multiply idfMap1 and a and make 1 matrix
        for(String terms: a.keySet()){
            if(idfMap1.containsKey(terms)) {
                a.put(terms, a.get(terms) * idfMap1.get(terms));
            }
        }

        for(String terms: b.keySet()){
            if(idfMap2.containsKey(terms)) {
                b.put(terms, b.get(terms) * idfMap2.get(terms));
            }
        }

        // get unique words from both questions
        HashSet<String> intersection = new HashSet<>(a.keySet());
        intersection.retainAll(b.keySet());

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


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        AnalysisClass ac = new AnalysisClass();
        ac.readCsvFile("train.csv");
        int k = 0;
        System.out.println("Question Record size: " + questionRecords.size());

        similarityTracker = new double[questionRecords.size()];

        while (k < questionRecords.size()) {
            String question1 = questionRecords.get(k).getQ1();
            String question2 = questionRecords.get(k).getQ2();



            //TODO use tfidf
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

//        double goldenSimilarity = 0.48012771473507015; // submission3 score: 0.65230
        //double goldenSimilarity = 0.4739080133062724; // submission4 score: 0.66155   BEST
        //double goldenSimilarity = 0.4700000000000000; // submission5 score: 0.66072
        double goldenSimilarity = 0.4946917971720503; //submission6 score: 0.64780


        int i = 0;
        File fileLinear = null;
        FileWriter writerLinear = null;
        try {
            fileLinear = new File("./output6.csv");
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
