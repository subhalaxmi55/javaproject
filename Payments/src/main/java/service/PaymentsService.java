package com.selsoft.trackme.payments.service;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.models.Transaction;

public interface PaymentsService {

	public Map<String, Object> getAllCustomersFromForte(@RequestBody Map<String, Object> orgData) throws Throwable;

	public Map<String, String> getFortePaymentData() throws Throwable;

	public Transaction makeCardPayment(Transaction transaction) throws Throwable;

	public Transaction makeECheckPayment(Transaction transaction) throws Throwable;

	public Application submitMerchantApplication(Application application) throws Throwable;

	public Transaction cancelPayment(Transaction transaction) throws Throwable;

}