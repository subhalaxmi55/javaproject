package com.selsoft.transaction.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.exception.PropertyException;
import com.selsoft.trackme.exception.TransactionException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.transaction.service.TransactionService;


@RestController
@RequestMapping(value = "/transaction")
public class TransactionController {

	private static final Logger logger = Logger.getLogger(TransactionController.class);
	// private static TransactionType transactionType = new TransactionType();

	@Autowired
	private TransactionService transactionService;

	@RequestMapping(value = "/saveTransaction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveTransaction(@RequestBody Transaction transaction) {

		logger.info("Data comes into TransactionController saveTransaction() for processing");
		
		JSONObject jsonObject = new JSONObject();
		
		try {
			transactionService.saveTransaction(transaction);
			jsonObject.put("success", true);
		} catch(TransactionException e) {
			transaction.addError(new Error(e));
			jsonObject.put("success", false);
		} catch(Throwable t) {
			logger.error("Error while saving transaction", t);
			transaction.addError(new Error(new TransactionException("Fatal", t)));
			jsonObject.put("success", false);
		}
		
		jsonObject.put("transaction", transaction.toJSON());
		
		return jsonObject.toString();

	}
	
	@RequestMapping(value="/getAllTransactionsForProperty", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllCurrentYearTransactionsForProperty(@RequestBody Transaction transaction) {
		JSONObject jsonObject = new JSONObject();
		List<Transaction> transactions = null;
		List<Error> errorList = new ArrayList<Error>();
		
		try {
			transactions = transactionService.getAllTransactionsForProperty(transaction);
			jsonObject.put("success", true);
			jsonObject.put("transactions", transactions);
		} catch(TransactionException e) {
			errorList.add(new Error(e));
			jsonObject.put("error", errorList);
			jsonObject.put("success", false);
		} catch(Throwable t) {
			errorList.add(new Error(new TransactionException("Fatal", t)));
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(new TransactionException("Fatal", t)));
		}
		
		return jsonObject.toString(); 
	}

	@RequestMapping(value = "/getTransactionReport", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTransactionReport(@RequestBody HashMap<String, String> dataHashMap) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		
		String managerId = null, reportType = null, duration = null;
		int year; 
		
		try {
			managerId = dataHashMap.get("managerId");
			reportType = dataHashMap.get("reportType");
			year = NumberUtils.toInt(dataHashMap.get("year"));
			duration = dataHashMap.get("duration");
			
			Map<String, List<Transaction>> reportData = transactionService.getTransactionReport(managerId, reportType, year, duration);
			jsonObject.put("success", true);
			jsonObject.put("transactions", reportData);
		} catch(TransactionException e) {
			errorList.add(new Error(e));
			jsonObject.put("success", false);
			jsonObject.put("errors", errorList);
		} catch(Throwable t) {
			logger.fatal("Error while getting the transaction report for report type : " + reportType, t);
			errorList.add(new Error(new TransactionException("Fatal", t)));
			jsonObject.put("success", false);
			jsonObject.put("error", errorList);
			
		} 
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/getTransactionReportForOwner", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTransactionReportForOwner(@RequestBody HashMap<String, String> dataHashMap) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		
		String ownerId = null, reportType = null, duration = null;
		int year; 
		
		try {
			ownerId = dataHashMap.get("ownerId");
			reportType = dataHashMap.get("reportType");
			year = NumberUtils.toInt(dataHashMap.get("year"));
			duration = dataHashMap.get("duration");
			
			Map<String, List<Transaction>> reportData = transactionService.getTransactionReportForOwner(ownerId, reportType, year, duration);
			jsonObject.put("success", true);
			jsonObject.put("transactions", reportData);
		} catch(TransactionException e) {
			errorList.add(new Error(e));
			jsonObject.put("success", false);
			jsonObject.put("errors", errorList);
		} catch(Throwable t) {
			logger.fatal("Error while getting the transaction report for report type : " + reportType, t);
			errorList.add(new Error(new TransactionException("Fatal", t)));
			jsonObject.put("success", false);
			jsonObject.put("error", errorList);
			
		} 
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/getTransactionReportForTenant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTransactionReportForTenant(@RequestBody HashMap<String, String> dataHashMap) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		
		String tenantId = null, reportType = null, duration = null;
		int year; 
		
		try {
			tenantId = dataHashMap.get("tenantId");
			reportType = dataHashMap.get("reportType");
			year = NumberUtils.toInt(dataHashMap.get("year"));
			duration = dataHashMap.get("duration");
			
			Map<String, List<Transaction>> reportData = transactionService.getTransactionReportForTenant(tenantId, reportType, year, duration);
			jsonObject.put("success", true);
			jsonObject.put("transactions", reportData);
		} catch(TransactionException e) {
			errorList.add(new Error(e));
			jsonObject.put("success", false);
			jsonObject.put("errors", errorList);
		} catch(Throwable t) {
			logger.fatal("Error while getting the transaction report for report type : " + reportType, t);
			errorList.add(new Error(new TransactionException("Fatal", t)));
			jsonObject.put("success", false);
			jsonObject.put("error", errorList);
			
		} 
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/updateTransaction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateTransaction(@RequestBody Transaction transaction) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating Property ");
		
		try {
			transactionService.updateTransaction(transaction);
			jsonObject.put("success", "true");
		} catch (PropertyException e) {
			jsonObject.put("success", false);
			transaction.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			transaction.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			transaction.addError(new Error("Fatal", t.toString()));
		}
		jsonObject.put("property", transaction.toJSON());
		return jsonObject.toString();
	}

}
