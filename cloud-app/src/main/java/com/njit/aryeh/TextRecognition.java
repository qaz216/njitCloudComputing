package com.njit.aryeh;

import java.util.List;
import java.util.concurrent.TimeUnit;

import software.amazon.awssdk.services.rekognition.RekognitionClient;
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
	private Boolean deleteMessages;

	public TextRecognition(SqsClient sqsClient, String queueName, Boolean deleteMessages) {
		this.sqsClient = sqsClient;
		this.queueName = queueName;
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
				if (messages.size() == 0) {
					System.out.println("no new messages");
					break;
				}
				for (Message message : messages) {
					String messageBody = message.body();
					System.out.println("message: " + messageBody);

					if (this.deleteMessages) {
						DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
								.receiptHandle(message.receiptHandle()).build();
						sqsClient.deleteMessage(deleteMessageRequest);
					}
					/*
					 * DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
					 * .queueUrl(queueUrl) .receiptHandle(message.receiptHandle()) .build();
					 * sqsClient.deleteMessage(deleteMessageRequest);
					 */
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

}
