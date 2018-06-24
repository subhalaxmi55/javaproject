package com.selsoft.user.utils;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.models.User;
import com.selsoft.trackme.utils.TrackMeUtils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component("jwtTokenUtils")
@PropertySource("classpath:jwtsecurity.properties")
public class JWTTokenUtils {

	@Autowired(required = true)
	private Environment environment;
	
	public String createJWTForUser(User user) {
		
		if(user == null) return null;
		
		String jwt = null;
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		JwtBuilder builder = null;
		
		byte[] apiKeySecretBytes = null;
		try {
			//apiKeySecretBytes = DatatypeConverter.parseBase64Binary(environment.getProperty("jwt.secretKey"));
			apiKeySecretBytes = environment.getProperty("jwt.secretKey").getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	    
	    JSONObject jsonObject = new JSONObject();
	    
		if(user.getErrors() != null && user.getErrors().size() > 0) {
			//builder = Jwts.builder().claim("user", user.toJSONString()).signWith(signatureAlgorithm, signingKey);
			jsonObject.put("success", false);
			jsonObject.put("errors", user.getErrorJSON().get("errors"));
		} else {
		    //Let's set the JWT Claims
		    builder = Jwts.builder().setHeaderParam("typ","JWT")
		    						.setId(environment.getProperty("jwt.id"))
		    						.setIssuedAt(TrackMeUtils.getCurrentUTCTimeAsSqlTimestamp())
		    						.setExpiration(TrackMeUtils.addTimeToCurrentUTCTimeAsSqlTimestamp(TrackMeConstants.TIMEOUT))
		    						.signWith(signatureAlgorithm, signingKey);
		    jwt = builder.compact();
		    jsonObject.put("success", true);
			jsonObject.put("token", StringUtils.join("Bearer ", jwt));
			jsonObject.put("user", user.toJSON());
		}
		return jsonObject.toString();
	}
}
