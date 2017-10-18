/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

/**
 * An Interface for testing on Requests to see if we can get a User and Password
 * It works for CadiWrap, but also, Container Specific Wraps (aka Tomcat) should also
 * implement.
 * 
 * @author jg1555
 *
 */
public interface BasicCred extends GetCred {
	public void setUser(String user);
	public void setCred(byte[] passwd);
	public String getUser();
}
