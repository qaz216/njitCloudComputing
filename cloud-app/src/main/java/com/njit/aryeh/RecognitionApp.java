package com.njit.aryeh;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class RecognitionApp {
	public static final Region REGION = Region.US_EAST_1;
	private Properties prop = null;
	private SqsClient sqsClient = null;
	private ProfileCredentialsProvider credentialsProvider;
	private RekognitionClient rekClient;

	public RecognitionApp() {
		this.prop = new Properties();
		try {
			this.prop.load(RecognitionApp.class.getClassLoader().getResourceAsStream("application.properties"));

			this.sqsClient = SqsClient.builder().region(REGION).build();
			//String queueUrl = createQueue(this.sqsClient, this.queueName);
			//System.out.println("queueUrl: " + queueUrl);
			
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
			System.out.println("car recognition called ...");
			CarRecognition carReco = new CarRecognition(app.getBucketName(), 
					                                    app.getSqsClient(), 
					                                    app.getQueueName(),
					                                    app.getGroupId(),
					                                    app.getDedupId(),
					                                    app.getRekClient());
			carReco.processImages();
			TimeUnit.SECONDS.sleep(1000);
		}
		else if(mode.equals("text_recognition")) {
			System.out.println("text recognition called ...");
			TextRecognition textApp = new TextRecognition(app.getSqsClient(), app.getQueueName());
			textApp.receiveImages();
		}
	}
	



	private RekognitionClient getRekClient() {
		return this.rekClient;
	}


	private String getMode() {
		return this.prop.getProperty("app.mode");
	}

	private String getQueueName() {
		return this.prop.getProperty("app.queue.name");
	}

	private String getGroupId() {
		return this.prop.getProperty("app.queue.group.id");
	}

	private String getDedupId() {
		return this.prop.getProperty("app.queue.dedup.id");
	}

	private String getBucketName() {
		return this.prop.getProperty("app.bucket");
	}
	
	private SqsClient getSqsClient() {
		return this.sqsClient;
	}
}
