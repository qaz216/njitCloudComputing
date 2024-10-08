package com.njit.aryeh;

import java.io.IOException;
import java.util.List;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class CarRecognition {
	private String bucketName = null;
	private RekognitionClient rekClient;
	private S3Client s3Client;
	private SqsClient sqsClient;
	private String queueName;

	public CarRecognition(String bucketName, SqsClient sqsClient, String queueName, RekognitionClient rekClient) {
		this.bucketName = bucketName;
		this.rekClient = rekClient;
		this.s3Client = S3Client.builder().region(RecognitionApp.REGION).build();
		this.sqsClient = sqsClient;
		this.queueName = queueName;
	}

	public void processImages() {
		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(this.bucketName).build();
		try {
			ListObjectsV2Response response;
			do {
				response = this.s3Client.listObjectsV2(request);

				for (S3Object object : response.contents()) {
					String key = object.key();
					System.out.println("processing image: " + key);

					GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(this.bucketName)
							.key(object.key()).build();

					ResponseInputStream<GetObjectResponse> responseBytes = this.s3Client.getObject(getObjectRequest);

					byte[] bytes = responseBytes.readAllBytes();

					SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);
					Image souImage = Image.builder().bytes(sourceBytes).build();

					DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(souImage)
							.maxLabels(10).build();

					DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);

					List<Label> labels = labelsResponse.labels();
					for (Label label : labels) {
						String labelName = label.name().toLowerCase().trim();
						Float confidence = label.confidence();
						if (labelName.equals("car") && confidence >= 90.0) {
							System.out.println("Found a car with confidence over 90%");
							System.out.println(
									"image name: " + key + " label: " + labelName + " confidence: " + confidence);
							this.sendQueueMessage(key);
						}
					}

					request = ListObjectsV2Request.builder().bucket(this.bucketName)
							.continuationToken(response.nextContinuationToken()).build();
				}
			} while (response.isTruncated());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendQueueMessage(String key) {
		GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder().queueName(queueName).build();

		String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
		SendMessageRequest sendMsgRequest = SendMessageRequest.builder().queueUrl(queueUrl).messageBody(key)
				.delaySeconds(5).build();

		System.out.println("sending message for key: "+key);
		sqsClient.sendMessage(sendMsgRequest);
		System.out.println("sent message for key: "+key);

	}
}