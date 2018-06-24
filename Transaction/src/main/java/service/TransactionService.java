package com.selsoft.transaction.service;

import java.util.List;
import java.util.Map;

import com.selsoft.trackme.models.Transaction;

public interface TransactionService {

	public void saveTransaction(Transaction transaction) throws Throwable;

	public List<Transaction> getAllTransactionsForProperty(Transaction transaction) throws Throwable;

	public Map<String, List<Transaction>> getTransactionReport(String managerId, String reportType, int year,
			String duration) throws Throwable;

	public Map<String, List<Transaction>> getTransactionReportForOwner(String ownerId, String reportType, int year,
			String duration) throws Throwable;

	public Map<String, List<Transaction>> getTransactionReportForTenant(String tenantId, String reportType, int year,
			String duration) throws Throwable;

	public Transaction updateTransaction(Transaction transaction) throws Throwable;

}