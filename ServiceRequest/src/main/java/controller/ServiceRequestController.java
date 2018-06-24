package com.selsoft.servicerequest.controller;

import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.selsoft.servicerequest.service.ServiceRequestService;
import com.selsoft.trackme.exception.ServiceRequestException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.ServiceResponse;

@RestController
@RequestMapping(value = "/servicerequest")
public class ServiceRequestController {
	private static final Logger logger = Logger.getLogger(ServiceRequestController.class);

	@Autowired
	private ServiceRequestService serviceRequestService;

	@Autowired(required = true)
	private Environment environment;

	@RequestMapping(value = "/createServiceRequest", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createServiceRequest(@RequestBody ServiceRequest serviceRequest) throws Throwable {

		JSONObject jsonObject = new JSONObject();

		logger.info("Creating service request ");

		try {
			serviceRequestService.saveNewServiceRequest(serviceRequest);
			jsonObject.put("success", "true");
		} catch (ServiceRequestException e) {
			jsonObject.put("success", false);
			serviceRequest.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			serviceRequest.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			serviceRequest.addError(new Error("Fatal", t.toString()));
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/openServiceRequestById/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ServiceResponse> openServiceRequestById(@PathVariable("userId") String userId) {
		JSONObject jsonObject = new JSONObject();
		List<ServiceResponse> serviceResponseList = null;
		try {
			serviceResponseList = serviceRequestService.fetchOpenServiceRequestById(userId);
			jsonObject.put("success", "true");
		} catch (ServiceRequestException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(e));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(new ServiceRequestException("Fatal", t)));
		}
		return serviceResponseList;

	}

	@RequestMapping(value = "/assignServiceRequestById/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ServiceResponse> assignServiceRequestById(@PathVariable("userId") String userId) {
		JSONObject jsonObject = new JSONObject();
		List<ServiceResponse> serviceResponseList = null;
		try {
			serviceResponseList = serviceRequestService.fetchAssignServiceRequestById(userId);
			jsonObject.put("success", "true");
		} catch (ServiceRequestException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(e));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(new ServiceRequestException("Fatal", t)));
		}

		return serviceResponseList;

	}

	@RequestMapping(value = "/inProgressServiceRequestById/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ServiceResponse> inProgressServiceRequestById(@PathVariable("userId") String userId) {
		JSONObject jsonObject = new JSONObject();
		List<ServiceResponse> serviceResponseList = null;

		try {
			serviceResponseList = serviceRequestService.fetchInProgressServiceRequestById(userId);
			jsonObject.put("success", "true");
		} catch (ServiceRequestException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(e));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(new ServiceRequestException("Fatal", t)));
		}

		return serviceResponseList;

	}

	@RequestMapping(value = "/cancelServiceRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String cancelServiceRequest(@RequestBody ServiceRequest serviceRequest) {
		JSONObject jsonObject = new JSONObject();

		try {
			serviceRequest = serviceRequestService.cancelServiceRequest(serviceRequest);
			jsonObject.put("success", true);
		} catch (ServiceRequestException e) {
			logger.info("Error while cancelling the service request", e);
			jsonObject.put("success", false);
			jsonObject.put("message", "already cancelled,plz enter valid user id and servicerequest id");
			serviceRequest.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while cancelling the service request", t);
			jsonObject.put("success", false);
			serviceRequest.addError(new Error(new ServiceRequestException("Fatal", t)));
		}
		jsonObject.put("serviceRequest", serviceRequest.toJSON());

		return jsonObject.toString();
	}

	@RequestMapping(value = "/deleteServiceRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteServiceRequest(@RequestBody ServiceRequest serviceRequest) {
		JSONObject jsonObject = new JSONObject();

		logger.info("deleting user ");

		try {
			serviceRequest = serviceRequestService.deleteServiceRequest(serviceRequest);
			jsonObject.put("sucess", true);
			jsonObject.put("message", "user id and service id   deleted successfully");
		} catch (Throwable e) {
			jsonObject.put("success", false);
			jsonObject.put("message", "user id and service id does not exist");
		}
		return jsonObject.toString();
	}

	@RequestMapping(value = "/updateServiceRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateServiceRequest(@RequestBody ServiceRequest serviceRequest) {
		JSONObject jsonObject = new JSONObject();
		try {
			serviceRequest = serviceRequestService.updateServiceRequest(serviceRequest);
			jsonObject.put("success", true);
		} catch (ServiceRequestException e) {
			logger.info("Error while updating the service request", e);
			jsonObject.put("success", false);
			serviceRequest.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Error while updating the service request", t);
			jsonObject.put("success", false);
			serviceRequest.addError(new Error(new ServiceRequestException("Fatal", t)));
		}
		jsonObject.put("transaction", serviceRequest.toJSON());

		return jsonObject.toString();
	}

}
