package com.njit.aryeh;

import java.io.IOException;
import java.util.Properties;

public class RecognitionApp {
	private Properties prop = null;

	public RecognitionApp() {
		this.prop = new Properties();
		try {
			this.prop.load(RecognitionApp.class.getClassLoader().getResourceAsStream("application.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Recognition Application starting ...");
		RecognitionApp app = new RecognitionApp();
		String mode = app.getMode();
		String bucketName = app.getBucketName();
		System.out.println("mode = " + mode);
		if(mode.equals("car_recognition")) {
			System.out.println("car recognition called ...");
			CarRecognition carReco = new CarRecognition(bucketName);
			carReco.processImages();
		}
	}
	
	private String getMode() {
		return this.prop.getProperty("app.mode");
	}

	private String getBucketName() {
		return this.prop.getProperty("app.bucket");
	}
}
