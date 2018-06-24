package com.selsoft.owner.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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

import com.selsoft.owner.service.OwnerService;
import com.selsoft.trackme.exception.OwnerException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Owner;

@RestController
@RequestMapping(value = "/owner")

/**
 * 
 * @author sudhansu
 *
 */

public class OwnerController {

	private static final Logger logger = Logger.getLogger(OwnerController.class);

	@Autowired
	OwnerService ownerService; // Service which will do all data

	@Autowired(required = true)
	private Environment environment; // retrieval/manipulation work

	// Retrieve All Owners
	@RequestMapping(value = "/getAllPropertyOwners", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Owner> getAllPropertyOwners(@RequestBody Owner owner) {
		List<Owner> owners = null;

		try {
			owners = ownerService.getAllPropertyOwnersForManager(owner.getManagerId());
		} catch (Throwable t) {
			logger.error("Error while getting all property owners", t);
		}

		return owners;
	}

	// ------------------- save a Owners
	// --------------------------------------------------------
	@RequestMapping(value = "/addOwner", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addOwner(HttpServletRequest request, @RequestBody Owner owner) {
		JSONObject jsonObject = new JSONObject();
		try {
			if (owner == null) {
				throw new OwnerException("Error", "Owner data missing, cannot proceed");
			}

			owner = ownerService.saveNewOwner(owner, getUserActivationURL(request));
			jsonObject.put("success", true);
		} catch (OwnerException e) {
			jsonObject.put("success", false);
			owner.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			owner.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			owner.addError(new Error("Fatal", t.toString()));
		}
		jsonObject.put("owner", owner.toJSON());
		return jsonObject.toString();
	}

	// ------------------- Update a Owner
	// --------------------------------------------------------

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateOwner(HttpServletRequest request, @RequestBody Owner owner) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating Owner ");

		try {
			ownerService.updateOwner(owner, getUserActivationURL(request));
			jsonObject.put("success", "true");
		} catch (OwnerException e) {
			jsonObject.put("success", false);
			owner.addError(new Error(e));
		} catch (Exception e) {
			jsonObject.put("success", false);
			owner.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			owner.addError(new Error("Fatal", t.toString()));
		}
		jsonObject.put("owner", owner.toJSON());
		return jsonObject.toString();
	}

	@RequestMapping(value = "/activateUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String activateUser(HttpServletRequest request, @RequestBody Owner owner) {
		JSONObject jsonObject = new JSONObject();
		try {
			// String activationUrl = new
			// StringBuffer(request.getScheme()).append("://").append(request.getServerName())
			// .append(":").append(request.getServerPort()).append("/")
			// .append(environment.getProperty("user.contextRoot")).append("/user").toString();

			ownerService.activateUser(owner, getUserActivationURL(request));
			jsonObject.put("success", true);
		} catch (OwnerException e) {
			logger.fatal("Error while activating user", e);
			jsonObject.put("success", false);
			owner.addError(new Error(e));
		} catch (Throwable t) {
			logger.fatal("Fatal error while activating user", t);
			jsonObject.put("success", false);
			owner.addError(new Error(new OwnerException("Fatal", t)));
		}
		jsonObject.put("owner", owner.toJSON());
		return jsonObject.toString();
	}

	private String getUserActivationURL(HttpServletRequest request) {
		return new StringBuffer(request.getScheme()).append("://").append(request.getServerName()).append(":")
				.append(request.getServerPort()).append("/").append(environment.getProperty("user.contextRoot"))
				.append("/user").toString();
	}

	@RequestMapping(value = "/getOwnerById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getOwnerById(@RequestBody String ownerId) {
		JSONObject jsonObject = new JSONObject();
		List<Error> errorList = new ArrayList<Error>();

		try {
			ownerId = StringUtils.trimToEmpty(ownerId);

			if (StringUtils.isBlank(ownerId))
				throw new OwnerException("Error", "Owner id missing, cannot get owner detail");

			Owner owner = ownerService.getOwnerById(ownerId);

			if (owner != null) {
				jsonObject.put("success", true);
				System.out.println(owner.toJSON());
				jsonObject.put("owner", owner.toJSON());
			} else {
				jsonObject.put("success", false);
				errorList.add(new Error(new OwnerException("Error", "No owners found for this id")));
				jsonObject.put("errors", errorList);
			}

		} catch (OwnerException e) {
			jsonObject.put("success", false);
			errorList.add(new Error(e));
			jsonObject.put("errors", errorList);
		} catch (Throwable t) {
			jsonObject.put("success", false);
			errorList.add(new Error(new OwnerException("Fatal", t)));
			jsonObject.put("errors", errorList);
		}

		return jsonObject.toString();
	}

	@RequestMapping(value = "/deleteOwner", method = RequestMethod.DELETE)
	public String deleteOwner(@RequestParam String ownerId) throws Throwable {

		JSONObject jsonObject = new JSONObject();
		logger.info("deleting Owner ");

		try {
			ownerService.deleteOwner(ownerId);
			jsonObject.put("sucess", "true");
			jsonObject.put("message", "Owner id  deleted successfully");

		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("message", "Owner id  does not exist");
		}
		return jsonObject.toString();
	}

}