package com.njit.aryeh;

import java.io.IOException;
import java.util.Properties;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class RecognitionApp {
	public static final Region REGION = Region.US_EAST_1;
	private Properties prop = null;
	private SqsClient sqsClient = null;
	private String queueName;

	public RecognitionApp() {
		this.prop = new Properties();
		try {
			this.prop.load(RecognitionApp.class.getClassLoader().getResourceAsStream("application.properties"));

			this.queueName = this.prop.getProperty("app.queue.name");
			System.out.println("queueName: " + this.queueName);
			this.sqsClient = SqsClient.builder().region(REGION).build();
			String queueUrl = createQueue(this.sqsClient, this.queueName);
			System.out.println("queueUrl: " + queueUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Recognition Application starting ...");
		RecognitionApp app = new RecognitionApp();
		String mode = app.getMode();
		String bucketName = app.getBucketName();
		System.out.println("mode = " + mode);
		if (mode.equals("car_recognition")) {
			System.out.println("car recognition called ...");
			CarRecognition carReco = new CarRecognition(bucketName, app.getSqsClient(), app.getQueueName());
			carReco.processImages();
		}
	}

	public static String createQueue(SqsClient sqsClient, String queueName) {
		try {
			System.out.println("\nCreate Queue");

			CreateQueueRequest createQueueRequest = CreateQueueRequest.builder().queueName(queueName).build();

			sqsClient.createQueue(createQueueRequest);

			System.out.println("\nGet queue url");

			GetQueueUrlResponse getQueueUrlResponse = sqsClient
					.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
			return getQueueUrlResponse.queueUrl();

		} catch (SqsException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		return "";
	}

	private String getMode() {
		return this.prop.getProperty("app.mode");
	}

	private String getQueueName() {
		return this.queueName;
	}

	private String getBucketName() {
		return this.prop.getProperty("app.bucket");
	}
	
	private SqsClient getSqsClient() {
		return this.sqsClient;
	}
}
