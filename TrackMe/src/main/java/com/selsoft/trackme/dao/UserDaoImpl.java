package com.selsoft.trackme.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.PasswordResetToken;
import com.selsoft.trackme.model.User;
import com.selsoft.trackme.utils.Utils;



@Repository
public class UserDaoImpl implements UserDao {
	private static final Logger logger = Logger.getLogger(UserDaoImpl.class);

	@Autowired
	private MongoTemplate template;

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
	public void saveUser(User user) {

		template.save(user);
		logger.info("User " + user.getFirstName() + " Saved in Database");

	}

	@Override
	public void saveUserLogin(User user) {

		Query query = new Query(Criteria.where("email").is(user.getEmail().toLowerCase()));
		Update update = new Update();
		update.set("loggedOn", user.isLoggedOn());
		update.set("lastAccessed", user.getLastAccessed());

		template.updateFirst(query, update, User.class);

		logger.info("User Email " + user.getEmail() + " last access time " + user.getLastAccessed());

	}

	@Override
	public User findUserByEmail(String email) {
		Query query = new Query(Criteria.where("email").is(email.toLowerCase()));
		List<User> userExist = template.find(query, User.class);
		return userExist.get(0);
	                                            }

	
	public void saveResetPasswordToken(PasswordResetToken token) {
		
		template.save(token);

	                                         } 

	
	public void userLogout(String email) {
		Query query = new Query(Criteria.where("email").is(email.toLowerCase()));
		Update update = new Update();
		update.set("loggedOn", false);
		template.updateFirst(query, update, User.class);

		logger.info("User " + email + " Logged out Successfully");

	}

	@Override
	public Errors saveUserType(User user, String userType) {

		Query query = new Query(Criteria.where("userType").is(user.getUserType()));
		Update update = new Update();
		update.set(" propertyOwner", "OWN");
		update.set("propertyManager", "MGR");
		update.set("propertyTenant", "TNT");
		template.updateFirst(query, update, User.class);
		return null;

	}

	@Override
	public User checkUserLogin(User user) {
		Query query = new Query(Criteria.where("email").is(user.getEmail()));

		List<User> userExist = template.find(query, User.class);
		if (userExist.size() > 0) {

			User returnedUser = userExist.get(0);
			String pass = returnedUser.getPassword();
			String password = Utils.decryptPassword(pass);
			if (StringUtils.equals(user.getPassword(), password)) {
				return returnedUser;
			} else {
				return null;
			}
		}
		return null;

	}

	@Override
	public void changeUserPassword(User user, String password) {

		Query query = new Query(Criteria.where("email").is(user.getEmail()));

		List<User> userExist = template.find(query, User.class);
		if (userExist.size() > 0) {

			User returnedUser = userExist.get(0);
			returnedUser.setPassword(password);
			template.save(returnedUser);
		}

	}

}
