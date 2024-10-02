package com.njit.aryeh;

import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		String AWS_BUCKET = "njit-cs-643";
		Region AWS_REGION = Region.US_EAST_1;

		S3Client s3Client = S3Client.builder().region(AWS_REGION).build();

		ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(AWS_BUCKET).build();
		ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

		List<S3Object> contents = listObjectsV2Response.contents();

		System.out.println("Number of objects in the bucket: " + contents.stream().count());
		contents.stream().forEach(System.out::println);

		s3Client.close();
	}

}
