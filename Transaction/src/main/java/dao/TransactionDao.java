package com.selsoft.transaction.dao;

import java.util.List;

import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.Transaction;

public interface TransactionDao {

	public void saveTransaction(Transaction transaction) throws Throwable;

	public Property getPropertyData(Transaction transaction) throws Throwable;

	public List<Transaction> getAllTransactionsForProperty(Transaction transaction) throws Throwable;

	public List<Transaction> getTransactionReport(String managerId, String fromDate, String toDate) throws Throwable;

	public List<Transaction> getAllTransactionsForProperty(Property property) throws Throwable;

	public Lease getCurrentLeaseDataForProperty(Property property) throws Throwable;

	public Tenant getCurrentTenantForProperty(Lease lease) throws Throwable;

	public Transaction getTransactionByProperty(String propertyId) throws Throwable;

	// public void updateTransaction(Transaction transaction) throws Throwable;

	public List<Transaction> getTransactionReportForTenant(String tenantId, String fromDate, String toDate)
			throws Throwable;

	public Transaction updateTransaction(Transaction transaction) throws Throwable;

	public List<Transaction> getAllTransactionsForOwner(Owner owner) throws Throwable;

	public List<Transaction> getTransactionReportForOwner(String ownerId, String fromDate, String toDate)
			throws Throwable;

	public Transaction findById(String id) throws Throwable;

}
