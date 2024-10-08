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
	private Properties prop = null;
	public static final Region REGION = Region.US_EAST_1;

	public RecognitionApp() {
		this.prop = new Properties();
		try {
			this.prop.load(RecognitionApp.class.getClassLoader().getResourceAsStream("application.properties"));

			String queueName = this.prop.getProperty("app.queue.name");
			System.out.println("queueName: " + queueName);

			SqsClient sqsClient = SqsClient.builder().region(REGION).build();

			// Perform various tasks on the Amazon SQS queue.
			String queueUrl = createQueue(sqsClient, queueName);
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
			CarRecognition carReco = new CarRecognition(bucketName);
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

	private String getBucketName() {
		return this.prop.getProperty("app.bucket");
	}
}
