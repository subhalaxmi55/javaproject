package com.selsoft.trackme.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selsoft.trackme.constants.ErrorConstants;
import com.selsoft.trackme.dao.UserDao;
import com.selsoft.trackme.dto.PasswordDto;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.PasswordResetToken;
import com.selsoft.trackme.model.User;
import com.selsoft.trackme.model.ValidError;
import com.selsoft.trackme.utils.Utils;
import com.selsoft.trackme.validation.UserValidation;

/**
 * 
 * @author Sudhansu Sekhar
 * 
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	private static final Logger logger = Logger.getLogger(UserService.class);
	private UserValidation validation = new UserValidation();

	/**
	 * save the valid user to the user table
	 */

	public Errors saveUser(User user) {
		Errors errors = validateNewUser(user);
		List<ValidError> err = errors.getError();
		int count = 0;
		for (ValidError vError : err) {
			if (StringUtils.equals("Success", vError.getErrorCode())) {
				count++;
			}
		}
		if (count == 3) {
			logger.info("User data is Valid and processing to Dao");
			String encryptPass = Utils.encryptPassword(user.getPassword());
			user.setPassword(encryptPass);
			userDao.saveUser(user);
		} else {
			logger.info("User data is not Valid returning Error Data");
		}
		return errors;

	}

	/**
	 * It saves user login,if it is a valid user and encrypts the password otherwise
	 * throws error message
	 */
	public Errors saveUserLogin(User user) {
		if (isValidLogin(user)) {
			logger.info("User data is Valid and processing to Dao");

			String password = user.getPassword();
			try {
				// encrypting the password seeing to User object
				// setting last accessed time & userlogin boolean flag.
				String encryptPwd = Utils.encryptPassword(password);
				user.setPassword(encryptPwd);
				user.setLoggedOn(true);
				Calendar time = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				Date date = time.getTime();
				user.setLastAccessed(date.toString());
				userDao.saveUserLogin(user);
			} catch (Exception e) {
				// throw custom seception
			}

		} else {
			logger.info("Email Id or Password are not valid returning Error Data");
			ValidError validError = new ValidError(ErrorConstants.AUTHENTICATIONERROR,
					ErrorConstants.AUTHENTICATIONERROR_MESSAGE);
			List<ValidError> errorList = new ArrayList<>();
			errorList.add(validError);
			return new Errors(errorList);
		}
		return null;
	}

	/**
	 * searches based on email
	 */
	public User findUserByEmail(String email) {
		User user = userDao.findUserByEmail(email);

		return user;
	}

	/**
	 * It creates a token while reseting a password
	 */

	public void createPasswordResetTokenForUser(User user, String token) {
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Date date = time.getTime();
		PasswordResetToken myToken = new PasswordResetToken(token, user, date);
		userDao.saveResetPasswordToken(myToken);
	}

	/**
	 * checks for valid user
	 * 
	 * @param user
	 * @return
	 */

	public Errors validateNewUser(User user) {

		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		String email = user.getEmail();
		String password = user.getPassword();

		ValidError nameErrors = validation.nameValidation(firstName, lastName);
		ValidError emailErrors = validation.emailValidation(email);
		ValidError passwordErrors = validation.passwordValidation(password);
		List<ValidError> errorList = new ArrayList<ValidError>();
		errorList.add(nameErrors);
		errorList.add(emailErrors);
		errorList.add(passwordErrors);
		Errors errors = new Errors();
		errors.setError(errorList);
		return errors;
	}

	public boolean isValidUser(User user) {

		String email = user.getEmail();
		String password = user.getPassword();

		ValidError emailErrors = validation.emailValidation(email);
		ValidError passwordErrors = validation.passwordValidation(password);

		if (StringUtils.equals("Success", emailErrors.getErrorCode())
				&& StringUtils.equals("Success", passwordErrors.getErrorCode())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isValidLogin(User user) {

		// String encryptPwd = Utils.encryptPassword(user.getPassword());
		// user.setPassword(encryptPwd);
		User validUser = userDao.checkUserLogin(user);
		if (validUser != null) {
			return true;
		} else {
			return false;
		}

	}

	public SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, User user) {
		String url = contextPath + "/user/changePassword?id=" + user.getEmail() + "&token=" + token;
		String message = generateContent(user, token, contextPath);
		return constructEmail("Reset Password", message, user);
	}

	public String generateContent(User user, String token, String appUrl) {

		StringBuilder builder = new StringBuilder();
		builder.append("<html>Hi " + user.getFirstName()
				+ "<br> Please use below link to Reset your password.<br><a href='" + appUrl + "?token=" + token
				+ "'>Click here to Reset Password</a><br>" + "Thanks,<br> TrackMe Inc.</html>");
		return builder.toString();
	}

	private SimpleMailMessage constructEmail(String subject, String body, User user) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
		email.setText(body);
		email.setTo(user.getEmail());
		return email;
	}

	@Override
	public Errors changeUserPassword(User user, PasswordDto password) {
		if (StringUtils.equals(password.getPassword(), password.getConfirmPassword())) {
			ValidError passwordErrors = validation.passwordValidation(password.getPassword());

			if (StringUtils.equals("Success", passwordErrors.getErrorCode())) {
				String encryptPass = Utils.encryptPassword(password.getPassword());
				userDao.changeUserPassword(user, encryptPass);
			}
		}

		ValidError passwordErrors = new ValidError("SUCCESSUPDATEPASSWORD107", "SUCCESSUPDATEPASSWORD107_MESSAGE");

		List<ValidError> list = new ArrayList<ValidError>();
		list.add(passwordErrors);
		return new Errors(list);
	}

	/**
	 * logged out for the valid email
	 */
	@Override
	public Errors userLogout(String email) {
		userDao.userLogout(email);
		ValidError loggedOutErrors = new ValidError("SUCCESSLOGOUT108", "SUCCESSLOGOUT108");

		List<ValidError> list = new ArrayList<ValidError>();
		list.add(loggedOutErrors);
		return new Errors(list);

	}

}
