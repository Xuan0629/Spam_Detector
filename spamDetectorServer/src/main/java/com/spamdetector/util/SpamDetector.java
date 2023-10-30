package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {
    private Map<String, Double> trainHamFreq;
    private Map<String, Double> trainSpamFreq;

    public SpamDetector() {
        this.trainHamFreq = new HashMap<>();
        this.trainSpamFreq = new HashMap<>();
    }

    // This method takes two directory paths as parameters, one for the directory containing the ham emails and
    // one for the directory containing the spam emails. It returns a list of TestFile objects containing the results
    // of processing the files in those directories.
    public List<TestFile> test(String hamDirPath, String spamDirPath) {
        // Initialize an empty list to hold the results.
        List<TestFile> results = new ArrayList<>();

        // Process the ham files in the ham directory using the processFiles method and add the results to the list.
        processFiles(hamDirPath, "ham", results);
        // Process the spam files in the spam directory using the processFiles method and add the results to the list.
        processFiles(spamDirPath, "spam", results);

        // Return the list of TestFile objects containing the results.
        return results;
    }

    // This is a helper method used by the test method to process the files in a directory. It takes four parameters:
    // the directory path, the actual class of the files (ham or spam), a list to hold the results, and a DecimalFormat
    // object to format the spam probability values. It iterates through each file in the directory, calculates the
    // spam probability of the file using the calculateFileSpamProbability method, and adds a new TestFile object to the
    // results list with the name of the file, the formatted spam probability value, and the actual class of the file.
    private void processFiles(String dirPath, String actualClass, List<TestFile> results) {
        // Create a File object representing the directory.
        File dir = new File(dirPath);
        // Iterate through each file in the directory.
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            // If the file is a file (as opposed to a directory).
            if (file.isFile()) {
                // Calculate the spam probability of the file using the calculateFileSpamProbability method.
                double spamProb = calculateFileSpamProbability(file, trainHamFreq, trainSpamFreq);
                // Add a new TestFile object to the results list with the name of the file, the formatted spam probability
                // value, and the actual class of the file.

                results.add(new TestFile(file.getName(), spamProb, actualClass));
            }
        }
    }

    // This is a helper method used by the processFiles method to calculate the spam probability of a file. It takes
    // three parameters: the file to be processed, a map containing the frequency of words in ham emails, and a map
    // containing the frequency of words in spam emails. It reads the file word by word, calculates the log of the
    // probability ratio of the word being spam to the word being ham, and returns the sigmoid of the sum of the logs.
    private double calculateFileSpamProbability(File file, Map<String, Double> trainHamFreq, Map<String, Double> trainSpamFreq) {
        // Initialize a variable to hold the sum of the logs of the probability ratios.
        double logSum = 0;
        try (Scanner scanner = new Scanner(file)) {
            // Read the file word by word.
            while (scanner.hasNext()) {
                // Convert the word to lowercase.
                String word = scanner.next().toLowerCase();
                // Get the frequency of the word in the spam emails, or 0 if the word is not in the map.
                double spamProb = trainSpamFreq.getOrDefault(word, 0.0);
                // or 0 if the word is not in the map.
                double hamProb = trainHamFreq.getOrDefault(word, 0.0);
                if(spamProb+hamProb==0) {
                    continue;
                }
                // Calculate Pr(S|Wi)
                double prSwi=spamProb/(spamProb+hamProb);
                if(prSwi==0){
                    continue;
                }
                logSum += Math.log(1-prSwi)-Math.log(prSwi);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Calculate Pr(S|F)
        return 1 / (1 + Math.pow(Math.E,logSum));
    }
    public List<TestFile> trainAndTest(File mainDirectory) throws IOException {
//        TODO: main method of loading the directories and files, training and testing the model

        // Create a new list to hold the results of the test
        List<TestFile> results = new ArrayList<>();
        // Get the paths to the "ham" and "spam" sub-directories
        String hamFolderPath = mainDirectory + "/train/ham";
        String spamFolderPath = mainDirectory + "/train/spam";
//        // for test
//        String hamFolderPath = mainDirectory + "/train/h";
//        String spamFolderPath = mainDirectory + "/train/sp";
        // Create "File" objects representing the "ham" and "spam" sub-directories
        File hamFolder = new File(hamFolderPath);
        File spamFolder = new File(spamFolderPath);
        // Calculate the frequency of each word in the "ham" and "spam" directories
        Map<String,Integer> trainHamFreq = fileFrequency(hamFolder);
        Map<String, Integer> trainSpamFreq = fileFrequency(spamFolder);

        // Compute Pr(S|Wi) values for each word Wi
        Map<String, Double> prSwiMap = new HashMap<>();

        for (String word: trainSpamFreq.keySet()) {
            int getNum;

            if(trainHamFreq.get(word) == null) {
                getNum = 0;
            } else {
                getNum = trainHamFreq.get(word);
            }
            double prWiH = (double) getNum/getNumFilesInDir(hamFolder);
            double prWiS = (double) trainSpamFreq.get(word)/getNumFilesInDir(spamFolder);
            if(prWiS + prWiH==0) {
                continue;
            }
            double prSwi = prWiS / (prWiS + prWiH);
            prSwiMap.put(word, prSwi);
        }

        // Test the model on the test data
        results.addAll(test(mainDirectory + "/test/ham", mainDirectory + "/test/spam"));
//        results.addAll(test(mainDirectory + "/train/h", mainDirectory + "/train/sp"));

        // Return the list of test results
        return results;
    }
    /**
     * Gets the number of files in the given directory.
     * @param dir the directory to count the files in
     * @return the number of files in the directory
     */
    private int getNumFilesInDir(File dir) {
        File[] filesInDir = dir.listFiles();
        int numFiles = filesInDir.length;
        return numFiles;
    }
    /**
     * Counts the frequency of each word in the files of the given directory.
     * For each word, it counts the number of files containing that word.
     * @param dir the directory containing the files to analyze
     * @return a map with each word in the directory as a key and the number of files containing that word as a value
     * @throws IOException if there is an error reading the files in the directory
     */
    private Map<String, Integer> fileFrequency(File dir) throws IOException {
        Map<String, Integer> fileFreq = new TreeMap<>();
        Map<String, Integer> frequencies = wordFrequencyDir(dir);

        File[] filesInDir = dir.listFiles();
        //iterate over each word and count how many files contain the word
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            int count = 0;
            for (File file : filesInDir) {
                if (!file.isFile()) {
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains(entry.getKey())) {
                            count+=1;
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error reading file: " + file.getAbsolutePath());
                }
            }
            fileFreq.put(entry.getKey(), count);
        }
        return fileFreq;
    }

    private Map<String, Integer> wordFrequencyDir(File dir) throws IOException {
        Map<String, Integer> frequencies = new TreeMap<>();

        File[] filesInDir = dir.listFiles();
        int numFiles = filesInDir.length;
        //iterate over each file in the dir an count their words
        for  (int i=0; i<numFiles; i++) {
            Map<String,Integer> wordMap = countWordFile(filesInDir[i]);

            // merge the file wordMap into the global frequencies
            Set<String> keys = wordMap.keySet();
            Iterator<String> keyIterator = keys.iterator();
            while(keyIterator.hasNext()) {
                String word = keyIterator.next();
                int count = wordMap.get(word);
                if (frequencies.containsKey(word)) {
                    // increment
                    int oldCount = frequencies.get(word);
                    frequencies.put(word, count + oldCount);
                } else {
                    frequencies.put(word, count);
                }
            }

        }
        return frequencies;
    }
    /**
     * Counts the frequency of each word in the given file.
     * @param file the file to analyze
     * @return a map with each word in the file as a key and the number of times that word appears in the file as a value
     * @throws IOException if there is an error reading the file
     */
    private Map<String, Integer> countWordFile(File file) throws IOException {
        Map<String, Integer> wordMap = new TreeMap<>();
        if (file.exists()) {
            // load all the data and process it into words
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                // ignore the casing for words
                String word = scanner.next().toLowerCase();
                if (isWord(word)) {
                    // add the word if it does not exist yet
                    if (!wordMap.containsKey(word)) {
                        wordMap.put(word, 1);
                    } else { // increment the count if it exists
                        int oldCount = wordMap.get(word);
                        wordMap.put(word, oldCount + 1);
                    }
                }

            }
        }
        return wordMap;
    }
    /**
     * Checks if the given string is a valid word.
     * @param word the string to check
     * @return true if the string consists only of alphabetical characters, false otherwise
     */
    private Boolean isWord(String word) {
        if (word == null) {
            return false;
        }

        String pattern = "^[a-zA-Z]*$";
        if (word.matches(pattern)) {
            return true;
        }

        return false;
    }

}
