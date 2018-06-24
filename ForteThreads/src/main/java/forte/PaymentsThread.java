package com.selsoft.trackme.forte;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.selsoft.trackme.constants.PaymentConstants;
import com.selsoft.trackme.models.Settlement;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.payments.utils.RestTemplateCustomErrorHandler;

public class PaymentsThread implements Runnable {

	private static final Logger logger = Logger.getLogger(PaymentsThread.class);
	private static final String FORTE_ORG_ID = "org_357018";
	private static final String FORTE_USER_NAME = "b57f9906a9dfdbe2aa4327f133f678e1";
	private static final String FORTE_PASSWORD = "8a3d117645c2d12d0bccda0f029c1185";

	private static final String FORTE_SANDBOX_URL = "https://sandbox.forte.net/api/v3/organizations/org_357018/";
	private static final String FORTE_LIVE_URL = "https://api.forte.net/v3/organizations/org_357018/applications/";
	private static String locationId;

	@Override
	public synchronized void run() {
		System.out.println("Started");
		for (int i = 0; i < 1; i++) {
			System.out.println(Thread.currentThread().getName() + i);
			try {
				getAllTransactionFromForte();
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
	private void getAllTransactionFromForte() {
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
			ResponseEntity<HashMap> response = restTemplate.exchange(getForteUrlWithOrgIdAndLocation(locationId),
					HttpMethod.GET, entity, HashMap.class);
			if (response != null && response.getBody() != null) {
				result = response.getBody();
				System.out.println(result);
				if (result.containsKey("results") && result.get("results") != null) {
					List<Settlement> forteSettlments = (List<Settlement>) result.get("results");
					if (forteSettlments != null)
						updateTransactionStatus(forteSettlments);
				}
			}
		} catch (Exception ex) {
			ex.fillInStackTrace();
		}
	}

	private void updateTransactionStatus(List<Settlement> forteSettlments) {
		List<Transaction> transactions = getTransactionForLocationId();
		for (Settlement settlement : forteSettlments) {
			for (Transaction transaction : transactions) {
				if (settlement.getTransactionId().equals(transaction.getForteTransactionId())) {
					if (settlement.getSettleResponseCode().equals("A01")) {
						updateTransaction(settlement, true);
					} else {
						updateTransaction(settlement, false);
					}
				}
			}
		}
	}

	private void updateTransaction(Settlement settlement, boolean isApproved) {
		ServerAddress serverAddress = new ServerAddress("66.175.215.173", 27017);
		MongoCredential credential = MongoCredential.createCredential("trackme-dev-user", "user-data",
				"trackmeDevUser".toCharArray());
		MongoClientOptions options = MongoClientOptions.builder().sslEnabled(false).build();
		MongoClient mongoClient = new MongoClient(serverAddress, credential, options);
		MongoDatabase db = mongoClient.getDatabase("TrackMeDev");
		MongoCollection<Document> transactionCollection = db.getCollection("TRANSACTION");

		transactionCollection.updateOne(Filters.eq("forteTransactionId", settlement.getTransactionId()),
				new Document("$set", new Document("responseCode", settlement.getSettleResponseCode())));
		if (isApproved) {
			transactionCollection.updateOne(Filters.eq("forteTransactionId", settlement.getTransactionId()),
					new Document("$set",
							new Document("paymentStatus", PaymentConstants.FORTE_PAYMENT_STATUS_APPROVED)));
		} else if (settlement.getSettleResponseCode().contains("R")) {
			transactionCollection.updateOne(Filters.eq("forteTransactionId", settlement.getTransactionId()),
					new Document("$set", new Document("paymentStatus", PaymentConstants.FORTE_PAYMENT_STATUS_REJECT)));
		} else {
			transactionCollection.updateOne(Filters.eq("forteTransactionId", settlement.getTransactionId()),
					new Document("$set", new Document("paymentStatus", PaymentConstants.FORTE_PAYMENT_STATUS_PENDING)));
		}

		if (mongoClient != null)
			mongoClient.close();
	}

	private List<Transaction> getTransactionForLocationId() {
		List<Transaction> mongoDbTransactions = new ArrayList<>();
		ServerAddress serverAddress = new ServerAddress("66.175.215.173", 27017);
		MongoCredential credential = MongoCredential.createCredential("trackme-dev-user", "user-data",
				"trackmeDevUser".toCharArray());
		MongoClientOptions options = MongoClientOptions.builder().sslEnabled(false).build();
		MongoClient mongoClient = new MongoClient(serverAddress, credential, options);
		MongoDatabase db = mongoClient.getDatabase("TrackMeDev");
		MongoCollection<Document> transactionCollection = db.getCollection("TRANSACTION");

		FindIterable<Document> transactions = transactionCollection.find(Filters.eq("locationId", locationId));
		Iterator<Document> transactionsIterator = transactions.iterator();

		while (transactionsIterator.hasNext()) {
			System.out.println("1");
			Document document = transactionsIterator.next();
			try {
				Transaction dbTransaction = new Transaction();
				dbTransaction.setTransactionId((String) document.get("_id"));
				dbTransaction.setForteTransactionId((String) document.get("forteTransactionId"));
				dbTransaction.setLocationId((String) document.get("locationId"));
				mongoDbTransactions.add(dbTransaction);
			} catch (Exception e) {
				System.out.println(e);
				logger.error(e);
			}

		}

		if (mongoClient != null)
			mongoClient.close();

		return mongoDbTransactions;
	}

	@SuppressWarnings({ "deprecation", "unchecked", "resource" })
	public static void main(String[] args) {

		Map<String, Document> locationSet = getLocationIdTransaction();

		locationSet.forEach((locationId, document) -> {
			System.out.println("Key : " + locationId);
			System.out.println(
					"StartDate : " + document.getString("startDate") + " endDate : " + document.getString("endDate"));
		});

		PaymentsThread testREST1 = new PaymentsThread();
		Thread t1 = new Thread(testREST1, "t1");
		t1.start();

	}

	private static Map<String, Document> getLocationIdTransaction() {
		ServerAddress serverAddress = new ServerAddress("66.175.215.173", 27017);
		MongoCredential credential = MongoCredential.createCredential("trackme-dev-user", "user-data",
				"trackmeDevUser".toCharArray());
		MongoClientOptions options = MongoClientOptions.builder().sslEnabled(false).build();
		MongoClient mongoClient = new MongoClient(serverAddress, credential, options);
		MongoDatabase db = mongoClient.getDatabase("TrackMeDev");
		MongoCollection<Document> transactionCollection = db.getCollection("TRANSACTION");

		SimpleDateFormat documentDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss");
		SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		FindIterable<Document> transactions = transactionCollection
				.find(Filters.eq("paymentStatus", PaymentConstants.FORTE_PAYMENT_STATUS_PENDING));

		Set<String> locationSet = new HashSet<String>();

		Map<String, Document> transactionSubSet = new HashMap<String, Document>();

		// transactions.forEach((Document document) -> System.out.println(document));

		Iterator<Document> transactionsIterator = transactions.iterator();

		while (transactionsIterator.hasNext()) {
			System.out.println("1");
			Document document = transactionsIterator.next();

			// System.out.println(document);
			Document transactionDocument = null;
			try {
				Date enteredDate;
				if (document.containsKey("locationId") && StringUtils.isNotBlank((String) document.get("locationId"))) {
					String locationId = (String) document.get("locationId");
					locationSet.add(locationId);
					if (transactionSubSet.get(locationId) != null) {
						transactionDocument = transactionSubSet.get(locationId);
						enteredDate = documentDateFormat.parse((String) document.get("enteredOn"));
						if (enteredDate.before(isoDateFormat.parse((String) transactionDocument.get("startDate")))) {
							transactionDocument.put("startDate", isoDateFormat.format(enteredDate));
						} else if (enteredDate
								.after(isoDateFormat.parse((String) transactionDocument.get("endDate")))) {
							transactionDocument.put("endDate", isoDateFormat.format(enteredDate));
						}
					} else {
						transactionDocument = new Document();
						String startDate = isoDateFormat
								.format(documentDateFormat.parse((String) document.get("enteredOn")));
						transactionDocument.put("startDate", startDate);
						transactionDocument.put("endDate", startDate);
						transactionSubSet.put(locationId, transactionDocument);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
				logger.error(e);
			}

		}

		if (mongoClient != null)
			mongoClient.close();

		return transactionSubSet;

		// transactions.forEach((Document document) -> {});

	}

	private String getForteUrlWithOrgIdAndLocation(String locationId) {
		locationId = getFormattedLocationId(locationId);
		return new StringBuilder(FORTE_SANDBOX_URL).append("/locations/").append(locationId).append("/settlements")
				.toString();
	}

	private static String getFormattedLocationId(String locationId) {
		if (StringUtils.isBlank(locationId))
			return StringUtils.EMPTY;
		return new StringBuilder("loc_").append(locationId).toString();
	}

}
