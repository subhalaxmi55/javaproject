package com.selsoft.servicerequest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.selsoft.servicerequest.dao.ServiceRequestDao;
import com.selsoft.servicerequest.email.service.MailSenderService;
import com.selsoft.trackme.exception.ServiceRequestException;
import com.selsoft.trackme.exception.UserException;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.ServiceResponse;
import com.selsoft.trackme.models.User;

@Service("serviceRequestService")
public class ServiceRequestServiceImpl implements ServiceRequestService {

	@Autowired
	private ServiceRequestDao serviceRequestDao;

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

	private static final Logger logger = Logger.getLogger(ServiceRequestServiceImpl.class);

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	/**
	 * save the new service request to ServiceRequest table
	 */
	public ServiceRequest saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		try {

			serviceRequestDao.saveNewServiceRequest(serviceRequest);
			sendMailtoManager();
			// sendMailtoOwner();

		} catch (Throwable t) {
			logger.fatal("` while saving service request", t);
			throw new ServiceRequestException("Fatal", "Errow while saving service request, please try again later");
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

	public List<ServiceResponse> fetchOpenServiceRequestById(String userId) throws Throwable {

		User manager = null;

		List<ServiceResponse> serviceResponseList = null;

		List<ServiceRequest> serviceRequestList = null;

		if (StringUtils.isBlank(userId))

			throw new ServiceRequestException("Error", "user  id not present, cannot fetch open status");

		serviceResponseList = new ArrayList<>();

		try {

			manager = serviceRequestDao.isUserManagerById(userId);

			serviceRequestList = serviceRequestDao.fetchOpenServiceRequestById(manager.getUserId());

			for (ServiceRequest serviceRequest : serviceRequestList) {

				ServiceResponse serviceRes = new ServiceResponse();

				serviceRes.setManagerId(serviceRequest.getManagerId());

				serviceRes.setOwnerId(serviceRequest.getOwnerId());

				serviceRes.setPropertyId(serviceRequest.getPropertyId());

				serviceRes.setTenantId(serviceRequest.getTenantId());

				serviceResponseList.add(serviceRes);

			}

		} catch (ServiceRequestException e) {

			throw e;

		} catch (Throwable t) {

			logger.error("Error while getting all users for service request : " + userId, t);

			throw new ServiceRequestException("Error", "Error while getting all user id, please try again later");

		}
		return serviceResponseList;

	}

	@Override

	public List<ServiceResponse> fetchAssignServiceRequestById(String userId) throws Throwable {

		List<ServiceRequest> serviceRequestList = null;

		List<ServiceResponse> serviceResponseList = null;

		User manager = null;

		if (StringUtils.isBlank(userId))

			throw new ServiceRequestException("Error", "user  id not present, cannot fetch Assigned status");

		try {

			manager = serviceRequestDao.isUserManagerById(userId);

			serviceRequestList = serviceRequestDao.fetchAssignServiceRequestById(manager.getUserId());

			for (ServiceRequest serviceRequest : serviceRequestList) {

				ServiceResponse serviceRes = new ServiceResponse();

				serviceRes.setManagerId(serviceRequest.getManagerId());

				serviceRes.setOwnerId(serviceRequest.getOwnerId());

				serviceRes.setPropertyId(serviceRequest.getPropertyId());

				serviceRes.setTenantId(serviceRequest.getTenantId());

				// serviceRes.setserviceRequestId(serviceRequest.getserviceRequestId());

				serviceResponseList.add(serviceRes);

			}

		} catch (ServiceRequestException e) {

			throw e;

		} catch (Throwable t) {

			logger.error("Error while getting all users for service request : " + userId, t);

			throw new ServiceRequestException("Error", "Error while getting all user id, please try again later");

		}
		return serviceResponseList;

	}

	@Override

	public List<ServiceResponse> fetchInProgressServiceRequestById(String userId) throws Throwable {

		List<ServiceRequest> serviceRequestList = null;

		List<ServiceResponse> serviceResponseList = null;

		User manager = null;

		if (StringUtils.isBlank(userId))

			throw new ServiceRequestException("Error", "user  id not present, cannot fetch In progress");

		try {

			manager = serviceRequestDao.isUserManagerById(userId);

			serviceRequestList = serviceRequestDao.fetchInProgressServiceRequestById(manager.getUserId());

			for (ServiceRequest serviceRequest : serviceRequestList) {

				ServiceResponse serviceRes = new ServiceResponse();

				serviceRes.setManagerId(serviceRequest.getManagerId());

				serviceRes.setOwnerId(serviceRequest.getOwnerId());

				serviceRes.setPropertyId(serviceRequest.getPropertyId());

				serviceRes.setTenantId(serviceRequest.getTenantId());

				// serviceRes.setserviceRequestId(serviceRequest.getserviceRequestId());

				serviceResponseList.add(serviceRes);

			}

		} catch (ServiceRequestException e) {

			throw e;

		} catch (Throwable t) {

			logger.error("Error while getting all users for service request : " + userId, t);

			throw new ServiceRequestException("Error", "Error while getting all user id, please try again later");

		}

		return serviceResponseList;

	}

	@Override
	public ServiceRequest deleteServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		logger.info("Deleting service request data");
		String serviceRequestId = serviceRequest.getServiceRequestId();
		String userId = serviceRequest.getUserId();
		if ((serviceRequestDao.findServiceRequestById(serviceRequestId)== null)
				|| (serviceRequestDao.findById(userId)== null)) {
			logger.info("user  with id and serviceRequest with id " + serviceRequestId + userId + " not found");
			throw new ServiceRequestException("Error",
					"serviceRequestId and not found, please enter a valid serviceRequest id and user id");
		}

		serviceRequestDao.deleteServiceRequest(serviceRequest);
		return serviceRequest;

	}

	@Override
	public ServiceRequest updateServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		ServiceRequest serviceRequestFromDB = null;
		User user = null;
		String userType = "";
		String requestStatus = "";
		String userId = serviceRequest.getUserId();
		String serviceRequestId = serviceRequest.getServiceRequestId();
		try {
			user = serviceRequestDao.findById(userId);
			serviceRequestFromDB = serviceRequestDao.findServiceRequestById(serviceRequestId);

			if (userType != null) {
				userType = user.getUserType();
				if (userType.equalsIgnoreCase("TNT")
						&& (requestStatus.equalsIgnoreCase("Open") || requestStatus.equalsIgnoreCase("New"))) {

					serviceRequestFromDB.setRequestSummary(serviceRequest.getRequestSummary());
					serviceRequestFromDB.setRequestDescription(serviceRequest.getRequestDescription());
					serviceRequestFromDB.setRequestType(serviceRequest.getRequestType());
					serviceRequestFromDB.setComments(serviceRequest.getComments());
				}

				if (requestStatus != null) {
					requestStatus = serviceRequestFromDB.getRequestStatus();
					if (userType.equalsIgnoreCase("MGR")
							&& (requestStatus.equalsIgnoreCase("Open") || requestStatus.equalsIgnoreCase("New"))) {
						serviceRequestFromDB.setRequestType(serviceRequest.getRequestType());
						serviceRequestFromDB.setComments(serviceRequest.getComments());
						serviceRequestFromDB.setRequestStatus(serviceRequest.getRequestStatus());
						serviceRequestFromDB.setVendorId(serviceRequest.getVendorId());
						serviceRequestFromDB.setCost(serviceRequest.getCost());
					}
					if (userType.equalsIgnoreCase("MGR") && (requestStatus.equalsIgnoreCase("In progress"))) {
						serviceRequestFromDB.setRequestStatus(serviceRequest.getRequestStatus());
						serviceRequestFromDB.setCost(serviceRequest.getCost());
						serviceRequestFromDB.setComments(serviceRequest.getComments());
					}
					if (userType.equalsIgnoreCase("MGR") && (requestStatus.equalsIgnoreCase("In progress"))) {
						serviceRequestFromDB.setComments(serviceRequest.getComments());
					}
					if(serviceRequestId !=null){
						serviceRequestFromDB.setOwnerId(serviceRequest.getOwnerId());	
					}
				}
			}

			serviceRequestDao.updateserviceRequest(serviceRequest);
		} catch (Exception e) {
			throw new ServiceRequestException("Error", "Invalid cancel request, invalid id");

		}

		return serviceRequestFromDB;

	}

	
	@Override
	public ServiceRequest cancelServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		logger.info("Cancelling the Service request");

		ServiceRequest serviceRequestFromDB = null;
		User user = null;
		String userType = "";
		String requestStatus = "";
		String userId = "";
		userId = serviceRequest.getUserId();
		String serviceRequestId = "";
		serviceRequestId = serviceRequest.getServiceRequestId();
		if (StringUtils.isBlank(userId) && StringUtils.isBlank(serviceRequestId))

			throw new ServiceRequestException("Error",
					"user  id  and serviceRequest id are not present, cannot cancel service request");

		try {
			user = serviceRequestDao.findById(userId);
			serviceRequestFromDB = serviceRequestDao.findServiceRequestById(serviceRequestId);

			if (userType != null && requestStatus != null) {
				userType = user.getUserType();
				requestStatus = serviceRequestFromDB.getRequestStatus();
				if ((userType.equalsIgnoreCase("TNT")
						&& (requestStatus.equalsIgnoreCase("Open") || requestStatus.equalsIgnoreCase("New")))
						|| (userType.equalsIgnoreCase("MGR") && (requestStatus.equalsIgnoreCase("Open")
								|| requestStatus.equalsIgnoreCase("New") || requestStatus.equalsIgnoreCase("Assigned")
								|| requestStatus.equalsIgnoreCase("In progress")))) {
					serviceRequestFromDB = serviceRequestDao.cancelServiceRequest(serviceRequestId);
				}
			} else {
				throw new ServiceRequestException("Error", "Invalid cancel request");

			}
		} catch (Exception e) {
			throw new ServiceRequestException("Error", "Invalid cancel request, invalid id");

		}

		return serviceRequestFromDB;

	}

}