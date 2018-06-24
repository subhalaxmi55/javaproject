package com.selsoft.servicerequest.dao;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.selsoft.trackme.exception.LeaseException;
import com.selsoft.trackme.exception.ServiceRequestException;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.User;

@Repository
@Qualifier("serviceRequestDao")
public class ServiceRequestDaoImpl implements ServiceRequestDao {
	private static final Logger logger = Logger.getLogger(ServiceRequestDaoImpl.class);

	@Autowired
	private MongoTemplate template;

	final String COLLECTION = "SERVICEREQUEST";

	public void saveNewServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		try {
			template.insert(serviceRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public User isUserManagerById(String userId) throws Throwable {
		User manager = null;

		try {
			manager = template.findOne(new Query(Criteria.where("_id").is(userId).and("userType").is("MGR")),
					User.class);

		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + userId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return manager;

	}

	@Override
	public List<ServiceRequest> fetchOpenServiceRequestById(String managerId) throws Throwable {
		List<ServiceRequest> serviceRequestlist = null;
		ServiceRequest serviceRequest = null;

		try {
			serviceRequestlist = template.find(
					new Query(Criteria.where("managerId").is(managerId).and("requestStatus").is("Open")),
					ServiceRequest.class);

		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return serviceRequestlist;

	}

	@Override
	public List<ServiceRequest> fetchAssignServiceRequestById(String managerId) throws Throwable {
		List<ServiceRequest> serviceRequestlist = null;

		try {
			serviceRequestlist = template.find(
					new Query(Criteria.where("managerId").is(managerId).and("requestStatus").is("Assigned")),
					ServiceRequest.class);

		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return serviceRequestlist;

	}

	@Override
	public List<ServiceRequest> fetchInProgressServiceRequestById(String managerId) throws Throwable {
		List<ServiceRequest> serviceRequestlist = null;

		try {

			serviceRequestlist = template.find(
					new Query(Criteria.where("managerId").is(managerId).and("requestStatus").is("In progress")),
					ServiceRequest.class);

		} catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + managerId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return serviceRequestlist;
	}

	@Override
	public void deleteServiceRequest(ServiceRequest serviceRequest) throws Throwable {
		String serviceRequestId="";
		serviceRequestId=serviceRequest.getServiceRequestId();
		try{
		if (StringUtils.isNotBlank(serviceRequestId)) {
		template.remove(new Query(Criteria.where("serviceRequestId").is(serviceRequestId)), ServiceRequest.class);
		}
		}catch (Throwable t) {
			logger.error("Error while fetching service request id : " + serviceRequestId, t);
			throw new ServiceRequestException("Fatal", t);
		}
	}

	@Override
	public ServiceRequest findServiceRequestById(String serviceRequestId) throws Throwable {
		ServiceRequest request=null;
		try{
		Query query = new Query(Criteria.where("serviceRequestId").is(serviceRequestId));
		 request = template.findOne(query, ServiceRequest.class); 
		}catch (Throwable t) {
			logger.error("Error while fetching user for user id : " + serviceRequestId, t);
			throw new ServiceRequestException("Fatal", t);
		}
		return request;
	}

	@Override
	public ServiceRequest updateserviceRequest(ServiceRequest serviceRequest) throws Throwable {
		ServiceRequest serviceRequestFromDB = null;
		String serviceRequestId = "";
		serviceRequestId = serviceRequest.getServiceRequestId();
		Query query = null;
		Update update = null;
		if (StringUtils.isNotBlank(serviceRequestId)) {
			query = new Query(Criteria.where("serviceRequestId").is(serviceRequestId));
			update = new Update();
			update.set(" requestStatus", "Cancelled/Closed");
			update.set("comment", "Assigned to Vendor");
			update.set("commentedBy", "user id of the person commenting");
			update.set("commentedOn", "get the current timestamp as string from trackmeutils");
			update.set("comment", "Vendor started working");
			template.updateFirst(query, update, ServiceRequest.class);

		}
		serviceRequestFromDB = findServiceRequestById(serviceRequestId);
		return serviceRequestFromDB;

	}

	@Override
	public User findById(String userId) {
		Query query = new Query(Criteria.where("userId").is(userId));
		User userExist = template.findOne(query, User.class);
		return userExist;
	}

	@Override
	public ServiceRequest cancelServiceRequest(String serviceRequestId) throws Throwable {
		ServiceRequest serviceRequestFromDB = null;
		Query query = null;
		Update update = null;
		if (StringUtils.isNotBlank(serviceRequestId)) {
			query = new Query(Criteria.where("serviceRequestId").is(serviceRequestId));
			update = new Update();
			update.set(" requestStatus", "Cancelled");

			template.updateFirst(query, update, ServiceRequest.class);

		}
		serviceRequestFromDB = findServiceRequestById(serviceRequestId);
		return serviceRequestFromDB;

	}

	@Override
	public ServiceRequest getServiceRequestByProperty(String propertyId) throws Throwable {
		ServiceRequest serviceRequest = null;
		try {
			Query query = new Query(Criteria.where("propertyId").is(propertyId));
			serviceRequest = template.findOne(query, ServiceRequest.class);
		} catch (Throwable t) {
			logger.error("Error while fetching lease for lease id : " + propertyId, t);
			throw new LeaseException("Fatal", t);
		}
		return serviceRequest;
	}
}
