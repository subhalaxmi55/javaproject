package com.selsoft.tenant.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.selsoft.tenant.service.TenantService;
import com.selsoft.trackme.constants.TenantConstants.TENANT_STATUS;
import com.selsoft.trackme.exception.TenantException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.ServiceRequest;
import com.selsoft.trackme.models.Tenant;

@RestController
@RequestMapping(value = "/tenant")
public class TenantController {
	private static final Logger logger = Logger.getLogger(TenantController.class);

	@Autowired
	private TenantService tenantService;

	@Autowired(required = true)
	private Environment environment;

	/*
	 * //-------------------Add new
	 * tenants--------------------------------------------------------
	 */

	// @RequestMapping(value = "/addNewTenant", method = RequestMethod.PUT)
	@RequestMapping(value = "/addNewTenant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addNewTenant(HttpServletRequest request, @RequestBody Tenant tenant) {
		logger.info("Data comes into TenantController addNewTenant() for processing");

		JSONObject jsonObject = new JSONObject();

		try {
			if (tenant == null) {
				throw new TenantException("Error", "Tenant data missing, cannot proceed");
			}

			String activationUrl = new StringBuffer(request.getScheme()).append("://").append(request.getServerName())
					.append(":").append(request.getServerPort()).append("/")
					.append(environment.getProperty("user.contextRoot")).append("/user").toString();

			tenantService.addNewTenant(tenant, activationUrl);

			jsonObject.put("success", true);
		} catch (TenantException e) {
			tenant.addError(new Error(e));
			jsonObject.put("success", false);
		} catch (Exception e) {
			jsonObject.put("success", false);
			tenant.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			tenant.addError(new Error("Fatal", t.toString()));
		}
		jsonObject.put("tenant", tenant.toJSON());
		return jsonObject.toString();
	}
	/*
	 * //-------------------Retrieve All
	 * tenants--------------------------------------------------------
	 */

	@RequestMapping(value = "/getAllTenantsForManager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllTenantsForManager(@RequestBody Tenant tenant) {
		List<Tenant> tenants = null;
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();

		try {
			tenants = tenantService.getAllTenantsForManager(tenant.getManagerId());
			jsonObject.put("success", true);
			jsonObject.put("tenants", tenants);
		} catch (TenantException e) {
			logger.error("Error while getting all tenants for manager", e);
			errorList.add(new Error(e));
			jsonObject.put("success", false);
			jsonObject.put("error", errorList);
		} catch (Throwable t) {
			logger.error("Error while getting all tenants for manager", t);
			errorList.add(new Error(new TenantException("Fatal", t)));
			jsonObject.put("success", false);
			jsonObject.put("error", errorList);
		}
		return jsonObject.toString();
	}

	/*
	 * //-------------------Retrieve New
	 * tenants--------------------------------------------------------
	 */

	@RequestMapping(value = "/getNewTenantsForManager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNewTenantsForManager(@RequestBody Tenant tenant) {
		List<Tenant> tenants = null;
		JSONObject jsonObject = new JSONObject();

		try {
			tenants = tenantService.getTenantsForManagerBasedOnStatus(TENANT_STATUS.NEW, tenant.getManagerId());
			jsonObject.put("success", true);
			jsonObject.put("tenants", tenants);
		} catch (TenantException e) {
			logger.error("Error while getting all new tenants for manager", e);
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(e));
		} catch (Throwable t) {
			logger.error("Error while getting all new tenants for manager", t);
			jsonObject.put("success", false);
			jsonObject.put("error", new Error(new TenantException("Fatal", t)));
		}
		return jsonObject.toString();
	}

	// ------------------- Update a Tenant

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)

	public String updateTenant(@RequestBody Tenant tenant) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating Tenant ");

		try {
			tenantService.updateTenant(tenant);
			jsonObject.put("success", "true");
		} catch (TenantException e) {
			jsonObject.put("success", false);
			tenant.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			tenant.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			tenant.addError(new Error("Fatal", t.toString()));
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/activateUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String activateUser(HttpServletRequest request, @RequestBody Tenant tenant) {
		JSONObject jsonObject = new JSONObject();
		try {
			/*String activationUrl = new StringBuffer(request.getScheme()).append("://").append(request.getServerName())
					.append(":").append(request.getServerPort()).append("/")
					.append(environment.getProperty("user.contextRoot")).append("/user").toString();*/

			tenantService.activateUser(tenant, getUserActivationURL(request));
			jsonObject.put("success", true);
		} catch (TenantException e) {
			logger.fatal("Error while activating user", e);
			jsonObject.put("success", false);
			tenant.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Fatal error while activating user", t);
			jsonObject.put("success", false);
			tenant.addError(new Error(new TenantException("Fatal", t)));
		}
		jsonObject.put("tenant", tenant.toJSON());
		return jsonObject.toString();
	}

	@RequestMapping(value = "/createServiceRequest", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createServiceRequest(@RequestBody ServiceRequest serviceRequest) throws Throwable {

		JSONObject jsonObject = new JSONObject();

		logger.info("Creating service request ");

		try {
			tenantService.saveNewServiceRequest(serviceRequest);
			jsonObject.put("success", "true");
		} catch (TenantException e) {
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

	// @RequestMapping(value = "/createServiceRequest", method = RequestMethod.POST,
	// produces = MediaType.APPLICATION_JSON_VALUE)
	// public String createServiceRequest(@RequestBody ServiceRequest
	// serviceRequest) throws Throwable {
	//
	// JSONObject jsonObject = new JSONObject();
	//
	// logger.info("Creating service request ");
	//
	// try {
	// tenantService.saveNewServiceRequest(serviceRequest);
	// jsonObject.put("success", "true");
	// } catch (TenantException e) {
	// jsonObject.put("success", false);
	// serviceRequest.addError(new Error(e));
	// } catch (Exception e) {
	// jsonObject.put("success", false);
	// serviceRequest.addError(new Error("Fatal", e.toString()));
	// } catch (Throwable t) {
	// jsonObject.put("success", false);
	// serviceRequest.addError(new Error("Fatal", t.toString()));
	// }
	//
	// return jsonObject.toString();
	// }

	@RequestMapping(value = "/deleteTenant", method = RequestMethod.DELETE)
	public String deleteTenant(@RequestParam String tenantId) throws Throwable {

		JSONObject jsonObject = new JSONObject();
		logger.info("deleting Lease ");

		try {
			tenantService.deleteTenant(tenantId);
			jsonObject.put("sucess", "true");
			jsonObject.put("message", "tenant id  deleted successfully");

		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("message", "tenant id  does not exist");
		}
		return jsonObject.toString();
	}

	private String getUserActivationURL(HttpServletRequest request) {
		return new StringBuffer(request.getScheme()).append("://").append(request.getServerName()).append(":")
				.append(request.getServerPort()).append("/").append(environment.getProperty("user.contextRoot"))
				.append("/user").toString();
	}

}
