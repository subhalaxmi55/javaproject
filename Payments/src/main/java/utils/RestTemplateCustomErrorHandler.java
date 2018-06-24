package com.selsoft.trackme.payments.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selsoft.trackme.constants.PaymentConstants;

public class RestTemplateCustomErrorHandler extends DefaultResponseErrorHandler {
	
	private static final Logger logger = Logger.getLogger(RestTemplateCustomErrorHandler.class);

	@Override
	public void handleError(ClientHttpResponse response) throws RestClientException {
		String line = null;
		StringBuilder inputStringBuilder = new StringBuilder();
		Map<String,Object> map = new HashMap<String,Object>();
	    try {
	    	if (response != null && response.getBody() != null && isReadableResponse(response)) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-16"));
				line = bufferedReader.readLine();
			    while (line != null) {
			        inputStringBuilder.append(line);
			        inputStringBuilder.append('\n');
			        line = bufferedReader.readLine();
			    }
			    logger.info("Error while accessing the rest service " + response.getStatusText());
		    	logger.info("============================response begin==========================================");
				logger.info("Status code  : {" + response.getStatusCode() + "}");
				logger.info("Status text  : {" + response.getStatusText() + "}");
				logger.info("Headers      : {" + response.getHeaders() + "}");
				logger.info("Response body: {" + inputStringBuilder.toString() + "}");
			    logger.info("=======================response end=================================================");
			    
			    ObjectMapper mapper = new ObjectMapper();
			    String inputStringValue = inputStringBuilder.toString();
			    map = mapper.readValue(inputStringValue, new TypeReference<HashMap<String,Object>>(){});
			    if(map.containsKey("response")) {
			    	Map<String, Object> forteResponse = (HashMap<String, Object>) map.get("response");
			    	if(forteResponse.containsKey("response_code")) {
				    	String errorDesc = PaymentConstants.FORTE_RESPONSE_CODE_DESC.get(StringUtils.trimToEmpty((String)forteResponse.get("response_code")));
				    	if(StringUtils.isBlank(errorDesc)){
				    		if(forteResponse.containsKey("response_desc")) {
				    			throw new RestClientException((String)forteResponse.get("response_desc"));
				    		} else {
				    			logger.error(inputStringValue);
				    			throw new RestClientException("Cannot process the request in Forte");
				    		}
				    	}
				    	throw new RestClientException(errorDesc);
			    	} else if(forteResponse.containsKey("response_desc")) {
				    	throw new RestClientException(StringUtils.trimToEmpty((String)forteResponse.get("response_desc")));
			    	}
			    }
			}
		} catch (IOException e) {
			throw new RestClientException(e.getMessage());
		}
	    throw new RestClientException("Irrecoverable error occurred, please contact helpdesk");
	}
	
	private String getBodyAsJson(String bodyString) {
        if (bodyString == null || bodyString.length() == 0) {
            return null;
        } else {
            if (isValidJSON(bodyString)) {
                return bodyString;
            } else {
                bodyString.replaceAll("\"", "\\\"");
                return "\"" + bodyString + "\"";
            }
        }
    }
	
	private String getBodyString(ClientHttpResponse response) {
        try {
            if (response != null && response.getBody() != null && isReadableResponse(response)) {
                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }
                return inputStringBuilder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
	
	private boolean isReadableResponse(ClientHttpResponse response) {
        for (String contentType: response.getHeaders().get("Content-Type")) {
            if (isReadableContentType(contentType)) {
                return true;
            }
        }
        return false;
    }
	
	public boolean isValidJSON(final String json) {
        boolean valid = false;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json);
        } catch(IOException e) {
            valid = false;
        }

        return valid;
    }
	
	private boolean isReadableContentType(String contentType) {
        return contentType.startsWith("application/json")
                || contentType.startsWith("text");
    }
	
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		if(response == null) return false;
		if(super.hasError(response)) {
			return super.getHttpStatusCode(response).series() == HttpStatus.Series.CLIENT_ERROR;
		}
		logger.info("Error while accessing the rest service. Response has error : " + response.getStatusText());
		return false;
	}
}