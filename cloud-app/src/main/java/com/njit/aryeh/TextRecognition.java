package com.njit.aryeh;

import java.util.List;

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

	public TextRecognition(SqsClient sqsClient, String queueName) {
		this.sqsClient = sqsClient;
		this.queueName = queueName;
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
					DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
	                        .queueUrl(queueUrl)
	                        .receiptHandle(message.receiptHandle())
	                        .build();
	                sqsClient.deleteMessage(deleteMessageRequest);
					/*
					DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteMessageRequest);
                    */
					if(messageBody.equals("-1")) {
						System.out.println("-1 received ... exiting");
						exitLoop = true;
						break;
					}
				}
				if(exitLoop) {
					System.out.println("exiting loop");
					break;
				}

			} catch (SqsException e) {
				System.err.println(e.awsErrorDetails().errorMessage());
				System.exit(1);
			}
		}

	}

}
