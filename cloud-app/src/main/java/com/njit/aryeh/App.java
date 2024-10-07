package com.njit.aryeh;

import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;


/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("aws s3 app started ...");
		
		String bucketName = "njit-cs-643";
        Region region = Region.US_EAST_1; 

		// Create a ProfileCredentialsProvider that reads from the default credentials file
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        
        System.out.println("provide: "+credentialsProvider.toString());

        // Create an S3 client using the credentials provider
        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .build();
        
        S3Client s3Client = S3Client.builder()
                .region(region)
                .build();
        
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);

            for (S3Object object : response.contents()) {
                System.out.println(object.key());
            }

            request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated());

        s3Client.close();
	}

}
