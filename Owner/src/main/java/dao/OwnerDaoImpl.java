package com.selsoft.owner.dao;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

import com.selsoft.trackme.exception.OwnerException;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;

@Repository
public class OwnerDaoImpl implements OwnerDao {

	private static final Logger logger = Logger.getLogger(OwnerDaoImpl.class);

	@Autowired
	private MongoTemplate template;

	@Override

	/**
	 * saves new owner in owner table
	 */
	public void saveNewOwner(Owner owner) throws Throwable {
		try {
			template.insert(owner);
		} catch (Throwable t) {
			logger.error("Fatal Error while saving the owner", t);
			throw new OwnerException("Fatal", t);
		}
	}

	@Override
	/* GET */
	public List<Owner> getAllPropertyOwnersForManager(String managerId) throws Throwable {
		List<Owner> ownerList = null;

		try {
			ownerList = template.find(new Query(Criteria.where("managerId").is(managerId)), Owner.class);
		} catch (Throwable t) {
			logger.error("Fatal Error while getting all the owners", t);
			throw new OwnerException("Fatal", t);
		}

		return ownerList;
	}

	public Owner getOwnerForEmail(String email) throws Throwable {
		Query query = null;
		Owner owner = null;
		try {
			query = new Query(Criteria.where("email").is(email));
			owner = template.findOne(query, Owner.class);
		} catch (Throwable t) {
			logger.error("Fatal Error while getting the user with email " + email, t);
			throw new OwnerException("Fatal", t);
		}
		return owner;
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
			throw new OwnerException("Fatal", t);
		}
		return user;
	}

	public User findEnteredByUserId(String managerId) throws Throwable {
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
			throw new OwnerException("Fatal", t);
		}
		return user;
	}

	@Override
	public User findUserByEmail(String email) {
		if (StringUtils.isBlank(email))
			return null;
		return template.findOne(new Query(Criteria.where("email").is(StringUtils.lowerCase(email))), User.class);
	}

	@Override
	public String createTemporaryTokenAndLinkForUserActivation(String email) {
		if (StringUtils.isBlank(email))
			return null;
		UUID randomId = UUID.randomUUID();
		BasicDBObject newValues = new BasicDBObject("temporaryToken", randomId.toString());
		newValues.append("userStatus", "Pending");
		BasicDBObject set = new BasicDBObject("$set", newValues);
		Update update = new BasicUpdate(set);
		template.upsert(new Query(Criteria.where("email").is(StringUtils.lowerCase(email))), update, User.class);
		return randomId.toString();
	}

	@Override
	public void saveUser(User user) throws OwnerException {
		try {
			template.insert(user);
		} catch (DuplicateKeyException e) {
			logger.info(user.getEmail() + " already exists in the database");
			throw new OwnerException("Error", "User with the email id " + user.getEmail()
					+ " already exists. Please login with your password or click on Forget Password");
		} catch (Throwable t) {
			logger.error("Fatal Error while saving the user", t);
			throw new OwnerException("Fatal", t);
		}
		logger.info("User " + user.getFirstName() + " inserted in Database");
	}

	@Override
	public void deleteOwner(Owner owner) {
		template.remove(owner);
	}

	@Override
	public void deleteUser(User user) {
		template.remove(user);
	}

	@Override
	public Owner findOwnerById(String ownerId) {
		Query query = new Query(Criteria.where("_id").is(ownerId));
		return template.findOne(query, Owner.class);
	}

	@Override
	public void updateOwner(Owner owner) {
		BasicDBObject set = new BasicDBObject("$set", owner);
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("_id").is(owner.getOwnerId())), update, Owner.class);
	}

	@Override
	public void updateUserEmail(Owner owner) {
		BasicDBObject set = new BasicDBObject("email", owner.getEmail());
		Update update = new BasicUpdate(set);
		template.updateFirst(new Query(Criteria.where("userTypeId").is(owner.getOwnerId())), update, User.class);
	}

	@Override
	public void deleteOwner(String ownerId) {
		template.remove(new Query(Criteria.where("ownerId").is(ownerId)), Owner.class);
	}
	
	public List<Transaction> getAllTransactionsForOwner(Owner owner) throws Throwable {
		return template.find(new Query(Criteria.where("ownerId").is(owner.getOwnerId())), Transaction.class);
	}
	
}