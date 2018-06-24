package com.selsoft.trackme.payments.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.exception.PaymentsException;
import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.payments.service.PaymentsService;

@RestController
@RequestMapping("/payments")
public class PaymentsController {

	private static final Logger logger = Logger.getLogger(PaymentsController.class);

	@Autowired
	private PaymentsService paymentsService;

	@RequestMapping(value = "/getAllCustomers", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllCustomers(@RequestBody Map<String, Object> orgData) {
		if (orgData == null || orgData.isEmpty())
			return "{\"error\":\"Invalid request\"}";

		if (StringUtils.isEmpty((String) orgData.get("locationId")))
			return "{\"error\":\"Invalid request\"}";

		JSONObject jsonObject = new JSONObject();
		try {
			orgData = paymentsService.getAllCustomersFromForte(orgData);
			jsonObject.put("success", true);
		} catch (PaymentsException e) {
			logger.info("Error while submitting card payment to Forte", e);
			jsonObject.put("success", false);
			orgData.put("error", new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while submitting card payment to Forte", t);
			jsonObject.put("success", false);
			orgData.put("error", new Error(new PaymentsException("Fatal", t)));
		}
		jsonObject.put("customers", orgData);
		return jsonObject.toString();
	}

	@RequestMapping(value = "/payCard", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String payCard(@RequestBody Transaction transaction) {

		JSONObject jsonObject = new JSONObject();
		try {
			transaction = paymentsService.makeCardPayment(transaction);
			jsonObject.put("success", true);
		} catch (PaymentsException e) {
			logger.info("Error while submitting card payment to Forte", e);
			jsonObject.put("success", false);
			transaction.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while submitting card payment to Forte", t);
			jsonObject.put("success", false);
			transaction.addError(new Error(new PaymentsException("Fatal", t)));
		}
		jsonObject.put("transaction", transaction.toJSON());

		return jsonObject.toString();
	}

	@RequestMapping(value = "/payECheck", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String payECheck(@RequestBody Transaction transaction) {

		JSONObject jsonObject = new JSONObject();
		try {
			transaction = paymentsService.makeECheckPayment(transaction);
			jsonObject.put("success", true);
		} catch (PaymentsException e) {
			logger.info("Error while submitting echeck payment to Forte", e);
			jsonObject.put("success", false);
			transaction.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while submitting echeck payment to Forte", t);
			jsonObject.put("success", false);
			transaction.addError(new Error(new PaymentsException("Fatal", t)));
		}
		jsonObject.put("transaction", transaction.toJSON());

		return jsonObject.toString();
	}

	@RequestMapping(value = "/createMerchantApplication", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createMerchantApplication(@RequestBody Application application) {

		JSONObject jsonObject = new JSONObject();
		try {
			application = paymentsService.submitMerchantApplication(application);
			jsonObject.put("success", true);
		} catch (PaymentsException e) {
			logger.info("Error while submitting the merchant application", e);
			jsonObject.put("success", false);
			application.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while submitting the merchant application", t);
			jsonObject.put("success", false);
			application.addError(new Error(new PaymentsException("Fatal", t.getMessage())));
		}

		jsonObject.put("application", application.toJSON());

		return jsonObject.toString();
	}

	@RequestMapping(value = "/cancelPayment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String cancelPayment(@RequestBody Transaction transaction) {
		JSONObject jsonObject = new JSONObject();
		try {
			transaction = paymentsService.cancelPayment(transaction);
			jsonObject.put("success", true);
		} catch (PaymentsException e) {
			logger.info("Error while submitting echeck payment to Forte", e);
			jsonObject.put("success", false);
			transaction.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while submitting echeck payment to Forte", t);
			jsonObject.put("success", false);
			transaction.addError(new Error(new PaymentsException("Fatal", t)));
		}
		jsonObject.put("transaction", transaction.toJSON());

		return jsonObject.toString();
	}

}