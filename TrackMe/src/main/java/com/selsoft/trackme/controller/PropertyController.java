package com.selsoft.trackme.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Property;
import com.selsoft.trackme.model.PropertyStatus;
import com.selsoft.trackme.model.Tenant;
import com.selsoft.trackme.service.PropertyService;



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

	@RequestMapping(value = "/addNewProperty", method = RequestMethod.POST)
	
	/*
	 * This method takes property object as parameter and adds new property to Property table
	 */
	public void addNewProperty(@RequestBody Property property) {
		logger.info(
				property.getOwnerFirstName() + " data comes into PropertyController addNewProperty() for processing");

		propertyService.saveNewProperty(property);

	}
	/*
	 * This method sets property as Active
	 */
	

	@RequestMapping(value = "/setPropertyAsActive", method = RequestMethod.PUT)
	public ResponseEntity<Errors> setPropertyAsActive(@RequestBody Property property) {
		logger.info(
				property.getPropertyStatus() + " data comes into PropertyController setPropertyAsActive() for processing");

		Errors errors = propertyService.setPropertyAsActive(property);

		return new ResponseEntity<Errors>(errors, HttpStatus.CREATED);

	}

	
	@RequestMapping(value = "/getAllActiveProperties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Property>  getAllActiveProperties(String status) {
		return propertyService.getAllProperties(PropertyStatus.ACTIVE.toString());
	}
	
}
