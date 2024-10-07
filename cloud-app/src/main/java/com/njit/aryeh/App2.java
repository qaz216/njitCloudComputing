package com.njit.aryeh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Hello world!
 *
 */
public class App2 {
	public static void main(String[] args) {
		String bucketName = "njit-cs-643";
		String key = "1.jpg";

		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

		try {
			S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
			InputStream objectData = object.getObjectContent();

			byte[] imageBytes = readAllBytes(objectData);
			System.out.println("done ...");

			// Use the imageBytes as needed
			// For example, you can display the image in a Swing application
			// or process it using a library like OpenCV

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	   private static byte[] readAllBytes(InputStream inputStream) throws IOException {
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        int nRead;
	        byte[] data = new byte[16384];

	        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
	            buffer.write(data, 0, nRead);
	        }

	        buffer.flush();
	        return buffer.toByteArray();
	    }
}