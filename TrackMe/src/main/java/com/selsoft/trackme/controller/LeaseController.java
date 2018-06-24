package com.selsoft.trackme.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.trackme.constants.ErrorConstants;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.model.Lease;
import com.selsoft.trackme.model.LeaseType;
import com.selsoft.trackme.model.Owner;
import com.selsoft.trackme.model.Property;
import com.selsoft.trackme.model.RentalDetail;
import com.selsoft.trackme.model.User;
import com.selsoft.trackme.model.ValidError;
import com.selsoft.trackme.service.LeaseService;
import com.selsoft.trackme.utils.UserType;

@RestController
@RequestMapping(value = "/lease")
public class LeaseController {

	private static final Logger logger = Logger.getLogger(LeaseController.class);

	//private static LeaseType leaseType = new LeaseType();

	@Autowired
	private LeaseService leaseService;

	@RequestMapping(value = "/createLease", method = RequestMethod.POST)
	public ValidError createLease(@RequestBody Lease lease) {

		ValidError error = leaseService.validateNewLeaseData(lease);

		logger.info(lease.getPropertyId() + " data comes into LeaseControllercreateLease() for processing");
		if (error == null) {
			leaseService.createLease(lease);
			error=new ValidError(ErrorConstants.ERROR109, ErrorConstants.ERRROR109_MESSAGE);
		}
		return error;

	}
	
	
	 //------------------- save RentalDetail --------------------------------------------------------
		@RequestMapping(value = "/saveRentalDetail", method = RequestMethod.PUT)
		public void  saveRentalDetail(@RequestBody RentalDetail rentalDetail,@RequestParam("pId") String propertyId) {
			
			 
			/*Lease leaseWithType = null;
			logger.info(property.getPropertyId() +" data comes into LeaseController saveRentalDetail() for processing");

			if (StringUtils.equals("RENT", rentalDetail.getLeaseType())) {
				//leaseWithType = leaseType.getValue();
			}

			else if (StringUtils.equals("LEASE", rentalDetail.getLeaseType())) {
				//leaseWithType = leaseType.BOTH.getValue();

			                     }
			else if (StringUtils.equals("BOTH", rentalDetail.getLeaseType())) {
				//leaseWithType = leaseType.BOTH.getValue();
			}*/
			
			leaseService. saveRentalDetail(rentalDetail, propertyId);
		}

}
