package Analysis;

import java.io.*;
import java.util.ArrayList;

public class AnalysisClass {

    private static ArrayList<QuestionRecord> questionRecords = new ArrayList<>();
    private static ArrayList<String[]> docs = new ArrayList<>();
    //private static ArrayList<QuestionRecord> questionRecordsTest = new ArrayList<>();
    static double[] similarityTracker = null;

    public void readCsvFile(String filepath /*, ArrayList<QuestionRecord> Record */) {
        BufferedReader br = null;
        FileReader fr = null;
        File file = new File(filepath);
        System.out.println("Path: " + file.getAbsolutePath());
        String line = "";

        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            //line = br.readLine();
            while ((line = br.readLine()) != null) {
                // TODO: do not Skip the questions that have in between commas.
                String[] record = line.replaceAll("\"", "").split("\\s*,\\s*");
                if (record.length == 5) {
                    questionRecords.add(new QuestionRecord(line));
                }
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
            try {
                br.close();
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
        //return result/doc.length;// tf
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
        //System.out.println(out);

        //Extract question1 and 2 for the entire file
        int k = 0;

        similarityTracker = new double[questionRecords.size()];
        while (k < questionRecords.size()) {
            String question1 = questionRecords.get(k).getQ1();
            String question2 = questionRecords.get(k).getQ2();

            System.out.println(question1);
            System.out.println(question2);

            // TODO: better csv reader
            // Tokenize question1 string
            String[] token1 = question1.split(" ");

            // Tokenize question2 string
            String[] token2 = question2.split(" ");

            // create a list of two string[] each represents splited q1 and q2 respectively
            // TODO: Union of two strings
            docs.add(token1);
            docs.add(token2);

            // TODO: List of stop words???

            int maxTotalWords = Math.max(token1.length, token2.length);

            // Calculate term frequencies of tokens in question1
            double[] tfCollection1 = new double[maxTotalWords];

            for (int i = 0; i < token1.length; i++) {
                tfCollection1[i] = ac.calculateTf(token1, token1[i]);
            }
            // Calculate term frequencies of tokens in question2
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
        //private boolean isDuplicate = false;

        public QuestionRecord(String line) {
            String[] record = line.replaceAll("\"", "").split("\\s*,\\s*");
            id = record[0];
            qid1 = record[1];
            qid2 = record[2];
            q1 = record[3];
            q2 = record[4];
            //isDuplicate = Integer.parseInt(record[5]) == 1;


        }

        //public boolean isDuplicate() {
        //    return isDuplicate;
        //}

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
    }

    public void UseModelToPredict() {
        double goldenSimilarity = 0.8872903633401067;
        //AnalysisClass ac = new AnalysisClass();
        //ac.readCsvFile("test.csv"/*, questionRecordsTest*/);
        int i = 0;
        File fileLinear = null;
        FileWriter writerLinear = null;
        try {
            fileLinear = new File("./output.csv");
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
                    //writerLinear.write(questionRecords.get(i).getQId2() + "," + "0" + "\n");


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //write the number to csv file with the both question id
                try {
                    writerLinear.write(questionRecords.get(i).getId() + "," + "1" + "\n");
                    //writerLinear.write(questionRecords.get(i).getQId2() + "," + "1" + "\n");

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


