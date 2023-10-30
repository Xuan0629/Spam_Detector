package com.spamdetector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {

    ObjectMapper mapper = new ObjectMapper();
    //    your SpamDetector Class responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();

    List<TestFile> testResults = null;
    public SpamResource(){
//      TODO: load resources, train and test to improve performance on the endpoint calls
        System.out.print("Training and testing the model, please wait");

//      TODO: call  this.trainAndTest();
        testResults=trainAndTest();

    }
    @GET
    @Produces("application/json")
    public Response getSpamResults() throws JsonProcessingException {
//      TODO: return the test results list of TestFile, return in a Response object

        return Response.ok()
                .header("Access-Control-Allow-Origin","*")
                .header("Content-Type","application/json").entity(mapper.writeValueAsString(testResults)).build();
//        return null;
    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() throws JsonProcessingException {
//      TODO: return the accuracy of the detector, return in a Response object
        // Accuracy = (The total number of the file that actual Class is 'ham/spam' and the test solution also think it is 'ham/spam' )/ (Total number of the files)
        List<TestFile> testResults = trainAndTest();
        int numTurePositives=0;
        int numTureNegatives=0;
        int numGuesses = 0;
        for(TestFile file: testResults){
            if(file.getSpamProbability()<=0.5){
                if(file.getActualClass()=="ham"){
                    numTurePositives = numTurePositives+1;
                }
            }
            if(file.getSpamProbability()>0.5){
                if(file.getActualClass()=="spam"){
                    numTureNegatives = numTureNegatives+1;
                }
            }
            numGuesses = numGuesses+1;
        }
        int numCorrectGuesses=numTurePositives+numTureNegatives;
        double accuracy = (double)numCorrectGuesses / numGuesses;
        return Response.ok()
                .header("Access-Control-Allow-Origin","*")
                .header("Content-Type","application/json").entity(mapper.writeValueAsString(accuracy)).build();
        // return null;
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() throws JsonProcessingException {
//      TODO: return the precision of the detector, return in a Response object
        // Precision = (The total number of the file that actual Class is 'ham' and the test solution also think it is 'ham' ) / (The total number of the file that actual Class is 'ham/spam' and the test solution also think it is 'ham/spam' )
        List<TestFile> testResults = trainAndTest();
        int numTurePositives=0;
        int numFalsePositives = 0;
        for(TestFile file: testResults){
            if(file.getSpamProbability()<=0.5){
                if(file.getActualClass()=="ham"){
                    numTurePositives = numTurePositives+1;
                }
                else{
                    numFalsePositives = numFalsePositives+1;
                }
            }
        }
        int numPositivesGuesses=numTurePositives+numFalsePositives;
        double precision = (double)numTurePositives / numPositivesGuesses;
        return Response.ok()
                .header("Access-Control-Allow-Origin","*")
                .header("Content-Type","application/json").entity(mapper.writeValueAsString(precision)).build();
        // return null;
    }

    private List<TestFile> trainAndTest()  {

//      TODO: load the main directory "data" here from the Resources folder
        File mainDirectory = new File(getClass().getClassLoader().getResource("data").getFile());
//        File mainDirectory = null;
        try {
            return this.detector.trainAndTest(mainDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}