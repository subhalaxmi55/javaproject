package com.selsoft.property.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.selsoft.property.dao.PropertyDAO;
import com.selsoft.trackme.exception.PropertyException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.Owner;
import com.selsoft.trackme.models.Property;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;
import com.selsoft.trackme.models.Transaction;
import com.selsoft.trackme.utils.TrackMeUtils;

@Service("propertyService")
public class PropertyServiceImpl implements PropertyService {

	@Autowired
	private PropertyDAO propertyDAO;

	private static final Logger logger = Logger.getLogger(PropertyServiceImpl.class);

	/**
	 * save the new property to property table
	 */
	public Property saveNewProperty(Property property) throws Throwable {
		try {
			property = validateProperty(property);
			// property.getRentalDetail().setProperytId(property.getPropertyId());
			property.setPropertyStatus("Active");
			propertyDAO.saveNewProperty(property);
		} catch (PropertyException e) {
			throw e;
		} catch (Throwable t) {
			logger.fatal("` while saving property", t);
			throw new PropertyException("Fatal", "Errow while saving property, please try again later");
		}
		return property;
	}

	public JSONObject saveNewGroupedProperty(List<Property> properties) throws Throwable {
		JSONObject jsonObject = new JSONObject();
		if (properties == null || properties.size() == 0) {
			logger.info("No properties to save, returning");
			jsonObject.put("error", new Error("Error", "No properties to save, please try again later"));
			return jsonObject;
		}

		try {
			properties.forEach(property -> {
				try {
					property = validateProperty(property);
					property.setPropertyStatus("Active");
					property.setEnteredOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());
					property.setGroupedProperty(true);
				} catch (PropertyException e) {
					logger.error("Error while saving property group, please try again later", e);
					jsonObject.put("error", new Error(e).toJSON());
					jsonObject.put("success", false);
				} catch (Throwable t) {
					logger.fatal("Error while saving grouped property", t);
					jsonObject.put("error",
							new Error("Fatal", "Errow while saving property, please try again later").toJSON());
					jsonObject.put("success", false);
				}
			});

		} catch (Throwable t) {
			logger.fatal("Error while saving grouped property", t);
			jsonObject.put("error", new Error("Fatal", "Errow while saving property, please try again later"));
		}

		if (jsonObject.isNull("error")) {
			propertyDAO.saveNewPropertyGroup(properties);
			jsonObject.put("success", true);
		}

		jsonObject.put("properties", properties);

		/*
		 * try { property = validateProperty(property);
		 * //property.getRentalDetail().setProperytId(property.getPropertyId());
		 * property.setPropertyStatus("Active");
		 * propertyDAO.saveNewProperty(property); } catch(PropertyException e) {
		 * throw e; } catch(Throwable t) {
		 * logger.fatal("Error while saving property", t); throw new
		 * PropertyException("Fatal",
		 * "Errow while saving property, please try again later"); }
		 */
		return jsonObject;
	}

	private Property validateProperty(Property property) throws Throwable {
		property = validatePropertyData(property);
		validateEnteredOrUpdatedBy(property.getEnteredBy());
		return property;
	}

	private void validateEnteredOrUpdatedBy(String userId) throws Throwable {
		if (StringUtils.isNotBlank(userId)) {
			if (propertyDAO.findUserByUserId(userId) == null) {
				throw new PropertyException("Error",
						"Entered/Updated by information is not valid, cannot add/update property");
			}
		} else {
			throw new PropertyException("Error", "Entered/Updated by information missing, cannot add/update property");
		}
	}

	private Property validatePropertyData(Property property) throws Throwable {
		if (property == null)
			throw new PropertyException("Error", "Property information not found, cannot add property");

		if (StringUtils.isBlank(property.getAddress1()) || StringUtils.isBlank(property.getCity())
				|| StringUtils.isBlank(property.getState()) || StringUtils.isBlank(property.getZipCode())) {
			throw new PropertyException("Error",
					"Address1, city, state and zipcode are mandatory for adding a property. Some values are missing.");
		}

		// Can create property without owner.
		/*
		 * if(StringUtils.isBlank(property.getOwnerId())) { throw new
		 * PropertyException("Error",
		 * "Owner information not present, cannot add property"); } else
		 */
		if (StringUtils.isBlank(property.getManagerId())) {
			throw new PropertyException("Error", "Manager information not present, cannot add/update property");
		}

		// If owner information present, check the owner and manager are tied
		// together
		if (StringUtils.isNotBlank(property.getOwnerId())) {
			Owner owner = propertyDAO.findOwnerById(property);

			if (owner == null) {
				throw new PropertyException("Error", "Owner information is not valid, cannot add/update property");
			} else if (!StringUtils.equals(owner.getManagerId(), property.getManagerId())) {
				throw new PropertyException("Error", "Manager information is not valid, cannot add/update property");
			} else {
				property.setOwnerFirstName(owner.getOwnerFirstName());
				property.setOwnerLastName(owner.getOwnerLastName());
			}
		} else { // If no owner information is present, validate just the
					// manager.
			if (propertyDAO.findManagerByUserId(property.getManagerId()) == null) {
				throw new PropertyException("Error",
						"Manager information is not valid, cannot add property. Please contact the helpdesk.");
			}
		}

		return property;
	}

	/**
	 * sets property as Active
	 */
	@Override
	public void setPropertyAsActive(Property property) {
		propertyDAO.setPropertyAsActive(property);
	}

	@Override
	public List<Property> getAllPropertiesForManager(String managerId) throws Throwable {
		List<Property> properties = propertyDAO.getAllPropertiesForManager(managerId);
		if (properties != null && properties.size() > 0) {
			List<Transaction> transactions = null;
			double grossIncome = 0, expenses = 0, netIncome = 0;
			for (Property property : properties) {
				grossIncome = 0;
				expenses = 0;
				netIncome = 0;
				if (property != null) {
					transactions = propertyDAO.getAllTransactionsForProperty(property);
					if (StringUtils.equals(property.getPropertyStatus(), "Occupied")) {
						attachLeaseAndTenantInformation(property);
					}

					if (transactions != null && transactions.size() > 0) {
						for (Transaction transaction : transactions) {
							if (StringUtils.equals(transaction.getTransactionType(), "Income")) {
								grossIncome += transaction.getAmount();
							} else if (StringUtils.equals(transaction.getTransactionType(), "Expense")) {
								expenses += transaction.getAmount();
							}
						}

						netIncome = grossIncome - expenses;
						property.setGrossIncome(grossIncome);
						property.setExpense(expenses);
						property.setNetIncome(netIncome);
					}
				}
			}
		}
		return properties;
	}

	private void attachLeaseAndTenantInformation(Property property) throws Throwable {
		Lease lease = propertyDAO.getCurrentLeaseDataForProperty(property);
		if (lease != null) {
			Tenant tenant = propertyDAO.getCurrentTenantForProperty(lease);
			lease.setTenant(tenant);
			property.setLease(lease);
		}
	}

	@Override
	public List<Property> getAllActivePropertiesForManager(String managerId) throws Throwable {
		List<Property> properties = propertyDAO.getAllPropertiesForManagerAndStatus(managerId, "Active");
		if (properties != null && properties.size() > 0) {
			List<Transaction> transactions = null;
			double grossIncome = 0, expenses = 0, netIncome = 0;
			for (Property property : properties) {
				grossIncome = 0;
				expenses = 0;
				netIncome = 0;
				transactions = propertyDAO.getAllTransactionsForProperty(property);
				if (transactions != null && transactions.size() > 0) {
					for (Transaction transaction : transactions) {
						if (StringUtils.equals(transaction.getTransactionType(), "Income")) {
							grossIncome += transaction.getAmount();
						} else if (StringUtils.equals(transaction.getTransactionType(), "Expense")) {
							expenses += transaction.getAmount();
						}
					}

					netIncome = grossIncome - expenses;
					property.setGrossIncome(grossIncome);
					property.setExpense(expenses);
					property.setNetIncome(netIncome);
				}
			}
		}
		return properties;
	}

	@Override
	public List<Property> getAllActivePropertiesForOwner(String ownerId) throws Throwable {
		List<Property> properties = propertyDAO.getAllPropertiesForOwnerAndStatus(ownerId, "Active");
		if (properties != null && properties.size() > 0) {
			List<Transaction> transactions = null;
			double grossIncome = 0, expenses = 0, netIncome = 0;
			for (Property property : properties) {
				grossIncome = 0;
				expenses = 0;
				netIncome = 0;
				transactions = propertyDAO.getAllTransactionsForProperty(property);
				if (transactions != null && transactions.size() > 0) {
					for (Transaction transaction : transactions) {
						if (StringUtils.equals(transaction.getTransactionType(), "Income")) {
							grossIncome += transaction.getAmount();
						} else if (StringUtils.equals(transaction.getTransactionType(), "Expense")) {
							expenses += transaction.getAmount();
						}
					}

					netIncome = grossIncome - expenses;
					property.setGrossIncome(grossIncome);
					property.setExpense(expenses);
					property.setNetIncome(netIncome);
				}
			}
		}
		return properties;
	}

	@Override
	public List<Property> getAllPropertiesForOwner(String ownerId) throws Throwable {
		List<Property> properties = propertyDAO.getAllPropertiesForOwner(ownerId);
		if (properties != null && properties.size() > 0) {
			List<Transaction> transactions = null;
			double grossIncome = 0, expenses = 0, netIncome = 0;
			for (Property property : properties) {
				grossIncome = 0;
				expenses = 0;
				netIncome = 0;
				transactions = propertyDAO.getAllTransactionsForProperty(property);

				if (StringUtils.equals(property.getPropertyStatus(), "Occupied")) {
					attachLeaseAndTenantInformation(property);
				}

				if (transactions != null && transactions.size() > 0) {
					for (Transaction transaction : transactions) {
						if (StringUtils.equals(transaction.getTransactionType(), "Income")) {
							grossIncome += transaction.getAmount();
						} else if (StringUtils.equals(transaction.getTransactionType(), "Expense")) {
							expenses += transaction.getAmount();
						}
					}

					netIncome = grossIncome - expenses;
					property.setGrossIncome(grossIncome);
					property.setExpense(expenses);
					property.setNetIncome(netIncome);
				}
			}
		}
		return properties;
	}

	public void deleteProperties(List<Property> properties) {
		propertyDAO.delete(properties);
	}

	@Override
	public void updateProperty(Property newProperty) throws Throwable {

		Property propertyFromDB = null;
		String id = newProperty.getPropertyId();

		if (StringUtils.isNotBlank(id)) {
			propertyFromDB = propertyDAO.findPropertyById(id);
		} else {
			throw new PropertyException("Error", "Invalid update request, property id not found");
		}

		if (propertyFromDB == null) {
			logger.info("Property with id " + id + " not found");
			throw new PropertyException("Error", "Invalid property detail, property not found");
		}

		newProperty = validatePropertyData(newProperty);
		validateEnteredOrUpdatedBy(newProperty.getEnteredBy());
		newProperty.setUpdatedOn(TrackMeUtils.getCurrentUTCTimeAsSqlTimestampString());		

		if (newProperty.isGroupedProperty()) {
			List<Property> groupedProperties = propertyDAO.getAllGroupedProperty(newProperty.getPropertyName());
			for (Property gProperty : groupedProperties) {
				gProperty.setOwnerFirstName(newProperty.getOwnerFirstName());
				gProperty.setOwnerLastName(newProperty.getOwnerLastName());
				gProperty.setOwnerId(newProperty.getOwnerId());
				updatePropertyInDB(gProperty, propertyFromDB);
			}
		} else {
			updatePropertyInDB(newProperty, propertyFromDB);
		}
	}

	private void updatePropertyInDB(Property newProperty, Property oldProperty) throws Throwable {
		try {
			propertyDAO.updateProperty(newProperty);
		} catch (Throwable e) {
			logger.fatal("Error occured while saving property to the database", e);
			propertyDAO.updateProperty(oldProperty);
			throw new PropertyException("Fatal", e);
		}

		// update LEASE Table
		updateLeaseByProperty(newProperty);
		// update SERVICEREQUEST Table
		updateServiceRequestByProperty(newProperty);
		// update TRANSACTION
		updateTransactionByProperty(newProperty);

	}

	private void updateLeaseByProperty(Property property) throws Throwable {
		Lease lease = propertyDAO.getLeaseForProperty(property.getPropertyId());
		if (lease != null) {
			lease.setOwnerId(property.getOwnerId());
			lease.setOwnerFirstName(property.getOwnerFirstName());
			lease.setOwnerLastName(property.getOwnerLastName());
			try {
				propertyDAO.updateLease(lease);
			} catch (Exception e) {
				logger.fatal("Error occured while update lease to the database", e);
				throw new PropertyException("Fatal", e);
			}
		}

	}

	private void updateServiceRequestByProperty(Property property) throws Throwable {
		ServiceRequest serviceRequest = propertyDAO.getServiceRequestByProperty(property.getPropertyId());
		if (serviceRequest != null) {
			serviceRequest.setOwnerId(property.getOwnerId());
			try {
				propertyDAO.updateserviceRequest(serviceRequest);
			} catch (Exception e) {
				logger.fatal("Error occured while update service request to the database", e);
				throw new PropertyException("Fatal", e);
			}
		}

	}

	private void updateTransactionByProperty(Property property) throws Throwable {
		Transaction transaction = propertyDAO.getTransactionByProperty(property.getPropertyId());
		if (transaction != null) {
			transaction.setOwnerId(property.getOwnerId());
			try {
				propertyDAO.updateTransaction(transaction);
			} catch (Exception e) {
				logger.fatal("Error occured while update transaction to the database", e);
				throw new PropertyException("Fatal", e);
			}
		}

	}

	@Override
	public Property getPropertyById(String propertyId) throws Throwable {
		return propertyDAO.findPropertyById(propertyId);
	}

}