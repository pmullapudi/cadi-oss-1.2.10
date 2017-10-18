/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import com.att.cadi.SecuritySetter;
import com.att.cadi.Symm;
import com.att.cadi.config.SecurityInfo;

public class BasicAuthSS implements SecuritySetter<HttpURLConnection> {
	private String cred;
	private SecurityInfo securityInfo;
	
	public BasicAuthSS(String user, String pass, SecurityInfo si) throws IOException {
		cred = "Basic " + Symm.base64().encode(user+':'+pass);
		securityInfo = si;
	}
	public BasicAuthSS(String user, String pass) throws IOException {
		cred = "Basic " + Symm.base64().encode(user+':'+pass);
	}
	
	@Override
	public void setSecurity(HttpURLConnection huc) {
		huc.setRequestProperty("Authorization" , cred);
		if(securityInfo!=null && huc instanceof HttpsURLConnection) {
			securityInfo.setSocketFactoryOn((HttpsURLConnection)huc);
		}
	}

//	public String toString() {
//		try {
//			ByteArrayInputStream bis = new ByteArrayInputStream(cred.getBytes());
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			Symm.base64().decode(bis,baos,6);
//			return baos.toString();
//		} catch (Exception e) {
//			return e.getMessage();
//		}
//	}
}
