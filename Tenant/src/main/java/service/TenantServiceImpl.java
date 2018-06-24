package com.selsoft.tenant.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.selsoft.tenant.dao.TenantDAO;
import com.selsoft.tenant.email.service.MailSenderService;
import com.selsoft.trackme.constants.TenantConstants.TENANT_STATUS;
import com.selsoft.trackme.exception.TenantException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.utils.TrackMeUtils;

@Service("tenantService")
public class TenantServiceImpl implements TenantService {

	@Autowired
	private TenantDAO tenantDao;
	
	@Autowired(required = true)
	private Environment environment;
	
	@Autowired
	private VelocityEngine velocityEngine;

	@Autowired(required = true)
	private JavaMailSender mailSender;

	@Autowired
	private MailSenderService mailSenderService;

	@Autowired
	Properties properties;
	
	private static final Logger logger = Logger.getLogger(TenantService.class);

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	/**
	 * add new tenant to the tenant table
	 * 
	 * @throws Throwable
	 */
	@Override
	public Tenant addNewTenant(Tenant tenant, String activationUrl) throws Throwable {
		validateNewTenant(tenant);

		User user = null;

		if (tenant.isCreateUser()) {
			user = getUserObject(tenant);
			validateNewUser(user);
		}

		try {
			tenantDao.saveNewTenant(tenant);

			if (tenant.isCreateUser() && user != null && StringUtils.isNotBlank(user.getEmail())) {
				user.setUserType(tenant.getTenantId());
				addNewUser(user, activationUrl);
			}

		} catch (TenantException e) {
			if (StringUtils.equals("Fatal", e.getErrorType())) {
				logger.fatal("Fatal error occurred, rolling back tenant and user", e);
				tenantDao.deleteTenant(tenant);
				if (tenant.isCreateUser()) {
					tenantDao.deleteUser(user);
				}
				throw e;
			}
		}

		return tenant;
	}

	private void validateNewTenant(Tenant tenant) throws Throwable {
		tenant.setTenantStatus(TENANT_STATUS.NEW.getValue());
		tenant.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
		tenant.setTenantFirstName(StringUtils.trimToEmpty(tenant.getTenantFirstName()));
		tenant.setTenantLastName(StringUtils.trimToEmpty(tenant.getTenantLastName()));
		tenant.setManagerId(StringUtils.trimToEmpty(tenant.getManagerId()));
		tenant.setEnteredBy(StringUtils.trimToEmpty(tenant.getEnteredBy()));

		nameValidation(tenant.getTenantFirstName(), tenant.getTenantLastName());

		if (StringUtils.isNotBlank(tenant.getTenantEmailId())) {
			tenant.setTenantEmailId(StringUtils.trimToEmpty(StringUtils.lowerCase(tenant.getTenantEmailId())));
			emailValidation(tenant.getTenantEmailId());
		}

		if (StringUtils.isNotBlank(tenant.getTenantPhoneNumber())) {
			tenant.setTenantPhoneNumber(StringUtils.trimToEmpty(tenant.getTenantPhoneNumber()));
			phoneNumberValidation(tenant.getTenantPhoneNumber());
		}

		managerValidation(tenant.getManagerId());
		enteredByValidation(tenant.getEnteredBy());
	}

	// First Name and Last Name Validation
	private void nameValidation(String firstName, String lastName) throws Throwable {
		logger.info("Validating user first name and last name");
		String letterChars = "[a-zA-Z]+";
		if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
			throw new TenantException("Error", "First Name and/or Last Name missing, cannot create the user");
		} else if (StringUtils.length(firstName) > 30 || StringUtils.length(lastName) > 30) {
			throw new TenantException("Error", "Name should be under 30 Characters");
		} else if (!firstName.matches(letterChars) || !lastName.matches(letterChars)) {
			throw new TenantException("Error", "Name should contain only Characters");
		}
	}

	// Email Address Validation
	private void emailValidation(String email) throws Throwable {
		logger.info("Validating user email");
		if (StringUtils.isBlank(email)) {
			throw new TenantException("Error", "Email id missing, please enter a proper email id");
		} else if (!email.matches(EMAIL_PATTERN)) {
			throw new TenantException("Error", "Email is not Valid");
		} else if (tenantDao.getTenantByEmail(email) != null) {
			throw new TenantException("Error", "Tenant with the email id " + email
					+ " already exists, please add a tenant with a different email id.");
		}
	}

	// Phone number validation
	private void phoneNumberValidation(String phoneNumber) throws Throwable {
		if (StringUtils.isBlank(phoneNumber)) {
			throw new TenantException("Error", "Phone number missing, please enter a valid phone number");
		} else if (StringUtils.length(phoneNumber) != 10) {
			throw new TenantException("Error", "Phone number not valid, phone number should be 10 characters");
		} else if (!NumberUtils.isDigits(phoneNumber)) {
			throw new TenantException("Error", "Phone number should contain only numbers, please re-enter");
		}
	}

	// Manager validation
	private void managerValidation(String managerId) throws Throwable {
		managerId = StringUtils.trimToEmpty(managerId);
		if (StringUtils.isNotBlank(managerId)) {
			if (tenantDao.findManagerByUserId(managerId) == null) {
				throw new TenantException("Error", "Manager information passed is not valid, cannot add a tenant");
			}
		} else {
			throw new TenantException("Error",
					"Manager information not present, manager id needed for adding a tenant");
		}
	}

	/**
	 * get all the tenants from the tenant table
	 */
	@Override
	public List<Tenant> getAllTenants(String status) {
		return tenantDao.fetchTenants(status);
	}

	public List<Tenant> getAllTenantsForManager(String managerId) throws Throwable {
		List<Tenant> tenants = null;

		if (StringUtils.isBlank(managerId))
			throw new TenantException("Error", "Manager id not present, cannot fetch tenants");

		try {
			tenants = tenantDao.fetchTenantsForManager(managerId, null);

			if (tenants != null && tenants.size() > 0) {
				Lease lease = null;
				for (Tenant tenant : tenants) {
					if (tenant != null && StringUtils.equals(tenant.getTenantStatus(), "Active")) {
						lease = tenantDao.getActiveLeaseDetailForTenantById(tenant.getTenantId());
						if (lease != null) {
							tenant.setLeaseData(lease);
						}
					}
				}
			}

		} catch (TenantException e) {
			throw e;
		} catch (Throwable t) {
			logger.error("Error while getting all tenants for manager : " + managerId, t);
			throw new TenantException("Error", "Error while getting all tenants, please try again later");
		}
		return tenants;
	}

	public List<Tenant> getTenantsForManagerBasedOnStatus(TENANT_STATUS tenantStatus, String managerId) throws Throwable {
		List<Tenant> tenants = null;

		if (StringUtils.isBlank(managerId))
			throw new TenantException("Error", "Manager id not present, cannot fetch tenants");

		try {
			tenants = tenantDao.fetchTenantsForManager(managerId, tenantStatus);
		} catch (TenantException e) {
			throw e;
		} catch (Throwable t) {
			logger.error("Error while getting all tenants for manager : " + managerId, t);
			throw new TenantException("Error", "Error while getting all tenants, please try again later");
		}
		return tenants;
	}

	public User addNewUser(User user, String activationUrl) throws Throwable {

		validateNewUser(user);

		logger.info("User data is Valid and processing to Dao");

		user.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());

		tenantDao.saveUser(user);

		try {
			if (user.isSendActivation()) {
				sendUserActivationEmail(user, "a Tenant", activationUrl);
			}
		} catch (Throwable t) {
			logger.error("Error while sending activation email", t);
			tenantDao.deleteUser(user);
			throw new TenantException("Error", "Error while sending activation email, cannot add user/tenant");
		}

		user.clearSecuredData();
		return user;
	}

	@Override
	public void updateTenant(Tenant newTenant) throws Throwable {
		Tenant tenantFromDB = null;
		String id = newTenant.getTenantId();
		if (StringUtils.isNotBlank(id)) {
			tenantFromDB = tenantDao.findTenantById(id);
		} else {
			throw new TenantException("Error", "Invalid update request, tenant id not found");
		}

		if (tenantFromDB == null) {
			logger.info("Tenant with id " + id + " not found");
			throw new TenantException("Error", "Invalid tenant detail, tenant not found");
		}

		validateUpdateRequest(newTenant, tenantFromDB);

		try {
			tenantDao.updateTenant(newTenant);
			if (StringUtils.isNotBlank(newTenant.getTenantEmailId())
					&& !StringUtils.equals(newTenant.getTenantEmailId(), tenantFromDB.getTenantEmailId())) {
				tenantDao.updateUserEmail(newTenant);
			}
		} catch (Throwable e) {
			logger.fatal("Error occured while saving tenant/user to the database", e);
			tenantDao.updateTenant(tenantFromDB);
			tenantDao.updateUserEmail(tenantFromDB);
			throw new TenantException("Fatal", e);
		}
	}

	private void validateUpdateRequest(Tenant newTenant, Tenant tenantFromDB) throws Throwable {
		nameValidation(newTenant.getTenantFirstName(), newTenant.getTenantLastName());

		newTenant.setTenantEmailId(StringUtils.lowerCase(newTenant.getTenantEmailId()));
		tenantFromDB.setTenantEmailId(StringUtils.lowerCase(newTenant.getTenantEmailId()));

		if (StringUtils.isNotBlank(newTenant.getTenantEmailId())
				&& !StringUtils.equals(newTenant.getTenantEmailId(), tenantFromDB.getTenantEmailId())) {
			emailValidation(newTenant.getTenantEmailId());

			// Check if there is an active user for the old tenant email id, if
			// so throw an error
			if (StringUtils.isNotBlank(tenantFromDB.getTenantEmailId())) {
				User userForOldOwnerEmail = tenantDao.findUserByEmail(tenantFromDB.getTenantEmailId());
				if (userForOldOwnerEmail != null
						&& StringUtils.equals(userForOldOwnerEmail.getUserStatus(), "Active")) {
					throw new TenantException("Error",
							"Invalid update request, a user is active with the old email id for this tenant. Cannot update.");
				}
			}

			// Check if there is a user present for the new tenant email id, if
			// so throw an error
			User userForNewOwnerEmail = tenantDao.findUserByEmail(newTenant.getTenantEmailId());
			if (userForNewOwnerEmail != null) {
				throw new TenantException("Error",
						"Invalid update request, a user record is already present with the email id entered for this tenant. Cannot update.");
			}
		}

		if (StringUtils.isNotBlank(newTenant.getTenantPhoneNumber())) {
			newTenant.setTenantPhoneNumber(StringUtils.trimToEmpty(newTenant.getTenantPhoneNumber()));
			phoneNumberValidation(newTenant.getTenantPhoneNumber());
		}
	}

	private User getUserObject(Tenant tenant) throws Throwable {
		User user = new User();
		user.setUserTypeId(tenant.getTenantId());
		user.setManagerId(tenant.getManagerId());
		user.setEnteredBy(tenant.getEnteredBy());
		user.setEmail(tenant.getTenantEmailId());
		user.setFirstName(tenant.getTenantFirstName());
		user.setLastName(tenant.getTenantLastName());
		user.setUserStatus("Pending");
		user.setUserType("TNT");
		user.setAddress1(tenant.getAddress1());
		user.setAddress2(tenant.getAddress2());
		user.setAddress3(tenant.getAddress3());
		user.setCity(tenant.getCity());
		user.setState(tenant.getState());
		user.setCountry(tenant.getCountry());
		user.setZipCode(tenant.getZipCode());
		user.setPhoneNumber(tenant.getTenantPhoneNumber());
		user.setSendActivation(tenant.isCreateUser());
		return user;
	}

	public void validateNewUser(User user) throws Throwable {
		if (user == null)
			return;

		try {
			user.setEmail(StringUtils.lowerCase(user.getEmail()));
			user.setFirstName(StringUtils.trimToEmpty(user.getFirstName()));
			user.setLastName(StringUtils.trimToEmpty(user.getLastName()));

			nameValidation(user.getFirstName(), user.getLastName());

			if (StringUtils.isBlank(user.getEmail())) {
				throw new TenantException("Error",
						"Cannot create/update tenant, please add email for the tenant to create a user ");
			} else if (findUserByEmail(user.getEmail()) != null) {
				throw new TenantException("Error", "Cannot add user, user with email : " + user.getEmail()
						+ " already exists. Please use a different email id.");
			}

		} catch (TenantException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("Unexpected error while validation user", e);
			throw new TenantException("Fatal", "Unexpected error while validting user");
		}
	}

	private void enteredByValidation(String enteredBy) throws Throwable {
		enteredBy = StringUtils.trimToEmpty(enteredBy);
		if (StringUtils.isNotBlank(enteredBy)) {
			if (tenantDao.findManagerByUserId(enteredBy) == null) {
				throw new TenantException("Error", "Entered By information passed is not valid, cannot add a tenant");
			}
		} else {
			throw new TenantException("Error",
					"Entered By information not present, entered By id needed for adding a tenant");
		}
	}

	public User findUserByEmail(String email) throws Throwable {
		return tenantDao.findUserByEmail(email);
	}

	private void sendUserActivationEmail(final User user, final String userType, final String url)
			throws TenantException {
		try {
			String activationToken = tenantDao.createTemporaryTokenAndLinkForUserActivation(user.getEmail());
			MimeMessagePreparator preparator = new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
					message.setTo(user.getEmail());
					message.setFrom(environment.getProperty("mail.from")); // could
																			// be
																			// parameterized...
					message.setSubject(environment.getProperty("mail.activateUserEmailSubject")); // could
																									// be
																									// parameterized...
					Map<String, Object> model = new HashMap<String, Object>();
					StringWriter writer = new StringWriter();
					model.put("user", user);
					Context context = new VelocityContext();
					context.put("userType", userType);
					context.put("firstName", user.getFirstName());
					context.put("emailId", user.getEmail());
					context.put("token", activationToken);
					context.put("url", url);
					velocityEngine.mergeTemplate("/templates/ActivateUserEmailTemplate.vm", "UTF-8", context, writer);
					message.setText(writer.toString(), true);
				}
			};
			this.mailSender.send(preparator);
		} catch (Throwable e) {
			logger.error("Error while sending password reset email for user id " + user.getUserId()
					+ " with email id : " + user.getEmail(), e);
			throw new TenantException("Fatal", "Error while sending password reset email");
		}
	}

	public void activateUser(Tenant tenant, String activationUrl) throws Throwable {
		if (tenant == null)
			throw new TenantException("Fatal", "Invalid request");
		if (StringUtils.isNotBlank(tenant.getTenantId())) {
			tenant = tenantDao.findTenantById(tenant.getTenantId());
			if (tenant != null) {
				User user = getUserObject(tenant);
				user.setSendActivation(true);
				addNewUser(user, activationUrl);
			} else {
				logger.error("Error while activating user, tenant information is not present in database");
				throw new TenantException("Error",
						"Error while activating user, tenant information is not present in database");
			}
		} else {
			logger.error("Error while activating user, tenant id is missing");
			throw new TenantException("Error", "Error while activating user, tenant id is missing");
		}
	}

	/**
	 * save the new service request to ServiceRequest table
	 */
	public ServiceRequest saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		try {

			tenantDao.saveNewServiceRequest(serviceRequest);
			sendMailtoManager();
			// sendMailtoOwner();

		} catch (Throwable t) {
			logger.fatal("` while saving service request", t);
			throw new TenantException("Fatal", "Errow while saving service request, please try again later");
		}
		return serviceRequest;
	}

	private void sendMailtoManager() {
		String to = "silu.lect@gmail.com";
		String subject = "Hi Sudhansu";
		String body = "mail body";

		mailSenderService.sendMail(constructEmail(subject, body, to));

	}

	private String generateContent(User user) {

		StringBuilder builder = new StringBuilder();
		builder.append("<html> <br>User information <br>" + "<br>Email Id:" + user.getEmail() + "<br>"
				+ "Thanks,<br> TrackMe Inc.</html>");
		return builder.toString();
	}

	/*
	 * private void sendMailtoOwner() {
	 * 
	 * Owner owner=null; mailSender
	 * .sendMail(constructEmail("Tenant Information", generateContent(owner)); }
	 */

	private SimpleMailMessage constructEmail(String subject, String body, String to) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
		email.setText(body);
		email.setTo(to);
		return email;
	}

	private String generateContent(Owner owner) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html> <br>Tenant information <br>" + "<br>Email Id:" + owner.getEmail() + "<br>"
				+ "Thanks,<br> TrackMe Inc.</html>");
		return builder.toString();
	}

	private SimpleMailMessage constructEmail(String subject, String body, Owner owner) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
		email.setText(body);
		email.setTo(owner.getEmail());
		return email;
	}

	@Override
	public void deleteTenant(String tenantId) throws Throwable {
		if (tenantDao.findById(tenantId) == null) {
			logger.info("Tenant with id " + tenantId + " not found");
			throw new TenantException("Error", "tenant id not found, please enter a valid tenant id");
		}

		tenantDao.deleteTenant(tenantId);
	}

//	@Override
//	public ServiceRequest saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable {
//		try {
//
//			tenantDao.saveNewServiceRequest(serviceRequest);
//			sendMailtoManager();
//			// sendMailtoOwner();
//
//		} catch (Throwable t) {
//			logger.fatal("` while saving service request", t);
//			throw new TenantException("Fatal", "Errow while saving service request, please try again later");
//		}
//		return serviceRequest;
//	}


//	@Override
//	public String getTenantStatusById(int id) {
//		return leaseDAO.getTenantStatusById(id);
//	}

}
