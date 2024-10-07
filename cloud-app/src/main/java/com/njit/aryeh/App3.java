package com.njit.aryeh;

import java.io.IOError;
import java.io.IOException;
import java.util.List;

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
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;

/**
 * Hello world!
 *
 */
public class App3 {
	public static void main(String[] args) {
		System.out.println("aws s3 app started ...");

		String bucketName = "njit-cs-643";
		Region region = Region.US_EAST_1;

		// Create a ProfileCredentialsProvider that reads from the default credentials
		// file
		ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

		RekognitionClient rekClient = RekognitionClient.builder().credentialsProvider(credentialsProvider)
				.region(Region.US_EAST_1).build();

		System.out.println("provide: " + credentialsProvider.toString());

		// Create an S3 client using the credentials provider
		/*
		 * S3Client s3 = S3Client.builder() .region(Region.US_EAST_1)
		 * .credentialsProvider(credentialsProvider) .build();
		 */

		S3Client s3Client = S3Client.builder().region(region).build();

		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();

		try {
			ListObjectsV2Response response;
			do {
				response = s3Client.listObjectsV2(request);

				for (S3Object object : response.contents()) {
					System.out.println(object.key());

					// ResponseInputStream<GetObjectResponse> responseBytes =
					// s3Client.getObject(request);

					GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(object.key())
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

				request = ListObjectsV2Request.builder().bucket(bucketName)
						.continuationToken(response.nextContinuationToken()).build();
			} while (response.isTruncated());
		} catch (IOException e) {
			e.printStackTrace();
		}

		s3Client.close();
	}

}
