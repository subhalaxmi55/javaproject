package com.selsoft.user.dao;

import java.security.spec.InvalidKeySpecException;
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
import com.selsoft.trackme.constants.UserConstants.USER_STATUS;
import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.PasswordResetToken;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.utils.TrackMeUtils;
import com.selsoft.user.utils.AuthenticationUtils;

@Repository
public class UserDaoImpl implements UserDao {
	private static final Logger logger = Logger.getLogger(UserDaoImpl.class);

	@Autowired
	private MongoTemplate template;

	@Autowired
	private AuthenticationUtils authenticationUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.selsoft.trackme.dao.UserDao#saveUser(com.selsoft.trackme.model.User)
	 * When User data comes from Service This method, will saves the Data into DB by
	 * calling MongoTemplate save() =======
	 * 
	 * @see com.selsoft.trackme.dao.UserDao#saveUser(com.selsoft.trackme.model.User)
	 * When User data comes from Service This method, will saves the Data into DB by
	 * calling MongoTemplate save() >>>>>>> refs/remotes/origin/master
	 * 
	 */
	@Override
	public void saveUser(User user) throws UserException {
		try {
			template.insert(user);
		} catch (DuplicateKeyException e) {
			logger.info(user.getEmail() + " already exists in the database");
			throw new UserException("Error", "User with the email id " + user.getEmail()
					+ " already exists. Please login with your password or click on Forget Password");
		} catch (Throwable t) {
			logger.error("Fatal Error while saving the user", t);
			throw new UserException("Fatal", t);
		}
		logger.info("User " + user.getFirstName() + " inserted in Database");
	}

	@Override
	public void saveUserLogin(User user) throws Throwable {
		Query query = new Query(Criteria.where("email").is(user.getEmail().toLowerCase()));
		Update update = new Update();
		update.set("loggedOnFlag", user.isLoggedOnFlag());
		update.set("lastAccessedOn", user.getLastAccessedOn());
		template.updateFirst(query, update, User.class);
	}

	@Override
	public User findUserByEmail(String email) {
		if (StringUtils.isBlank(email))
			return null;
		return template.findOne(new Query(Criteria.where("email").is(email.toLowerCase())), User.class);
	}

	@Override
	public User findUserByEmailAndType(User user) {
		if (StringUtils.isBlank(user.getEmail()))
			return null;
		return template.findOne(new Query(
				Criteria.where("email").is(user.getEmail().toLowerCase()).and("userType").is(user.getUserType())),
				User.class);
	}

	@Override
	public User findUserByTemporaryToken(String temporaryToken) throws Throwable {
		if (StringUtils.isBlank(temporaryToken))
			return null;
		return template.findOne(new Query(Criteria.where("temporaryToken").is(temporaryToken)), User.class);
	}

	@Override
	public String createTemporaryTokenAndLink(String id) {
		UUID randomId = UUID.randomUUID();
		BasicDBObject newValues = new BasicDBObject("temporaryToken", randomId.toString()).append("resetPasswordTime",
				TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
		BasicDBObject set = new BasicDBObject("$set", newValues);
		Update update = new BasicUpdate(set);
		template.upsert(new Query(Criteria.where("_id").is(id)), update, User.class);
		return randomId.toString();
	}

	@Override
	public String createTemporaryTokenAndLinkForUserActivation(String email) {
		UUID randomId = UUID.randomUUID();
		BasicDBObject newValues = new BasicDBObject("temporaryToken", randomId.toString());
		newValues.append("userStatus", USER_STATUS.PENDING.getUserStatusValue());
		BasicDBObject set = new BasicDBObject("$set", newValues);
		Update update = new BasicUpdate(set);
		template.upsert(new Query(Criteria.where("email").is(email)), update, User.class);
		return randomId.toString();
	}

	public void userLogout(String email) {
		Query query = new Query(Criteria.where("email").is(email.toLowerCase()));
		Update update = new Update();
		update.set("loggedOn", false);
		template.updateFirst(query, update, User.class);

		logger.info("User " + email + " Logged out Successfully");

	}

	@Override
	public void saveUserType(User user, String userType) {

		Query query = new Query(Criteria.where("userType").is(user.getUserType()));
		Update update = new Update();
		update.set(" propertyOwner", "OWN");
		update.set("propertyManager", "MGR");
		update.set("propertyTenant", "TNT");
		template.updateFirst(query, update, User.class);

	}

	@Override
	public User checkUserLogin(User user) throws Throwable {
		String secureUserPassword = null;

		Query query = new Query(Criteria.where("email").is(user.getEmail()));
		// query.fields().include("firstName").include("lastName").include("email").include("_id").include("userType").include("salt").include("password").include("userStatus");

		User existingUser = template.findOne(query, User.class);

		try {
			if (existingUser != null) {

				if (!StringUtils.equals(USER_STATUS.ACTIVE.getUserStatusValue(), existingUser.getUserStatus())) {
					throw new UserException("Error", "Invalid User Status: '" + existingUser.getUserStatus()
							+ "'. User should be Active for logging in.");
				}

				secureUserPassword = authenticationUtil.createSecurePassword(user.getPassword(),
						existingUser.getSalt());
				if (secureUserPassword != null && secureUserPassword.equalsIgnoreCase(existingUser.getPassword())
						&& user.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
					existingUser.clearSecuredData();
				} else {
					throw new UserException("Error", "Invalid User id or password, cannot login");
				}
			} else {
				throw new UserException("Error", "Invalid User id or password, cannot login");
			}
		} catch (InvalidKeySpecException ex) {
			logger.fatal(null, ex);
			throw new UserException("Fatal", ex.getLocalizedMessage());
		}

		return existingUser;
	}

	@Override
	public void changeUserPassword(User user) {
		Query where = new Query(
				Criteria.where("email").is(user.getEmail()).and("temporaryToken").is(user.getTemporaryToken()));
		Update update = Update.update("password", user.getPassword()).set("salt", user.getSalt()).set("userStatus",
				USER_STATUS.ACTIVE.getUserStatusValue());
		template.updateFirst(where, update, User.class);
		removeTemporaryPasswordResetFields(user);
	}

	@Override
	public List<User> getAllUsers(String userType) {
		Query query = new Query();
		if (StringUtils.isNotBlank(userType)) {
			query.addCriteria(Criteria.where("userType").is(userType));
		}
		query.fields().include("firstName").include("lastName").include("_id").include("userType");
		return template.find(query, User.class);
	}

	@Override
	public User checkIfResetRequestIsValid(String temporaryToken) throws Throwable {
		Query searchResetPasswordIdQuery = new Query(Criteria.where("temporaryToken").is(temporaryToken));
		searchResetPasswordIdQuery.fields().include("firstName").include("lastName").include("_id").include("emailId")
				.include("resetPasswordTime").include("temporaryToken");
		return template.findOne(searchResetPasswordIdQuery, User.class);
	}

	@Override
	public void removeTemporaryPasswordResetFields(User user) {
		// Remove temporarily added reset id and reset time
		Query resetWhere = new Query(Criteria.where("temporaryToken").is(user.getTemporaryToken()));
		BasicDBObject newValues = new BasicDBObject("temporaryToken", StringUtils.EMPTY).append("resetPasswordTime",
				StringUtils.EMPTY);
		BasicDBObject unset = new BasicDBObject("$unset", newValues);
		Update resetUpdate = new BasicUpdate(unset);
		template.updateFirst(resetWhere, resetUpdate, User.class);
	}

	@Override
	public String getOwnerIdByEmail(String email) throws Throwable {
		Query query = new Query(Criteria.where("email").is(email));
		query.fields().include("_id");
		Owner owner = template.findOne(query, Owner.class);
		if (owner == null) {
			throw new UserException("Error", "Owner information not found, cannot add user");
		}
		return owner.getOwnerId();
	}

	@Override
	public String getTenantIdByEmail(String email) throws Throwable {
		Query query = new Query(Criteria.where("tenantEmailId").is(email));
		query.fields().include("_id");
		Tenant tenant = template.findOne(query, Tenant.class);
		if (tenant == null) {
			throw new UserException("Error", "Tenant information not found, cannot add user");
		}
		return tenant.getTenantId();
	}

	@Override
	public void saveResetPasswordToken(PasswordResetToken token) {
		// TODO Auto-generated method stub
		template.save(token);
	}

	@Override
	public User findByEmail(String email) {
		Query query = new Query(Criteria.where("email").is(email));
		User userExist = template.findOne(query, User.class);
		return userExist;
	}

	@Override
	public User updateUser(User user) {
		template.save(user);
		return user;
	}

	@Override
	public void deleteUser(String userId) throws Throwable {
		template.remove(new Query(Criteria.where("userId").is(userId)), User.class);
	}

	@Override
	public User findUserById(String userId) {
		Query query = new Query(Criteria.where("userId").is(userId));
		User userExist = template.findOne(query, User.class);
		return userExist;
	}

	@Override
	public User getUserDetails(String userId) throws Throwable {
		logger.info("Inside user");

		User userDetails = null;
		try {
			if (userId != null) {
				Query query = new Query(Criteria.where("userId").is(userId));
				userDetails = template.findOne(query, User.class);
			}
		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + userId, t);
			throw new UserException("Fatal", t);
		}
		return userDetails;
	}

}
