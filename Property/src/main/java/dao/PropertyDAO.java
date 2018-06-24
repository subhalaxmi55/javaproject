package com.selsoft.property.dao;

import java.util.List;

import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.models.User;

public interface PropertyDAO {

	public void saveNewProperty(Property property);

	public Owner findOwnerById(Property property);

	public User findManagerByUserId(String managerId) throws Throwable;

	public User findUserByUserId(String managerId) throws Throwable;

	public void setPropertyAsActive(Property property);

	public List<Property> getAllPropertiesForManager(String managerId);

	public List<Property> getAllPropertiesForManagerAndStatus(String managerId, String status);

	public List<Property> getAllPropertiesForOwner(String ownerId);

	public List<Property> getAllPropertiesForOwnerAndStatus(String ownerId, String status);

	public void updateProperty(RentalDetail rentalDetail, String propertyId);

	public List<Property> findAll();

	public void saveNewPropertyGroup(List<Property> properties);

	public void delete(List<Property> properties);

	public Property findPropertyById(String propertyId);

	public void updateProperty(Property property);

	public Lease findLeaseById(String leaseId) throws Throwable;

	public ServiceRequest findServiceRequestById(String serviceRequestId) throws Throwable;

	public Transaction findTransactionById(String transactionId) throws Throwable;

	public List<Property> getAllGroupedProperty(String propertyName) throws Throwable;

	public Lease getLeaseForProperty(String propertyId) throws Throwable;

	public List<Transaction> getAllTransactionsForProperty(Property property) throws Throwable;

	public Lease getCurrentLeaseDataForProperty(Property property) throws Throwable;

	public Tenant getCurrentTenantForProperty(Lease lease) throws Throwable;

	public void updateLease(Lease lease);

	public ServiceRequest updateserviceRequest(ServiceRequest serviceRequest) throws Throwable;

	public ServiceRequest getServiceRequestByProperty(String propertyId) throws Throwable;

	public Transaction getTransactionByProperty(String propertyId) throws Throwable;

	public void updateTransaction(Transaction transaction);

}
