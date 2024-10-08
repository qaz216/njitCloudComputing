package com.njit.aryeh;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
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

public class TextRecognition {
	private SqsClient sqsClient;
	private String queueName;
	private String bucketName;
	private S3Client s3Client;
	private RekognitionClient rekClient;
	private Boolean deleteMessages;

	public TextRecognition(String bucketName, S3Client s3Client, SqsClient sqsClient, String queueName,
			RekognitionClient rekClient, Boolean deleteMessages) {
		this.sqsClient = sqsClient;
		this.queueName = queueName;
		this.bucketName = bucketName;
		this.s3Client = s3Client;
		this.rekClient = rekClient;
		System.out.println("delete messages: + "+deleteMessages);
		this.deleteMessages = deleteMessages;
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
						.build();
				List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

				for (Message message : messages) {
					String messageBody = message.body();
					System.out.println("message: " + messageBody);
					Image img = this.getImage(messageBody, bucketName);

					DetectTextRequest textRequest = DetectTextRequest.builder().image(img).build();

					DetectTextResponse textResponse = rekClient.detectText(textRequest);

					List<TextDetection> textCollection = textResponse.textDetections();
					if (textCollection != null) {
						System.out.println("Detected lines and words");
						for (TextDetection text : textCollection) {
							System.out.println("Detected: " + text.detectedText());
							System.out.println("Confidence: " + text.confidence().toString());
							System.out.println("Id : " + text.id());
							System.out.println("Parent Id: " + text.parentId());
							System.out.println("Type: " + text.type());
							System.out.println();
						}
					}

					if (this.deleteMessages) {
						DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
								.receiptHandle(message.receiptHandle()).build();
						sqsClient.deleteMessage(deleteMessageRequest);
					}

					if (messageBody.equals("-1")) {
						System.out.println("-1 received ... exiting");
						exitLoop = true;
						break;
					}

					TimeUnit.SECONDS.sleep(2);
				}
				if (exitLoop) {
					System.out.println("exiting loop");
					break;
				}

			} catch (SqsException | InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private Image getImage(String key, String bucketName) {
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
			ResponseInputStream<GetObjectResponse> responseBytes = this.s3Client.getObject(getObjectRequest);
			byte[] bytes = responseBytes.readAllBytes();
			SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);
			return Image.builder().bytes(sourceBytes).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
