package com.selsoft.trackme.payments.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.selsoft.trackme.constants.PaymentConstants;
import com.selsoft.trackme.exception.PaymentsException;
import com.selsoft.trackme.models.AccountOwner;
import com.selsoft.trackme.models.ApplicantOrganization;
import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.payments.dao.PaymentsDao;
import com.selsoft.trackme.payments.utils.PaymentsUtility;
import com.selsoft.trackme.payments.utils.RestTemplateCustomErrorHandler;
import com.selsoft.trackme.utils.TrackMeUtils;
import com.selsoft.trackme.utils.serializer.AccountOwnerSerializer;
import com.selsoft.trackme.utils.serializer.ApplicantOrganizationSerializer;
import com.selsoft.trackme.utils.serializer.ApplicationSerializer;

@Service
public class PaymentsServiceImpl implements PaymentsService {

	private static final Logger logger = Logger.getLogger(PaymentsServiceImpl.class);

	@Autowired
	private PaymentsDao paymentsDao;

	private static String organizationId;
	private static String forteFormattedOrganizationId;
	private static String forteBaseUrl;
	private static String forteUserId;
	private static String fortePassword;
	private static String forteUrlWithOrgId;
	private static RestTemplate restTemplate;
	private static HttpHeaders headers;

	@PostConstruct
	public void init() {
		try {
			Map<String, String> forteData = getFortePaymentData();
			if (forteData != null && forteData.size() > 0) {
				organizationId = forteData.get(PaymentConstants.FORTE_ORGANIZATION_ID_STRING);
				forteFormattedOrganizationId = PaymentsUtility.getFormattedOrganizationId(organizationId);
				forteBaseUrl = forteData.get(PaymentConstants.FORTE_BASE_URL_STRING);
				forteUserId = forteData.get(PaymentConstants.FORTE_AUTH_USER_ID_STRING);
				fortePassword = forteData.get(PaymentConstants.FORTE_AUTH_PASSWORD_STRING);
				forteUrlWithOrgId = new StringBuilder(forteBaseUrl).append("/organizations/")
						.append(forteFormattedOrganizationId).toString();
			} else {
				logger.info("Forte data not found in the database");
			}

			restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new RestTemplateCustomErrorHandler());
			headers = new HttpHeaders();
			headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.add("X-Forte-Auth-Organization-Id", forteFormattedOrganizationId);

			restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(getForteUserId(), getFortePassword()));

		} catch (PaymentsException e) {
			logger.info("Cannot initialize forte data", e);
		} catch (Throwable t) {
			logger.fatal("Cannot initialize forte data", t);
		}
	}

	@Override
	public Map<String, Object> getAllCustomersFromForte(@RequestBody Map<String, Object> orgData) throws Throwable {
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<Object> result = restTemplate.exchange(getForteCustomerUrl((String) orgData.get("locationId")),
				HttpMethod.GET, entity, Object.class);
		orgData.put("result", result.getBody());
		return orgData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Transaction makeCardPayment(Transaction transaction) throws Throwable {

		if (transaction == null)
			throw new PaymentsException("Error", "No information found for making payment");

		JSONObject jsonObject = new JSONObject();

		String locationId = transaction.getLocationId();

		double authorizationAmount = transaction.getAmount();
		String cardType = transaction.getCardType();
		String nameOnCard = transaction.getNameOnCard();
		String cardNumber = transaction.getCardNumber();
		String expiryMonth = transaction.getExpiryMonth();
		String expiryYear = transaction.getExpiryYear();
		String cardCVV = transaction.getCardCVV();

		String firstName = transaction.getFirstName();
		String lastName = transaction.getLastName();

		// if(StringUtils.isEmpty(organizationId) ||

		if (StringUtils.isEmpty(locationId)) {
			throw new PaymentsException("Error", "Invalid request, location id is missing. Cannot make payment.");
		}

		JSONObject jsonRequest = new JSONObject();
		JSONObject billingAddress = new JSONObject();
		JSONObject card = new JSONObject();

		jsonRequest.put("action", PaymentConstants.FORTE_SALE_ACTION);
		jsonRequest.put("authorization_amount", authorizationAmount);

		/*
		 * if(StringUtils.isNotBlank(subTotalAmount)) {
		 * jsonRequest.put("subtotal_amount",
		 * Double.parseDouble(subTotalAmount)); }
		 */

		billingAddress.put("first_name", firstName);
		billingAddress.put("last_name", lastName);

		jsonRequest.put("billing_address", billingAddress);

		card.put("card_type", cardType);
		card.put("name_on_card", nameOnCard);
		card.put("account_number", cardNumber);
		card.put("expire_month", expiryMonth);
		card.put("expire_year", expiryYear);
		card.put("card_verification_value", cardCVV);
		jsonRequest.put("card", card);

		ResponseEntity<HashMap> response = null;
		HashMap<String, String> result = null;

		try {

			validateTransactionData(transaction);

			HttpEntity<String> entity = new HttpEntity<String>(jsonRequest.toString(), headers);

			// Call forte for making card payment
			response = restTemplate.exchange(getForteTransactionsUrl(locationId), HttpMethod.POST, entity,
					HashMap.class);

			// Decode the response
			if (response != null && response.getBody() != null) {
				result = response.getBody();
				logger.info("Forte Result : " + result);
				// Create the transaction object
				if (result.containsKey("transaction_id") && StringUtils.isNotBlank(result.get("transaction_id"))) {
					transaction.setForteTransactionId(result.get("transaction_id"));
				}

				if (result.containsKey("masked_account_number")
						&& StringUtils.isNotBlank(result.get("masked_account_number"))) {
					transaction.setMaskedCardNumber(result.get("masked_account_number"));
				}

				// Save the response in the database
				jsonObject.put("result", result);
			} else if (response == null) {
				jsonObject.put("error", "Error while posting the payment, something went terribly wrong");
			} else {
				jsonObject.put("error", "Error while posting the payment, please try again later");
				jsonObject.put("result",
						response.getBody() != null ? response.getBody() : response.getStatusCode().getReasonPhrase());
			}

			transaction.setPaymentMode(PaymentConstants.PAYMENT_MODE_CARD);
			if (transaction.getResponseType().equals("D")) {
				transaction.setPaymentStatus(PaymentConstants.FORTE_PAYMENT_STATUS_DECLINED);
			} else {
				transaction.setPaymentStatus(PaymentConstants.FORTE_PAYMENT_STATUS_PENDING);
			}
			paymentsDao.saveTransaction(transaction);

		} catch (HttpClientErrorException e) {
			logger.error("Error while posting the card payment. Response : " + response, e);
			throw new PaymentsException("Error", "Error while posting the payment, please try again later");
		} catch (PaymentsException e) {
			logger.info(e);
			throw e;
		} catch (Throwable t) {
			logger.error("Error while making the card payment", t);
			throw new PaymentsException("Fatal", t);
		}
		return transaction;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Transaction makeECheckPayment(Transaction transaction) throws Throwable {

		if (transaction == null)
			throw new PaymentsException("Error", "No information found for making payment");

		JSONObject jsonObject = new JSONObject();

		String locationId = transaction.getLocationId();

		String accountName = transaction.getAccountName();
		String accountNumber = transaction.getAccountNumber();
		String routingNumber = transaction.getRoutingNumber();
		String accountType = transaction.getAccountType();
		double authorizationAmount = transaction.getAuthorizationAmount();
		String firstName = transaction.getFirstName();
		String lastName = transaction.getLastName();
		String customerToken = transaction.getCustomerToken();
		String standardEntityClassCode = transaction.getStandardEntityClassCode();
		String companyName = transaction.getCompanyName();
		String phoneNumber = transaction.getPhoneNumber();
		String email = transaction.getEmail();
		String physicalStreet1 = transaction.getPhysicalAddress1();
		String physicalStreet2 = transaction.getPhysicalAddress2();
		String locality = transaction.getPhysicalCity();
		String region = transaction.getPhysicalState();
		String postalCode = transaction.getPhysicalZipCode();

		if (StringUtils.isEmpty(locationId)) {
			throw new PaymentsException("Error", "Invalid request, location id is missing. Cannot make payment.");
		}

		JSONObject jsonRequest = new JSONObject();
		JSONObject billingAddress = new JSONObject();
		JSONObject physicalAddress = new JSONObject();
		JSONObject eCheckData = new JSONObject();

		jsonRequest.put("action", PaymentConstants.FORTE_SALE_ACTION);
		jsonRequest.put("authorization_amount", authorizationAmount);

		billingAddress.put("customer_token", customerToken);
		billingAddress.put("first_name", firstName);
		billingAddress.put("last_name", lastName);
		billingAddress.put("company_name", companyName);
		billingAddress.put("phone", phoneNumber);
		billingAddress.put("email", email);
		physicalAddress.put("street_line1", physicalStreet1);
		physicalAddress.put("street_line2", physicalStreet2);
		physicalAddress.put("locality", locality);
		physicalAddress.put("region", region);
		physicalAddress.put("postal_code", postalCode);

		billingAddress.put("physical_address", physicalAddress);
		jsonRequest.put("billing_address", billingAddress);

		eCheckData.put("sec_code", standardEntityClassCode);
		eCheckData.put("account_type", accountType);
		eCheckData.put("routing_number", routingNumber);
		eCheckData.put("account_number", accountNumber);
		eCheckData.put("account_holder", accountName);

		jsonRequest.put("echeck", eCheckData);

		HttpEntity<?> entity = new HttpEntity<String>(jsonRequest.toString(), headers);
		ResponseEntity<HashMap> response = null;
		HashMap<String, Object> result = null;
		try {
			validateTransactionData(transaction);

			// Call forte for making echeck payment
			response = restTemplate.exchange(getForteTransactionsUrl(locationId), HttpMethod.POST, entity,
					HashMap.class);
			// Decode the response
			if (response != null && response.getBody() != null) {
				result = response.getBody();
				logger.info("Forte Result : " + result);
				// Create the transaction object
				if (result.containsKey("transaction_id")
						&& StringUtils.isNotBlank((String) result.get("transaction_id"))) {
					transaction.setForteTransactionId((String) result.get("transaction_id"));
				}

				if (result.containsKey("response") && result.get("response") != null) {
					Map<String, Object> forteResponse = (HashMap<String, Object>) result.get("response");
					if (forteResponse.containsKey("response_type")
							&& StringUtils.isNotBlank((String) forteResponse.get("response_type"))) {
						transaction.setResponseType((String) forteResponse.get("response_type"));
					}

					if (forteResponse.containsKey("response_code")
							&& StringUtils.isNotBlank((String) forteResponse.get("response_code"))) {
						transaction.setResponseCode((String) forteResponse.get("response_code"));
					}

					if (forteResponse.containsKey("response_desc")
							&& StringUtils.isNotBlank((String) forteResponse.get("response_desc"))) {
						transaction.setResponseDescription((String) forteResponse.get("response_desc"));
					}

					if (forteResponse.containsKey("authorization_code")
							&& StringUtils.isNotBlank((String) forteResponse.get("authorization_code"))) {
						transaction.setAuthorizationCode((String) forteResponse.get("authorization_code"));
					}

					if (forteResponse.containsKey("preauth_result")
							&& StringUtils.isNotBlank((String) forteResponse.get("preauth_result"))) {
						transaction.setPreAuthResult((String) forteResponse.get("preauth_result"));
					}

					if (forteResponse.containsKey("preauth_desc")
							&& StringUtils.isNotBlank((String) forteResponse.get("preauth_desc"))) {
						transaction.setPreAuthDescription((String) forteResponse.get("preauth_desc"));
					}
				}

				if (result.containsKey("echeck") && result.get("echeck") != null) {
					Map<String, Object> echeckMap = (HashMap<String, Object>) result.get("echeck");
					if (echeckMap.containsKey("masked_account_number")
							&& StringUtils.isNotBlank((String) echeckMap.get("masked_account_number"))) {
						transaction.setMaskedAccountNumber((String) echeckMap.get("masked_account_number"));
					}

					if (echeckMap.containsKey("last_4_account_number")
							&& StringUtils.isNotBlank((String) echeckMap.get("last_4_account_number"))) {
						transaction.setLast4AccountNumber((String) echeckMap.get("last_4_account_number"));
					}
				}

				// Save the response in the database
				jsonObject.put("result", result);
			} else if (response == null) {
				throw new PaymentsException("Error", "Error while posting the payment, something went terribly wrong");
			} else {
				jsonObject.put("error", "Error while posting the payment, please try again later");
				throw new PaymentsException("Error", "Error while posting the payment, please try again later");
			}
			transaction.setPaymentMode(PaymentConstants.PAYMENT_MODE_CARD);
			if (transaction.getResponseType().equals("D")) {
				transaction.setPaymentStatus(PaymentConstants.FORTE_PAYMENT_STATUS_DECLINED);
			} else {
				transaction.setPaymentStatus(PaymentConstants.FORTE_PAYMENT_STATUS_PENDING);
			}
			paymentsDao.saveTransaction(transaction);
		} catch (RestClientException e) {
			logger.info("Error wile making echeck payment", e);
			throw new PaymentsException("Error", e);
		} catch (PaymentsException e) {
			logger.info(e);
			throw e;
		} catch (Throwable t) {
			logger.error("Error while making the echeck payment", t);
			throw new PaymentsException("Fatal", t);
		}
		return transaction;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Application submitMerchantApplication(Application application) throws Throwable {

		try {
			validateApplicationRequest(application);

			// Generate unique risk session id
			application.setRiskSessionId(generateUniqueRiskSessionId());

			ObjectMapper objectMapper = new ObjectMapper();
			SimpleModule simpleModule = new SimpleModule("application");
			simpleModule.addSerializer(new ApplicantOrganizationSerializer(ApplicantOrganization.class));
			simpleModule.addSerializer(new AccountOwnerSerializer(AccountOwner.class));
			simpleModule.addSerializer(new ApplicationSerializer(Application.class));
			objectMapper.registerModule(simpleModule);

			String requestString = objectMapper.writeValueAsString(application);
			HttpEntity<String> entity = new HttpEntity<String>(requestString, headers);
			HashMap<String, Object> result = null;
			ResponseEntity<Object> response = restTemplate.exchange(getForteApplicationsUrl(), HttpMethod.POST, entity,
					Object.class);
			if (response != null && response.getBody() != null) {
				result = (HashMap<String, Object>) response.getBody();
				logger.info(result);
				if (result.containsKey("application_id")
						&& StringUtils.isNotBlank((String) result.get("application_id"))) {
					application.setForteApplicationId((String) result.get("application_id"));
				}

				/*
				 * if(result.containsKey("status") &&
				 * StringUtils.isNotBlank((String)result.get("status"))) {
				 * String status = (String) result.get("status");
				 * if(StringUtils.equalsIgnoreCase(PaymentConstants.
				 * FORTE_PAYMENT_STATUS_APPROVED, status)) {
				 * application.setStatus(PaymentConstants.
				 * FORTE_PAYMENT_STATUS_APPROVED); } else {
				 */
				application.setStatus(PaymentConstants.FORTE_PAYMENT_STATUS_PENDING);
				/*
				 * } }
				 */

				if (result.containsKey("links")) {
					Map<String, Object> linksMap = new HashMap<String, Object>();
					linksMap = (HashMap<String, Object>) result.get("links");

					if (linksMap.containsKey("documents")
							&& StringUtils.isNotBlank((String) linksMap.get("documents"))) {
						application.setDocumentsLink((String) linksMap.get("documents"));
					}

					if (linksMap.containsKey("self") && StringUtils.isNotBlank((String) linksMap.get("self"))) {
						application.setApplicationLink((String) linksMap.get("self"));
					}
				}

				application.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
			}
			paymentsDao.saveMerchantApplication(application);
		} catch (JsonProcessingException e) {
			logger.fatal("Error while processing the data", e);
			throw new PaymentsException("Fatal",
					"Cannot submit the application. Error while processing the data, invalid format");
		} catch (RestClientException e) {
			logger.info("Error wile making echeck payment", e);
			throw new PaymentsException("Error", e);
		} catch (Throwable t) {
			logger.fatal("Error while processing the data", t);
			throw new PaymentsException("Fatal", t);
		}
		return application;
	}

	private void validateApplicationRequest(Application application) throws Throwable {

		if (StringUtils.isBlank(application.getUserId())) {
			throw new PaymentsException("Error", "Entered by user id field is missing, cannot create the application");
		} else if (paymentsDao.findManagerByUserId(application.getUserId()) == null) {
			throw new PaymentsException("Error", "Invalid entered by user id, cannot create the application");
		}
	}

	public Transaction cancelPayment(Transaction transaction) throws Throwable {
		logger.info("Voiding transaction data");
		Transaction transactionFromDB = null;

		if (StringUtils.isBlank(transaction.getTransactionId())
				&& StringUtils.isBlank(transaction.getForteTransactionId())) {
			throw new PaymentsException("Error", "Cannot void the transaction, transaction id missing");
		} else {
			if (StringUtils.isNotBlank(transaction.getTransactionId())) {
				transactionFromDB = paymentsDao.getTransactionForTransactionId(transaction.getTransactionId());
			} else if (StringUtils.isNotBlank(transaction.getForteTransactionId())) {
				transactionFromDB = paymentsDao
						.getTransactionForForteTransactionId(transaction.getForteTransactionId());
			}
		}

		if (transactionFromDB == null)
			throw new PaymentsException("Error", "Invalid transaction id, cannot cancel the payment");

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("action", PaymentConstants.FORTE_VOID_ACTION);
		// jsonRequest.put("location_id",
		// PaymentsUtility.getFormattedLocationId(transactionFromDB.getLocationId()));
		jsonRequest.put("authorization_code", transactionFromDB.getAuthorizationCode());

		HttpEntity<?> entity = new HttpEntity<String>(jsonRequest.toString(), headers);
		ResponseEntity<HashMap> response = null;
		HashMap<String, Object> result = null;

		try {
			// Call forte for making echeck payment
			logger.info(getForteTransactionsUrlForUpdate(transactionFromDB.getLocationId(),
					transactionFromDB.getForteTransactionId()));
			response = restTemplate.exchange(getForteTransactionsUrlForUpdate(transactionFromDB.getLocationId(),
					transactionFromDB.getForteTransactionId()), HttpMethod.PUT, entity, HashMap.class);
			// Decode the response
			if (response != null && response.getBody() != null) {
				result = (HashMap<String, Object>) response.getBody();
				logger.info(result);
			} else if (response == null) {
				throw new PaymentsException("Error",
						"Error while cancelling the payment, something went terribly wrong");
			} else {
				jsonObject.put("error", "Error while cancelling the payment, please try again later");
				throw new PaymentsException("Error", "Error while cancelling the payment, please try again later");
			}
			transaction.setTransactionId(transactionFromDB.getTransactionId());
			transaction.setForteTransactionId(transactionFromDB.getForteTransactionId());
			transaction.setPaymentStatus(PaymentConstants.FORTE_PAYMENT_STATUS_CANCELLED);
			paymentsDao.updateTransactionStatus(transaction);
		} catch (RestClientException e) {
			logger.info("Error wile cancelling the payment", e);
			throw new PaymentsException("Error", e);
		} catch (PaymentsException e) {
			logger.info(e);
			throw e;
		} catch (Throwable t) {
			logger.error("Error while cancelling the payment", t);
			throw new PaymentsException("Fatal", t);
		}

		return transaction;
	}

	private void validateTransactionData(Transaction transaction) throws PaymentsException {
		logger.info("Validating transaction data");

		validateProperty(transaction);

		if (StringUtils.isBlank(transaction.getTransactionType())) {
			throw new PaymentsException("Error",
					"Transaction type (Income/Expense) is missing, cannot process the payment");
		} else if (StringUtils.isBlank(transaction.getTransactionCode())) {
			throw new PaymentsException("Error", "Transaction code is missing, cannot process the payment");
		} else if (transaction.getAuthorizationAmount() <= 0) {
			throw new PaymentsException("Error", "Invalid transaction amount, cannot process the payment");
		}

		if (StringUtils.isBlank(transaction.getPaidBy())) {
			throw new PaymentsException("Error", "Paid by information missing, cannot process the payment");
		} else {
			if (paymentsDao.findPaidByUserId(transaction.getPaidBy()) == null) {
				throw new PaymentsException("Error", "Paid by information invalid, cannot process the payment");
			}
		}

		if (transaction.getPaidOn() == null) {
			throw new PaymentsException("Error", "Transaction date missing, cannot process the payment");
		} else {
			try {
				Timestamp currentUTCDate = TrackMeUtils.getCurrentUTCTimeAsSqlTimestamp();
				Date paidOn = PaymentsUtility.isoDateFormat.parse(transaction.getPaidOn());
				if (currentUTCDate.before(paidOn)) {
					throw new PaymentsException("Error",
							"Invalid transaction date, Paid on should be on or before current date. Cannot process the payment");
				}
			} catch (Throwable t) {
				logger.error("Error while comparing dates", t);
				throw new PaymentsException("Error", t);
			}
		}

	}

	private void validateProperty(Transaction transaction) throws PaymentsException {
		logger.info("Validating property information");
		if (transaction == null)
			throw new PaymentsException("Error",
					"Error while processing payment, no transaction data present to make payment");

		if (StringUtils.isBlank(transaction.getPropertyId())) {
			throw new PaymentsException("Error", "Property information missing, cannot process the payment");
		} else if (StringUtils.isBlank(transaction.getOwnerId())) {
			throw new PaymentsException("Error", "Owner information missing, cannot process the payment");
		} else if (StringUtils.isBlank(transaction.getManagerId())) {
			throw new PaymentsException("Error", "Manager information missing, cannot process the payment");
		}

		try {
			Property property = paymentsDao.getPropertyData(transaction);
			if (property == null) {
				throw new PaymentsException("Error",
						"Property information not correct. Property, Owner and Manager information are not matching");
			}
		} catch (PaymentsException e) {
			throw e;
		} catch (Throwable t) {
			logger.error("Error while validating property", t);
		}
	}

	@Override
	public Map<String, String> getFortePaymentData() throws Throwable {
		Map<String, String> fortePaymentData = new HashMap<String, String>();

		List<Document> forteData = paymentsDao.getFortePaymentData();
		for (Document document : forteData) {
			if (StringUtils.equals(PaymentConstants.DB_COLLECTION_ORG_ID,
					document.getString(PaymentConstants.DB_COLLECTION_CODE))) {
				fortePaymentData.put(PaymentConstants.FORTE_ORGANIZATION_ID_STRING,
						document.getString(PaymentConstants.DB_COLLECTION_VALUE));
			} else if (StringUtils.equals(PaymentConstants.DB_COLLECTION_AUTH_USER_ID,
					document.getString(PaymentConstants.DB_COLLECTION_CODE))) {
				fortePaymentData.put(PaymentConstants.FORTE_AUTH_USER_ID_STRING,
						document.getString(PaymentConstants.DB_COLLECTION_VALUE));
			} else if (StringUtils.equals(PaymentConstants.DB_COLLECTION_AUTH_PWD,
					document.getString(PaymentConstants.DB_COLLECTION_CODE))) {
				fortePaymentData.put(PaymentConstants.FORTE_AUTH_PASSWORD_STRING,
						document.getString(PaymentConstants.DB_COLLECTION_VALUE));
			} else if (StringUtils.equals(PaymentConstants.DB_COLLECTION_BASE_URL,
					document.getString(PaymentConstants.DB_COLLECTION_CODE))) {
				fortePaymentData.put(PaymentConstants.FORTE_BASE_URL_STRING,
						document.getString(PaymentConstants.DB_COLLECTION_VALUE));
			}
		}

		return fortePaymentData;
	}

	/**
	 * Method to generate unique risk session id
	 * 
	 * @return
	 * @throws Throwable
	 */
	private String generateUniqueRiskSessionId() throws Throwable {
		String uniqueRiskSessionId = null;
		do {
			uniqueRiskSessionId = PaymentsUtility.createUniqueValue(PaymentConstants.RISK_SESSION_ID_LENGTH);
		} while (paymentsDao.getMerchantApplicationForRiskSessionId(uniqueRiskSessionId) != null);
		return uniqueRiskSessionId;
	}

	private String getOrganizationId() {
		if (StringUtils.isBlank(organizationId)) {
			init();
		}
		return organizationId;
	}

	private String getForteFormattedOrganizationId() {
		if (StringUtils.isBlank(forteFormattedOrganizationId)) {
			init();
		}
		return forteFormattedOrganizationId;
	}

	private String getForteBaseUrl() {
		if (StringUtils.isBlank(forteBaseUrl)) {
			init();
		}
		return forteBaseUrl;
	}

	private String getForteUserId() {
		if (StringUtils.isBlank(forteUserId)) {
			init();
		}
		return forteUserId;
	}

	private String getFortePassword() {
		if (StringUtils.isBlank(fortePassword)) {
			init();
		}
		return fortePassword;
	}

	private String getForteUrlWithOrgId() {
		if (StringUtils.isBlank(forteUrlWithOrgId)) {
			init();
		}
		return forteUrlWithOrgId;
	}

	private String getForteUrlWithOrgIdAndLocation(String locationId) {
		locationId = PaymentsUtility.getFormattedLocationId(locationId);
		return new StringBuilder(getForteUrlWithOrgId()).append("/locations/").append(locationId).toString();
	}

	// Methods for Getting Forte URLs - Start

	/**
	 * Get Forte Transaction URL from the base URL
	 * 
	 * @param locationId
	 * @return
	 */
	private String getForteTransactionsUrl(String locationId) {
		return new StringBuilder(getForteUrlWithOrgIdAndLocation(locationId)).append("/transactions").toString();
	}

	/**
	 * Get Forte Customer URL from the base URL
	 * 
	 * @param locationId
	 * @return
	 */
	private String getForteCustomerUrl(String locationId) {
		return new StringBuilder(getForteUrlWithOrgIdAndLocation(locationId)).append("/customers").toString();
	}

	/**
	 * Get Forte Merchant Application URL from the base URL
	 * 
	 * @return
	 */
	private String getForteApplicationsUrl() {
		return new StringBuilder(getForteUrlWithOrgId()).append("/applications").toString();
	}

	private String getForteTransactionsUrlForUpdate(String locationId, String forteTransactionId) {
		return new StringBuilder(getForteTransactionsUrl(locationId)).append("/").append(forteTransactionId).toString();
	}

	// Methods for Getting Forte URLs - End

}