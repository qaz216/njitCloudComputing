package com.njit.aryeh;

import java.io.IOException;
import java.util.Properties;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public class RecognitionApp {
    public static final Region REGION = Region.US_EAST_1;
    private Properties prop = null;
    private SqsClient sqsClient = null;
    private ProfileCredentialsProvider credentialsProvider;
    private RekognitionClient rekClient;
    private S3Client s3Client = S3Client.builder().region(RecognitionApp.REGION).build();;

    public RecognitionApp() {
        this.prop = new Properties();
        try {
            this.prop.load(RecognitionApp.class.getClassLoader().getResourceAsStream("application.properties"));
            this.sqsClient = SqsClient.builder().region(REGION).build();
            this.credentialsProvider = ProfileCredentialsProvider.create();
            this.rekClient = RekognitionClient.builder().credentialsProvider(this.credentialsProvider)
                .region(Region.US_EAST_1).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Recognition Application starting ...");
        RecognitionApp app = new RecognitionApp();
        String mode = app.getMode();
        System.out.println("mode = " + mode);
        if (mode.equals("car_recognition")) {
            System.out.println("running car recognition node ...");
            CarRecognition carReco = new CarRecognition(app.getBucketName(),
                app.getS3Client(),
                app.getSqsClient(),
                app.getQueueName(),
                app.getGroupId(),
                app.getRekClient(),
                app.getCarRecoDelay());
            carReco.processImages();
        } else if (mode.equals("text_recognition")) {
            System.out.println("running text recognition node ...");
            TextRecognition textApp = new TextRecognition(app.getBucketName(),
                app.getSqsClient(),
                app.getS3Client(),
                app.getQueueName(),
                app.getRekClient(),
                app.getOutputFile());
            textApp.receiveImages();
        }
    }




    private S3Client getS3Client() {
        return this.s3Client;
    }

    private int getCarRecoDelay() {
        return Integer.valueOf(this.prop.getProperty("app.car.recognition.delay"));
    }

    private RekognitionClient getRekClient() {
        return this.rekClient;
    }

    private String getMode() {
        return this.prop.getProperty("app.mode");
    }

    private String getOutputFile() {
        return this.prop.getProperty("app.file.location");
    }

    private String getQueueName() {
        return this.prop.getProperty("app.queue.name");
    }

    private String getGroupId() {
        return this.prop.getProperty("app.queue.group.id");
    }

    private String getBucketName() {
        return this.prop.getProperty("app.bucket");
    }

    private SqsClient getSqsClient() {
        return this.sqsClient;
    }
}