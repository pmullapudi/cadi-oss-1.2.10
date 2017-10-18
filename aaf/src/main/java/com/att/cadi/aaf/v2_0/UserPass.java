/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.v2_0;

import java.io.IOException;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.SecuritySetter;
import com.att.cadi.Symm;

public class UserPass implements SecuritySetter<DME2Client> {
	private final String headValue;
	public UserPass(String user, String pass) throws IOException {
		headValue = "Basic " + Symm.base64.encode(user + ':' + pass); 
	}
	
	public void setSecurity(DME2Client client) {
		client.addHeader("Authorization", headValue);
	}
}
