package com.selsoft.trackme.payments.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.selsoft.trackme.exception.OwnerException;
import com.selsoft.trackme.exception.PaymentsException;
import com.selsoft.trackme.models.Application;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.utils.TrackMeUtils;

@Repository
public class PaymentsDaoImpl implements PaymentsDao {

	private static final Logger logger = Logger.getLogger(PaymentsDaoImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Document> getFortePaymentData() throws PaymentsException {
		return mongoTemplate.find(new Query(Criteria.where("module").is("PAYMENTS").and("subModule").is("FORTE")),
				Document.class, "COMMONUTILITY");
	}

	@Override
	public void saveTransaction(Transaction transaction) throws PaymentsException {
		logger.info("Saving transaction data");
		try {
			transaction.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
			mongoTemplate.insert(transaction);
		} catch (Throwable t) {
			logger.error("Error while getting the property data", t);
			throw new PaymentsException("Fatal", t);
		}
	}

	@Override
	public Property getPropertyData(Transaction transaction) throws PaymentsException {
		logger.info("Getting property information for : " + transaction.getPropertyId());
		Property property = null;
		try {
			property = mongoTemplate.findOne(
					new Query(Criteria.where("_id").is(transaction.getPropertyId()).and("ownerId")
							.is(transaction.getOwnerId()).and("managerId").is(transaction.getManagerId())),
					Property.class);
		} catch (Throwable t) {
			logger.error("Error while getting the property data", t);
			throw new PaymentsException("Fatal", t);
		}
		return property;
	}

	@Override
	public void saveApplication(Application application) throws PaymentsException {
		logger.info("Saving application data");
		try {
			mongoTemplate.insert(application);
		} catch (Throwable t) {
			logger.error("Error while getting the application  data", t);
			throw new PaymentsException("Fatal", t);
		}
	}

	@Override
	public User findPaidByUserId(String userId) throws PaymentsException {
		if (StringUtils.isBlank(userId))
			return null;
		User user = null;
		try {
			user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(userId)), User.class);
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + userId, t);
			throw new PaymentsException("Fatal", t);
		}
		return user;
	}

	@Override
	public Transaction getTransactionForForteTransactionId(String forteTransactionId) throws PaymentsException {
		forteTransactionId = StringUtils.trimToEmpty(forteTransactionId);
		logger.info("Getting transaction for forte transaction id : " + forteTransactionId);
		return StringUtils.isBlank(forteTransactionId) ? null
				: mongoTemplate.findOne(new Query(Criteria.where("forteTransactionId").is(forteTransactionId)),
						Transaction.class);
	}

	@Override
	public Transaction getTransactionForTransactionId(String transactionId) throws PaymentsException {
		logger.info("Getting transaction for forte transaction id : " + transactionId);
		return mongoTemplate.findOne(new Query(Criteria.where("_id ").is(transactionId)), Transaction.class);
	}

	@Override
	public void updateTransactionStatus(Transaction transaction) throws PaymentsException {
		logger.info("Updating transaction status for transaction id : " + transaction.getTransactionId()
				+ " with status : " + transaction.getPaymentStatus());
		BasicDBObject set = new BasicDBObject("paymentStatus", transaction.getPaymentStatus());
		Update update = new BasicUpdate(set);
		mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(transaction.getTransactionId())), update,
				Transaction.class);
	}

	@Override
	public void saveMerchantApplication(Application application) throws PaymentsException {
		logger.error("Saving merchant application for : " + application.getOrganization().getLegalName());
		try {
			mongoTemplate.insert(application);
		} catch (Throwable t) {
			logger.error("Error while saving the application for : " + application.getOrganization().getLegalName(), t);
			throw new PaymentsException("Error",
					"Error while saving the application for : " + application.getOrganization().getLegalName(), t);
		}
	}

	@Override
	public Application getMerchantApplicationForRiskSessionId(String riskSessionId) throws Throwable {
		riskSessionId = StringUtils.trimToEmpty(riskSessionId);
		logger.error("Getting merchant application for risk session id : " + riskSessionId);
		return StringUtils.isNotBlank(riskSessionId)
				? mongoTemplate.findOne(new Query(Criteria.where("riskSessionId").is(riskSessionId)), Application.class)
				: null;
	}

	@Override
	public User findManagerByUserId(String managerId) throws Throwable {
		if (StringUtils.isBlank(managerId))
			return null;
		User user = null;
		try {
			user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(managerId).and("userType").is("MGR")),
					User.class);
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new OwnerException("Fatal", t);
		}
		return user;
	}

}