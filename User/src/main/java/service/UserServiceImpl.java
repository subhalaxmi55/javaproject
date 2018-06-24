package com.selsoft.user.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.selsoft.trackme.constants.ErrorConstants;
import com.selsoft.trackme.constants.UserConstants;
import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.Errors;
import com.selsoft.trackme.models.PasswordResetToken;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.models.UserResponse;
import com.selsoft.trackme.models.ValidError;
import com.selsoft.trackme.utils.TrackMeUtils;
import com.selsoft.user.dao.UserDao;
import com.selsoft.user.dto.PasswordDto;
import com.selsoft.user.email.service.MailSenderService;
import com.selsoft.user.utils.AuthenticationUtils;

/**
 * 
 * @author Sudhansu Sekhar
 * 
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	MailSenderService mailService;

	@Autowired
	private AuthenticationUtils authenticationUtil;

	@Autowired
	Properties properties;

	private static final Logger logger = Logger.getLogger(UserService.class);
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	/**
	 * save the valid user to the user table
	 */

	public void saveUser(User user) throws Throwable {

		validateNewUser(user);
		if (user.getErrors() != null && user.getErrors().size() > 0)
			return;
		logger.info("User data is Valid and processing to Dao");
		// Generate salt
		String salt = authenticationUtil.createSaltValue(30);

		// Generate secure user password
		String secureUserPassword = null;

		try {
			secureUserPassword = authenticationUtil.createSecurePassword(user.getPassword(), salt);
		} catch (InvalidKeySpecException ex) {
			logger.fatal(null, ex);
			throw new UserException("Fatal", ex.getLocalizedMessage());
		}

		// Generate secure public user id
		// String securePublicUserId =
		// authenticationUtil.createSecureUserId(30);

		user.setSalt(salt);
		user.setPassword(secureUserPassword);

		user.setLastAccessedOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
		user.setLoggedOnFlag(true);
		//user.setLocationId(user.getLocationId());
		if (user.getUserType().equals("MGR")) {
			user.setTrialPeriodStartDate(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
		}

		userDao.saveUser(user);

		Calendar calendar = Calendar.getInstance();
		Date currentDate = new Date();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DAY_OF_MONTH, 30);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String endTrialPeriodDate = dateFormat.format(calendar.getTime());
		mailService
				.sendMail(constructEmail("Trial Period Information", generateContent(user, endTrialPeriodDate), user));

		String adminMailId = properties.getProperty("mail.userName");
		mailService.sendMail(constructEmail("SingedUp UserDetails", generateContent(user), adminMailId));

		user.clearSecuredData();
	}

	public void loadProperties() {
		InputStream inputStream = null;

		inputStream = this.getClass().getResourceAsStream("/emailServices.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.info(e.getCause());

		}

	}

	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}

	public String generateContent(User user) {

		StringBuilder builder = new StringBuilder();
		builder.append("<html> <br>new user has signed up.<br>" + "<br>Email Id:" + user.getEmail()
				+ ".<br> <br>Email Id:" + user.getPhoneNumber() + "<br>" + "Thanks,<br> TrackMe Inc.</html>");
		return builder.toString();
	}

	public String generateContent(User user, String endTrialPeriodDate) {

		StringBuilder builder = new StringBuilder();
		builder.append(
				"<html>Hi " + user.getFirstName() + "<br>the trial period starts today and ends 30 days from now."
						+ "the trial end date is" + endTrialPeriodDate + ".<br>" + "Thanks,<br> TrackMe Inc.</html>");
		return builder.toString();
	}
	/*
	 * private SimpleMailMessage constructEmail(String subject, String body,
	 * User user) { SimpleMailMessage email = new SimpleMailMessage();
	 * email.setSubject(subject); email.setText(body);
	 * email.setTo(user.getEmail()); return email; }
	 */

	/**
	 * It saves user login,if it is a valid user and encrypts the password
	 * otherwise throws error message
	 */

	public User loginUser(User user) throws Throwable {
		User existingUser = isValidLogin(user);
		if (existingUser != null) {
			logger.info("User data is Valid and processing to Dao");

			try {
				existingUser.setLoggedOnFlag(true);
				existingUser.setLastAccessedOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
				existingUser.setLocationId(existingUser.getLocationId());
				userDao.saveUserLogin(existingUser);
			} catch (Exception e) {
				logger.error("Unable to validate the User Login", e);
			} catch (Throwable t) {
				logger.fatal("Irrecoverable error occurred, contact helpdesk", t);
				throw new UserException("Fatal", t);
			}
		} else {
			logger.info("Email Id or Password are not valid");
			throw new UserException(ErrorConstants.AUTHENTICATIONERROR, ErrorConstants.AUTHENTICATIONERROR_MESSAGE);
		}
		return existingUser;
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

	public void validateNewUser(User user) throws Throwable {
		if (user == null)
			return;

		try {
			user.setEmail(StringUtils.lowerCase(user.getEmail()));
			validateUser(user);
		} catch (UserException e) {
			throw e;
		}
	}
	
	public void validateUser(User user) throws UserException {
		try {
			nameValidation(user.getFirstName(), user.getLastName());
			emailValidation(user.getEmail());
			passwordValidation(user.getPassword());
			userTypeValidation(user.getUserType());
		} catch (UserException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("Unexpected error while validation user", e);
			throw new UserException("Fatal", "Unexpected error while validting user");
		}
	}
	
	private void userTypeValidation(String userType) throws Throwable {
		logger.info("Validating user type");
		if (StringUtils.isBlank(userType)) {
			throw new UserException("Error", "User type must be specified, please retry");
		} else {
			boolean found = false;
			for (UserConstants.USER_TYPE userTypeFromEnum : UserConstants.USER_TYPE.values()) {
				if (StringUtils.equals(userTypeFromEnum.getUserTypeValue(), userType)) {
					found = true;
					break;
				}
			}

			if (!found) {
				throw new UserException("Error", "User type not found, please send a valid user type");
			}
		}
	}
	

	public User isValidLogin(User user) throws Throwable {

		// String encryptPwd = Utils.encryptPassword(user.getPassword());
		// user.setPassword(encryptPwd);

		user.setEmail(StringUtils.lowerCase(user.getEmail()));

		return userDao.checkUserLogin(user);
	}

	public SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, User user) {
		// String url = contextPath + "/user/changePassword?id=" +
		// user.getEmail() + "&token=" + token;
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

	private SimpleMailMessage constructEmail(String subject, String body, String adminMailId) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
		email.setText(body);
		email.setTo(adminMailId);
		return email;
	}

	@Override
	public void changeUserPassword(User user, PasswordDto password) throws Throwable {
		if (StringUtils.equals(password.getPassword(), password.getConfirmPassword())) {
			passwordValidation(password.getPassword());
			// Generate salt
			String salt = authenticationUtil.createSaltValue(30);

			// Generate secure user password
			String secureUserPassword = null;

			try {
				secureUserPassword = authenticationUtil.createSecurePassword(user.getPassword(), salt);
			} catch (InvalidKeySpecException ex) {
				logger.fatal(null, ex);
				throw new UserException("Fatal", ex.getLocalizedMessage());
			}
			user.setSalt(salt);
			user.setPassword(secureUserPassword);
			userDao.changeUserPassword(user);
			user.clearSecuredData();
		}
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

	@Override
	public List<User> getAllUsers(String userType) {
		return userDao.getAllUsers(userType);
	}

	@Override
	public User updateUser(User userRequest) throws Throwable {
		User user = null;
		String userId = userRequest.getUserId();
		if (userId != null)
			user = userDao.findUserById(userId);
		if (user == null) {
			logger.info("User " + userId + " not found");

		}
		validateUpdateRequest(userRequest);
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setEmail(userRequest.getEmail());
		user.setPhoneNumber(userRequest.getPhoneNumber());
		user.setLocationId(userRequest.getLocationId());
		return userDao.updateUser(user);
	}

	private void validateUpdateRequest(User user) throws Throwable {
		nameValidation(user.getFirstName(), user.getLastName());
		emailValidation(user.getEmail());
		passwordValidation(user.getPassword());
		// phoneNumberValidation(user.getPhoneNumber());
	}

	// First Name and Last Name Validation
	private void nameValidation(String firstName, String lastName) throws Throwable {
		logger.info("Validating user first name and last name");
		String letterChars = "[a-zA-Z]+";
		if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
			throw new UserException("Error", "First Name and/or Last Name missing, cannot create the user");
		} else if (StringUtils.length(firstName) > 30 || StringUtils.length(lastName) > 30) {
			throw new UserException("Error", "Name should be under 30 Characters");
		} else if (!firstName.matches(letterChars) || !lastName.matches(letterChars)) {
			throw new UserException("Error", "Name should contain only Characters");
		}
	}

	// Email Address Validation
	private void emailValidation(String email) throws Throwable {
		logger.info("Validating user email");
		if (!email.matches(EMAIL_PATTERN)) {
			throw new UserException("Error", "Email is not Valid");
		}
	}

	// Password Validation
	public void passwordValidation(String password) throws Throwable {
		logger.info("Validating user password");
		if (StringUtils.isBlank(password)) {
			throw new UserException("Error", "Password missing, cannot create the User.");
		} else if (password.length() > 20 || password.length() < 8) {
			throw new UserException("Error",
					"Password should be at least 8 characters and below 20 characters in length.");
		}
		// Upper case characters validation
		if (!password.matches("(.*[A-Z].*)")) {
			throw new UserException("Error", "Password should contain atleast one upper case alphabet");
		}
		// Lower case characters validation
		if (!password.matches("(.*[a-z].*)")) {
			throw new UserException("Error", "Password should contain atleast one lower case alphabet");
		}
		// Number validation
		if (!password.matches("(.*[0-9].*)")) {
			throw new UserException("Error", "Password should contain atleast one number.");
		}
		// Special characters validation
		if (!password.matches("(.*[.,@,#,$,*,_,&].*$)")) {
			throw new UserException("Error",
					"Password should contain atleast one of these special characters (.,@,#,$,*,_,&)");
		}
	}

	// Phone number validation
	private void phoneNumberValidation(String phoneNumber) throws Throwable {
		if (StringUtils.isBlank(phoneNumber)) {
			throw new UserException("Error", "Phone number missing, please enter a valid phone number");
		} else if (StringUtils.length(phoneNumber) != 10) {
			throw new UserException("Error", "Phone number not valid, phone number should be 10 characters");
		} else if (!NumberUtils.isDigits(phoneNumber)) {
			throw new UserException("Error", "Phone number should contain only numbers, please re-enter");
		}
	}

	@Override
	public void validateExpiryUser(String createdOn) throws UserException {
		Date date = null;
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = formatter.parse(createdOn);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date expandDate = addDays(date, 30);
		Date currentDate = new Date();
		if (expandDate.compareTo(currentDate) > 0) {
			throw new UserException("Error", "The trial period ended");
		}
	}

	private Date addDays(Date d, int days) {
		d.setTime(d.getTime() + days * 1000 * 60 * 60 * 24);
		return d;
	}

	@Override
	public void deleteUser(String userId) throws Throwable {
		if (userDao.findUserById(userId) == null) {
			logger.info("user  with id " + userId + " not found");
			throw new UserException("Error", "userId not found, please enter a valid user id");
		}

		userDao.deleteUser(userId);

	}
	
	@Override
	public UserResponse getUserDetails(User user) throws Throwable {
		logger.info("Getting all the user data");
		UserResponse userResponse = null;
		userResponse = new UserResponse();
		//User userDetail = new User();
		try {
			User newUser = userDao.getUserDetails(user.getUserId());
			if (newUser != null) {
				userResponse.setSuccess("true");
				userResponse.setUser(newUser);
			} else {
				userResponse.setSuccess("false");
				userResponse.setError("user id does not exist");
			}
		} catch (UserException e) {
			throw new UserException("Error", "user id   not found, please enter a valid  user id");
		} catch (Throwable t) {
			logger.info("user  with id  " + " not found");
			throw new UserException("Error", "user id   not found, please enter a valid  user id");
		}
		return userResponse;
	}

}
