/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.dme2;

import java.io.IOException;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.SecuritySetter;

public class DME2ClientSS implements SecuritySetter<DME2Client> {
	private Access access;
	private String user,crd;
	
	public DME2ClientSS(Access access, String user, String pass) throws IOException {
		this.access = access;
		this.user = user;
		this.crd = pass;
	}
	
	@Override
	public void setSecurity(DME2Client client) {
		try {
			client.setCredentials(user, access.decrypt(crd, false));
		} catch (IOException e) {
			access.log(Level.ERROR,e,"Error decrypting DME2 Password");
		}
	}
}