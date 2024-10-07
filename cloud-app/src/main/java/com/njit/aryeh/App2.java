package com.njit.aryeh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;

/**
 * Hello world!
 *
 */
public class App2 {
	public static void main(String[] args) {
		String bucketName = "njit-cs-643";
		String key = "1.jpg";
		
		S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
		try {
			
			RekognitionClient rekClient = RekognitionClient.builder()
	                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
	                .region(Region.US_EAST_1)
	                .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            //ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest);
            //ResponseBytes<GetObjectResponse> s = s3Client.getObjectAsBytes(getObjectRequest);
            
            ResponseInputStream<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest);
            

            byte[] bytes =responseBytes.readAllBytes();
            System.out.println("Object bytes length: " + bytes.length);
            
            SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);
            Image souImage = Image.builder()
                    .bytes(sourceBytes)
                    .build();
            
            DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                    .image(souImage)
                    .maxLabels(10)
                    .build();


            DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);
            
            List<Label> labels = labelsResponse.labels();
            for(Label label : labels) {
            	System.out.println("label: "+label.name());
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            s3Client.close();
        }

		/*
		S3Client s3Client = S3Client.builder()
			      .region(Region.US_EAST_1)
			      .build();

			    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
			      .bucket(bucketName)
			      .build();
			    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

			    List<S3Object> contents = listObjectsV2Response.contents();

			    System.out.println("Number of objects in the bucket: " + contents.stream().count());
			    contents.stream().forEach(System.out::println);
			    
			    s3Client.close();
		 */
	}
}