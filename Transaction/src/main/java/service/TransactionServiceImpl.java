package com.selsoft.transaction.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.exception.TransactionException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.utils.TrackMeUtils;
import com.selsoft.transaction.dao.TransactionDao;


@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
	
	@Autowired
	private TransactionDao  transactionDAO;
	final String[] MONTHS_IN_YEAR = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
	private static final String[] QUARTERS_IN_YEAR = {"Q1", "Q2", "Q3", "Q4"};
	private static final String[] LEAP_YEAR_MONTH_DAYS = {"31", "29", "31", "30", "31", "30", "31", "31", "30", "31", "30", "31"};
	private static final String[] NON_LEAP_YEAR_MONTH_DAYS = {"31", "28", "31", "30", "31", "30", "31", "31", "30", "31", "30", "31"};
	private static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static final Logger logger = Logger.getLogger(TransactionServiceImpl.class);

	@Override
	public void saveTransaction(Transaction transaction) throws Throwable {
		
		try {
			
			validateTransactionData(transaction);
			transactionDAO.saveTransaction(transaction);
			
		} catch(TransactionException e) {
			throw e;
		} catch(Throwable t) {
			logger.error("Error while saving the transaction", t);
		}
		
	}

	public void validateTransactionData(Transaction transaction) throws TransactionException {
		logger.info("Validating transaction data");

		validateProperty(transaction);
		
		if(StringUtils.isBlank(transaction.getTransactionType())) { 
			throw new TransactionException("Error", "Transaction type (Income/Expense) is missing, cannot add transaction");
		} else if(StringUtils.isBlank(transaction.getTransactionCode())) {
			throw new TransactionException("Error", "Transaction code is missing, cannot add transaction");
		} else if(transaction.getAmount() <= 0) {
			throw new TransactionException("Error", "Invalid transaction amount, cannot add transaction");
		} 
		
		if(transaction.getPaidOn() == null) {
			throw new TransactionException("Error", "Transaction date missing, cannot add transaction");
		} else {
			try {
				Timestamp currentUTCDate = TrackMeUtils.getCurrentUTCTimeAsSqlTimestamp();
				Date paidOn = TrackMeConstants.isoDateFormat.parse(transaction.getPaidOn());
				if(currentUTCDate.before(paidOn)) {
					throw new TransactionException("Error", "Invalid transaction date, Paid on should be on or before current date. Cannot add transaction");
				}
			} catch(Throwable t) {
				logger.error("Error while comparing dates", t);
				throw new TransactionException("Error", t);
			}
		}
				
	}
	
	private void validateProperty(Transaction transaction) throws TransactionException {
		if(transaction == null) throw new TransactionException("Error", "Error while saving transaction, no transaction data present to save");
		
		if(StringUtils.isBlank(transaction.getPropertyId())) {
			throw new TransactionException("Error", "Property information missing, cannot add transaction");
		} else if(StringUtils.isBlank(transaction.getOwnerId())) {
			throw new TransactionException("Error", "Owner information missing, cannot add transaction");
		} else if(StringUtils.isBlank(transaction.getManagerId())) {
			throw new TransactionException("Error", "Manager information missing, cannot add transaction");
		}
		
		try {
			Property property = transactionDAO.getPropertyData(transaction);
			if(property == null) {
				throw new TransactionException("Error", "Property information not correct. Property, Owner and Manager information are not matching");
			}
		} catch(TransactionException e) {
			throw e;
		} catch(Throwable t) {
			logger.error("Error while validating property", t);
		}
	}

	@Override
	public List<Transaction> getAllTransactionsForProperty(Transaction transaction) throws Throwable {
		List<Transaction> transactions = null;
		
		try {
			transactions = transactionDAO.getAllTransactionsForProperty(transaction);
		} catch(TransactionException e) {
			throw e;
		} catch(Throwable t) {
			logger.error("Error while getting all transctions for property : " + transaction.getPropertyId(), t);
			throw new TransactionException("Error", t);
		}
		
		return transactions;
	}
	
	@Override
	public Map<String, List<Transaction>> getTransactionReport(String managerId, String reportType, int year, String duration) throws Throwable {
		String fromDate = null, toDate = null, paidOn = null;
		Map<String, List<Transaction>> reportData = new HashMap<String, List<Transaction>>();
		List<Transaction> transactions = null;
		
		reportType = StringUtils.upperCase(StringUtils.trimToEmpty(reportType));
		duration = StringUtils.upperCase(StringUtils.trimToEmpty(duration));
		managerId = StringUtils.trimToEmpty(managerId);
		
		if(StringUtils.isBlank(managerId)) {
			throw new TransactionException("Error", "Manager information missing, cannot retrieve the data");
		} else if (year <= 0) {
			throw new TransactionException("Error", "Invalid year " + year + ", cannot generate report");
		} else if(StringUtils.isBlank(reportType)) {
			throw new TransactionException("Error", "Invalid reportType " + reportType + ", cannot generate report");
		}

		Map<String, String> fromToDates = getFromToDates(reportType, year, duration);
		
		fromDate = fromToDates.get("fromDate");
		toDate = fromToDates.get("toDate");
		
		try {
			
			transactions = transactionDAO.getTransactionReport(managerId, fromDate, toDate);
		
			reportData = splitMonthwiseTransactions(reportType, duration, paidOn, transactions);
			
		} catch(ParseException p) {
			logger.error("Error while parsing the paid on date " + paidOn, p);
			throw new TransactionException("Error", "Error while parsing the paid on date " + paidOn);
		} catch(ArrayIndexOutOfBoundsException a) {
			logger.error("Error while getting the month for the paid on date " + paidOn, a);
			throw new TransactionException("Error", "Error while getting the month for the paid on date " + paidOn);
		} catch(Throwable t) {
			logger.fatal("Error while splitting the transactions for report : reportType " + reportType 
					+ ", year : " + year + ", duration" + duration + ", managerId" + managerId, t);
			throw new TransactionException("Error", "Error while splitting the transactions for report");
		}
		
		return reportData;
	}
	
	@Override
	public Map<String, List<Transaction>> getTransactionReportForOwner(String ownerId, String reportType, int year, String duration) throws Throwable {
		String fromDate = null, toDate = null, paidOn = null;
		Map<String, List<Transaction>> reportData = null;
		List<Transaction> transactions = null;
		
		reportType = StringUtils.upperCase(StringUtils.trimToEmpty(reportType));
		duration = StringUtils.upperCase(StringUtils.trimToEmpty(duration));
		ownerId = StringUtils.trimToEmpty(ownerId);
		
		if(StringUtils.isBlank(ownerId)) {
			throw new TransactionException("Error", "Owner information missing, cannot retrieve the data");
		} else if (year <= 0) {
			throw new TransactionException("Error", "Invalid year " + year + ", cannot generate report");
		} else if(StringUtils.isBlank(reportType)) {
			throw new TransactionException("Error", "Invalid reportType " + reportType + ", cannot generate report");
		}

		Map<String, String> fromToDates = getFromToDates(reportType, year, duration);
		
		fromDate = fromToDates.get("fromDate");
		toDate = fromToDates.get("toDate");
		
		try {
			
			transactions = transactionDAO.getTransactionReportForOwner(ownerId, fromDate, toDate);
		
			reportData = splitMonthwiseTransactions(reportType, duration, paidOn, transactions);
			
		} catch(ParseException p) {
			logger.error("Error while parsing the paid on date " + paidOn, p);
			throw new TransactionException("Error", "Error while parsing the paid on date " + paidOn);
		} catch(ArrayIndexOutOfBoundsException a) {
			logger.error("Error while getting the month for the paid on date " + paidOn, a);
			throw new TransactionException("Error", "Error while getting the month for the paid on date " + paidOn);
		} catch(Throwable t) {
			logger.fatal("Error while splitting the transactions for report : reportType " + reportType 
					+ ", year : " + year + ", duration" + duration + ", managerId" + ownerId, t);
			throw new TransactionException("Error", "Error while splitting the transactions for report");
		}
		
		return reportData;
	}
	
	@Override
	public Map<String, List<Transaction>> getTransactionReportForTenant(String tenantId, String reportType, int year, String duration) throws Throwable {
		String fromDate = null, toDate = null, paidOn = null;
		Map<String, List<Transaction>> reportData = null;
		List<Transaction> transactions = null;
		
		reportType = StringUtils.upperCase(StringUtils.trimToEmpty(reportType));
		duration = StringUtils.upperCase(StringUtils.trimToEmpty(duration));
		tenantId = StringUtils.trimToEmpty(tenantId);
		
		if(StringUtils.isBlank(tenantId)) {
			throw new TransactionException("Error", "Tenant information missing, cannot retrieve the data");
		} else if (year <= 0) {
			throw new TransactionException("Error", "Invalid year " + year + ", cannot generate report");
		} else if(StringUtils.isBlank(reportType)) {
			throw new TransactionException("Error", "Invalid reportType " + reportType + ", cannot generate report");
		}

		Map<String, String> fromToDates = getFromToDates(reportType, year, duration);
		
		fromDate = fromToDates.get("fromDate");
		toDate = fromToDates.get("toDate");
		
		try {
			
			transactions = transactionDAO.getTransactionReportForTenant(tenantId, fromDate, toDate);
		
			reportData = splitMonthwiseTransactions(reportType, duration, paidOn, transactions);
			
		} catch(ParseException p) {
			logger.error("Error while parsing the paid on date " + paidOn, p);
			throw new TransactionException("Error", "Error while parsing the paid on date " + paidOn);
		} catch(ArrayIndexOutOfBoundsException a) {
			logger.error("Error while getting the month for the paid on date " + paidOn, a);
			throw new TransactionException("Error", "Error while getting the month for the paid on date " + paidOn);
		} catch(Throwable t) {
			logger.fatal("Error while splitting the transactions for report : reportType " + reportType 
					+ ", year : " + year + ", duration" + duration + ", managerId" + tenantId, t);
			throw new TransactionException("Error", "Error while splitting the transactions for report");
		}
		
		return reportData;
	}

	private Map<String, List<Transaction>> splitMonthwiseTransactions(String reportType, String duration, String paidOn,
			List<Transaction> transactions) throws ParseException {
		String month;
		List<Transaction> transactionsPerMonth;
		Date paidDate;
		Calendar date;
		Map<String, List<Transaction>> reportData = new HashMap<String, List<Transaction>>();
		//Split transactions month wise
		for(Transaction transaction : transactions) {
			paidOn = transaction.getPaidOn();
			paidDate = null;
			month = null;
			transactionsPerMonth = null;
			if(StringUtils.isNotBlank(paidOn)) {
				paidDate = isoDateFormat.parse(paidOn);
				//paidDate.getMonth();
				date = Calendar.getInstance();
				date.setTime(paidDate);
				month = MONTHS_IN_YEAR[date.get(Calendar.MONTH)];
				transactionsPerMonth = reportData.get(month);
				if(transactionsPerMonth == null) {
					transactionsPerMonth = new ArrayList<Transaction>();
					transactionsPerMonth.add(transaction);
					reportData.put(month, transactionsPerMonth);
				} else {
					transactionsPerMonth.add(transaction);
				}
			} else {
				transactionsPerMonth = reportData.get("X");
				if(transactionsPerMonth == null) {
					reportData.put("X", transactionsPerMonth);
				} else {
					transactionsPerMonth.add(transaction);
				}
			}
		}
		
		addEmptyMonths(reportType, duration, reportData);
		return reportData;
	}
	
	private Map<String, String> getFromToDates(String reportType, int year, String duration) throws TransactionException {
		String fromDate = null;
		String toDate = null;
		
		Map<String, String> fromToDates = new HashMap<String, String>();
		
		if (StringUtils.equals("Y", reportType)) { // Yearly report

			fromDate = year + "-01-01";
			toDate = year + "-12-31";

		} else if (StringUtils.equals("Q", reportType)) { // Quarterly report
			
			if (year == 0 || StringUtils.isBlank(duration) || !ArrayUtils.contains(QUARTERS_IN_YEAR, duration)) {
				throw new TransactionException("Error", "Invalid quarter " + duration + ", cannot generate report");
			}
			
			if (StringUtils.equals("Q1", duration)) {
				fromDate = year + "-01-01";
				toDate = year + "-03-31";
			} else if (StringUtils.equals("Q2", duration)) {
				fromDate = year + "-04-01";
				toDate = year + "-06-30";
			} else if (StringUtils.equals("Q3", duration)) {
				fromDate = year + "-07-01";
				toDate = year + "-09-30";
			} else if (StringUtils.equals("Q4", duration)) {
				fromDate = year + "-10-01";
				toDate = year + "-12-31";
			}

		} else if (StringUtils.equals("M", reportType)) { // Monthly report
			
			if (year == 0 || StringUtils.isBlank(duration) || !ArrayUtils.contains(MONTHS_IN_YEAR, duration)) {
				throw new TransactionException("Error", "Invalid month " + duration + ", cannot generate report");
			}

			int selectedMonth = ArrayUtils.indexOf(MONTHS_IN_YEAR, duration);
			
			//Check for leap year
			int lastDayOfMonth = (year % 4 == 0) ? NumberUtils.toInt(LEAP_YEAR_MONTH_DAYS[selectedMonth]) : NumberUtils.toInt(NON_LEAP_YEAR_MONTH_DAYS[selectedMonth]);
			
			String yearAndMonth = year + "-" + StringUtils.leftPad(String.valueOf(selectedMonth + 1), 2, "0");
			
			fromDate = yearAndMonth + "-01";
			toDate = yearAndMonth + "-" + StringUtils.leftPad(String.valueOf(lastDayOfMonth), 2, "0");

		}
		
		fromToDates.put("fromDate", fromDate);
		fromToDates.put("toDate", toDate);
		return fromToDates;
	}
	
	private void addEmptyMonths(String reportType, String duration, Map<String, List<Transaction>> reportData) {
		final String[] QUARTER1 = {"JAN", "FEB", "MAR"};
		final String[] QUARTER2 = {"APR", "MAY", "JUN"};
		final String[] QUARTER3 = {"JUL", "AUG", "SEP"};
		final String[] QUARTER4 = {"OCT", "NOV", "DEC"};
		//Empty the months if no data available for yearly and quarterly
		if (StringUtils.equals("Y", reportType)) { // Yearly report
			fillEmptyMonths(reportData, MONTHS_IN_YEAR);
		} else if (StringUtils.equals("Q", reportType)) {
			if (StringUtils.equals("Q1", duration)) {
				fillEmptyMonths(reportData, QUARTER1);
			} else if (StringUtils.equals("Q2", duration)) {
				fillEmptyMonths(reportData, QUARTER2);
			} else if (StringUtils.equals("Q3", duration)) {
				fillEmptyMonths(reportData, QUARTER3);
			} else if (StringUtils.equals("Q4", duration)) {
				fillEmptyMonths(reportData, QUARTER4);
			}
		}
		
	}

	private void fillEmptyMonths(Map<String, List<Transaction>> reportData, String[] months) {
		List<Transaction> transactionsPerMonth = null;
		for(String monthOfYear : months) {
			transactionsPerMonth = reportData.get(monthOfYear);
			if(transactionsPerMonth == null) {
				reportData.put(monthOfYear, new ArrayList<Transaction>());
			}
		}
	}

	/*@Override
	public Map<String, List<Transaction>> getTransactionReportForTenant(String tenantId, String reportType, int year,
			String duration) throws Throwable {
		String fromDate = null, toDate = null, paidOn = null;
		Map<String, List<Transaction>> reportData = null;
		List<Transaction> transactions = null;
		
		reportType = StringUtils.upperCase(StringUtils.trimToEmpty(reportType));
		duration = StringUtils.upperCase(StringUtils.trimToEmpty(duration));
		tenantId = StringUtils.trimToEmpty(tenantId);
		
		if(StringUtils.isBlank(tenantId)) {
			throw new TransactionException("Error", "Tenant information missing, cannot retrieve the data");
		} else if (year <= 0) {
			throw new TransactionException("Error", "Invalid year " + year + ", cannot generate report");
		} else if(StringUtils.isBlank(reportType)) {
			throw new TransactionException("Error", "Invalid reportType " + reportType + ", cannot generate report");
		}

		Map<String, String> fromToDates = getFromToDates(reportType, year, duration);
		
		fromDate = fromToDates.get("fromDate");
		toDate = fromToDates.get("toDate");
		
		try {
			
			transactions = transactionDAO.getTransactionReportForTenant(tenantId, fromDate, toDate);
			reportData = splitMonthwiseTransactions(reportType, duration, paidOn, transactions);
			
		} catch(ParseException p) {
			logger.error("Error while parsing the paid on date " + paidOn, p);
			throw new TransactionException("Error", "Error while parsing the paid on date " + paidOn);
		} catch(ArrayIndexOutOfBoundsException a) {
			logger.error("Error while getting the month for the paid on date " + paidOn, a);
			throw new TransactionException("Error", "Error while getting the month for the paid on date " + paidOn);
		} catch(Throwable t) {
			logger.fatal("Error while splitting the transactions for report : reportType " + reportType 
					+ ", year : " + year + ", duration" + duration + ", managerId" + tenantId, t);
			throw new TransactionException("Error", "Error while splitting the transactions for report");
		}
		
		return reportData;
	}*/

	@Override
	public Transaction updateTransaction(Transaction transactionRequest) throws Throwable {
		Transaction transaction = null;
		String id = transactionRequest.getTransactionId();
		if (id != null) {
			transaction = transactionDAO.findById(id);
		}
		if (transaction == null) {
			logger.info("Transaction with id " + id + " not found");
			 throw new TransactionException("Error", "transaction id not found, please enter a valid transaction Id");
		}
		transaction.setPropertyId(transactionRequest.getPropertyId());
		transaction.setOwnerId(transactionRequest.getOwnerId());
		transaction.setManagerId(transactionRequest.getManagerId());
		transaction.setAmount(transactionRequest.getAmount());
		transaction.setTransactionType(transactionRequest.getTransactionType());
		transaction.setTransactionCode(transactionRequest.getTransactionCode());
		transaction.setPaidById(transactionRequest.getPaidById());
		transaction.setPaidOn(transactionRequest.getPaidOn());
		transaction.setEnteredOn(transactionRequest.getEnteredOn());
		return transactionDAO.updateTransaction(transaction);
	}
}