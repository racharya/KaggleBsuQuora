package Analysis;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class AnalysisClass {

    private static ArrayList<QuestionRecord> questionRecords = new ArrayList<>();
    private static ArrayList<String[]> docs = new ArrayList<>();

    public String readCsvFile(String filepath){

        File file = new File(filepath);
        System.out.println("Path: " + file.getAbsolutePath());
        String line= "";
        int lineNumber = -1;

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            line = br.readLine();
            //while( line != null) {
                questionRecords.add(new QuestionRecord(line));
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public double calculateTf(String[] doc, String term){
        double result = 0;

        for(String word: doc){
            if(term.equalsIgnoreCase(word)){
                result++;
            }
        }
        //return result/doc.length;// tf
        return result;
    }

    public double calculateIdf(ArrayList<String[]> docs, String term){
        double result = 0;
        for(String[] doc: docs){
            for(String word: doc){
                if(term.equalsIgnoreCase(word))
                    result++;
                    break;
                }
            }

        return Math.log((docs.size()/result));
    }


    public static void main(String[] args){
        AnalysisClass ac = new AnalysisClass();
        String out = ac.readCsvFile("hello.csv");
        System.out.println(out);

        //Extract question1 and 2
        String question1 = questionRecords.get(0).getQ1();
        String question2 = questionRecords.get(0).getQ2();

        System.out.println(question1);
        System.out.println(question2);

        // Tokenize question1 string
        StringTokenizer st1 = new StringTokenizer(question1);
        String[] token1 = question1.split(" ");

        StringTokenizer st2 = new StringTokenizer(question2);
        String[] token2 = question2.split(" ");

        // create a list of two string[] each represents q1 and q2 respectively
        docs.add(token1);
        docs.add(token2);
        // List of stop words???

        int maxTotalWords = Math.max(token1.length,token2.length);

        // Calculate term frequencies of tokens in question1
        double[] tfCollection1 = new double[maxTotalWords];

        for(int i = 0; i < token1.length; i++){
            tfCollection1[i]= ac.calculateTf(token1, token1[i]);


        }
        // Calculate term frequencies of tokens in question2
        double[] tfCollection2 = new double[maxTotalWords];

        for(int i = 0; i < token2.length; i++){
            tfCollection2[i]= ac.calculateTf(token2, token2[i]);

        }

       // a*b

        double ab = tfCollection1[0] * tfCollection2[0];
        double sumA = Math.pow(tfCollection1[0],2);
        double sumB = Math.pow(tfCollection2[0],2);

        for(int i = 1; i < maxTotalWords - 1; i++) {
            ab += tfCollection1[i] * tfCollection2[i];
            sumA += Math.pow(tfCollection1[i],2);
            sumB += Math.pow(tfCollection2[i],2);
        }

        double magA = Math.sqrt(sumA);
        double magB = Math.sqrt(sumB);

        double similarity = (ab / (magA * magB));

        System.out.println("Similarity = " + similarity);



        // Tokenize question2 string


    }

//    public int getTableSize(){
//        return questionRecords.length;
//    }

// extract q1 and q2

    class QuestionRecord {
        private final int id;
        private final int qid1;
        private final int qid2;
        private final String q1;
        private final String q2;
        private final boolean isDuplicate;

        public QuestionRecord(String line){
            String[] record = line.replaceAll("\"","").split("\\s*,\\s*");
            id = Integer.parseInt(record[0]);
            qid1 = Integer.parseInt(record[1]);
            qid2 = Integer.parseInt(record[2]);
            q1 = record[3];
            q2 = record[4];
            isDuplicate = Integer.parseInt(record[5]) == 1;

        }

        public boolean isDuplicate() {
            return isDuplicate;
        }

        public String getQ2() {
            return q2;
        }

        public String getQ1() {
            return q1;
        }

        public int getQId2() {
            return qid2;
        }

        public int getQId1() {
            return qid1;
        }

        public int getId() {
            return id;
        }
    }


}
