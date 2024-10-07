package com.njit.aryeh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

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

			    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
			      .bucket(bucketName)
			      .build();
			    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

			    List<S3Object> contents = listObjectsV2Response.contents();

			    System.out.println("Number of objects in the bucket: " + contents.stream().count());
			    contents.stream().forEach(System.out::println);
			    
			    s3Client.close();
	}
}