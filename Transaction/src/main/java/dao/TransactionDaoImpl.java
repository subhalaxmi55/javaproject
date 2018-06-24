package com.selsoft.transaction.dao;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.selsoft.trackme.exception.TransactionException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.utils.TrackMeUtils;

@Repository
public class TransactionDaoImpl implements TransactionDao {

	private static final Logger logger = Logger.getLogger(TransactionDaoImpl.class);

	@Autowired
	private MongoTemplate template;

	@Override
	public void saveTransaction(Transaction transaction) throws Throwable {
		logger.info("Saving transaction data");
		try {
			transaction.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
			template.insert(transaction);
		} catch (Throwable t) {
			logger.error("Error while getting the property data", t);
			throw new TransactionException("Fatal", t);
		}
	}

	@Override
	public Property getPropertyData(Transaction transaction) throws Throwable {
		Property property = null;
		try {
			property = template.findOne(
					new Query(Criteria.where("_id").is(transaction.getPropertyId()).and("ownerId")
							.is(transaction.getOwnerId()).and("managerId").is(transaction.getManagerId())),
					Property.class);
		} catch (Throwable t) {
			logger.error("Error while getting the property data", t);
			throw new TransactionException("Fatal", t);
		}
		return property;
	}

	@Override
	public List<Transaction> getAllTransactionsForProperty(Transaction transaction) throws Throwable {
		List<Transaction> transactions = null;

		try {
			transactions = template.find(new Query(Criteria.where("propertyId").is(transaction.getPropertyId())),
					Transaction.class);
		} catch (Throwable t) {
			logger.error("Error while getting all transactions for proeprty : " + transaction.getPropertyId(), t);
			throw new TransactionException("Error", t);
		}

		return transactions;
	}

	@Override
	public List<Transaction> getTransactionReport(String managerId, String fromDate, String toDate) throws Throwable {
		List<Transaction> transactionList = null;

		try {
			Query query = new Query();

			if (StringUtils.isNotBlank(managerId) && StringUtils.isNotBlank(fromDate)
					&& StringUtils.isNotBlank(toDate)) {
				query.addCriteria(Criteria.where("managerId").is(managerId).and("paidOn").gte(fromDate).lte(toDate));
				transactionList = template.find(query, Transaction.class);
			}
		} catch (Throwable t) {
			logger.error("Error while getting the transaction report between " + fromDate + " and " + toDate, t);
			throw new TransactionException("Error", t);
		}

		return transactionList;
	}

	@Override
	public List<Transaction> getTransactionReportForOwner(String ownerId, String fromDate, String toDate)
			throws Throwable {
		List<Transaction> transactionList = null;

		try {
			Query query = new Query();

			if (StringUtils.isNotBlank(ownerId) && StringUtils.isNotBlank(fromDate) && StringUtils.isNotBlank(toDate)) {
				query.addCriteria(Criteria.where("ownerId").is(ownerId).and("paidOn").gte(fromDate).lte(toDate));
				transactionList = template.find(query, Transaction.class);
			}
		} catch (Throwable t) {
			logger.error("Error while getting the transaction report between " + fromDate + " and " + toDate, t);
			throw new TransactionException("Error", t);
		}

		return transactionList;
	}
	
	@Override
	public List<Transaction> getTransactionReportForTenant(String tenantId, String fromDate, String toDate) throws Throwable {
		List<Transaction> transactionList = null;
		
		try {
			Query query = new Query();
	
			if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(fromDate) && StringUtils.isNotBlank(toDate)) {
				query.addCriteria(Criteria.where("paidBy").is(tenantId).and("paidOn").gte(fromDate).lte(toDate));
				transactionList = template.find(query, Transaction.class);
			}
		} catch(Throwable t) {
			logger.error("Error while getting the transaction report between " + fromDate + " and " + toDate, t);
			throw new TransactionException("Error", t);
		}

		return transactionList;
	}

	@Override
	public Transaction findById(String transactionId) throws Throwable {
		Query query = new Query(Criteria.where("transactionId").is(transactionId));
		Transaction transactionExist = template.findOne(query, Transaction.class);
		return transactionExist;
	}

	@Override
	public Transaction updateTransaction(Transaction transaction) {
		BasicDBObject set = new BasicDBObject("$set", transaction);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(transaction.getTransactionId())), update, Transaction.class);
		return transaction;
	}

	@Override
	public List<Transaction> getAllTransactionsForProperty(Property property) throws Throwable {
		return template.find(new Query(Criteria.where("propertyId").is(property.getPropertyId())), Transaction.class);
	}

	@Override
	public Lease getCurrentLeaseDataForProperty(Property property) throws Throwable {
		return template.findOne(
				new Query(Criteria.where("propertyId").is(property.getPropertyId()).and("leaseStatus").is("Active")),
				Lease.class);
	}

	@Override
	public Tenant getCurrentTenantForProperty(Lease lease) throws Throwable {
		return template.findOne(new Query(Criteria.where("_id").is(lease.getTenantId())), Tenant.class);
	}

	@Override
	public Transaction getTransactionByProperty(String propertyId) throws Throwable {
		Transaction transaction = null;
		try {
			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			transaction = template.findOne(query, Transaction.class);
		} catch (Throwable t) {
			logger.error("Error while fetching Transaction for property id : " + propertyId, t);
			throw new TransactionException("Fatal", t);
		}
		return transaction;

	}

	@Override
	public List<Transaction> getAllTransactionsForOwner(Owner owner) throws Throwable {
		return template.find(new Query(Criteria.where("ownerId").is(owner.getOwnerId())), Transaction.class);
	}

}
