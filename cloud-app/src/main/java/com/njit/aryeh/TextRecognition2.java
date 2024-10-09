package com.njit.aryeh;

import java.io.IOException;
import java.util.List;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class TextRecognition2 {
	private SqsClient sqsClient;
	private S3Client s3Client;
	private String queueName;
	private String bucketName;
	private RekognitionClient rekClient;

	public TextRecognition2(String bucketName, 
			                SqsClient sqsClient, 
			                S3Client s3Client, 
			                String queueName, 
			                RekognitionClient rekClient) {
		this.sqsClient = sqsClient;
		this.queueName = queueName;
		this.bucketName = bucketName;
		this.s3Client = s3Client;
		this.rekClient = rekClient;
	}

	public void receiveImages() {
		boolean exitLoop = false;
		while (true) {
			try {
				GetQueueUrlResponse getQueueUrlResponse = sqsClient
						.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
				String queueUrl = getQueueUrlResponse.queueUrl();
				System.out.println("queue url: " + queueUrl);
				ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl)
						.maxNumberOfMessages(5).build();
				List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
				for (Message message : messages) {
					String messageBody = message.body();
					System.out.println("message: " + messageBody);

					DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
							.receiptHandle(message.receiptHandle()).build();
					sqsClient.deleteMessage(deleteMessageRequest);

					if (messageBody.equals("-1")) {
						System.out.println("-1 received ... exiting");
						exitLoop = true;
						break;
					}
					
					GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(messageBody)
							.build();
					
					ResponseInputStream<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest);
					
					byte[] bytes = responseBytes.readAllBytes();
					System.out.println("Object bytes length: " + bytes.length);

					SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);
					Image souImage = Image.builder().bytes(sourceBytes).build();
					
					DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(souImage)
							.maxLabels(10).build();
					
					DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);

					List<Label> labels = labelsResponse.labels();
					for (Label label : labels) {
						System.out.println("label: " + label.name());
					}
					
					
				}
				if (exitLoop) {
					System.out.println("exiting loop");
					break;
				}

			} catch (SqsException | IOException e) {
				e.printStackTrace();
			}
		}

	}

}