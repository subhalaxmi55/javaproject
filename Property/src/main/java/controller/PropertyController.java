package com.selsoft.property.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.selsoft.property.service.PropertyService;
import com.selsoft.trackme.exception.PropertyException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Property;

/**
 * 
 * @author selsoft
 *
 */
@RestController
@RequestMapping(value = "/property")
public class PropertyController {

	private static final Logger logger = Logger.getLogger(PropertyController.class);

	@Autowired
	PropertyService propertyService;

	/*
	 * This method takes property object as parameter and adds new property to
	 * Property table
	 */
	@RequestMapping(value = "/addNewProperty", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addNewProperty(@RequestBody Property property) {
		logger.info(
				property.getOwnerFirstName() + " data comes into PropertyController addNewProperty() for processing");
		JSONObject jsonObject = new JSONObject();
		try {
			property = propertyService.saveNewProperty(property);
			jsonObject.put("success", true);
		} catch (PropertyException e) {
			jsonObject.put("success", false);
			property.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			property.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			property.addError(new Error("Fatal", t.toString()));
		}

		jsonObject.put("property", property.toJSON());
		return jsonObject.toString();
	}

	@RequestMapping(value = "/addNewGroupedProperty", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addNewProperty(@RequestBody Map<String, Object> propertiesJSON) {
		logger.info("Inside grouped properties controller method");

		if (propertiesJSON == null || propertiesJSON.size() == 0)
			return propertiesJSON.toString();

		JSONObject jsonObject = new JSONObject();
		List<Property> properties = getPropertiesListFromRequestJSON(propertiesJSON);

		try {
			jsonObject = propertyService.saveNewGroupedProperty(properties);
		} catch (PropertyException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(e).toJSON());
			propertyService.deleteProperties(properties);
		} catch (Exception e) {
			if (jsonObject == null)
				jsonObject = new JSONObject();
			jsonObject.put("success", false);
			jsonObject.put("error", new Error("Fatal", e.toString()).toJSON());
			propertyService.deleteProperties(properties);
		} catch (Throwable t) {
			if (jsonObject == null)
				jsonObject = new JSONObject();
			jsonObject.put("success", false);
			jsonObject.put("error", new Error("Fatal", t.toString()).toJSON());
			propertyService.deleteProperties(properties);
		}

		if (jsonObject.get("properties") == null) {
			jsonObject.put("properties", properties);
		}

		return jsonObject.toString();
	}

	// Takes the JSON object as input and constructs the Properties list
	private List<Property> getPropertiesListFromRequestJSON(Map<String, Object> propertiesJSON) {
		List<Property> properties = new ArrayList<Property>();
		String propertyName, ownerId, managerId, enteredBy;
		boolean groupedProperty;

		propertyName = (propertiesJSON.containsKey("propertyName")) ? (String) propertiesJSON.get("propertyName")
				: null;
		ownerId = (propertiesJSON.containsKey("ownerId")) ? (String) propertiesJSON.get("ownerId") : null;
		groupedProperty = (propertiesJSON.containsKey("groupedProperty"))
				? (boolean) propertiesJSON.get("groupedProperty")
				: true;
		managerId = (propertiesJSON.containsKey("managerId")) ? (String) propertiesJSON.get("managerId") : null;
		enteredBy = (propertiesJSON.containsKey("enteredBy")) ? (String) propertiesJSON.get("enteredBy") : null;

		if (propertiesJSON.containsKey("units")) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> unitsArray = (List<Map<String, Object>>) propertiesJSON.get("units");

			unitsArray.forEach(unit -> {
				Property property = new Property();
				property.setPropertyName(propertyName);
				property.setOwnerId(ownerId);
				property.setGroupedProperty(groupedProperty);
				property.setManagerId(managerId);
				property.setEnteredBy(enteredBy);

				unit.forEach((key, value) -> {
					if (StringUtils.equals(key, "address1")) {
						property.setAddress1((String) value);
					} else if (StringUtils.equals(key, "address2")) {
						property.setAddress2((String) value);
					} else if (StringUtils.equals(key, "address3")) {
						property.setAddress3((String) value);
					} else if (StringUtils.equals(key, "city")) {
						property.setCity((String) value);
					} else if (StringUtils.equals(key, "state")) {
						property.setState((String) value);
					} else if (StringUtils.equals(key, "zipCode")) {
						property.setZipCode((String) value);
					}
				});

				properties.add(property);
			});
		}

		return properties;
	}

	/*
	 * This method sets property as Active
	 */

	@RequestMapping(value = "/setPropertyAsActive", method = RequestMethod.PUT)
	public void setPropertyAsActive(@RequestBody Property property) {
		logger.info(property.getPropertyStatus()
				+ " data comes into PropertyController setPropertyAsActive() for processing");

		propertyService.setPropertyAsActive(property);
	}

	@RequestMapping(value = "/getAllPropertiesForManager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllPropertiesForManager(@RequestBody Property property) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		List<Property> properties = null;

		try {
			String managerId = StringUtils.trimToEmpty(property.getManagerId());

			if (StringUtils.isBlank(managerId))
				throw new PropertyException("Error", "Manager id missing, cannot get properties");

			properties = propertyService.getAllPropertiesForManager(managerId);

			if (properties != null && properties.size() > 0) {
				jsonObject.put("success", true);
				jsonObject.put("properties", properties);
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new PropertyException("Error",
						"No properties found for this manager, please add a property to manage")));
				jsonObject.put("errors", errorList);
			}

		} catch (PropertyException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new PropertyException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/getAllActivePropertiesForManager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllActivePropertiesForManager(@RequestBody Property property) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		List<Property> properties = null;

		try {
			String managerId = StringUtils.trimToEmpty(property.getManagerId());

			if (StringUtils.isBlank(managerId))
				throw new PropertyException("Error", "Manager id missing, cannot get properties");

			properties = propertyService.getAllActivePropertiesForManager(managerId);

			if (properties != null && properties.size() > 0) {
				jsonObject.put("success", true);
				jsonObject.put("properties", properties);
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new PropertyException("Error",
						"No properties found for this manager, please add a property to manage")));
				jsonObject.put("errors", errorList);
			}

		} catch (PropertyException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new PropertyException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/getAllPropertiesForOwner", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllPropertiesForOwner(@RequestBody Property property) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		List<Property> properties = null;

		try {
			String ownerId = StringUtils.trimToEmpty(property.getOwnerId());

			if (StringUtils.isBlank(ownerId))
				throw new PropertyException("Error", "Owner id missing, cannot get properties");

			properties = propertyService.getAllPropertiesForOwner(ownerId);

			if (properties != null && properties.size() > 0) {
				jsonObject.put("success", true);
				jsonObject.put("properties", properties);
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new PropertyException("Error",
						"No properties found for this owner, please add a property to manage")));
				jsonObject.put("errors", errorList);
			}

		} catch (PropertyException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new PropertyException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/getAllActivePropertiesForOwner", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllActivePropertiesForOwner(@RequestBody Property property) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();
		List<Property> properties = null;

		try {
			String ownerId = StringUtils.trimToEmpty(property.getOwnerId());

			if (StringUtils.isBlank(ownerId))
				throw new PropertyException("Error", "Owner id missing, cannot get properties");

			properties = propertyService.getAllActivePropertiesForOwner(ownerId);

			if (properties != null && properties.size() > 0) {
				jsonObject.put("success", true);
				jsonObject.put("properties", properties);
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new PropertyException("Error",
						"No properties found for this owner, please add a property to manage")));
				jsonObject.put("errors", errorList);
			}

		} catch (PropertyException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new PropertyException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/getPropertyById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPropertyById(@RequestBody String propertyId) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();

		try {
			propertyId = StringUtils.trimToEmpty(propertyId);

			if (StringUtils.isBlank(propertyId))
				throw new PropertyException("Error", "Property id missing, cannot get property detail");

			Property property = propertyService.getPropertyById(propertyId);

			if (property != null) {
				jsonObject.put("success", true);
				System.out.println(property.toJSON());
				jsonObject.put("property", property.toJSON());
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new PropertyException("Error", "No properties found for this id")));
				jsonObject.put("errors", errorList);
			}

		} catch (PropertyException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new PropertyException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateProperty(@RequestBody Property property) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating Property ");

		try {
			propertyService.updateProperty(property);
			jsonObject.put("success", "true");
		} catch (PropertyException e) {
			jsonObject.put("success", false);
			property.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			property.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			property.addError(new Error("Fatal", t.toString()));
		}
		jsonObject.put("property", property.toJSON());
		return jsonObject.toString();
	}

}