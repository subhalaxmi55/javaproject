package com.selsoft.tenant.dao;

import java.util.List;
import java.util.UUID;
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
import com.selsoft.trackme.constants.TenantConstants.TENANT_STATUS;
import com.mongodb.DuplicateKeyException;
import com.selsoft.trackme.exception.TenantException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.User;

@Repository
@Qualifier("tanantDAO")
public class TenantDAOImpl implements TenantDAO {
	private static final Logger logger = Logger.getLogger(TenantDAOImpl.class);

	@Autowired
	private MongoTemplate template;

	final String COLLECTION = "TENANT";

	/**
	 * saves new tenant to tenant table
	 */
	public void saveNewTenant(Tenant tenant) throws Throwable {

		try {
			template.insert(tenant);
		} catch (Throwable e) {
			logger.info(tenant.getTenantEmailId() + " already exists in the database");
			throw new TenantException("Error", "Tenant with the email id " + tenant.getTenantEmailId()
					+ " already exists, please add a tenant " + "with a different email id.");
		}
	}

	/**
	 * find all tenants
	 */
	public List<Tenant> findAll() {
		return (List<Tenant>) template.findAll(Tenant.class);
	}

	@Override

	/**
	 * fetching tenants based on status
	 */
	public List<Tenant> fetchTenants(String status) {

		List<Tenant> tenantList = null;

		if (status != null) {

			Query query = new Query(Criteria.where("tenantStatus").is(status));
			tenantList = template.find(query, Tenant.class);
		} else {

			tenantList = template.findAll(Tenant.class);
		}

		return tenantList;
	}

	public List<Tenant> fetchTenantsForManager(String managerId, TENANT_STATUS tenantStatus) throws Throwable {
		List<Tenant> tenantList = null;
		if (StringUtils.isNotBlank(managerId)) {
			Query query = new Query();
			Criteria criteria = Criteria.where("managerId").is(managerId);
			if (tenantStatus != null) {
				criteria = criteria.and("tenantStatus").is(tenantStatus);
			}
			query.addCriteria(criteria);
			tenantList = template.find(query, Tenant.class);
		}
		return tenantList;
	}

	public Tenant getTenantByEmail(String tenantEmailId) throws Throwable {
		Tenant tenant = null;
		tenantEmailId = StringUtils.trimToEmpty(StringUtils.lowerCase(tenantEmailId));
		try {
			if (StringUtils.isBlank(tenantEmailId))
				throw new TenantException("Error", "Email id not present, please check the request");
			tenant = template.findOne(new Query(Criteria.where("tenantEmailId").is(tenantEmailId)), Tenant.class);
		} catch (Throwable t) {
			logger.error("Fatal Error while getting the tenant with email " + tenantEmailId, t);
			throw new TenantException("Fatal", t);
		}
		return tenant;
	}

	@Override
	public User findManagerByUserId(String managerId) throws Throwable {
		if (StringUtils.isBlank(managerId))
			return null;
		User user = null;
		try {
			user = template.findOne(new Query(Criteria.where("_id").is(managerId).and("userType").is("MGR")),
					User.class);
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new TenantException("Fatal", t);
		}
		return user;
	}

	@Override
	public User findUserByEmail(String email) {
		if (StringUtils.isBlank(email))
			return null;
		return template.findOne(new Query(Criteria.where("email").is(email.toLowerCase())), User.class);

	}

	public String createTemporaryTokenAndLinkForUserActivation(String email) {
		UUID randomId = UUID.randomUUID();
		BasicDBObject newValues = new BasicDBObject("temporaryToken", randomId.toString());
		newValues.append("userStatus", "Pending");
		BasicDBObject set = new BasicDBObject("$set", newValues);
		Update update = new BasicUpdate(set);
		template.upsert(new Query(Criteria.where("email").is(email)), update, User.class);
		return randomId.toString();
	}

	@Override
	public void saveUser(User user) throws TenantException {
		try {
			template.insert(user);
		} catch (DuplicateKeyException e) {
			logger.info(user.getEmail() + " already exists in the database");
			throw new TenantException("Error", "User with the email id " + user.getEmail()
					+ " already exists. Please login with your password or click on Forget Password");
		} catch (Throwable t) {
			logger.error("Fatal Error while saving the user", t);
			throw new TenantException("Fatal", t);
		}
		logger.info("User " + user.getFirstName() + " inserted in Database");
	}

	@Override
	public void deleteTenant(Tenant tenant) {
		template.remove(tenant);
	}

	@Override
	public void deleteUser(User user) {
		template.remove(user);
	}

	@Override
	public Tenant findTenantById(String tenantId) {

		Query query = new Query(Criteria.where("_id").is(tenantId));
		return template.findOne(query, Tenant.class);
	}

	@Override
	public void updateTenant(Tenant tenant) {
		BasicDBObject set = new BasicDBObject("$set", tenant);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(tenant.getTenantId())), update, Tenant.class);
	}

	@Override
	public void updateUserEmail(Tenant tenant) {
		BasicDBObject set = new BasicDBObject("email", tenant.getTenantEmailId());
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("userTypeId").is(tenant.getTenantId())), update, User.class);
	}

	@Override
	public Tenant findById(String tenantId) {
		Query query = new Query(Criteria.where("_id").is(tenantId));
		return template.findOne(query, Tenant.class);
	}

	@Override
	public void saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		try {
			template.insert(serviceRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deleteTenant(String tenantId) throws Throwable {
		template.remove(new Query(Criteria.where("tenantId").is(tenantId)), Tenant.class);		
	}
	
	public Lease getActiveLeaseDetailForTenantById(String id) {
		logger.info("Getting lease details for tenant : " + id);
		return template.findOne(new Query(Criteria.where("tenantId").is(id).and("leaseStatus").is("Active")), Lease.class);
	}

}
