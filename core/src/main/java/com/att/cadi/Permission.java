/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

public interface Permission {
	public String permType();
	public String getKey();
	public boolean match(Permission p);
}
