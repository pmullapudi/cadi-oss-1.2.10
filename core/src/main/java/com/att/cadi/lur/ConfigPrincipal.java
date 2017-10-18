/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.GetCred;
import com.att.cadi.Symm;

public class ConfigPrincipal implements Principal, GetCred {
	private String name;
	private byte[] cred;
	private String content;

	public ConfigPrincipal(String name, String passwd) {
		this.name = name;
		this.cred = passwd.getBytes();
		content = null;
	}

	public ConfigPrincipal(String name, byte[] cred) {
		this.name = name;
		this.cred = cred;
		content = null;
	}

	public String getName() {
		return name;
	}
	
	public byte[] getCred() {
		return cred;
	}

	public String toString() {
		return name;
	}
	
	public String getAsBasicAuthHeader() throws IOException {
		if(content ==null) {
			String s = name + ':' + new String(cred);
			content = "Basic " + Symm.base64.encode(s);  
		} else if(!content.startsWith("Basic ")) { // content is the saved password from construction
			String s = name + ':' + content;
			content = "Basic " + Symm.base64.encode(s);  
		}
		return content;
	}
}
