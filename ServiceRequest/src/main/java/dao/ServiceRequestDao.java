package com.selsoft.servicerequest.dao;

import java.util.List;

import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.User;

public interface ServiceRequestDao {

	public void saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	public User isUserManagerById(String userId) throws Throwable;

	public List<ServiceRequest> fetchOpenServiceRequestById(String managerId) throws Throwable;

	public List<ServiceRequest> fetchAssignServiceRequestById(String managerId) throws Throwable;

	public List<ServiceRequest> fetchInProgressServiceRequestById(String managerId) throws Throwable;

	public void deleteServiceRequest(ServiceRequest serviceRequest) throws Throwable;

	public ServiceRequest findServiceRequestById(String serviceRequestId) throws Throwable;

	public ServiceRequest updateserviceRequest(ServiceRequest serviceRequest) throws Throwable;

	public User findById(String userId) throws Throwable;

	public ServiceRequest getServiceRequestByProperty(String propertyId) throws Throwable;

	public ServiceRequest cancelServiceRequest(String serviceRequestId) throws Throwable;
}
