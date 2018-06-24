package com.selsoft.property.dao;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.mongodb.BasicDBObject;
import com.selsoft.trackme.exception.LeaseException;
import com.selsoft.trackme.exception.PropertyException;
import com.selsoft.trackme.exception.ServiceRequestException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;

@Repository
@Qualifier("propertyDAO")
public class PropertyDAOImpl implements PropertyDAO {
	private static final Logger logger = Logger.getLogger(PropertyDAOImpl.class);

	@Autowired
	private MongoTemplate template;

	/**
	 * save new property to property table
	 */
	@Override
	public void saveNewProperty(Property property) {
		template.insert(property);
		// template.save(property.getRentalDetail());
	}

	@Override

	/**
	 * It checks owner based on ownerid
	 */
	public Owner findOwnerById(Property property) {
		return template.findOne(new Query(Criteria.where("ownerId").is(property.getOwnerId())), Owner.class);
	}

	@Override
	public User findManagerByUserId(String managerId) throws Throwable {
		if (StringUtils.isBlank(managerId))
			return null;
		User user = null;
		try {
			user = template
					.findOne(
							new Query(Criteria.where("_id").is(managerId).orOperator(
									Criteria.where("userType").is("MGR"), Criteria.where("userType").is("MADM"))),
							User.class);
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new PropertyException("Fatal", t);
		}
		return user;
	}

	@Override
	public User findUserByUserId(String managerId) throws Throwable {
		if (StringUtils.isBlank(managerId))
			return null;
		User user = null;
		try {
			user = template
					.findOne(
							new Query(Criteria.where("_id").is(managerId).orOperator(
									Criteria.where("userType").is("MGR"), Criteria.where("userType").is("MADM"))),
							User.class);
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new PropertyException("Fatal", t);
		}
		return user;
	}

	@Override
	/**
	 * It sets property as active
	 */
	public void setPropertyAsActive(Property property) {
		Query query = new Query(Criteria.where("PropertyId").is(property.getPropertyId()));
		Update update = new Update();
		update.set("propertyStatus", "Active");
		template.updateFirst(query, update, Property.class);
	}

	@Override
	public List<Property> getAllPropertiesForManager(String managerId) {
		return template.find(new Query(Criteria.where("managerId").is(managerId)), Property.class);
	}

	@Override
	public List<Property> getAllPropertiesForManagerAndStatus(String managerId, String status) {
		return template.find(new Query(Criteria.where("managerId").is(managerId).and("propertyStatus").is(status)),
				Property.class);
	}

	@Override
	public List<Property> getAllPropertiesForOwner(String ownerId) {
		return template.find(new Query(Criteria.where("ownerId").is(ownerId)), Property.class);
	}

	@Override
	public List<Property> getAllPropertiesForOwnerAndStatus(String ownerId, String status) {
		return template.find(new Query(Criteria.where("ownerId").is(ownerId).and("propertyStatus").is(status)),
				Property.class);
	}

	@Override
	public List<Property> findAll() {

		return (List<Property>) template.findAll(Property.class);
	}

	@Override
	public void updateProperty(RentalDetail rentalDetail, String propertyId) {
		Query query = new Query(Criteria.where("propertyId").is(propertyId));
		Update update = new Update();
		rentalDetail.setProperytId(propertyId);
		update.set("rentalDetail", rentalDetail);
		template.updateFirst(query, update, Property.class);
	}

	@Override
	public void saveNewPropertyGroup(List<Property> properties) {
		template.insert(properties, Property.class);
	}

	@Override
	public void delete(List<Property> properties) {
		template.remove(properties);
	}

	@Override
	public Property findPropertyById(String propertyId) {
		Query query = new Query(Criteria.where("_id").is(propertyId));
		return template.findOne(query, Property.class);
	}

	@Override
	public void updateProperty(Property property) {
		BasicDBObject set = new BasicDBObject("$set", property);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(property.getPropertyId())), update, Property.class);
	}

	@Override
	public Lease findLeaseById(String leaseId) throws Throwable {
		Lease lease = null;
		try {
			Query query = new Query(Criteria.where("leaseId").is(leaseId));
			lease = template.findOne(query, Lease.class);
		} catch (Throwable t) {
			logger.error("Error while fetching lease for lease id : " + leaseId, t);
			throw new LeaseException("Fatal", t);
		}
		return lease;
	}

	@Override
	public ServiceRequest findServiceRequestById(String serviceRequestId) throws Throwable {
		ServiceRequest request = null;
		try {
			Query query = new Query(Criteria.where("serviceRequestId").is(serviceRequestId));
			request = template.findOne(query, ServiceRequest.class);
		} catch (Throwable t) {
			logger.error("Error while fetching service request for service request id : " + serviceRequestId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return request;
	}

	@Override
	public Transaction findTransactionById(String transactionId) throws Throwable {
		Transaction transaction = null;
		try {
			Query query = new Query(Criteria.where("transactionId").is(transactionId));
			transaction = template.findOne(query, Transaction.class);
		} catch (Throwable t) {
			logger.error("Error while fetching Transaction for transaction id : " + transactionId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return transaction;
	}

	@Override
	public List<Property> getAllGroupedProperty(String propertyName) throws Throwable {
		List<Property> groupedProperties = null;
		try {

			groupedProperties = template.find(
					new Query(Criteria.where("groupedProperty").is(true).and("propertyName").is(propertyName)),
					Property.class);
		} catch (Throwable t) {
			logger.error("Error while fetching proprty name  : " + propertyName, t);
			throw new ServiceRequestException("Fatal", t);
		}

		return groupedProperties;
	}

	@Override
	public Lease getLeaseForProperty(String propertyId) throws Throwable {
		Lease lease = null;
		try {
			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			lease = template.findOne(query, Lease.class);
		} catch (Throwable t) {
			logger.error("Error while fetching lease for property id : " + propertyId, t);
			throw new LeaseException("Fatal", t);
		}
		return lease;
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
	public void updateLease(Lease lease) {
		BasicDBObject set = new BasicDBObject("$set", lease);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(lease.getLeaseId())), update, Lease.class);
	}

	@Override
	public ServiceRequest updateserviceRequest(ServiceRequest serviceRequest) throws Throwable {
		ServiceRequest serviceRequestFromDB = null;
		String serviceRequestId = "";
		serviceRequestId = serviceRequest.getServiceRequestId();
		Query query = null;
		Update update = null;
		if (StringUtils.isNotBlank(serviceRequestId)) {
			query = new Query(Criteria.where("serviceRequestId").is(serviceRequestId));
			update = new Update();
			update.set(" requestStatus", "Cancelled/Closed");
			update.set("comment", "Assigned to Vendor");
			update.set("commentedBy", "user id of the person commenting");
			update.set("commentedOn", "get the current timestamp as string from trackmeutils");
			update.set("comment", "Vendor started working");
			template.updateFirst(query, update, ServiceRequest.class);

		}
		serviceRequestFromDB = findServiceRequestById(serviceRequestId);
		return serviceRequestFromDB;

	}

	@Override
	public ServiceRequest getServiceRequestByProperty(String propertyId) throws Throwable {
		ServiceRequest serviceRequest = null;
		try {
			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			serviceRequest = template.findOne(query, ServiceRequest.class);
		} catch (Throwable t) {
			logger.error("Error while fetching lease for property id : " + propertyId, t);
			throw new PropertyException("Fatal", t);
		}
		return serviceRequest;
	}

	@Override
	public Transaction getTransactionByProperty(String propertyId) throws Throwable {
		Transaction transaction = null;
		try {
			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			transaction = template.findOne(query, Transaction.class);
		} catch (Throwable t) {
			logger.error("Error while fetching Transaction for property id : " + propertyId, t);
			throw new PropertyException("Fatal", t);
		}
		return transaction;
	}

	@Override
	public void updateTransaction(Transaction transaction) {
		BasicDBObject set = new BasicDBObject("$set", transaction);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(transaction.getTransactionId())), update, Transaction.class);
		
	}

}