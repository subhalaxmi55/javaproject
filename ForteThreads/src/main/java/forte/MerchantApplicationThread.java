package com.selsoft.trackme.forte;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.selsoft.trackme.constants.PaymentConstants;
import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.payments.utils.RestTemplateCustomErrorHandler;

import static com.mongodb.client.model.Filters.eq;

public class MerchantApplicationThread implements Runnable {

	private static final String FORTE_ORG_ID = "org_357018";
	private static final String FORTE_USER_NAME = "b57f9906a9dfdbe2aa4327f133f678e1";
	private static final String FORTE_PASSWORD = "8a3d117645c2d12d0bccda0f029c1185";

	private static final String FORTE_SANDBOX_URL = "https://sandbox.forte.net/api/v3/organizations/org_357018/applications/";
	private static final String FORTE_LIVE_URL = "https://api.forte.net/v3/organizations/org_357018/applications/";

	@Override
	public synchronized void run() {
		System.out.println("Started");
		for (int i = 0; i < 1; i++) {
			System.out.println(Thread.currentThread().getName() + i);
			try {
				getAllForteApplication();
				this.wait(86400000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e);
			}
		}
		System.out.println("Stopped");
	}

	@SuppressWarnings("unchecked")
	private void getAllForteApplication() {

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new RestTemplateCustomErrorHandler());
		HashMap<String, Object> result = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			headers.add("Accept", "application/json");
			headers.add("X-Forte-Auth-Organization-Id", FORTE_ORG_ID);
			headers.add("Authorization",
					"Basic " + Base64.getEncoder().encodeToString((FORTE_USER_NAME + ":" + FORTE_PASSWORD).getBytes()));
			headers.add("Cache-Control", "no-cache");

			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<HashMap> response = restTemplate.exchange(FORTE_SANDBOX_URL, HttpMethod.GET, entity,
					HashMap.class);
			if (response != null && response.getBody() != null) {
				result = response.getBody();
				System.out.println(result);
				if (result.containsKey("results") && result.get("results") != null) {
					List<Application> applications = (List<Application>) result.get("results");
					if (applications != null)
						updateApplicationsStatus(applications);
				}
			}
		} catch (Exception ex) {
			ex.fillInStackTrace();
		}

	}

	private void updateApplicationsStatus(List<Application> applications) {
		List<Application> localApplication = getApplicationFromDB();
		if (localApplication != null) {
			for (Application forteApp : applications) {
				for (Application pendingApp : localApplication) {
					if (forteApp.getApplicationId().equals(pendingApp.getForteApplicationId())) {
						updateApplication(forteApp);
					}
				}
			}
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked", "resource" })
	private List<Application> getApplicationFromDB() {
		MongoCredential credential = MongoCredential.createCredential("trackme-dev-user", "user-data",
				"trackmeDevUser".toCharArray());
		MongoClient mongoClient = new MongoClient(new ServerAddress("66.175.215.173", 27017),
				Arrays.asList(credential));

		MongoDatabase mongoDatabase = mongoClient.getDatabase("TrackMeDev");
		MongoCollection<Document> merchantApplicationConnection = mongoDatabase.getCollection("MERCHANT_APPLICATION");

		List<Application> pendingApplication = (List<Application>) merchantApplicationConnection
				.find(eq("applicationStatus", PaymentConstants.FORTE_PAYMENT_STATUS_PENDING), Application.class);

		if (mongoClient != null) {
			mongoClient.close();
		}

		return pendingApplication;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void main(String[] args) {
		MerchantApplicationThread testREST1 = new MerchantApplicationThread();
		Thread t1 = new Thread(testREST1, "t1");
		t1.start();

	}


	@SuppressWarnings({ "deprecation", "unchecked" })
	private void updateApplication(Application application) {

		MongoCredential credential = MongoCredential.createCredential("trackme-dev-user", "user-data",
				"trackmeDevUser".toCharArray());
		MongoClient mongoClient = new MongoClient(new ServerAddress("66.175.215.173", 27017),
				Arrays.asList(credential));

		MongoDatabase mongoDatabase = mongoClient.getDatabase("TrackMeDev");
		MongoCollection<Document> merchantApplicationConnection = mongoDatabase.getCollection("MERCHANT_APPLICATION");

		try {
			// update status with UPPER CASE
			if (application.getStatus().equals("approved")) {
				merchantApplicationConnection.updateOne(eq("_id", application.getForteApplicationId()), new Document(
						"$set", new Document("applicationStatus", PaymentConstants.FORTE_PAYMENT_STATUS_APPROVED)));
			} else if (application.getStatus().equals("declined")) {
				merchantApplicationConnection.updateOne(eq("_id", application.getForteApplicationId()), new Document(
						"$set", new Document("applicationStatus", PaymentConstants.FORTE_PAYMENT_STATUS_DECLINED)));
			} else if (application.getStatus().equals("reject")) {
				merchantApplicationConnection.updateOne(eq("_id", application.getForteApplicationId()), new Document(
						"$set", new Document("applicationStatus", PaymentConstants.FORTE_PAYMENT_STATUS_REJECT)));
			}

			// update location id
			merchantApplicationConnection.updateOne(eq("_id", application.getForteApplicationId()),
					new Document("$set", new Document("locationId", application.getLocationId())));


			// Add update locationId in USER.java for the submit user (USER Table)
			MongoCollection<Document> userConnection = mongoDatabase.getCollection("USER");
			userConnection.updateOne(eq("_id", application.getUserId()),
					new Document("$set", new Document("locationId", application.getLocationId())));

			// Update LocationId for OWN and TNT for the user=managerId
			userConnection.updateOne(eq("managerId", application.getUserId()),
					new Document("$set", new Document("locationId", application.getLocationId())));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (mongoClient != null) {
			mongoClient.close();
		}
	}


}
