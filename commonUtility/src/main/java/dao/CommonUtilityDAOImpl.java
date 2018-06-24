package com.selsoft.commonutility.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.selsoft.trackme.models.CommonUtility;

@Repository
public class CommonUtilityDAOImpl implements CommonUtilityDAO {

	private static final Logger logger = Logger.getLogger(CommonUtilityDAOImpl.class);

	@Autowired
	private MongoTemplate template;

	@Override
	public List<CommonUtility> getCommonData(CommonUtility commonUtility) {
		logger.info("Inside common utility");
		
		List<CommonUtility> commonUtilityList = null;
		Query query = new Query();
		
		if(commonUtility != null) {
			
			String module = StringUtils.trimToEmpty(StringUtils.upperCase(commonUtility.getModule()));
			String subModule = StringUtils.trimToEmpty(StringUtils.upperCase(commonUtility.getSubModule()));
			String code = StringUtils.trimToEmpty(StringUtils.upperCase(commonUtility.getCode()));
			
			if(StringUtils.isNotBlank(module)) {
				query.addCriteria(Criteria.where("module").is(commonUtility.getModule()));
			} 
			if(StringUtils.isNotBlank(subModule)) {
				query.addCriteria(Criteria.where("subModule").is(commonUtility.getSubModule()));
			} 
			if(StringUtils.isNotBlank(code)) {
				query.addCriteria(Criteria.where("code").is(commonUtility.getCode()));
			}
		}
		
		commonUtilityList = template.find(query, CommonUtility.class);

		return commonUtilityList;
	}

}
