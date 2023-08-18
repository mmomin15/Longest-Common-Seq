package com.mycompany.app;

import java.util.Random;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;

public class App {

    // class to hold one sequence and it's name after loading from file
    public class DnaSeq {
        public String name;
        public String seq;

        public DnaSeq(String name, String seq){
            this.name = name;
            this.seq = seq;
        }
    }

    // computed result of longest common subsequence, result from main algorithm for printing
    public class LcsResult {
        public String X;
        public String Y;
        public Character[][] b;
        public int[][] c;
        public int countOperations;
        long executionTimeNanoseconds;

        public LcsResult(String X, String Y, Character[][] b, int[][] c, int countOperations, long executionTimeNanoseconds) {
            this.X = X;
            this.Y = Y;
            this.b = b;
            this.c = c;
            this.countOperations = countOperations;
            this.executionTimeNanoseconds = executionTimeNanoseconds;
        }
    }

    // entrypoint for application, loads input files, compares sequences, writes output
    public static void main(String[] args) throws IOException {
        var app = new App();

        // get input & outfile file from user via cmd line
        String inputFilename = "DynamicLab2Input.txt";
        String outputFilename = "DynamicLab2Output.txt";
        if (args.length == 2){
            inputFilename = args[0].trim();
            outputFilename = args[1].trim();
        }
        else {
            System.out.printf("Expecting two cmd line arguments with input and output filename\n");
            System.exit(-1);
        }

        try {
            // read all sequences from file
            var inputSequences = app.readInputFile(inputFilename);

            // compute all LCS and write to output file
            app.execute(inputSequences, outputFilename);
        }
        catch (IOException e){
            System.out.printf("Input file %s not found\n", inputFilename);
            System.exit(-1);
        }
        catch (Exception e){
            System.out.printf("%s\n", e.getMessage());
            System.exit(-2);
        }
    }

    // main algorithm, implemented as per text book psuedo code
    public LcsResult lcsLength(String X, String Y) {
        int m = X.length();
        int n = Y.length();
        
        // initialize memoization tables
        Character[][] b = new Character[m+1][n+1];
        int[][] c = new int[m+1][n+1];

        // these variables are just to track algo cost
        int countOperations = 0;
        long startTime = System.nanoTime();

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                var x = X.charAt(i-1);
                var y = Y.charAt(j-1);

                if (x == y) { // if the two letters are equaL
                    c[i][j] = c[i-1][j-1] + 1;
                    b[i][j] = '↖';
                }
                else if (c[i-1][j] >= c[i][j-1]) { // 
                    c[i][j] = c[i-1][j];
                    b[i][j] = '↑';
                }
                else { // 
                    c[i][j] = c[i][j-1];
                    b[i][j] = '←';
                }

                countOperations++;
            }
        }

        long executionTimeNanoseconds = System.nanoTime() - startTime;

        return new LcsResult(X, Y, b, c, countOperations, executionTimeNanoseconds);
    }

    // overload of getLcs that supports LcsResult
    public String getLcs(LcsResult lcsResult) {
        return getLcs(lcsResult.b, lcsResult.X, lcsResult.X.length(), lcsResult.Y.length());
    }

    // main "printLcs" as described by textbook except it returns the sequence instead of printing it directly
    // recursive function
    public String getLcs(Character[][] b, String X, int i, int j) {
        if (i == 0 || j == 0) {
            return "";
        }
        if (b[i][j] == '↖') {
            return getLcs(b, X, i - 1, j - 1) + X.charAt(i-1);
        }
        else if (b[i][j] == '↑') {
            return getLcs(b, X, i - 1, j);
        }
        else {
            return getLcs(b, X, i, j - 1);
        }
    }

    // compares all permutations of inputs and writes LCS of those sequences to output
    public void execute(ArrayList<DnaSeq> inputs, String outputFilename) throws IOException {
        try (var outputFile = new PrintWriter(outputFilename, "UTF-8")) {

            int comparisonCount = 1;
            for (int i = 1; i < inputs.size(); i++) {
                for (int j = 0; j < i; j++) {

                    // Get the two sequences to compare
                    var X = inputs.get(i);
                    var Y = inputs.get(j);

                    // actually compute LCS
                    var result = lcsLength(X.seq, Y.seq);

                    // inspect result tables to get actual LCS sequence
                    var lcs = getLcs(result);

                    // print all results to outputfile
                    outputFile.printf("Comparison #%s\n", comparisonCount);
                    outputFile.printf("%s = %s; n = %s\n", X.name, X.seq, X.seq.length());
                    outputFile.printf("%s = %s; n = %s\n", Y.name, Y.seq, Y.seq.length());
                    outputFile.printf("SubSeq = %s; n = %s\n", lcs, lcs.length());
                    outputFile.printf("CountOperations = %s\n", result.countOperations);
                    outputFile.printf("Nanoseconds = %s\n", result.executionTimeNanoseconds);
                    outputFile.println();

                    comparisonCount++;
                }
            }
        }

        System.out.printf("Full output in '%s'\n", outputFilename);
    }

    // reads all sequences from file in "name = sequence" format
    private ArrayList<DnaSeq> readInputFile(String filename) throws IOException, Exception {
        ArrayList<DnaSeq> result = new ArrayList<DnaSeq>();
        try (Scanner fileScanner = new Scanner(new File(filename)))
        {
            while (fileScanner.hasNextLine()) {
                String nextLine = fileScanner.nextLine();
                if(nextLine.isBlank()){
                    continue;
                }
                // find the equal sign in the line and split two parts
                String[] lineItems = nextLine.split("=");

                // error case if the equal sign is missing or has two equals signs
                if(lineItems.length != 2){
                    throw new Exception(String.format("line '%s' is missing an equal sign or has too many", nextLine));
                }

                // collect the sequence and name in an arraylist
                result.add(new DnaSeq(lineItems[0].trim(), lineItems[1].trim()));
            }
        }

        return result;
    }

    // write some generated sequences to file
    public void writeSequencesToFile(ArrayList<DnaSeq> inputs, String outputFilename) throws IOException {
        try (var outputFile = new PrintWriter(outputFilename, "UTF-8")) {
              for (var dnaSeq : inputs){
                // each seqeuence is like "name = sequence"
                outputFile.printf("%s = %s\n", dnaSeq.name, dnaSeq.seq);
              }
        }
    }

    // generates "count" random sequences with length between minLength and maxLegth
    public ArrayList<DnaSeq> generateInputs(int count, int minLength, int maxLength) {
        var random = new Random();
        ArrayList<DnaSeq> result = new ArrayList<DnaSeq>();

        for (int i = 0; i < count; i++) {
            // Add one random sequence to the result
            result.add(generateSequence(random, minLength, maxLength));
        }

        return result;
    }

    // generates one random sequence with length between minLength and maxLegth
    private DnaSeq generateSequence(Random random, int minLength, int maxLength) {
        // character set is DNA ATGC
        Character [] characterSet = new Character[] {'T','G','C','A'};
        StringBuilder stringBuilder = new StringBuilder();

        int length = random.nextInt(maxLength - minLength) + minLength;

        for(int i = 0; i < length; i++) {
            // pick one random letter number
            var letterNumber = random.nextInt(characterSet.length);
            // lookup from characterset and add to new sequence
            stringBuilder.append(characterSet[letterNumber]);
        }

        // generate a random name for the sequence
        var name = String.format("RAND-N%s-%s%s%s%s", length, random.nextInt(10), 
            random.nextInt(10), random.nextInt(10), random.nextInt(10));

        return new DnaSeq(name, stringBuilder.toString());
    }
}