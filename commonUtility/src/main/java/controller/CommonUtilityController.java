	package com.selsoft.commonutility.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.selsoft.commonutility.service.CommonUtilityService;
import com.selsoft.trackme.models.CommonUtility;


@RestController
@RequestMapping(value = "/common")
public class CommonUtilityController {
	
	private static final Logger logger = Logger.getLogger(CommonUtilityController.class);
	
	@Autowired
	private CommonUtilityService commonUtilityService;

	@RequestMapping(value = "getCommonData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CommonUtility> getCommonData(@RequestBody(required=false) CommonUtility commonUtility) {
		logger.info("Get common data");
		return commonUtilityService.getCommonData(commonUtility);
	}

	
}
