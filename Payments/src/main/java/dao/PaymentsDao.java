package com.selsoft.trackme.payments.dao;

import java.util.List;

import org.bson.Document;

import com.selsoft.trackme.exception.PaymentsException;
import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;

public interface PaymentsDao {

	public void saveTransaction(Transaction transaction) throws PaymentsException;

	public Property getPropertyData(Transaction transaction) throws PaymentsException;

	public void saveApplication(Application application) throws PaymentsException;

	public List<Document> getFortePaymentData() throws PaymentsException;

	public User findPaidByUserId(String userId) throws PaymentsException;

	public Transaction getTransactionForForteTransactionId(String forteTransactionId) throws PaymentsException;

	public Transaction getTransactionForTransactionId(String transactionId) throws PaymentsException;

	public void updateTransactionStatus(Transaction transaction) throws PaymentsException;

	public void saveMerchantApplication(Application application) throws PaymentsException;

	public Application getMerchantApplicationForRiskSessionId(String riskSessionId) throws Throwable;

	public User findManagerByUserId(String managerId) throws Throwable;

}