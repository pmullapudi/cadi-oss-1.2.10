/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import com.att.cadi.Access;
import com.att.cadi.config.Get;

public class AccessGetter implements Get {
	private final Access access;
	public AccessGetter(Access access) {
		this.access = access;
	}
	public String get(String name, String def, boolean print) {
		return access.getProperty(name, def);
	}

}
