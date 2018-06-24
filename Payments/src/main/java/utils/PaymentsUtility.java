package com.selsoft.trackme.payments.utils;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

public class PaymentsUtility {

	public static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final String RISK_SESSION_ID_COMBINATION = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
	private static final Random RANDOM_VALUE = new SecureRandom();
	public static final int RISK_SESSION_ID_LENGTH = 128;
	
	public static String createUniqueValue(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(RISK_SESSION_ID_COMBINATION.charAt(RANDOM_VALUE.nextInt(RISK_SESSION_ID_COMBINATION.length())));
        }
        return new String(returnValue);
    }
	
	public static String getFormattedOrganizationId(String orgId) {
		if(StringUtils.isBlank(orgId)) return StringUtils.EMPTY;
		return new StringBuilder("org_").append(orgId).toString(); 
	}
	
	public static String getFormattedLocationId(String locationId) {
		if(StringUtils.isBlank(locationId)) return StringUtils.EMPTY;
		return new StringBuilder("loc_").append(locationId).toString(); 
	}

	public static String formatTimestampToForteTimestamp(Timestamp tcTimestamp) {
		return new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ").format(tcTimestamp);
	}

}
