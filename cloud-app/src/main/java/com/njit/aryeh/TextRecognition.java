package com.njit.aryeh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
	private S3Client s3Client;
	private String queueName;
	private String bucketName;
	private String outputFile;
	private RekognitionClient rekClient;

	public TextRecognition(String bucketName, SqsClient sqsClient, S3Client s3Client, String queueName,
			RekognitionClient rekClient, String outputFile) {
		this.sqsClient = sqsClient;
		this.queueName = queueName;
		this.bucketName = bucketName;
		this.s3Client = s3Client;
		this.rekClient = rekClient;
		this.outputFile = outputFile;
	}

	public void receiveImages() {
		StringBuilder output = new StringBuilder();
		boolean exitLoop = false;
		try {
			while (true) {
				GetQueueUrlResponse getQueueUrlResponse = sqsClient
						.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
				String queueUrl = getQueueUrlResponse.queueUrl();
				ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl)
						.maxNumberOfMessages(5).build();
				List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
				for (Message message : messages) {
					String messageBody = message.body();

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

					SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);
					Image souImage = Image.builder().bytes(sourceBytes).build();

					DetectTextRequest textRequest = DetectTextRequest.builder().image(souImage).build();
					DetectTextResponse textResponse = rekClient.detectText(textRequest);

					List<TextDetection> textCollection = textResponse.textDetections();
					for (TextDetection text : textCollection) {
						String textDetected = text.detectedText();
						Float confidence = text.confidence();
						String line = "Text detected for image: " + messageBody + " - text: " + textDetected
								+ " - confidence: " + confidence;
						System.out.println(line);
						output.append(line + "\n");
					}

				}
				if (exitLoop) {
					System.out.println("exiting loop");
					break;
				}
			}
			
			Path filePath = Paths.get(outputFile);
			Files.deleteIfExists(filePath);
			byte[] strToBytes = output.toString().getBytes();
		    Files.write(filePath, strToBytes);


		} catch (SqsException | IOException e) {
			e.printStackTrace();
		}

	}

}