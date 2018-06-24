package com.selsoft.lease.controller;

import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.selsoft.lease.service.LeaseService;
import com.selsoft.trackme.exception.LeaseException;
import com.selsoft.trackme.models.Error;
import com.selsoft.trackme.models.Lease;
import com.selsoft.trackme.models.RentalDetail;
import com.selsoft.trackme.models.ValidError;


@RestController
@RequestMapping(value = "/lease")
public class LeaseController {

	private static final Logger logger = Logger.getLogger(LeaseController.class);

	// private static LeaseType leaseType = new LeaseType();

	@Autowired
	private LeaseService leaseService;

	@RequestMapping(value = "/createLease", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createLease(@RequestBody Lease lease) {

		logger.info("Creating lease for property " + lease.getPropertyId());

		JSONObject jsonObject = new JSONObject();

		if (lease != null) {
			try {
				lease = leaseService.createLease(lease);
				jsonObject.put("success", true);
			} catch (LeaseException e) {
				logger.error(e);
				jsonObject.put("success", false);
				lease.addError(new Error(e));
			} catch (Throwable t) {
				logger.error("Error while creating lease", t);
				jsonObject.put("success", false);
				lease.addError(new Error(new LeaseException("Fatal", t)));
			}
		}
		jsonObject.put("lease", lease.toJSON());
		return jsonObject.toString();

	}

	// ------------------- save RentalDetail
	// --------------------------------------------------------
	@RequestMapping(value = "/saveRentalDetail", method = RequestMethod.PUT)
	public void saveRentalDetail(@RequestBody RentalDetail rentalDetail, @RequestParam("pId") String propertyId) {

		ValidError validError = leaseService.validateNewRentalData(rentalDetail);

	}

	@RequestMapping(value = "getAllRentalDetails/{propertyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RentalDetail> getAllRentalDetails(@PathVariable("propertyId") Integer propertyId) {
		return leaseService.getAllRentalDetails(propertyId);
	}

	@RequestMapping(value = "getRentalDetail/{propertyId}/{effectiveDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public RentalDetail getRentalDetail(@PathVariable("propertyId") Integer propertyId,
			@PathVariable("effectiveDate") String inputDate) {

		return leaseService.getRentalDetail(propertyId, inputDate);
	}

	// ------------------- Update a Lease
	// --------------------------------------------------------

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateLease(@RequestBody Lease lease) throws Throwable {
		JSONObject jsonObject = new JSONObject();

		logger.info("Updating Lease ");

		try {
			leaseService.updateLease(lease);
			jsonObject.put("sucess", "true");
		} catch (Exception e) {
			jsonObject.put("success", false);
			lease.addError(new Error("Fatal", e.toString()));
		} catch (Throwable t) {
			jsonObject.put("success", false);
			lease.addError(new Error("Fatal", t.toString()));
		}
		return jsonObject.toString();
	}

	@RequestMapping(value = "/deleteLease", method = RequestMethod.DELETE)
	public String deleteLease(@RequestParam("leaseId") String leaseId) throws Throwable {

		JSONObject jsonObject = new JSONObject();
		logger.info("deleting Lease ");

		try {
			leaseService.deleteLease(leaseId);
			jsonObject.put("sucess", "true");
			jsonObject.put("message", "Lease id  deleted successfully");

		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("message", "Lease id  does not exist");
		}
		return jsonObject.toString();
	}

}
