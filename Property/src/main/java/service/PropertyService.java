package com.selsoft.property.service;

import java.util.List;
import org.json.JSONObject;

import com.selsoft.trackme.models.Property;

public interface PropertyService {

	public Property saveNewProperty(Property property) throws Throwable;

	public void setPropertyAsActive(Property property);

	public List<Property> getAllActivePropertiesForManager(String managerId) throws Throwable;

	public List<Property> getAllPropertiesForManager(String managerId) throws Throwable;

	public List<Property> getAllActivePropertiesForOwner(String ownerId) throws Throwable;

	public List<Property> getAllPropertiesForOwner(String ownerId) throws Throwable;

	public JSONObject saveNewGroupedProperty(List<Property> properties) throws Throwable;

	public void deleteProperties(List<Property> properties);

	public void updateProperty(Property property) throws Throwable;

	public Property getPropertyById(String propertyId) throws Throwable;

}