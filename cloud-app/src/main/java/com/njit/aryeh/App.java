package com.njit.aryeh;

import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		Regions clientRegion = Regions.DEFAULT_REGION;
		String bucketName = "*** Bucket name ***";

		System.out.println("aws.accessKeyId: " + System.getProperty("aws.accessKeyId"));
		System.out.println("aws.secretAccessKey: " + System.getProperty("aws.secretAccessKey"));
	}

}
