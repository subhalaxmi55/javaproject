package com.selsoft.trackme.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.model.Tenant;
import com.selsoft.trackme.model.TenantStatus;
import com.selsoft.trackme.service.TenantService;



@RestController
@RequestMapping(value = "/tenant")
public class TenantController {
	private static final Logger logger = Logger.getLogger(TenantController.class);

	@Autowired
	private TenantService tenantService;

	/*
	 *  //-------------------Add new tenants--------------------------------------------------------
	 */
	
	
	@RequestMapping(value = "/addNewTenant", method = RequestMethod.PUT)
	public void addNewTenant(@RequestBody Tenant tenant) {
		logger.info(tenant.getTenantFirstName() + " data comes into TenantController addNewTenant() for processing");
		tenantService.addNewTenant(tenant);
	}

	/*
	 *  //-------------------Retrieve All tenants--------------------------------------------------------
	 */
	
	@RequestMapping(value = "/getAllTenants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Tenant> getAllTenants(String status) {
		return tenantService.getAllTenants(TenantStatus.NEW.toString());
	}
	
	/*
	 *  //-------------------Retrieve New tenants--------------------------------------------------------
	 */
	
	@RequestMapping(value = "/getNewTenants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Tenant>  getNewTenants() {
		return tenantService. getAllTenants("NEW");
	}

}