/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur;

import com.att.cadi.Permission;

public class LocalPermission implements Permission {
	private String key;
	
	public LocalPermission(String role) {
		this.key = role;
	}
	
	public String getKey() {
		return key;
	}

	public String toString() {
		return key;
	}

	public boolean match(Permission p) {
		return key.equals(p.getKey());
	}

	public String permType() {
		return "LOCAL";
	}
	
	
}
