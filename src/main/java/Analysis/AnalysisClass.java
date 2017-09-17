package Analysis;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;

public class AnalysisClass {

    private static ArrayList<QuestionRecord> questionRecords = new ArrayList<>();
    private static ArrayList<String[]> docs = new ArrayList<>();
    static double[] similarityTracker = null;

    public void readCsvFile(String filepath /*, ArrayList<QuestionRecord> Record */) {
        CSVReader reader;
        FileReader fr = null;

        try {
            fr = new FileReader(filepath);
            reader = new CSVReader(fr);
            String[] record;

            while ((record = reader.readNext()) != null) {
                QuestionRecord qr = new QuestionRecord();
                qr.setId(record[0]);
                qr.setQId1(record[1]);
                qr.setQId2(record[2]);
                qr.setQ1(record[3]);
                qr.setQ2(record[4]);
                questionRecords.add(qr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
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
        return result;
    }

    public double calculateIdf(ArrayList<String[]> docs, String term) {
        double result = 0;
        for (String[] doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word))
                    result++;
                break;
            }
        }

        return Math.log((docs.size() / result));
    }


    public static void main(String[] args) {
        AnalysisClass ac = new AnalysisClass();

        ac.readCsvFile("test.csv"/*, questionRecords*/);
        int k = 0;

        similarityTracker = new double[questionRecords.size()];
        while (k < questionRecords.size()) {
            String question1 = questionRecords.get(k).getQ1();
            String question2 = questionRecords.get(k).getQ2();

            System.out.println(question1);
            System.out.println(question2);

            String[] token1 = question1.split(" ");

            String[] token2 = question2.split(" ");

            docs.add(token1);
            docs.add(token2);

            int maxTotalWords = Math.max(token1.length, token2.length);

            double[] tfCollection1 = new double[maxTotalWords];

            for (int i = 0; i < token1.length; i++) {
                tfCollection1[i] = ac.calculateTf(token1, token1[i]);
            }
            double[] tfCollection2 = new double[maxTotalWords];

            for (int i = 0; i < token2.length; i++) {
                tfCollection2[i] = ac.calculateTf(token2, token2[i]);

            }

            // a*b
            double ab = tfCollection1[0] * tfCollection2[0];
            double sumA = Math.pow(tfCollection1[0], 2);
            double sumB = Math.pow(tfCollection2[0], 2);

            for (int i = 1; i < maxTotalWords - 1; i++) {
                ab += tfCollection1[i] * tfCollection2[i];
                sumA += Math.pow(tfCollection1[i], 2);
                sumB += Math.pow(tfCollection2[i], 2);
            }

            double magA = Math.sqrt(sumA);
            double magB = Math.sqrt(sumB);
            double similarity = (ab / (magA * magB));

            System.out.println("Similarity = " + similarity);

            similarityTracker[k] = similarity;
            k++;
        }

        // compute the average of the similarity to use on our test
        double sumSimilarity = 0.0;
        for (int i = 0; i < similarityTracker.length; i++) {
            sumSimilarity += similarityTracker[i];
        }
        double averageSimilarity = sumSimilarity / similarityTracker.length;
        System.out.println("Average Similarity = " + averageSimilarity);

        ac.UseModelToPredict();
    }

    class QuestionRecord {
        private String id = null;
        private String qid1 = null;
        private String qid2 = null;
        private String q1 = null;
        private String q2 = null;


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

//        public void setIsDuplicate(String isDuplicate) {
//            this.isDuplicate = isDuplicate;
//        }
    }

    public void UseModelToPredict() {
        double goldenSimilarity = 0.8715326395155435;
        int i = 0;
        File fileLinear = null;
        FileWriter writerLinear = null;
        try {
            fileLinear = new File("./output2.csv");
            writerLinear = new FileWriter(fileLinear);
            writerLinear.write("id,is_duplicate\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(similarityTracker.length);
        while (i < similarityTracker.length) {

            if (similarityTracker[i] < goldenSimilarity) {

                // write the number to csv file with that question id
                try {
                    writerLinear.write(questionRecords.get(i).getId() + "," + "0" + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //write the number to csv file with the both question id
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
