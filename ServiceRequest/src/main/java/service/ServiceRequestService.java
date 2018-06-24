package com.selsoft.servicerequest.service;

import java.util.List;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.ServiceResponse;

public interface ServiceRequestService {
	public ServiceRequest saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	public List<ServiceResponse> fetchOpenServiceRequestById(String userId) throws Throwable;

	public List<ServiceResponse> fetchAssignServiceRequestById(String userId) throws Throwable;

	public List<ServiceResponse> fetchInProgressServiceRequestById(String userId) throws Throwable;

	public ServiceRequest deleteServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	public ServiceRequest updateServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	//public ServiceRequest cancelServiceRequest(String userId, String serviceRequestId) throws Throwable;

	public ServiceRequest cancelServiceRequest(ServiceRequest serviceRequest) throws Throwable;

}
